import React, { useState } from 'react';
import { View, Text, TouchableOpacity, ScrollView, Switch } from 'react-native';
import { ArrowLeft, Bell, Shield, Moon, Fingerprint } from 'lucide-react-native';
import { useRouter } from 'expo-router';

export default function SettingsScreen() {
  const router = useRouter();

  // Settings State Mock
  const [pushEnabled, setPushEnabled] = useState(true);
  const [darkMode, setDarkMode] = useState(true);
  const [biometrics, setBiometrics] = useState(false);

  return (
    <View className="flex-1 bg-[#1a1a1a]">
      <View className="px-6 pt-16 pb-4 flex-row items-center border-b border-gray-800">
        <TouchableOpacity onPress={() => router.back()} className="mr-4 w-11 h-11 items-center justify-center">
          <ArrowLeft color="white" size={24} />
        </TouchableOpacity>
        <Text className="text-xl text-white font-bold flex-1">Settings</Text>
      </View>

      <ScrollView className="p-6">
        <Text className="text-gray-500 uppercase text-xs font-bold mb-3 tracking-wider px-2">App Preferences</Text>
        <View className="bg-[#0a0a0a] border border-gray-800 rounded-3xl overflow-hidden mb-8">
          
          <View className="flex-row items-center p-5 border-b border-gray-800">
            <Bell color="#9ca3af" size={20} />
            <Text className="text-white ml-4 flex-1 font-semibold">Push Notifications</Text>
            <Switch 
              value={pushEnabled} 
              onValueChange={setPushEnabled}
              trackColor={{ false: '#4b5563', true: '#FF8C42' }}
              thumbColor={pushEnabled ? '#ffffff' : '#9ca3af'}
            />
          </View>
          
          <View className="flex-row items-center p-5 border-b border-gray-800">
            <Moon color="#9ca3af" size={20} />
            <Text className="text-white ml-4 flex-1 font-semibold">Dark Mode</Text>
            <Switch 
              value={darkMode} 
              onValueChange={setDarkMode}
              trackColor={{ false: '#4b5563', true: '#FF8C42' }}
              thumbColor={darkMode ? '#ffffff' : '#9ca3af'}
            />
          </View>

        </View>

        <Text className="text-gray-500 uppercase text-xs font-bold mb-3 tracking-wider px-2">Security</Text>
        <View className="bg-[#0a0a0a] border border-gray-800 rounded-3xl overflow-hidden mb-8">
          
          <View className="flex-row items-center p-5 border-b border-gray-800">
            <Fingerprint color="#9ca3af" size={20} />
            <Text className="text-white ml-4 flex-1 font-semibold">Face ID / Biometrics</Text>
            <Switch 
              value={biometrics} 
              onValueChange={setBiometrics}
              trackColor={{ false: '#4b5563', true: '#FF8C42' }}
              thumbColor={biometrics ? '#ffffff' : '#9ca3af'}
            />
          </View>

          <TouchableOpacity className="flex-row items-center p-5">
            <Shield color="#9ca3af" size={20} />
            <Text className="text-white ml-4 flex-1 font-semibold">Privacy Policy</Text>
          </TouchableOpacity>
          
        </View>
      </ScrollView>
    </View>
  );
}
