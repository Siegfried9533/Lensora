import React, { useState } from 'react';
import { View, Text, TextInput, TouchableOpacity, Alert } from 'react-native';
import { useRouter, Link } from 'expo-router';
import { useAuth } from '@/context/AuthContext';
import { ArrowLeft } from 'lucide-react-native';

export default function SignupScreen() {
  const [userName, setUserName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const { signup } = useAuth();
  const router = useRouter();

  const handleSignup = () => {
    if (!userName || !email || !password) {
      Alert.alert('Error', 'Please fill in all fields');
      return;
    }

    const success = signup(email, password, userName);
    if (success) {
      Alert.alert('Success', 'Account created successfully!');
      router.replace('/(tabs)');
    } else {
      Alert.alert('Error', 'Email is already registered');
    }
  };

  return (
    <View className="flex-1 bg-[#1a1a1a] p-6 pt-16">
      <TouchableOpacity onPress={() => router.back()} className="mb-8">
        <ArrowLeft color="white" size={24} />
      </TouchableOpacity>

      <Text className="text-3xl text-white font-bold mb-2">Create Account</Text>
      <Text className="text-sm text-gray-400 mb-8">Sign up to get started</Text>

      <View className="space-y-4">
        <TextInput
          placeholder="User Name"
          placeholderTextColor="#6b7280"
          value={userName}
          onChangeText={setUserName}
          className="bg-[#0a0a0a] text-white p-4 rounded-xl border border-gray-800"
        />
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
          onPress={handleSignup}
          className="bg-[#FF8C42] py-4 rounded-xl items-center"
        >
          <Text className="text-black font-bold text-base">Sign Up</Text>
        </TouchableOpacity>
      </View>

      <View className="flex-row justify-center mt-6">
        <Text className="text-gray-400">Already have an account? </Text>
        <Link href="/(auth)/login" asChild>
          <TouchableOpacity>
            <Text className="text-[#FF8C42] font-semibold">Sign In</Text>
          </TouchableOpacity>
        </Link>
      </View>
    </View>
  );
}
