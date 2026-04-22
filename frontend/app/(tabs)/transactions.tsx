import { useAuth } from "@/frontend/context/AuthContext";
import { useRouter } from "expo-router";
import {
    Calendar,
    CheckCircle,
    Clock,
    Package,
    XCircle,
} from "lucide-react-native";
import { useEffect, useState } from "react";
import {
    ActivityIndicator,
    Image,
    ScrollView,
    Text,
    TouchableOpacity,
    View,
} from "react-native";

export default function TransactionsScreen() {
  const router = useRouter();
  const { orders, rentals, loadOrders, loadRentals, user } = useAuth();
  const [activeTab, setActiveTab] = useState<"orders" | "rentals">("orders");
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadData();
  }, [activeTab, user]);

  const loadData = async () => {
    try {
      setLoading(true);
      if (user) {
        await loadOrders();
        await loadRentals();
      }
    } catch (e) {
      console.error("Error loading transactions:", e);
    } finally {
      setLoading(false);
    }
  };

  const getStatusConfig = (status: string, type: "order" | "rental") => {
    const statusLower = status?.toLowerCase();

    if (type === "order") {
      switch (statusLower) {
        case "delivered":
          return {
            label: "Delivered",
            color: "#22c55e",
            bg: "bg-green-500/20",
            icon: CheckCircle,
          };
        case "shipping":
        case "shipped":
          return {
            label: "In Transit",
            color: "#3b82f6",
            bg: "bg-blue-500/20",
            icon: Clock,
          };
        case "pending":
          return {
            label: "Pending",
            color: "#eab308",
            bg: "bg-yellow-500/20",
            icon: Package,
          };
        case "cancelled":
          return {
            label: "Cancelled",
            color: "#ef4444",
            bg: "bg-red-500/20",
            icon: XCircle,
          };
        default:
          return {
            label: status,
            color: "#9ca3af",
            bg: "bg-gray-500/20",
            icon: Package,
          };
      }
    } else {
      switch (statusLower) {
        case "active":
          return {
            label: "Active",
            color: "#3b82f6",
            bg: "bg-blue-500/20",
            icon: Clock,
          };
        case "completed":
          return {
            label: "Completed",
            color: "#22c55e",
            bg: "bg-green-500/20",
            icon: CheckCircle,
          };
        case "pending":
          return {
            label: "Pending",
            color: "#eab308",
            bg: "bg-yellow-500/20",
            icon: Package,
          };
        case "cancelled":
          return {
            label: "Cancelled",
            color: "#ef4444",
            bg: "bg-red-500/20",
            icon: XCircle,
          };
        default:
          return {
            label: status,
            color: "#9ca3af",
            bg: "bg-gray-500/20",
            icon: Package,
          };
      }
    }
  };

  const formatDate = (dateStr: string) => {
    const date = new Date(dateStr);
    return date.toLocaleDateString("vi-VN");
  };

  const formatCurrency = (amount: number) => {
    return `₫${amount.toLocaleString()}`;
  };

  if (!user) {
    return (
      <View className="flex-1 bg-[#1a1a1a] items-center justify-center p-6">
        <Text className="text-white text-lg mb-4">
          Vui lòng đăng nhập để xem giao dịch
        </Text>
        <TouchableOpacity
          onPress={() => router.push("/(auth)/login" as any)}
          className="bg-[#FF8C42] px-6 py-3 rounded-full"
        >
          <Text className="text-black font-bold">Đăng nhập</Text>
        </TouchableOpacity>
      </View>
    );
  }

  if (loading) {
    return (
      <View className="flex-1 bg-[#1a1a1a] items-center justify-center">
        <ActivityIndicator size="large" color="#FF8C42" />
      </View>
    );
  }

  return (
    <View className="flex-1 bg-[#1a1a1a]">
      {/* Header */}
      <View className="px-6 pt-10 pb-4 bg-[#1a1a1a]">
        <Text className="text-3xl mb-6 text-white font-bold tracking-tight">
          TRANSACTIONS
        </Text>

        {/* Tabs */}
        <View className="flex-row gap-3">
          <TouchableOpacity
            onPress={() => setActiveTab("orders")}
            className={`flex-1 py-3 rounded-2xl items-center justify-center ${
              activeTab === "orders"
                ? "bg-[#FF8C42]"
                : "bg-[#0a0a0a] border border-gray-800"
            }`}
          >
            <Text
              className={
                activeTab === "orders"
                  ? "text-black font-semibold"
                  : "text-gray-400"
              }
            >
              Đơn hàng
            </Text>
          </TouchableOpacity>
          <TouchableOpacity
            onPress={() => setActiveTab("rentals")}
            className={`flex-1 py-3 rounded-2xl items-center justify-center ${
              activeTab === "rentals"
                ? "bg-[#FF8C42]"
                : "bg-[#0a0a0a] border border-gray-800"
            }`}
          >
            <Text
              className={
                activeTab === "rentals"
                  ? "text-black font-semibold"
                  : "text-gray-400"
              }
            >
              Đơn thuê
            </Text>
          </TouchableOpacity>
        </View>
      </View>

      {/* Content */}
      <ScrollView
        className="px-6 flex-1 pt-4"
        showsVerticalScrollIndicator={false}
      >
        {activeTab === "orders" ? (
          <View className="pb-8">
            {orders.length === 0 ? (
              <View className="items-center py-12">
                <Text className="text-gray-400">Chưa có đơn hàng</Text>
              </View>
            ) : (
              orders.map((order) => {
                const statusConfig = getStatusConfig(order.status, "order");
                const StatusIcon = statusConfig.icon;
                const firstItem = order.orderItems?.[0];
                return (
                  <TouchableOpacity
                    key={order.orderId}
                    onPress={() =>
                      router.push(`/orders/${order.orderId}` as any)
                    }
                    className="bg-[#0a0a0a] border border-gray-800 rounded-3xl overflow-hidden mb-4"
                  >
                    <View className="flex-row p-4">
                      {/* Image */}
                      <View className="w-24 h-24 rounded-xl overflow-hidden flex-shrink-0 bg-gray-900 border border-gray-800">
                        <Image
                          source={{ uri: firstItem?.imageUrl || undefined }}
                          className="w-full h-full"
                          resizeMode="cover"
                        />
                      </View>

                      {/* Info */}
                      <View className="flex-1 ml-4">
                        <Text
                          className="text-sm text-white font-semibold mb-1"
                          numberOfLines={2}
                        >
                          {firstItem?.productName ||
                            `Order ${order.orderId.slice(0, 8)}`}
                        </Text>
                        <Text className="text-xs text-gray-500 mb-2">
                          #{order.orderId.slice(0, 8)}
                        </Text>

                        {/* Status Badge */}
                        <View
                          className={`flex-row items-center gap-1.5 px-2.5 py-1.5 rounded-full border self-start ${statusConfig.bg} ${statusConfig.bg.replace("bg-", "border-").replace("/20", "/30")}`}
                        >
                          <StatusIcon size={14} color={statusConfig.color} />
                          <Text
                            className="text-xs font-semibold"
                            style={{ color: statusConfig.color }}
                          >
                            {statusConfig.label}
                          </Text>
                        </View>
                      </View>
                    </View>

                    {/* Footer */}
                    <View className="px-4 pb-4 pt-2 border-t border-gray-800">
                      <View className="flex-row justify-between items-center mb-2">
                        <Text className="text-gray-500 text-sm">
                          Order Date
                        </Text>
                        <Text className="text-gray-300 text-sm">
                          {formatDate(order.orderDate)}
                        </Text>
                      </View>
                      <View className="flex-row justify-between items-center">
                        <Text className="text-gray-500 text-sm">Total</Text>
                        <Text className="text-[#FF8C42] text-lg font-bold">
                          {formatCurrency(order.totalAmount)}
                        </Text>
                      </View>
                    </View>
                  </TouchableOpacity>
                );
              })
            )}
          </View>
        ) : (
          <View className="pb-8">
            {rentals.length === 0 ? (
              <View className="items-center py-12">
                <Text className="text-gray-400">Chưa có đơn thuê</Text>
              </View>
            ) : (
              rentals.map((rental) => {
                const statusConfig = getStatusConfig(rental.status, "rental");
                const StatusIcon = statusConfig.icon;

                const startDate = new Date(rental.startDate);
                const endDate = new Date(rental.endDate);
                const durationDays =
                  Math.ceil(
                    (endDate.getTime() - startDate.getTime()) /
                      (1000 * 60 * 60 * 24),
                  ) || 1;
                const dailyRate = rental.totalRentFee / durationDays;

                return (
                  <TouchableOpacity
                    key={rental.rentalId}
                    onPress={() =>
                      router.push(`/rentals/detail/${rental.rentalId}` as any)
                    }
                    className="bg-[#0a0a0a] border border-gray-800 rounded-3xl overflow-hidden mb-4"
                  >
                    <View className="flex-row p-4">
                      {/* Image */}
                      <View className="w-24 h-24 rounded-xl overflow-hidden flex-shrink-0 bg-gray-900 border border-gray-800">
                        <Image
                          source={{ uri: rental.primaryImageUrl || undefined }}
                          className="w-full h-full"
                          resizeMode="cover"
                        />
                      </View>

                      {/* Info */}
                      <View className="flex-1 ml-4">
                        <Text
                          className="text-sm text-white font-semibold mb-1"
                          numberOfLines={2}
                        >
                          {rental.assetName}
                        </Text>
                        <Text className="text-xs text-gray-500 mb-2">
                          #{rental.rentalId.slice(0, 8)}
                        </Text>

                        {/* Status Badge */}
                        <View
                          className={`flex-row items-center gap-1.5 px-2.5 py-1.5 rounded-full border self-start ${statusConfig.bg} ${statusConfig.bg.replace("bg-", "border-").replace("/20", "/30")}`}
                        >
                          <StatusIcon size={14} color={statusConfig.color} />
                          <Text
                            className="text-xs font-semibold"
                            style={{ color: statusConfig.color }}
                          >
                            {statusConfig.label}
                          </Text>
                        </View>
                      </View>
                    </View>

                    {/* Footer */}
                    <View className="px-4 pb-4 pt-2 border-t border-gray-800 space-y-2">
                      <View className="flex-row justify-between items-center">
                        <Text className="text-gray-500 text-sm">Duration</Text>
                        <View className="flex-row items-center">
                          <Calendar size={12} color="#9ca3af" />
                          <Text className="text-gray-300 text-sm ml-1">
                            {durationDays} days
                          </Text>
                        </View>
                      </View>
                      <View className="flex-row justify-between items-center">
                        <Text className="text-gray-500 text-sm">Period</Text>
                        <Text className="text-gray-300 text-xs">
                          {formatDate(rental.startDate)} -{" "}
                          {formatDate(rental.endDate)}
                        </Text>
                      </View>
                      <View className="flex-row justify-between items-center">
                        <Text className="text-gray-500 text-sm">
                          Daily Rate
                        </Text>
                        <Text className="text-gray-300 text-sm">
                          {formatCurrency(dailyRate)}
                        </Text>
                      </View>
                      <View className="flex-row justify-between items-center pt-2 border-t border-gray-800">
                        <Text className="text-gray-500 text-sm">Total</Text>
                        <Text className="text-[#FF8C42] text-lg font-bold">
                          {formatCurrency(rental.totalRentFee)}
                        </Text>
                      </View>
                    </View>
                  </TouchableOpacity>
                );
              })
            )}
          </View>
        )}
      </ScrollView>
    </View>
  );
}
