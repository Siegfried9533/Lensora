<<<<<<< HEAD
import { useAuth } from '@/context/AuthContext';
=======
import { useAuth } from "@/frontend/context/AuthContext";
>>>>>>> LTH
import { Link, useRouter } from "expo-router";
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

export default function SignupScreen() {
  const [userName, setUserName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const { signup, isLoading, error } = useAuth();
  const router = useRouter();
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleSignup = async () => {
    if (!userName || !email || !password) {
      Alert.alert("Thông báo", "Vui lòng điền đầy đủ thông tin");
      return;
    }

    setIsSubmitting(true);
    const success = await signup(email, password, userName);
    setIsSubmitting(false);

    if (success) {
      Alert.alert("Thành công", "Tài khoản đã được tạo!");
      router.replace("/(tabs)");
    } else {
      Alert.alert("Lỗi", error || "Email đã được đăng ký");
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

        <Text className="text-3xl text-white font-bold mb-2">
          Create Account
        </Text>
        <Text className="text-sm text-gray-400 mb-8">
          Sign up to get started
        </Text>

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
            disabled={isSubmitting || isLoading}
            className="bg-[#FF8C42] py-4 rounded-xl items-center"
            style={{ opacity: isSubmitting || isLoading ? 0.5 : 1 }}
          >
            {isSubmitting || isLoading ? (
              <ActivityIndicator color="black" />
            ) : (
              <Text className="text-black font-bold text-base">Sign Up</Text>
            )}
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
    </KeyboardAvoidingView>
  );
}
