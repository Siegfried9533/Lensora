import React from 'react';
import { View, Text, TouchableOpacity } from 'react-native';
import { XCircle, ArrowLeft } from 'lucide-react-native';
import { useRouter } from 'expo-router';

export default function PaymentFailedScreen() {
  const router = useRouter();

  return (
    <View className="flex-1 bg-[#1a1a1a]">
      <View className="px-6 pt-16 pb-8">
        {/* Status Icon */}
        <View className="items-center mb-8">
          <View className="w-24 h-24 bg-red-500/20 rounded-full items-center justify-center">
            <XCircle size={60} color="#ef4444" />
          </View>
        </View>

        {/* Title */}
        <Text className="text-3xl text-white font-bold text-center mb-2">
          Payment Failed
        </Text>
        <Text className="text-gray-400 text-center mb-8">
          Something went wrong with your payment
        </Text>

        {/* Order Details */}
        <View className="bg-[#0a0a0a] border border-gray-800 rounded-3xl p-6 mb-6">
          <Text className="text-sm font-bold text-gray-500 uppercase tracking-widest mb-4">
            What happened?
          </Text>
          <Text className="text-gray-400 leading-6">
            Your payment could not be processed. This could be due to:
          </Text>
          <View className="mt-4 space-y-2">
            <View className="flex-row items-start">
              <Text className="text-red-500 mr-2">•</Text>
              <Text className="text-gray-400 flex-1">Insufficient funds</Text>
            </View>
            <View className="flex-row items-start">
              <Text className="text-red-500 mr-2">•</Text>
              <Text className="text-gray-400 flex-1">Card expired or invalid</Text>
            </View>
            <View className="flex-row items-start">
              <Text className="text-red-500 mr-2">•</Text>
              <Text className="text-gray-400 flex-1">Network error</Text>
            </View>
          </View>
        </View>

        {/* Actions */}
        <TouchableOpacity
          onPress={() => router.back()}
          className="w-full bg-[#FF8C42] py-4 rounded-2xl items-center mb-4"
        >
          <Text className="text-black font-bold text-lg">Try Again</Text>
        </TouchableOpacity>

        <TouchableOpacity
          onPress={() => router.replace('/(tabs)/index' as any)}
          className="w-full bg-[#0a0a0a] border border-gray-800 py-4 rounded-2xl items-center"
        >
          <Text className="text-gray-400 font-semibold">Back to Home</Text>
        </TouchableOpacity>
      </View>
    </View>
  );
}
