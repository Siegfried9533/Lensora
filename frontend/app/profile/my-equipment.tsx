import { useAuth } from "@/frontend/context/AuthContext";
import { useRouter } from "expo-router";
import { ArrowLeft, ChevronRight, Package } from "lucide-react-native";
import { ScrollView, Text, TouchableOpacity, View } from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";

export default function MyEquipmentScreen() {
  const router = useRouter();
  const insets = useSafeAreaInsets();
  const { user, orders, rentals } = useAuth();

  const statusLabel = (status: string) => {
    switch (status) {
      case "PENDING":
        return "Chờ xác nhận";
      case "ACTIVE":
        return "Đang thuê";
      case "COMPLETED":
        return "Đã hoàn thành";
      case "CANCELLED":
        return "Đã hủy";
      case "SHIPPED":
        return "Đang giao";
      case "DELIVERED":
        return "Đã giao";
      default:
        return status;
    }
  };

  const statusBg = (status: string) => {
    if (status === "ACTIVE" || status === "DELIVERED") return "bg-green-500/20";
    if (status === "CANCELLED") return "bg-red-500/20";
    return "bg-yellow-500/20";
  };

  const statusText = (status: string) => {
    if (status === "ACTIVE" || status === "DELIVERED") return "text-green-500";
    if (status === "CANCELLED") return "text-red-500";
    return "text-yellow-500";
  };

  return (
    <View className="flex-1 bg-[#1a1a1a]">
      <View
        className="px-6 pb-4 flex-row items-center border-b border-gray-800"
        style={{ paddingTop: insets.top + 16 }}
      >
        <TouchableOpacity
          onPress={() => router.back()}
          className="mr-4 w-11 h-11 items-center justify-center"
        >
          <ArrowLeft color="white" size={24} />
        </TouchableOpacity>
        <Text className="text-xl text-white font-bold flex-1">
          Thiết bị của tôi
        </Text>
      </View>

      <ScrollView className="p-6">
        <Text className="text-lg font-bold text-white mb-4">Đang thuê</Text>

        {rentals.length === 0 ? (
          <View className="border border-dashed border-gray-800 rounded-2xl p-6 items-center bg-[#0a0a0a]/50">
            <Package color="#4b5563" size={32} style={{ marginBottom: 12 }} />
            <Text className="text-gray-500 text-center">
              Bạn chưa thuê thiết bị nào.
            </Text>
          </View>
        ) : (
          rentals.map((rental) => (
            <TouchableOpacity
              key={rental.rentalId}
              onPress={() =>
                router.push(`/rentals/detail/${rental.rentalId}` as any)
              }
              className="bg-[#0a0a0a] border border-gray-800 rounded-2xl p-4 mb-4"
            >
              <View className="flex-row items-center justify-between mb-3">
                <Text className="text-white font-bold">{rental.assetName}</Text>
                <View
                  className={`${statusBg(rental.status)} px-2 py-1 rounded`}
                >
                  <Text
                    className={`${statusText(rental.status)} text-[10px] uppercase font-bold`}
                  >
                    {statusLabel(rental.status)}
                  </Text>
                </View>
              </View>
              <Text className="text-gray-400 text-sm mb-1">
                Bắt đầu:{" "}
                {new Date(rental.startDate).toLocaleDateString("vi-VN")}
              </Text>

              <View className="flex-row justify-between items-center mt-3 pt-3 border-t border-gray-800">
                <Text className="text-gray-500 text-xs">Xem hợp đồng</Text>
                <ChevronRight size={16} color="#FF8C42" />
              </View>
            </TouchableOpacity>
          ))
        )}

        <View className="mt-8">
          <Text className="text-lg font-bold text-white mb-4">
            Sản phẩm đã mua
          </Text>
          {orders.length === 0 ? (
            <View className="border border-dashed border-gray-800 rounded-2xl p-6 items-center bg-[#0a0a0a]/50">
              <Package color="#4b5563" size={32} style={{ marginBottom: 12 }} />
              <Text className="text-gray-500 text-center">
                Bạn chưa mua sản phẩm nào.
              </Text>
            </View>
          ) : (
            orders.map((order) => (
              <TouchableOpacity
                key={order.orderId}
                onPress={() => router.push(`/orders/${order.orderId}` as any)}
                className="bg-[#0a0a0a] border border-gray-800 rounded-2xl p-4 mb-4 flex-row items-center justify-between"
              >
                <View>
                  <Text className="text-white font-bold mb-1">
                    Đơn hàng #{order.orderId.slice(0, 8)}
                  </Text>
                  <Text className="text-gray-400 text-xs">
                    {new Date(order.orderDate).toLocaleDateString("vi-VN")} •{" "}
                    {statusLabel(order.status)}
                  </Text>
                </View>
                <ChevronRight size={20} color="#FF8C42" />
              </TouchableOpacity>
            ))
          )}
        </View>
      </ScrollView>
    </View>
  );
}
