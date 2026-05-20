<<<<<<< HEAD
import { useAuth } from '@/context/AuthContext';
=======
import { useAuth } from "@/frontend/context/AuthContext";
>>>>>>> LTH
import { useRouter } from "expo-router";
import { ArrowLeft } from "lucide-react-native";
import { Image, Text, TextInput, TouchableOpacity, View } from "react-native";

export default function PersonalInfoScreen() {
  const router = useRouter();
  const { user } = useAuth();

  return (
    <View className="flex-1 bg-[#1a1a1a]">
      <View className="px-6 pt-16 pb-4 flex-row items-center border-b border-gray-800">
        <TouchableOpacity
          onPress={() => router.back()}
          className="mr-4 w-11 h-11 items-center justify-center"
        >
          <ArrowLeft color="white" size={24} />
        </TouchableOpacity>
        <Text className="text-xl text-white font-bold flex-1">
          Personal Info
        </Text>
      </View>

      <View className="p-6 space-y-4 shadow">
        <View className="items-center mb-4 mt-2">
          <View className="w-24 h-24 rounded-full bg-black/20 border-2 border-gray-800 items-center justify-center overflow-hidden mb-3">
            {user?.avatarUrl ? (
              <Image
                source={{ uri: user.avatarUrl }}
                className="w-full h-full"
              />
            ) : (
              <Text className="text-4xl text-white">👤</Text>
            )}
          </View>
        </View>

        <View className="mb-4">
          <Text className="text-sm text-gray-400 mb-2">Username</Text>
          <TextInput
            value={user?.userName}
            editable={false}
            className="bg-[#0a0a0a] text-gray-300 p-4 rounded-xl border border-gray-800"
          />
        </View>
        <View className="mb-4">
          <Text className="text-sm text-gray-400 mb-2">Email Address</Text>
          <TextInput
            value={user?.email}
            editable={false}
            className="bg-[#0a0a0a] text-gray-300 p-4 rounded-xl border border-gray-800"
          />
        </View>
        <View className="mb-4">
          <Text className="text-sm text-gray-400 mb-2">Role</Text>
          <TextInput
            value={user?.role}
            editable={false}
            className="bg-[#0a0a0a] text-gray-300 p-4 rounded-xl border border-gray-800 uppercase"
          />
        </View>
      </View>
    </View>
  );
}
