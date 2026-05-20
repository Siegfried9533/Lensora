<<<<<<< HEAD
import { useAuth } from '@/context/AuthContext';
=======
import { useAuth } from "@/frontend/context/AuthContext";
>>>>>>> LTH
import { paymentApi } from "@/services/api/paymentApi";
import { useLocalSearchParams, useRouter } from "expo-router";
import { CheckCircle, XCircle } from "lucide-react-native";
import { useEffect, useState } from "react";
import {
    ActivityIndicator,
    ScrollView,
    Text,
    TouchableOpacity,
    View,
} from "react-native";

export default function PaymentSuccessScreen() {
  const router = useRouter();
  const params = useLocalSearchParams();
  const { token } = useAuth();
  const [paymentStatus, setPaymentStatus] = useState<
    "success" | "failed" | "pending"
  >("pending");
  const [orderCode, setOrderCode] = useState<string>("");
  const [amount, setAmount] = useState<number>(0);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const checkPaymentStatus = async () => {
      const orderCodeParam = params.orderCode as string;
      if (!orderCodeParam) {
        setPaymentStatus("failed");
        setLoading(false);
        return;
      }

      try {
        const result = await paymentApi.getPaymentStatus(orderCodeParam);
        setOrderCode(result.orderCode);
        setAmount(result.amount);

        if (result.success) {
          setPaymentStatus("success");
        } else {
          setPaymentStatus("failed");
        }
      } catch (e) {
        setPaymentStatus("pending");
      } finally {
        setLoading(false);
      }
    };

    checkPaymentStatus();
  }, [params.orderCode]);

  if (loading) {
    return (
      <View className="flex-1 bg-[#1a1a1a] items-center justify-center">
        <ActivityIndicator size="large" color="#FF8C42" />
        <Text className="text-gray-400 mt-4">Checking payment status...</Text>
      </View>
    );
  }

  return (
    <ScrollView className="flex-1 bg-[#1a1a1a]">
      <View className="px-6 pt-16 pb-8">
        {/* Status Icon */}
        <View className="items-center mb-8">
          {paymentStatus === "success" ? (
            <View className="w-24 h-24 bg-green-500/20 rounded-full items-center justify-center">
              <CheckCircle size={60} color="#22c55e" />
            </View>
          ) : (
            <View className="w-24 h-24 bg-red-500/20 rounded-full items-center justify-center">
              <XCircle size={60} color="#ef4444" />
            </View>
          )}
        </View>

        {/* Title */}
        <Text className="text-3xl text-white font-bold text-center mb-2">
          {paymentStatus === "success"
            ? "Payment Successful!"
            : "Payment Failed"}
        </Text>
        <Text className="text-gray-400 text-center mb-8">
          {paymentStatus === "success"
            ? "Your order has been confirmed"
            : "Please try again or contact support"}
        </Text>

        {/* Order Details */}
        <View className="bg-[#0a0a0a] border border-gray-800 rounded-3xl p-6 mb-6">
          <Text className="text-sm font-bold text-gray-500 uppercase tracking-widest mb-4">
            Order Details
          </Text>
          <View className="space-y-3">
            <View className="flex-row justify-between">
              <Text className="text-gray-400">Order Code</Text>
              <Text className="text-white font-medium">
                {orderCode || "N/A"}
              </Text>
            </View>
            <View className="flex-row justify-between">
              <Text className="text-gray-400">Amount Paid</Text>
              <Text className="text-[#FF8C42] font-bold">
                ₫{amount ? amount.toLocaleString() : "0"}
              </Text>
            </View>
            <View className="flex-row justify-between">
              <Text className="text-gray-400">Status</Text>
              <Text
                className={`font-semibold ${
                  paymentStatus === "success"
                    ? "text-green-500"
                    : paymentStatus === "pending"
                      ? "text-yellow-500"
                      : "text-red-500"
                }`}
              >
                {paymentStatus.toUpperCase()}
              </Text>
            </View>
          </View>
        </View>

        {/* Actions */}
        {paymentStatus === "success" ? (
          <TouchableOpacity
            onPress={() => router.replace("/order-status" as any)}
            className="w-full bg-[#FF8C42] py-4 rounded-2xl items-center mb-4"
          >
            <Text className="text-black font-bold text-lg">
              View Order Status
            </Text>
          </TouchableOpacity>
        ) : (
          <TouchableOpacity
            onPress={() => router.replace("/checkout" as any)}
            className="w-full bg-[#FF8C42] py-4 rounded-2xl items-center mb-4"
          >
            <Text className="text-black font-bold text-lg">Try Again</Text>
          </TouchableOpacity>
        )}

        <TouchableOpacity
          onPress={() => router.replace("/(tabs)/index" as any)}
          className="w-full bg-[#0a0a0a] border border-gray-800 py-4 rounded-2xl items-center"
        >
          <Text className="text-gray-400 font-semibold">Back to Home</Text>
        </TouchableOpacity>
      </View>
    </ScrollView>
  );
}
