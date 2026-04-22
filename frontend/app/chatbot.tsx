import React, { useState, useRef, useEffect, useCallback } from 'react';
import { View, Text, TextInput, TouchableOpacity, ScrollView, KeyboardAvoidingView, Platform, ActivityIndicator } from 'react-native';
import { Send, Bot, User, ArrowLeft, WifiOff } from 'lucide-react-native';
import { useRouter } from 'expo-router';
import { streamChat, chatNonStream, type ChatMessage } from '../services/api/chatbotApi';

interface Message {
  id: string;
  text: string;
  isUser: boolean;
  timestamp: Date;
}

export default function ChatbotScreen() {
  const router = useRouter();
  const [messages, setMessages] = useState<Message[]>([
    {
      id: '1',
      text: 'Xin chào! Tôi là trợ lý ảo của CameraShop. Tôi có thể giúp bạn tìm thiết bị, tư vấn sản phẩm, hoặc trả lời câu hỏi về dịch vụ. Bạn cần gì?',
      isUser: false,
      timestamp: new Date(),
    },
  ]);
  const [inputText, setInputText] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [isBackendReady, setIsBackendReady] = useState(true);
  const scrollViewRef = useRef<ScrollView>(null);

  useEffect(() => {
    scrollViewRef.current?.scrollToEnd({ animated: true });
  }, [messages]);

  const appendBotText = useCallback((chunk: string) => {
    setMessages(prev => {
      const last = prev[prev.length - 1];
      if (last && !last.isUser) {
        const updated = [...prev];
        updated[updated.length - 1] = { ...last, text: last.text + chunk };
        return updated;
      }
      return prev;
    });
  }, []);

  const handleSend = async () => {
    if (!inputText.trim() || isLoading) return;

    const userText = inputText.trim();
    const userMessage: Message = {
      id: Date.now().toString(),
      text: userText,
      isUser: true,
      timestamp: new Date(),
    };

    setMessages(prev => [...prev, userMessage]);
    setInputText('');
    setIsLoading(true);
    setIsBackendReady(true);

    // Placeholder bot message for streaming
    const botId = (Date.now() + 1).toString();
    setMessages(prev => [
      ...prev,
      { id: botId, text: '', isUser: false, timestamp: new Date() },
    ]);

    try {
      const chatMessages: ChatMessage[] = [
        ...messages.filter(m => m.id !== '1').map(m => ({
          role: (m.isUser ? 'user' : 'assistant') as 'user' | 'assistant',
          content: m.text,
        })),
        { role: 'user' as const, content: userText },
      ];

      let hasResponse = false;
      try {
        for await (const chunk of streamChat(chatMessages)) {
          hasResponse = true;
          appendBotText(chunk);
        }
      } catch (streamErr) {
        // Fallback to non-streaming
        const text = await chatNonStream(chatMessages);
        if (text) {
          setMessages(prev => {
            const last = prev[prev.length - 1];
            if (last && !last.isUser) {
              const updated = [...prev];
              updated[updated.length - 1] = { ...last, text };
              return updated;
            }
            return prev;
          });
          hasResponse = true;
        }
      }

      if (!hasResponse) {
        setMessages(prev => {
          const updated = [...prev];
          updated[updated.length - 1] = {
            ...updated[updated.length - 1],
            text: 'Xin lỗi, tôi chưa hiểu câu hỏi. Bạn vui lòng thử lại hoặc liên hệ hotline 1900 xxxx nhé!',
          };
          return updated;
        });
      }
    } catch {
      setIsBackendReady(false);
      setMessages(prev => {
        const updated = [...prev];
        updated[updated.length - 1] = {
          ...updated[updated.length - 1],
          text: 'Không thể kết nối đến AI. Vui lòng kiểm tra backend và Ollama đang chạy, sau đó thử lại.',
        };
        return updated;
      });
    } finally {
      setIsLoading(false);
    }
  };

  const formatTime = (date: Date) => {
    return date.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' });
  };

  return (
    <KeyboardAvoidingView
      behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
      className="flex-1 bg-[#1a1a1a]"
    >
      {/* Header */}
      <View className="px-6 pt-16 pb-4 bg-[#FF8C42]">
        <View className="flex-row items-center">
          <TouchableOpacity onPress={() => router.back()} className="mr-4">
            <ArrowLeft color="white" size={24} />
          </TouchableOpacity>
          <View className="flex-row items-center flex-1">
            <View className="w-10 h-10 bg-white/20 rounded-full items-center justify-center mr-3">
              <Bot color="white" size={24} />
            </View>
            <View className="flex-1">
              <Text className="text-lg text-white font-bold">Trợ lý ảo</Text>
              <View className="flex-row items-center">
                {!isBackendReady && <WifiOff size={12} color="white" style={{ marginRight: 4 }} />}
                <Text className="text-sm text-white/80">
                  {isBackendReady ? 'Luôn sẵn sàng hỗ trợ bạn' : 'Mất kết nối AI'}
                </Text>
              </View>
            </View>
          </View>
        </View>
      </View>

      {/* Messages */}
      <ScrollView
        ref={scrollViewRef}
        className="flex-1 px-4 py-4"
        contentContainerStyle={{ paddingBottom: 20 }}
        showsVerticalScrollIndicator={false}
      >
        {messages.map((message) => (
          <View
            key={message.id}
            className={`flex-row mb-4 ${message.isUser ? 'flex-row-reverse' : 'flex-row'}`}
          >
            {/* Avatar */}
            <View
              className={`w-8 h-8 rounded-full items-center justify-center flex-shrink-0 ${
                message.isUser
                  ? 'bg-[#FF8C42] ml-2'
                  : 'bg-[#FF8C42]/20 mr-2'
              }`}
            >
              {message.isUser ? (
                <User size={16} color="white" />
              ) : (
                <Bot size={16} color="#FF8C42" />
              )}
            </View>

            {/* Message Bubble */}
            <View
              className={`max-w-[75%] px-4 py-3 rounded-2xl ${
                message.isUser
                  ? 'bg-[#FF8C42] rounded-br-sm'
                  : 'bg-[#0a0a0a] border border-gray-800 rounded-bl-sm'
              }`}
            >
              <Text className="text-sm text-white leading-relaxed">{message.text}</Text>
              <Text
                className={`text-xs mt-1 ${
                  message.isUser ? 'text-white/70 text-right' : 'text-gray-500'
                }`}
              >
                {formatTime(message.timestamp)}
              </Text>
            </View>
          </View>
        ))}

        {isLoading && messages[messages.length - 1]?.text === '' && (
          <View className="flex-row mb-4">
            <View className="w-8 h-8 bg-[#FF8C42]/20 rounded-full items-center justify-center mr-2">
              <Bot size={16} color="#FF8C42" />
            </View>
            <View className="bg-[#0a0a0a] border border-gray-800 px-4 py-3 rounded-2xl rounded-bl-sm flex-row items-center">
              <ActivityIndicator size="small" color="#FF8C42" />
              <Text className="text-gray-400 text-sm ml-2">Đang suy nghĩ...</Text>
            </View>
          </View>
        )}
      </ScrollView>

      {/* Input Area */}
      <View className="px-4 pb-6 pt-2 bg-[#1a1a1a] border-t border-gray-800">
        <View className="flex-row items-center gap-3">
          <TextInput
            value={inputText}
            onChangeText={setInputText}
            placeholder="Nhập tin nhắn..."
            placeholderTextColor="#4b5563"
            multiline
            className="flex-1 bg-[#0a0a0a] border border-gray-800 rounded-2xl px-4 py-3 text-white max-h-24"
            onSubmitEditing={handleSend}
            blurOnSubmit={false}
            returnKeyType="send"
            editable={!isLoading}
          />
          <TouchableOpacity
            onPress={handleSend}
            disabled={!inputText.trim() || isLoading}
            className={`w-12 h-12 rounded-full items-center justify-center ${
              inputText.trim() && !isLoading ? 'bg-[#FF8C42]' : 'bg-gray-800'
            }`}
          >
            <Send
              size={20}
              color={inputText.trim() && !isLoading ? 'black' : '#4b5563'}
              style={{ marginLeft: 2 }}
            />
          </TouchableOpacity>
        </View>
      </View>
    </KeyboardAvoidingView>
  );
}
