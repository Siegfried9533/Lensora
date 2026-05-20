<<<<<<< HEAD
import { useAuth } from '@/context/AuthContext';
=======
import { useAuth } from "@/frontend/context/AuthContext";
>>>>>>> LTH
import AsyncStorage from "@react-native-async-storage/async-storage";
import { useLocalSearchParams, useRouter } from "expo-router";
import { useEffect } from "react";
import { ActivityIndicator, Alert, Text, View } from "react-native";

export default function OAuthSuccessScreen() {
  const router = useRouter();
  const params = useLocalSearchParams();
  const { login } = useAuth();

  useEffect(() => {
    const handleToken = async () => {
      const token = params.token as string;

      if (token) {
        try {
          // Save token directly to AsyncStorage
          await AsyncStorage.setItem("camera_shop_token", token);

          // Fetch user data from API
          const { authApi } = await import("@/services/api/authApi");
          const userData = await authApi.getCurrentUser(token);

          // Save user data
          await AsyncStorage.setItem(
            "camera_shop_user",
            JSON.stringify({
              userId: userData.userId,
              userName: userData.userName,
              email: userData.email,
              role: userData.role,
              trustScore: userData.trustScore,
              avatarUrl: userData.avatarUrl,
            }),
          );

          // Redirect to home after successful OAuth
          setTimeout(() => {
            router.replace("/(tabs)" as any);
          }, 1000);
        } catch (error: any) {
          console.error("Failed to save OAuth token:", error);
          Alert.alert("Error", "Failed to complete sign in. Please try again.");
          router.replace("/(auth)/login" as any);
        }
      } else {
        // No token provided, redirect to login
        router.replace("/(auth)/login" as any);
      }
    };

    handleToken();
  }, [params.token]);

  return (
    <View className="flex-1 bg-[#1a1a1a] items-center justify-center">
      <ActivityIndicator size="large" color="#FF8C42" />
      <Text className="text-white mt-4 text-lg font-semibold">
        Completing sign in...
      </Text>
    </View>
  );
}
