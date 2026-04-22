import React, { useEffect, useState } from 'react';
import { View, Text, TouchableOpacity, ScrollView, Alert } from 'react-native';
import { useRouter } from 'expo-router';
import { testConnection, getDebugInfo } from '@/services/api/testConnection';
import { ArrowLeft } from 'lucide-react-native';

export default function DebugNetworkScreen() {
  const router = useRouter();
  const [testResult, setTestResult] = useState<{
    success: boolean;
    message: string;
    baseUrl: string;
  } | null>(null);

  const debugInfo = getDebugInfo();

  const runTest = async () => {
    const result = await testConnection();
    setTestResult({
      success: result.success,
      message: result.message,
      baseUrl: result.baseUrl,
    });

    Alert.alert(
      result.success ? 'Kết nối thành công!' : 'Kết nối thất bại',
      result.message + '\n\nURL: ' + result.baseUrl
    );
  };

  return (
    <View className="flex-1 bg-[#1a1a1a] p-6 pt-16">
      <TouchableOpacity onPress={() => router.back()} className="mb-8 w-11 h-11 items-center justify-center">
        <ArrowLeft color="white" size={24} />
      </TouchableOpacity>

      <Text className="text-2xl text-white font-bold mb-6">Debug Network</Text>

      {/* Debug Info */}
      <View className="bg-[#0a0a0a] border border-gray-800 rounded-xl p-4 mb-4">
        <Text className="text-gray-400 text-sm mb-2">Environment Info:</Text>
        <Text className="text-green-400 font-mono text-xs">
          BASE_URL: {debugInfo.BASE_URL}
        </Text>
        <Text className="text-green-400 font-mono text-xs">
          EXPO_PUBLIC_API_URL: {debugInfo.EXPO_PUBLIC_API_URL || 'undefined'}
        </Text>
        <Text className="text-green-400 font-mono text-xs">
          Platform: {debugInfo.platform}
        </Text>
        <Text className="text-green-400 font-mono text-xs">
          Is Dev: {debugInfo.isDev ? 'true' : 'false'}
        </Text>
      </View>

      {/* Test Result */}
      {testResult && (
        <View className={`border rounded-xl p-4 mb-4 ${
          testResult.success ? 'border-green-500 bg-green-500/10' : 'border-red-500 bg-red-500/10'
        }`}>
          <Text className={`text-sm mb-2 ${
            testResult.success ? 'text-green-400' : 'text-red-400'
          }`}>
            Test Result: {testResult.success ? 'SUCCESS' : 'FAILED'}
          </Text>
          <Text className="text-gray-300 text-sm">{testResult.message}</Text>
          <Text className="text-gray-500 text-xs mt-2">URL: {testResult.baseUrl}</Text>
        </View>
      )}

      {/* Action Buttons */}
      <TouchableOpacity
        onPress={runTest}
        className="bg-[#FF8C42] py-4 rounded-xl items-center mb-4"
      >
        <Text className="text-black font-bold">Test API Connection</Text>
      </TouchableOpacity>

      <TouchableOpacity
        onPress={() => {
          Alert.alert(
            'Hướng dẫn',
            '1. Kiểm tra backend: curl http://localhost:8080/api/auth/login\n\n' +
            '2. iOS Simulator: localhost works\n\n' +
            '3. Android Emulator: dùng 10.0.2.2\n\n' +
            '4. Physical device: dùng IP (192.168.x.x)\n\n' +
            '5. Restart: npx expo start -c'
          );
        }}
        className="bg-[#0a0a0a] border border-gray-800 py-4 rounded-xl items-center"
      >
        <Text className="text-white font-bold">Help</Text>
      </TouchableOpacity>

      {/* Current IP */}
      <View className="mt-6 p-4 bg-[#0a0a0a] rounded-xl border border-gray-800">
        <Text className="text-gray-400 text-xs mb-1">Current .env setting:</Text>
        <Text className="text-green-400 font-mono text-sm">
          http://192.168.1.146:8080/api
        </Text>
        <Text className="text-gray-500 text-xs mt-2">
          Đổi thành http://10.0.2.2:8080/api nếu dùng Android Emulator
        </Text>
      </View>
    </View>
  );
}
