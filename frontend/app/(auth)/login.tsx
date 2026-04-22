import { useAuth } from "@/frontend/context/AuthContext";
import { BASE_URL } from "@/services/api/config";
import { Link, useRouter } from "expo-router";
import * as WebBrowser from "expo-web-browser";
import { ArrowLeft } from "lucide-react-native";
import { useState } from "react";
import {
    ActivityIndicator,
    Alert,
    KeyboardAvoidingView,
    Platform,
    Text,
    TextInput,
    TouchableOpacity,
    View,
} from "react-native";

// Get API base URL for OAuth (remove /api suffix)
const getApiBaseUrl = () => BASE_URL.replace("/api", "");

export default function LoginScreen() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const { login, isLoading } = useAuth();
  const router = useRouter();
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleLogin = async () => {
    if (!email || !password) {
      Alert.alert("Thiếu thông tin", "Vui lòng nhập email và mật khẩu");
      return;
    }

    setIsSubmitting(true);
    try {
      await login(email, password);
      router.replace("/(tabs)");
    } catch (e: any) {
      const errMsg = e.message || "Đăng nhập thất bại";
      let errorMessage = errMsg;
      if (
        errMsg.includes("localhost") ||
        errMsg.includes("Cannot connect") ||
        errMsg.includes("Network request failed")
      ) {
        errorMessage =
          "Không thể kết nối đến máy chủ.\n\nVui lòng kiểm tra:\n1. Backend đang chạy trên cổng 8080\n2. Địa chỉ IP trong config.ts đúng với máy tính của bạn";
      } else if (
        errMsg.includes("Invalid email or password") ||
        errMsg.includes("Unauthorized")
      ) {
        errorMessage = "Email hoặc mật khẩu không đúng";
      }
      Alert.alert("Đăng nhập thất bại", errorMessage);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <KeyboardAvoidingView
      behavior={Platform.OS === "ios" ? "padding" : "height"}
      className="flex-1 bg-[#1a1a1a]"
    >
      <View className="flex-1 p-6 pt-16">
        <TouchableOpacity
          onPress={() => router.back()}
          className="mb-8 w-11 h-11 items-center justify-center"
        >
          <ArrowLeft color="white" size={24} />
        </TouchableOpacity>

        <Text className="text-3xl text-white font-bold mb-2">Welcome Back</Text>
        <Text className="text-sm text-gray-400 mb-8">
          Sign in to continue to Camera Shop
        </Text>

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
            disabled={isSubmitting || isLoading}
            className="bg-[#FF8C42] py-4 rounded-xl items-center"
            style={{ opacity: isSubmitting || isLoading ? 0.5 : 1 }}
          >
            {isSubmitting || isLoading ? (
              <ActivityIndicator color="black" />
            ) : (
              <Text className="text-black font-bold text-base">Sign In</Text>
            )}
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

        {/* Divider */}
        <View className="flex-row items-center my-6">
          <View className="flex-1 h-[1px] bg-gray-800" />
          <Text className="text-gray-500 px-4 text-sm">OR CONTINUE WITH</Text>
          <View className="flex-1 h-[1px] bg-gray-800" />
        </View>

        {/* OAuth Buttons */}
        <View className="space-y-3">
          <TouchableOpacity
            onPress={() => {
              // Open OAuth login in web browser
              // Backend will redirect to frontend oauth-success route with token
              // Use environment-aware base URL
              const baseUrl = getApiBaseUrl();
              const authUrl = `${baseUrl}/oauth2/authorization/google`;
              console.log("Opening OAuth URL:", authUrl);
              WebBrowser.openBrowserAsync(authUrl, {
                dismissButtonStyle: "cancel",
                preferredBarTintColor: "#1a1a1a",
                preferredControlTintColor: "white",
              });
            }}
            className="w-full bg-white py-4 rounded-xl items-center flex-row justify-center px-4"
          >
            <Text className="text-black font-bold text-base">
              Continue with Google
            </Text>
          </TouchableOpacity>

          <TouchableOpacity
            onPress={() => {
              // Apple login would be implemented similarly
              Alert.alert("Coming Soon", "Apple login will be available soon");
            }}
            className="w-full bg-[#0a0a0a] border border-gray-800 py-4 rounded-xl items-center flex-row justify-center px-4"
          >
            <Text className="text-white font-bold text-base">
              Continue with Apple
            </Text>
          </TouchableOpacity>
        </View>

        {/* Test credentials hint */}
        <View className="mt-8 p-4 bg-[#0a0a0a] rounded-xl border border-gray-800">
          <Text className="text-xs text-gray-500 mb-2 font-semibold">
            TEST CREDENTIALS:
          </Text>
          <Text className="text-xs text-gray-400">Email: test@example.com</Text>
          <Text className="text-xs text-gray-400">Password: password123</Text>
          <Text className="text-xs text-gray-500 mt-2">OR (Admin):</Text>
          <Text className="text-xs text-gray-400">Email: john@example.com</Text>
          <Text className="text-xs text-gray-400">Password: password123</Text>
        </View>
      </View>
    </KeyboardAvoidingView>
  );
}
