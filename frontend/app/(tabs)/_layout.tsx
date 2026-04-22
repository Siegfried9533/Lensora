import { Tabs } from "expo-router";
import { Bell, Bot, Compass, User, Wallet } from "lucide-react-native";
import { useEffect, useState } from "react";
import { Text, View } from "react-native";

import { useColorScheme } from "@/frontend/components/useColorScheme";
import { useAuth } from "@/frontend/context/AuthContext";
import { notificationApi } from "@/services/api/notificationApi";

function NotificationsTabIcon({ color }: { color: string }) {
  const { token } = useAuth();
  const [unreadCount, setUnreadCount] = useState(0);

  useEffect(() => {
    if (!token) return;

    const loadUnreadCount = async () => {
      try {
        const count = await notificationApi.getUnreadCount(token);
        setUnreadCount(typeof count === "number" ? count : 0);
      } catch (error) {
        console.error("Failed to load unread count:", error);
      }
    };

    loadUnreadCount();

    // Refresh every 30 seconds
    const interval = setInterval(loadUnreadCount, 30000);
    return () => clearInterval(interval);
  }, [token]);

  return (
    <View style={{ position: "relative" }}>
      <Bell size={24} color={color} />
      {unreadCount > 0 && (
        <View
          style={{
            position: "absolute",
            right: -8,
            top: -4,
            backgroundColor: "#FF8C42",
            borderRadius: 8,
            minWidth: 16,
            height: 16,
            alignItems: "center",
            justifyContent: "center",
            paddingHorizontal: 3,
          }}
        >
          <Text
            style={{
              color: "black",
              fontSize: 9,
              fontWeight: "bold",
            }}
          >
            {unreadCount > 9 ? "9+" : unreadCount}
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
        tabBarActiveTintColor: "#FF8C42",
        tabBarInactiveTintColor: "#6b7280",
        tabBarStyle: {
          backgroundColor: "#0a0a0a",
          borderTopColor: "#1a1a1a",
          borderTopWidth: 1,
          paddingTop: 8,
          paddingBottom: 8,
          height: 60,
        },
        headerShown: false,
        tabBarLabelStyle: {
          fontSize: 11,
          fontWeight: "500",
        },
      }}
    >
      <Tabs.Screen
        name="index"
        options={{
          title: "Discovery",
          tabBarIcon: ({ color }) => <Compass size={24} color={color} />,
        }}
      />
      <Tabs.Screen
        name="transactions"
        options={{
          title: "Giao dịch",
          tabBarIcon: ({ color }) => <Wallet size={24} color={color} />,
        }}
      />
      <Tabs.Screen
        name="chatbot"
        options={{
          title: "Chatbot",
          tabBarIcon: ({ color }) => <Bot size={24} color={color} />,
        }}
      />
      <Tabs.Screen
        name="notifications"
        options={{
          title: "Thông báo",
          tabBarIcon: ({ color }) => <NotificationsTabIcon color={color} />,
        }}
      />
      <Tabs.Screen
        name="profile"
        options={{
          title: "Hồ sơ",
          tabBarIcon: ({ color }) => <User size={24} color={color} />,
        }}
      />
    </Tabs>
  );
}
