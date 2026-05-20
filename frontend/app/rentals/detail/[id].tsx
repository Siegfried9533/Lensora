<<<<<<< HEAD
import { useAuth } from '@/context/AuthContext';
=======
import { useAuth } from "@/frontend/context/AuthContext";
>>>>>>> LTH
import { Rental, rentalApi } from "@/services/api/orderApi";
import { useLocalSearchParams, useRouter } from "expo-router";
import {
    ArrowLeft,
    ArrowRight,
    Camera,
    ShieldAlert
} from "lucide-react-native";
import { useEffect, useState } from "react";
import {
    ActivityIndicator,
    Alert,
    Image,
    ScrollView,
    Text,
    TouchableOpacity,
    View,
} from "react-native";

export default function RentalDetailScreen() {
  const router = useRouter();
  const { id } = useLocalSearchParams();
  const { rentals, token } = useAuth();
  const stringId = typeof id === "string" ? id : Array.isArray(id) ? id[0] : "";

  const [rental, setRental] = useState<Rental | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const cached = rentals.find((r) => r.rentalId === stringId);
    if (cached) {
      setRental(cached);
      setLoading(false);
    } else if (token) {
      rentalApi
        .getRentalById(token, stringId)
        .then((data) => setRental(data))
        .catch(() => {})
        .finally(() => setLoading(false));
    } else {
      setLoading(false);
    }
  }, [stringId, token]);

  if (loading) {
    return (
      <View className="flex-1 bg-[#1a1a1a] items-center justify-center">
        <ActivityIndicator size="large" color="#FF8C42" />
      </View>
    );
  }

  if (!rental) {
    return (
      <View className="flex-1 bg-[#1a1a1a] items-center justify-center">
        <Text className="text-white">Không tìm thấy hợp đồng thuê.</Text>
        <TouchableOpacity
          onPress={() => router.back()}
          className="mt-4 px-4 py-2 bg-[#FF8C42] rounded-full"
        >
          <Text className="text-black font-bold">Quay lại</Text>
        </TouchableOpacity>
      </View>
    );
  }

  const isActive = rental.status === "ACTIVE" || rental.status === "PENDING";

  const statusLabel =
    rental.status === "PENDING"
      ? "Chờ xác nhận"
      : rental.status === "ACTIVE"
        ? "Đang thuê"
        : rental.status === "COMPLETED"
          ? "Đã hoàn thành"
          : "Đã hủy";

  const statusColor =
    rental.status === "ACTIVE"
      ? "#22c55e"
      : rental.status === "PENDING"
        ? "#eab308"
        : rental.status === "COMPLETED"
          ? "#3b82f6"
          : "#ef4444";

  return (
    <View className="flex-1 bg-[#1a1a1a]">
      <View className="px-6 pt-16 pb-4 flex-row items-center border-b border-gray-800 bg-[#1a1a1a]">
        <TouchableOpacity
          onPress={() => router.back()}
          className="mr-4 w-11 h-11 items-center justify-center"
        >
          <ArrowLeft color="white" size={24} />
        </TouchableOpacity>
        <Text className="text-xl text-white font-bold flex-1">
          Hợp đồng thuê
        </Text>
        <View
          className="px-2.5 py-1 rounded-full"
          style={{ backgroundColor: statusColor + "33" }}
        >
          <Text
            className="text-xs font-bold uppercase"
            style={{ color: statusColor }}
          >
            {statusLabel}
          </Text>
        </View>
      </View>

      <ScrollView className="flex-1 p-6 pb-32">
        {rental.primaryImageUrl && (
          <View className="bg-[#0a0a0a] border border-gray-800 rounded-3xl overflow-hidden mb-6">
            <Image
              source={{ uri: rental.primaryImageUrl }}
              className="w-full h-48 bg-gray-900"
              resizeMode="cover"
            />
            <View className="p-5">
              <Text className="text-gray-500 text-xs font-bold mb-1 uppercase tracking-wider">
                {rental.assetBrand}
              </Text>
              <Text className="text-white text-xl font-bold">
                {rental.assetName}
              </Text>
            </View>
          </View>
        )}

        {!rental.primaryImageUrl && (
          <View className="bg-[#0a0a0a] border border-gray-800 rounded-3xl p-5 mb-6 items-center">
            <Camera size={40} color="#4b5563" />
            <Text className="text-white text-lg font-bold mt-2">
              {rental.assetName}
            </Text>
            <Text className="text-gray-500 text-xs">{rental.assetBrand}</Text>
          </View>
        )}

        <View className="bg-[#0a0a0a] border border-gray-800 rounded-2xl p-5 mb-4 space-y-4">
          <Text className="text-white font-bold mb-2">Thời gian thuê</Text>
          <View className="flex-row items-center justify-between border-b border-gray-800 pb-4">
            <View>
              <Text className="text-gray-500 text-xs mb-1">Ngày bắt đầu</Text>
              <Text className="text-white text-sm font-semibold">
                {new Date(rental.startDate).toLocaleDateString("vi-VN")}
              </Text>
            </View>
            <ArrowRight color="#4b5563" size={16} />
            <View>
              <Text className="text-gray-500 text-xs mb-1">Ngày kết thúc</Text>
              <Text className="text-white text-sm font-semibold">
                {new Date(rental.endDate).toLocaleDateString("vi-VN")}
              </Text>
            </View>
          </View>
        </View>

        <View className="bg-[#0a0a0a] border border-gray-800 rounded-2xl p-5 mb-8 space-y-4">
          <Text className="text-white font-bold mb-2">Chi tiết thanh toán</Text>

          <View className="flex-row justify-between items-center mb-2">
            <Text className="text-gray-400 text-sm">Tiền đặt cọc</Text>
            <Text className="text-white font-semibold">
              ₫{rental.depositFee.toLocaleString()}
            </Text>
          </View>

          <View className="flex-row justify-between items-center mb-2">
            <Text className="text-gray-400 text-sm">Tiền thuê</Text>
            <Text className="text-white font-semibold">
              ₫{rental.totalRentFee.toLocaleString()}
            </Text>
          </View>

          {rental.penaltyFee > 0 && (
            <View className="flex-row justify-between items-center mb-2 px-3 py-2 bg-red-500/10 rounded-lg">
              <View className="flex-row items-center">
                <ShieldAlert
                  size={14}
                  color="#f87171"
                  style={{ marginRight: 4 }}
                />
                <Text className="text-red-400 text-sm">Phí phạt</Text>
              </View>
              <Text className="text-red-400 font-semibold">
                ₫{rental.penaltyFee.toLocaleString()}
              </Text>
            </View>
          )}

          <View className="flex-row justify-between items-center pt-3 border-t border-gray-800">
            <Text className="text-white font-bold text-lg">Tổng cộng</Text>
            <Text className="text-[#FF8C42] font-bold text-xl">
              ₫{(rental.totalRentFee + rental.penaltyFee).toLocaleString()}
            </Text>
          </View>
        </View>
      </ScrollView>

      {isActive && (
        <View className="absolute bottom-0 left-0 right-0 bg-[#0a0a0a] border-t border-gray-800 px-6 pt-4 pb-8">
          <TouchableOpacity
            onPress={() => {
              Alert.alert(
                "Gia hạn thuê",
                "Bạn muốn gia hạn thêm thời gian thuê? Phí bổ sung sẽ được tính thêm.",
                [
                  { text: "Hủy", style: "cancel" },
                  {
                    text: "Xác nhận",
                    onPress: () =>
                      Alert.alert("Thành công", "Yêu cầu gia hạn đã được gửi."),
                  },
                ],
              );
            }}
            className="w-full bg-[#FF8C42] py-4 rounded-2xl items-center"
          >
            <Text className="text-black font-bold text-lg">Gia hạn thuê</Text>
          </TouchableOpacity>
        </View>
      )}
    </View>
  );
}
