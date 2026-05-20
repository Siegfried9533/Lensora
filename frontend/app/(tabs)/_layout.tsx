<<<<<<< HEAD
import React, { useState, useEffect } from 'react';
import { View, Text } from 'react-native';
import { Tabs } from 'expo-router';
import { Compass, Wallet, Bot, Bell, User } from 'lucide-react-native';

import Colors from '@/constants/Colors';
import { useColorScheme } from '@/components/useColorScheme';
import { useAuth } from '@/context/AuthContext';
import { notificationApi } from '@/services/api/notificationApi';
=======
import { Tabs } from "expo-router";
import { Bell, Bot, Compass, User, Wallet } from "lucide-react-native";
import { useEffect, useState } from "react";
import { Text, View } from "react-native";

import { useColorScheme } from "@/frontend/components/useColorScheme";
import { useAuth } from "@/frontend/context/AuthContext";
import { notificationApi } from "@/services/api/notificationApi";
>>>>>>> LTH

function NotificationsTabIcon({ color }: { color: string }) {
  const { token } = useAuth();
  const [unreadCount, setUnreadCount] = useState(0);

  useEffect(() => {
    if (!token) return;

    const loadUnreadCount = async () => {
      try {
        const count = await notificationApi.getUnreadCount(token);
<<<<<<< HEAD
        setUnreadCount(typeof count === 'number' ? count : 0);
      } catch (error) {
        console.error('Failed to load unread count:', error);
=======
        setUnreadCount(typeof count === "number" ? count : 0);
      } catch (error) {
        console.error("Failed to load unread count:", error);
>>>>>>> LTH
      }
    };

    loadUnreadCount();

    // Refresh every 30 seconds
    const interval = setInterval(loadUnreadCount, 30000);
    return () => clearInterval(interval);
  }, [token]);

  return (
<<<<<<< HEAD
    <View style={{ position: 'relative' }}>
=======
    <View style={{ position: "relative" }}>
>>>>>>> LTH
      <Bell size={24} color={color} />
      {unreadCount > 0 && (
        <View
          style={{
<<<<<<< HEAD
            position: 'absolute',
            right: -8,
            top: -4,
            backgroundColor: '#FF8C42',
            borderRadius: 8,
            minWidth: 16,
            height: 16,
            alignItems: 'center',
            justifyContent: 'center',
=======
            position: "absolute",
            right: -8,
            top: -4,
            backgroundColor: "#FF8C42",
            borderRadius: 8,
            minWidth: 16,
            height: 16,
            alignItems: "center",
            justifyContent: "center",
>>>>>>> LTH
            paddingHorizontal: 3,
          }}
        >
          <Text
            style={{
<<<<<<< HEAD
              color: 'black',
              fontSize: 9,
              fontWeight: 'bold',
            }}
          >
            {unreadCount > 9 ? '9+' : unreadCount}
=======
              color: "black",
              fontSize: 9,
              fontWeight: "bold",
            }}
          >
            {unreadCount > 9 ? "9+" : unreadCount}
>>>>>>> LTH
          </Text>
        </View>
      )}
    </View>
  );
}

export default function TabLayout() {
  const colorScheme = useColorScheme();
  const { token } = useAuth();

  return (
    <Tabs
      screenOptions={{
<<<<<<< HEAD
        tabBarActiveTintColor: '#FF8C42',
        tabBarInactiveTintColor: '#6b7280',
        tabBarStyle: {
          backgroundColor: '#0a0a0a',
          borderTopColor: '#1a1a1a',
=======
        tabBarActiveTintColor: "#FF8C42",
        tabBarInactiveTintColor: "#6b7280",
        tabBarStyle: {
          backgroundColor: "#0a0a0a",
          borderTopColor: "#1a1a1a",
>>>>>>> LTH
          borderTopWidth: 1,
          paddingTop: 8,
          paddingBottom: 8,
          height: 60,
        },
        headerShown: false,
        tabBarLabelStyle: {
          fontSize: 11,
<<<<<<< HEAD
          fontWeight: '500',
        },
      }}>
      <Tabs.Screen
        name="index"
        options={{
          title: 'Discovery',
=======
          fontWeight: "500",
        },
      }}
    >
      <Tabs.Screen
        name="index"
        options={{
          title: "Discovery",
>>>>>>> LTH
          tabBarIcon: ({ color }) => <Compass size={24} color={color} />,
        }}
      />
      <Tabs.Screen
        name="transactions"
        options={{
<<<<<<< HEAD
          title: 'Giao dịch',
=======
          title: "Giao dịch",
>>>>>>> LTH
          tabBarIcon: ({ color }) => <Wallet size={24} color={color} />,
        }}
      />
      <Tabs.Screen
        name="chatbot"
        options={{
<<<<<<< HEAD
          title: 'Chatbot',
=======
          title: "Chatbot",
>>>>>>> LTH
          tabBarIcon: ({ color }) => <Bot size={24} color={color} />,
        }}
      />
      <Tabs.Screen
        name="notifications"
        options={{
<<<<<<< HEAD
          title: 'Thông báo',
=======
          title: "Thông báo",
>>>>>>> LTH
          tabBarIcon: ({ color }) => <NotificationsTabIcon color={color} />,
        }}
      />
      <Tabs.Screen
        name="profile"
        options={{
<<<<<<< HEAD
          title: 'Hồ sơ',
=======
          title: "Hồ sơ",
>>>>>>> LTH
          tabBarIcon: ({ color }) => <User size={24} color={color} />,
        }}
      />
    </Tabs>
  );
<<<<<<< HEAD
}
=======
}
>>>>>>> LTH
