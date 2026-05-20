<<<<<<< HEAD
import { useAuth } from '@/context/AuthContext';
=======
import { useAuth } from "@/frontend/context/AuthContext";
>>>>>>> LTH
import { Order, orderApi } from "@/services/api/orderApi";
import { useLocalSearchParams, useRouter } from "expo-router";
import {
    ArrowLeft,
    CheckCircle,
    CreditCard,
    MapPin,
    Truck,
    XCircle
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

export default function OrderDetailScreen() {
  const router = useRouter();
  const { id } = useLocalSearchParams();
  const { orders, token } = useAuth();
  const stringId = typeof id === "string" ? id : Array.isArray(id) ? id[0] : "";

  const [order, setOrder] = useState<Order | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const cached = orders.find((o) => o.orderId === stringId);
    if (cached) {
      setOrder(cached);
      setLoading(false);
    } else if (token) {
      orderApi
        .getOrderById(token, stringId)
        .then((data) => setOrder(data))
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

  if (!order) {
    return (
      <View className="flex-1 bg-[#1a1a1a] items-center justify-center">
        <Text className="text-white">Không tìm thấy đơn hàng.</Text>
        <TouchableOpacity
          onPress={() => router.back()}
          className="mt-4 px-4 py-2 bg-[#FF8C42] rounded-full"
        >
          <Text className="text-black font-bold">Quay lại</Text>
        </TouchableOpacity>
      </View>
    );
  }

  const steps = [
    {
      id: 1,
      title: "Đơn hàng đã được đặt",
      desc: "Chúng tôi đã nhận được đơn hàng của bạn",
      time: order.orderDate,
    },
    {
      id: 2,
      title: "Đơn hàng được xác nhận",
      desc: "Đơn hàng của bạn đã được xác nhận",
      time: new Date(
        new Date(order.orderDate).getTime() + 30 * 60000,
      ).toISOString(),
    },
    {
      id: 3,
      title: "Đang chuẩn bị hàng",
      desc: "Sản phẩm đang được đóng gói",
      time: new Date(
        new Date(order.orderDate).getTime() + 4 * 60 * 60000,
      ).toISOString(),
    },
    {
      id: 4,
      title: "Đã giao cho đơn vị vận chuyển",
      desc: "Đơn hàng đang trên đường vận chuyển",
      time: new Date(
        new Date(order.orderDate).getTime() + 24 * 60 * 60000,
      ).toISOString(),
    },
    {
      id: 5,
      title: "Đang giao hàng",
      desc: "Đơn hàng đang được giao đến bạn",
      time: new Date(
        new Date(order.orderDate).getTime() + 48 * 60 * 60000,
      ).toISOString(),
    },
    {
      id: 6,
      title: "Đã giao hàng",
      desc: "Đơn hàng đã được giao thành công",
      time: new Date(
        new Date(order.orderDate).getTime() + 72 * 60 * 60000,
      ).toISOString(),
    },
  ];

  const getCurrentStep = () => {
    switch (order.status) {
      case "DELIVERED":
        return 5;
      case "SHIPPED":
        return 4;
      case "PENDING":
        return 1;
      case "CANCELLED":
        return -1;
      default:
        return 0;
    }
  };

  const currentStep = getCurrentStep();

  const formatDateTime = (dateStr: string) => {
    const date = new Date(dateStr);
    return `${date.toLocaleDateString("vi-VN")} ${date.toLocaleTimeString("vi-VN", { hour: "2-digit", minute: "2-digit" })}`;
  };

  const formatDate = (dateStr: string) => {
    return new Date(dateStr).toLocaleDateString("vi-VN");
  };

  return (
    <View className="flex-1 bg-[#1a1a1a]">
      {/* Header */}
      <View className="px-6 pt-16 pb-4 flex-row items-center border-b border-gray-800 bg-[#1a1a1a]">
        <TouchableOpacity
          onPress={() => router.back()}
          className="mr-4 w-11 h-11 items-center justify-center"
        >
          <ArrowLeft color="white" size={24} />
        </TouchableOpacity>
        <View className="flex-1">
          <Text className="text-xl text-white font-bold">
            Chi tiết đơn hàng
          </Text>
          <Text className="text-sm text-gray-500 mt-0.5">
            #{order.orderId.slice(0, 8)}
          </Text>
        </View>
      </View>

      <ScrollView className="flex-1 p-6" showsVerticalScrollIndicator={false}>
        {/* Product Card */}
        {order.orderItems && order.orderItems.length > 0 && (
          <View className="bg-[#0a0a0a] border border-gray-800 rounded-2xl p-4 mb-4">
            <View className="flex-row items-center">
              <View className="w-20 h-20 rounded-xl overflow-hidden bg-gray-900 border border-gray-800">
                <Image
                  source={{ uri: order.orderItems[0].imageUrl }}
                  className="w-full h-full"
                  resizeMode="cover"
                />
              </View>
              <View className="flex-1 ml-3">
                <Text
                  className="text-white font-semibold mb-1"
                  numberOfLines={2}
                >
                  {order.orderItems[0].productName}
                </Text>
                <Text className="text-gray-500 text-xs mb-2">
                  Số lượng: {order.orderItems[0].quantity}
                </Text>
                <Text className="text-[#FF8C42] font-bold">
                  ₫{order.orderItems[0].priceAtPurchase.toLocaleString()}
                </Text>
              </View>
            </View>
          </View>
        )}

        {/* Order Timeline */}
        {order.status !== "CANCELLED" && (
          <View className="bg-[#0a0a0a] border border-gray-800 rounded-2xl p-5 mb-4">
            <Text className="text-white font-bold mb-4 text-lg">
              Trạng thái đơn hàng
            </Text>
            <View className="space-y-0">
              {steps.map((step, index) => {
                const isCompleted = index <= currentStep;
                const isLast = index === steps.length - 1;

                return (
                  <View key={step.id} className="flex-row items-start relative">
                    {/* Icon */}
                    <View
                      className={`w-10 h-10 rounded-full items-center justify-center flex-shrink-0 z-10 ${
                        isCompleted ? "bg-[#FF8C42]" : "bg-gray-800"
                      }`}
                    >
                      <CheckCircle
                        size={18}
                        color={isCompleted ? "black" : "#4b5563"}
                      />
                    </View>

                    {/* Content */}
                    <View className="ml-4 flex-1 pb-8">
                      <Text
                        className={`font-semibold ${isCompleted ? "text-white" : "text-gray-500"}`}
                      >
                        {step.title}
                      </Text>
                      <Text
                        className={`text-xs mt-0.5 ${isCompleted ? "text-gray-400" : "text-gray-600"}`}
                      >
                        {step.desc}
                      </Text>
                      <Text
                        className={`text-xs mt-1 ${isCompleted ? "text-gray-500" : "text-gray-700"}`}
                      >
                        {formatDateTime(step.time)}
                      </Text>
                    </View>

                    {/* Connecting Line */}
                    {!isLast && (
                      <View
                        className={`absolute left-5 top-10 w-0.5 ${
                          isCompleted ? "bg-[#FF8C42]" : "bg-gray-800"
                        }`}
                        style={{ height: 60 }}
                      />
                    )}
                  </View>
                );
              })}
            </View>
          </View>
        )}

        {/* Cancelled Status */}
        {order.status === "CANCELLED" && (
          <View className="bg-[#0a0a0a] border border-red-500/20 rounded-2xl p-5 mb-4 items-center">
            <XCircle size={40} color="#ef4444" />
            <Text className="text-red-500 font-bold text-lg mt-2">
              Đơn hàng đã hủy
            </Text>
          </View>
        )}

        {/* Shipping Info */}
        <View className="bg-[#0a0a0a] border border-gray-800 rounded-2xl p-5 mb-4">
          <Text className="text-white font-bold mb-4 text-lg">
            Thông tin giao hàng
          </Text>
          <View className="flex-row items-start mb-4">
            <MapPin color="#FF8C42" size={20} />
            <View className="ml-3 flex-1">
              <Text className="text-white font-semibold">Nguyễn Văn A</Text>
              <Text className="text-gray-400 text-sm mt-1">0912345678</Text>
              <Text className="text-gray-400 text-sm mt-1">
                123 Đường ABC, Phường 1, Quận 1, TP.HCM
              </Text>
            </View>
          </View>
          <View className="flex-row items-center border-t border-gray-800 pt-4">
            <Truck color="#9ca3af" size={16} />
            <View className="ml-3">
              <Text className="text-white text-sm">Giao hàng tiêu chuẩn</Text>
              <Text className="text-gray-500 text-xs mt-0.5">
                Phí vận chuyển: Miễn phí
              </Text>
            </View>
          </View>
        </View>

        {/* Payment Info */}
        <View className="bg-[#0a0a0a] border border-gray-800 rounded-2xl p-5 mb-4">
          <Text className="text-white font-bold mb-4 text-lg">
            Thông tin thanh toán
          </Text>
          <View className="flex-row items-center mb-4">
            <CreditCard color="#9ca3af" size={16} />
            <View className="ml-3">
              <Text className="text-white text-sm">Chuyển khoản ngân hàng</Text>
              <Text className="text-green-500 text-xs mt-0.5 font-semibold">
                Đã thanh toán
              </Text>
            </View>
          </View>

          <View className="space-y-2 pt-4 border-t border-gray-800">
            <View className="flex-row justify-between">
              <Text className="text-gray-400 text-sm">Tạm tính</Text>
              <Text className="text-white text-sm">
                ₫{order.totalAmount.toLocaleString()}
              </Text>
            </View>
            <View className="flex-row justify-between">
              <Text className="text-gray-400 text-sm">Phí vận chuyển</Text>
              <Text className="text-[#FF8C42] text-sm">Miễn phí</Text>
            </View>
            <View className="flex-row justify-between items-center pt-3 border-t border-gray-800">
              <Text className="text-white font-bold text-lg">Tổng cộng</Text>
              <Text className="text-[#FF8C42] font-bold text-xl">
                ₫{order.totalAmount.toLocaleString()}
              </Text>
            </View>
          </View>
        </View>

        {/* Action Buttons */}
        <View className="pb-8">
          <TouchableOpacity className="w-full bg-[#FF8C42] py-4 rounded-2xl items-center mb-3">
            <Text className="text-black font-bold text-lg">Mua lại</Text>
          </TouchableOpacity>
          <TouchableOpacity className="w-full bg-[#0a0a0a] border border-gray-800 py-4 rounded-2xl items-center">
            <Text className="text-white font-semibold text-lg">
              Liên hệ hỗ trợ
            </Text>
          </TouchableOpacity>
        </View>
      </ScrollView>
    </View>
  );
}
