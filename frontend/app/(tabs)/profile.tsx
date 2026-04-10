import React from 'react';
import { View, Text, ScrollView, TouchableOpacity } from 'react-native';
import {
  User,
  Heart,
  Package,
  Settings,
  Bell,
  HelpCircle,
  Shield,
  LogOut,
  ChevronRight,
  Star,
  Camera,
  Award,
} from 'lucide-react-native';
import { useAuth } from '@/context/AuthContext';
import { Link, useRouter } from 'expo-router';

export default function ProfileScreen() {
  const { user, logout, favorites } = useAuth();
  const router = useRouter();

  if (!user) {
    return (
      <View className="flex-1 bg-[#1a1a1a] items-center justify-center p-6">
        <Text className="text-2xl text-white font-bold mb-4">You are not signed in</Text>
        <Text className="text-gray-400 text-center mb-8">Sign in to view your profile, orders, and manage your account.</Text>
        <Link href="/(auth)/login" asChild>
          <TouchableOpacity className="w-full bg-[#FF8C42] py-4 rounded-full items-center">
            <Text className="text-black font-bold">Sign In</Text>
          </TouchableOpacity>
        </Link>
      </View>
    );
  }

  const stats = [
    { label: "Orders", value: "0", icon: Package },
    { label: "Rentals", value: "1", icon: Camera },
    { label: "Rating", value: user.trustScore.toString(), icon: Star },
  ];

  const menuItems = [
    {
      section: "Account",
      items: [
        { icon: User, label: "Personal Information", path: "/profile/personal-info" },
        { icon: Heart, label: "Favorites", path: "/profile/favorites", badge: favorites.length.toString() },
        { icon: Award, label: "My Equipment", path: "/profile/my-equipment" },
      ],
    },
    {
      section: "Settings",
      items: [
        { icon: Settings, label: "Account Settings", path: "/profile/settings" },
      ],
    },
  ];

  return (
    <ScrollView className="flex-1 bg-[#1a1a1a]">
      {/* Header */}
      <View className="bg-[#FF8C42] px-6 pt-12 pb-8">
        <View className="flex-row items-center gap-4 mb-6">
          <View className="w-20 h-20 rounded-full bg-black/20 border-2 border-white/20 items-center justify-center">
            <Text className="text-3xl">👤</Text>
          </View>
          <View className="flex-1">
            <Text className="text-2xl mb-1 tracking-tight text-white font-bold">{user.userName}</Text>
            <Text className="text-sm opacity-90 text-white">{user.email}</Text>
            <Text className="text-xs opacity-75 mt-1 text-white">Trust Score: {user.trustScore}</Text>
          </View>
        </View>

        {/* Stats */}
        <View className="flex-row gap-3">
          {stats.map((stat) => {
            const Icon = stat.icon;
            return (
              <View
                key={stat.label}
                className="flex-1 bg-black/20 border border-white/20 rounded-2xl p-4 items-center"
              >
                <Icon size={20} color="white" className="mb-2" />
                <Text className="text-2xl mb-1 text-white font-bold mt-2">{stat.value}</Text>
                <Text className="text-[10px] opacity-90 uppercase tracking-wide text-white">{stat.label}</Text>
              </View>
            );
          })}
        </View>
      </View>

      {/* Menu Sections */}
      <View className="px-6 py-6 space-y-4">
        {menuItems.map((section) => (
          <View key={section.section} className="mb-6">
            <Text className="text-xs text-gray-500 uppercase tracking-wider mb-3 px-2 font-semibold">
              {section.section}
            </Text>
            <View className="bg-[#0a0a0a] border border-gray-800 rounded-3xl overflow-hidden">
              {section.items.map((item, index) => {
                const Icon = item.icon;
                return (
                  <TouchableOpacity
                    key={item.path}
                    onPress={() => router.push(item.path as any)}
                    className={`w-full flex-row items-center gap-4 px-5 py-4 ${
                      index !== section.items.length - 1 ? "border-b border-gray-800" : ""
                    }`}
                  >
                    <View className="w-10 h-10 rounded-xl bg-gray-900 border border-gray-800 flex items-center justify-center">
                      <Icon size={20} color="#9ca3af" />
                    </View>
                    <Text className="flex-1 text-left text-sm text-white font-medium">{item.label}</Text>
                    {item.badge && item.badge !== '0' && (
                      <View className="bg-[#FF8C42] px-2 py-0.5 rounded-full items-center justify-center">
                        <Text className="text-black text-xs font-semibold">{item.badge}</Text>
                      </View>
                    )}
                    <ChevronRight size={16} color="#4b5563" />
                  </TouchableOpacity>
                );
              })}
            </View>
          </View>
        ))}

        {/* Logout Button */}
        <View className="pt-2">
          <TouchableOpacity 
            onPress={() => {
              logout();
              router.replace('/');
            }}
            className="w-full bg-[#0a0a0a] border border-gray-800 rounded-3xl px-5 py-4 flex-row items-center gap-4 mb-8"
          >
            <View className="w-10 h-10 rounded-xl bg-red-500/10 border border-red-500/20 flex items-center justify-center">
              <LogOut size={20} color="#ef4444" />
            </View>
            <Text className="flex-1 text-left text-sm text-red-500 font-medium">Sign Out</Text>
            <ChevronRight size={16} color="rgba(239, 68, 68, 0.5)" />
          </TouchableOpacity>
        </View>

      </View>
    </ScrollView>
  );
}
