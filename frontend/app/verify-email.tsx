import { useAuth } from "@/frontend/context/AuthContext";
import { authApi } from "@/services/api/authApi";
import { useLocalSearchParams, useRouter } from "expo-router";
import { ArrowLeft, Mail } from "lucide-react-native";
import { useState } from "react";
import {
    ActivityIndicator,
    Alert,
    Text,
    TextInput,
    TouchableOpacity,
    View,
} from "react-native";

export default function VerifyEmailScreen() {
  const router = useRouter();
  const params = useLocalSearchParams();
  const { user, token } = useAuth();
  const [verificationCode, setVerificationCode] = useState("");
  const [isVerifying, setIsVerifying] = useState(false);
  const [isResending, setIsResending] = useState(false);

  const handleVerify = async () => {
    if (!verificationCode.trim()) {
      Alert.alert(
        "Error",
        "Please enter the verification code from your email",
      );
      return;
    }

    try {
      setIsVerifying(true);
      await authApi.verifyEmail(verificationCode.trim());
      Alert.alert("Success", "Email verified successfully!", [
        { text: "OK", onPress: () => router.back() },
      ]);
    } catch (e: any) {
      Alert.alert("Error", e.message || "Invalid or expired verification code");
    } finally {
      setIsVerifying(false);
    }
  };

  const handleResend = async () => {
    if (!user?.email) {
      Alert.alert("Error", "User email not found");
      return;
    }

    try {
      setIsResending(true);
      await authApi.resendVerificationEmail(user.email);
      Alert.alert(
        "Success",
        "Verification email sent. Please check your inbox.",
      );
    } catch (e: any) {
      Alert.alert("Error", e.message || "Failed to resend verification email");
    } finally {
      setIsResending(false);
    }
  };

  return (
    <View className="flex-1 bg-[#1a1a1a]">
      {/* Header */}
      <View className="px-6 pt-16 pb-4 flex-row items-center border-b border-gray-800">
        <TouchableOpacity
          onPress={() => router.back()}
          className="mr-4 w-11 h-11 items-center justify-center"
        >
          <ArrowLeft color="white" size={24} />
        </TouchableOpacity>
        <Text className="text-xl text-white font-bold flex-1">
          Verify Email
        </Text>
      </View>

      <View className="flex-1 px-6 py-12">
        {/* Icon and Title */}
        <View className="items-center mb-8">
          <View className="w-20 h-20 bg-[#FF8C42]/20 rounded-full items-center justify-center mb-4">
            <Mail size={40} color="#FF8C42" />
          </View>
          <Text className="text-2xl text-white font-bold text-center mb-2">
            Check Your Email
          </Text>
          <Text className="text-gray-400 text-center">
            We sent a verification link to{"\n"}
            <Text className="text-[#FF8C42]">{user?.email}</Text>
          </Text>
        </View>

        {/* Verification Code Input */}
        <View className="mb-6">
          <Text className="text-gray-400 text-sm mb-2">
            Click the link in the email or enter the verification code below:
          </Text>
          <TextInput
            placeholder="Verification code"
            placeholderTextColor="#6b7280"
            value={verificationCode}
            onChangeText={setVerificationCode}
            className="w-full bg-[#0a0a0a] text-white px-5 py-4 rounded-2xl border border-gray-800"
            autoCapitalize="none"
            autoCorrect={false}
          />
        </View>

        {/* Verify Button */}
        <TouchableOpacity
          onPress={handleVerify}
          disabled={isVerifying}
          className="w-full bg-[#FF8C42] py-4 rounded-2xl items-center mb-4"
          style={{ opacity: isVerifying ? 0.5 : 1 }}
        >
          {isVerifying ? (
            <ActivityIndicator color="black" />
          ) : (
            <Text className="text-black font-bold text-lg">Verify Email</Text>
          )}
        </TouchableOpacity>

        {/* Resend Button */}
        <TouchableOpacity
          onPress={handleResend}
          disabled={isResending}
          className="w-full bg-[#0a0a0a] border border-gray-800 py-4 rounded-2xl items-center"
          style={{ opacity: isResending ? 0.5 : 1 }}
        >
          {isResending ? (
            <ActivityIndicator color="#FF8C42" />
          ) : (
            <Text className="text-[#FF8C42] font-bold">
              Resend Verification Email
            </Text>
          )}
        </TouchableOpacity>

        {/* Help Text */}
        <View className="mt-8 items-center">
          <Text className="text-gray-500 text-sm text-center">
            Didn't receive the email? Check your spam folder or try another
            email address.
          </Text>
        </View>
      </View>
    </View>
  );
}
