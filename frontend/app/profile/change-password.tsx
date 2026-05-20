<<<<<<< HEAD
import { useAuth } from '@/context/AuthContext';
=======
import { useAuth } from "@/frontend/context/AuthContext";
>>>>>>> LTH
import { useRouter } from "expo-router";
import { ArrowLeft, Lock } from "lucide-react-native";
import { useState } from "react";
import {
    Alert,
    KeyboardAvoidingView,
    Platform,
    Text,
    TextInput,
    TouchableOpacity,
    View,
} from "react-native";

export default function ChangePasswordScreen() {
  const router = useRouter();
  const { changePassword } = useAuth();

  const [currentPassword, setCurrentPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [error, setError] = useState("");

  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleChangePassword = async () => {
    setError("");

    if (!currentPassword || !newPassword || !confirmPassword) {
      setError("Vui lòng điền đầy đủ thông tin");
      return;
    }

    if (newPassword !== confirmPassword) {
      setError("Mật khẩu mới không khớp");
      return;
    }

    setIsSubmitting(true);
    try {
      await changePassword(currentPassword, newPassword);
      Alert.alert("Thành công", "Mật khẩu đã được thay đổi.", [
        { text: "OK", onPress: () => router.back() },
      ]);
    } catch (e: any) {
      setError(e.message || "Mật khẩu hiện tại không đúng");
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <KeyboardAvoidingView
      behavior={Platform.OS === "ios" ? "padding" : "height"}
      className="flex-1 bg-[#1a1a1a]"
    >
      <View className="px-6 pt-16 pb-4 flex-row items-center border-b border-gray-800">
        <TouchableOpacity
          onPress={() => router.back()}
          className="mr-4 w-11 h-11 items-center justify-center"
        >
          <ArrowLeft color="white" size={24} />
        </TouchableOpacity>
        <Text className="text-xl text-white font-bold flex-1">
          Change Password
        </Text>
      </View>

      <View className="p-6">
        <View className="bg-[#0a0a0a] border border-gray-800 rounded-3xl p-6 mb-6">
          <View className="items-center mb-6">
            <View className="w-16 h-16 rounded-full bg-orange-500/10 items-center justify-center mb-2">
              <Lock color="#FF8C42" size={32} />
            </View>
            <Text className="text-white text-center font-medium">
              Create a new, strong password that you don't use for other
              websites.
            </Text>
          </View>

          {error ? (
            <Text className="text-red-500 text-sm mb-4 text-center">
              {error}
            </Text>
          ) : null}

          <View className="space-y-4">
            <View>
              <Text className="text-sm text-gray-400 mb-2 font-medium px-1">
                Current Password
              </Text>
              <TextInput
                value={currentPassword}
                onChangeText={setCurrentPassword}
                placeholder="Enter current password"
                placeholderTextColor="#6b7280"
                secureTextEntry
                className="w-full bg-[#1a1a1a] text-white px-5 py-4 rounded-2xl border border-gray-800"
              />
            </View>

            <View>
              <Text className="text-sm text-gray-400 mb-2 font-medium px-1">
                New Password
              </Text>
              <TextInput
                value={newPassword}
                onChangeText={setNewPassword}
                placeholder="Enter new password"
                placeholderTextColor="#6b7280"
                secureTextEntry
                className="w-full bg-[#1a1a1a] text-white px-5 py-4 rounded-2xl border border-gray-800"
              />
            </View>

            <View>
              <Text className="text-sm text-gray-400 mb-2 font-medium px-1">
                Confirm New Password
              </Text>
              <TextInput
                value={confirmPassword}
                onChangeText={setConfirmPassword}
                placeholder="Confirm new password"
                placeholderTextColor="#6b7280"
                secureTextEntry
                className="w-full bg-[#1a1a1a] text-white px-5 py-4 rounded-2xl border border-gray-800"
              />
            </View>
          </View>
        </View>

        <TouchableOpacity
          onPress={handleChangePassword}
          className="w-full bg-[#FF8C42] py-4 rounded-2xl items-center"
        >
          <Text className="text-black font-bold text-lg">Update Password</Text>
        </TouchableOpacity>
      </View>
    </KeyboardAvoidingView>
  );
}
