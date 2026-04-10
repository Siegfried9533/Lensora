import React, { useState } from 'react';
import { View, Text, TextInput, TouchableOpacity, Alert } from 'react-native';
import { useRouter, Link } from 'expo-router';
import { useAuth } from '@/context/AuthContext';
import { ArrowLeft } from 'lucide-react-native';

export default function LoginScreen() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const { login } = useAuth();
  const router = useRouter();

  const handleLogin = () => {
    if (!email || !password) {
      Alert.alert('Error', 'Please fill in all fields');
      return;
    }
    
    const success = login(email, password);
    if (success) {
      router.replace('/(tabs)');
    } else {
      Alert.alert('Error', 'Invalid credentials');
    }
  };

  return (
    <View className="flex-1 bg-[#1a1a1a] p-6 pt-16">
      <TouchableOpacity onPress={() => router.back()} className="mb-8">
        <ArrowLeft color="white" size={24} />
      </TouchableOpacity>

      <Text className="text-3xl text-white font-bold mb-2">Welcome Back</Text>
      <Text className="text-sm text-gray-400 mb-8">Sign in to continue to Camera Shop</Text>

      <View className="space-y-4">
        <TextInput
          placeholder="Email"
          placeholderTextColor="#6b7280"
          value={email}
          onChangeText={setEmail}
          autoCapitalize="none"
          keyboardType="email-address"
          className="bg-[#0a0a0a] text-white p-4 rounded-xl border border-gray-800"
        />
        <TextInput
          placeholder="Password"
          placeholderTextColor="#6b7280"
          value={password}
          onChangeText={setPassword}
          secureTextEntry
          className="bg-[#0a0a0a] text-white p-4 rounded-xl border border-gray-800 mb-6"
        />

        <TouchableOpacity 
          onPress={handleLogin}
          className="bg-[#FF8C42] py-4 rounded-xl items-center"
        >
          <Text className="text-black font-bold text-base">Sign In</Text>
        </TouchableOpacity>
      </View>

      <View className="flex-row justify-center mt-6">
        <Text className="text-gray-400">Don't have an account? </Text>
        <Link href="/(auth)/signup" asChild>
          <TouchableOpacity>
            <Text className="text-[#FF8C42] font-semibold">Sign Up</Text>
          </TouchableOpacity>
        </Link>
      </View>
    </View>
  );
}
