import { useAuth } from "@/frontend/context/AuthContext";
import { orderApi } from "@/services/api/orderApi";
import { paymentApi } from "@/services/api/paymentApi";
import { rentalApi } from "@/services/api/rentalApi";
import { useLocalSearchParams, useRouter } from "expo-router";
import { ArrowLeft, Calendar, CreditCard, Wallet } from "lucide-react-native";
import { useEffect, useMemo, useState } from "react";
import {
    Alert,
    Image,
    KeyboardAvoidingView,
    Linking,
    Platform,
    ScrollView,
    Text,
    TextInput,
    TouchableOpacity,
    View,
} from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";

interface CheckoutItem {
  id: string;
  type: "PRODUCT" | "ASSET";
  name: string;
  price: number;
  quantity: number;
  imageUrl?: string;
  startDate?: string;
  endDate?: string;
  rentalDays?: number;
}

interface CheckoutData {
  type: "BUY_NOW" | "RENT_NOW";
  items: CheckoutItem[];
  total: number;
  rentalInfo?: {
    startDate: string;
    endDate: string;
    days: number;
  };
}

export default function CheckoutScreen() {
  const router = useRouter();
  const insets = useSafeAreaInsets();
  const params = useLocalSearchParams();
  const { cartItems, clearCart, user, token, loadOrders, loadRentals } =
    useAuth();

  // Parse checkout data from params
  const checkoutData = useMemo<CheckoutData | null>(() => {
    if (params.data && typeof params.data === "string") {
      try {
        return JSON.parse(params.data);
      } catch (e) {
        console.error("Failed to parse checkout data:", e);
        return null;
      }
    }
    return null;
  }, [params.data]);

  const isDirectCheckout = checkoutData !== null;
  const checkoutItems = checkoutData?.items || cartItems;
  const checkoutType = checkoutData?.type;

  const [paymentMethod, setPaymentMethod] = useState<"COD" | "MoMo">("COD");
  const [shippingAddress, setShippingAddress] = useState("");
  const [phone, setPhone] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [shippingFee, setShippingFee] = useState<number>(35000);
  const [selectedProvince, setSelectedProvince] = useState("");
  const [selectedDistrict, setSelectedDistrict] = useState("");

  const totalPrice = useMemo(() => {
    if (checkoutData) {
      return checkoutData.total;
    }
    return cartItems.reduce(
      (sum, item) => sum + (item.price || 0) * item.quantity,
      0,
    );
  }, [checkoutData, cartItems]);

  const totalWithShipping = useMemo(() => {
    return totalPrice + (shippingFee || 0);
  }, [totalPrice, shippingFee]);

  // Calculate shipping fee when address changes
  useEffect(() => {
    const calculateFee = async () => {
      if (selectedDistrict && shippingAddress) {
        try {
          const weight = cartItems.reduce(
            (sum, item) => sum + item.quantity * 500,
            500,
          );
          const result = await paymentApi.calculateShippingFee({
            toDistrict: selectedDistrict,
            toWard: "",
            weight,
            insuranceValue: totalPrice,
          });
          setShippingFee(result.shippingFee);
        } catch (e) {
          console.error("Failed to calculate shipping fee:", e);
        }
      }
    };
    calculateFee();
  }, [selectedDistrict, shippingAddress]);

  const handlePlaceOrder = async () => {
    if (!token || !user) {
      Alert.alert("Error", "Please login to place an order");
      router.push("/(auth)/login" as any);
      return;
    }

    if (!shippingAddress || !phone) {
      Alert.alert("Error", "Please fill in shipping details");
      return;
    }

    try {
      setIsSubmitting(true);

      // Handle RENT_NOW flow
      if (checkoutType === "RENT_NOW" && checkoutData?.rentalInfo) {
        const rentalItem = checkoutData.items[0];
        if (!rentalItem || rentalItem.type !== "ASSET") {
          Alert.alert("Error", "Invalid rental item");
          return;
        }

        // Create rental
        const rental = await rentalApi.createRental(token, {
          assetId: rentalItem.id,
          startDate: checkoutData.rentalInfo.startDate,
          endDate: checkoutData.rentalInfo.endDate,
          shippingAddress: `${phone} - ${shippingAddress}`,
          paymentMethod: paymentMethod,
          shippingFee: shippingFee,
        });

        await loadRentals();

        // Handle MoMo payment for rental
        if (paymentMethod === "MoMo") {
          const paymentResponse = await paymentApi.createMoMoPayment(
            {
              orderId: rental.rentalId,
              amount: totalWithShipping,
              orderInfo: `Thanh toan don thue: ${rental.rentalId}`,
              requestType: "captureWallet",
            },
            token,
          );

          if (paymentResponse.payUrl) {
            Linking.openURL(paymentResponse.payUrl);
            router.replace("/rental-status" as any);
          }
        } else {
          Alert.alert("Success", "Rental placed successfully!");
          router.replace("/rental-status" as any);
        }
        return;
      }

      // Handle BUY_NOW or cart checkout flow
      const items = checkoutItems
        .filter((item) => item.type === "PRODUCT" && item.id)
        .map((item) => ({
          productId: item.id,
          quantity: item.quantity,
        }));

      if (items.length === 0) {
        Alert.alert("Error", "No products to purchase");
        return;
      }

      // Create order
      const order = await orderApi.createOrder(token, {
        shippingAddress: `${phone} - ${shippingAddress}`,
        paymentMethod: paymentMethod,
        shippingFee: shippingFee,
        items,
      });

      // Clear cart only if not direct checkout
      if (!isDirectCheckout) {
        await clearCart();
      }
      await loadOrders();

      // Handle MoMo payment
      if (paymentMethod === "MoMo") {
        const paymentResponse = await paymentApi.createMoMoPayment(
          {
            orderId: order.orderId,
            amount: totalWithShipping,
            orderInfo: `Thanh toan don hang: ${order.orderId}`,
            requestType: "captureWallet",
          },
          token,
        );

        if (paymentResponse.payUrl) {
          Linking.openURL(paymentResponse.payUrl);
          router.replace("/order-status" as any);
        }
      } else {
        Alert.alert("Success", "Order placed successfully!");
        router.replace("/order-status" as any);
      }
    } catch (e: any) {
      Alert.alert("Error", e.message || "Failed to place order");
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <KeyboardAvoidingView
      behavior={Platform.OS === "ios" ? "padding" : "height"}
      className="flex-1 bg-[#1a1a1a]"
    >
      {/* Header */}
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
        <Text className="text-xl text-white font-bold flex-1">Checkout</Text>
      </View>

      <ScrollView className="flex-1 px-6 pt-4 pb-20">
        {/* Order Summary */}
        {checkoutItems.length > 0 && (
          <>
            <Text className="text-sm font-bold text-gray-500 uppercase tracking-widest mb-3">
              Order Summary
            </Text>
            <View className="bg-[#0a0a0a] border border-gray-800 rounded-3xl p-5 mb-6">
              {checkoutItems.map((item, index) => (
                <View
                  key={index}
                  className={
                    index > 0 ? "mt-4 pt-4 border-t border-gray-800" : ""
                  }
                >
                  <View className="flex-row items-center">
                    {item.imageUrl && (
                      <View className="w-16 h-16 bg-black rounded-xl mr-4 overflow-hidden">
                        <Image
                          source={{ uri: item.imageUrl }}
                          className="w-full h-full"
                          resizeMode="cover"
                        />
                      </View>
                    )}
                    <View className="flex-1">
                      <Text
                        className="text-white font-semibold text-base"
                        numberOfLines={2}
                      >
                        {item.name}
                      </Text>
                      <View className="flex-row items-center mt-1">
                        <Text className="text-[#FF8C42] font-bold">
                          ₫{item.price?.toLocaleString()}
                        </Text>
                        {item.type === "ASSET" && (
                          <Text className="text-gray-500 text-xs ml-1">
                            / day
                          </Text>
                        )}
                        {item.type === "PRODUCT" && (
                          <Text className="text-gray-500 text-xs ml-2">
                            x {item.quantity}
                          </Text>
                        )}
                      </View>
                    </View>
                    {item.type === "ASSET" && item.rentalDays && (
                      <View className="items-end">
                        <Text className="text-gray-400 text-xs">
                          {item.rentalDays} day(s)
                        </Text>
                      </View>
                    )}
                  </View>
                </View>
              ))}
            </View>
          </>
        )}

        {/* Rental Info Display */}
        {checkoutData?.rentalInfo && (
          <>
            <Text className="text-sm font-bold text-gray-500 uppercase tracking-widest mb-3">
              Rental Period
            </Text>
            <View className="bg-[#0a0a0a] border border-gray-800 rounded-3xl p-5 mb-6">
              <View className="flex-row items-center mb-3">
                <Calendar size={20} color="#FF8C42" />
                <Text className="text-white font-semibold ml-3">
                  Start Date
                </Text>
              </View>
              <Text className="text-gray-300 ml-12 mb-4">
                {new Date(checkoutData.rentalInfo.startDate).toLocaleDateString(
                  "vi-VN",
                  {
                    weekday: "long",
                    day: "numeric",
                    month: "long",
                    year: "numeric",
                  },
                )}
              </Text>

              <View className="flex-row items-center mb-3">
                <Calendar size={20} color="#FF8C42" />
                <Text className="text-white font-semibold ml-3">
                  Return Date
                </Text>
              </View>
              <Text className="text-gray-300 ml-12 mb-4">
                {new Date(checkoutData.rentalInfo.endDate).toLocaleDateString(
                  "vi-VN",
                  {
                    weekday: "long",
                    day: "numeric",
                    month: "long",
                    year: "numeric",
                  },
                )}
              </Text>

              <View className="border-t border-gray-800 pt-3">
                <View className="flex-row justify-between items-center">
                  <Text className="text-gray-400">Rental Days</Text>
                  <Text className="text-white font-semibold">
                    {checkoutData.rentalInfo.days} day(s)
                  </Text>
                </View>
                <View className="flex-row justify-between items-center mt-2">
                  <Text className="text-gray-400">Daily Rate</Text>
                  <Text className="text-white font-semibold">
                    ₫{checkoutItems[0]?.price?.toLocaleString()}
                  </Text>
                </View>
                <View className="flex-row justify-between items-center mt-2">
                  <Text className="text-gray-400">Rental Subtotal</Text>
                  <Text className="text-[#FF8C42] font-bold">
                    ₫{checkoutData.total.toLocaleString()}
                  </Text>
                </View>
              </View>
            </View>
          </>
        )}

        <Text className="text-sm font-bold text-gray-500 uppercase tracking-widest mb-3">
          Shipping Details
        </Text>
        <View className="space-y-3 mb-8">
          <TextInput
            placeholder="Full Name"
            placeholderTextColor="#6b7280"
            defaultValue={user?.userName}
            className="w-full bg-[#0a0a0a] text-white px-5 py-4 rounded-2xl border border-gray-800"
          />
          <TextInput
            placeholder="Phone Number"
            placeholderTextColor="#6b7280"
            keyboardType="phone-pad"
            value={phone}
            onChangeText={setPhone}
            className="w-full bg-[#0a0a0a] text-white px-5 py-4 rounded-2xl border border-gray-800"
          />
          <TextInput
            placeholder="Full Delivery Address"
            placeholderTextColor="#6b7280"
            multiline
            value={shippingAddress}
            onChangeText={setShippingAddress}
            className="w-full bg-[#0a0a0a] text-white px-5 py-4 rounded-2xl border border-gray-800 h-24"
            textAlignVertical="top"
          />
        </View>

        <Text className="text-sm font-bold text-gray-500 uppercase tracking-widest mb-3">
          Payment Method
        </Text>
        <View className="space-y-3 mb-8">
          <TouchableOpacity
            onPress={() => setPaymentMethod("COD")}
            className={`w-full flex-row items-center px-5 py-4 rounded-2xl border ${paymentMethod === "COD" ? "bg-[#FF8C42]/10 border-[#FF8C42]" : "bg-[#0a0a0a] border-gray-800"}`}
          >
            <Wallet
              size={20}
              color={paymentMethod === "COD" ? "#FF8C42" : "#9ca3af"}
            />
            <Text
              className={`ml-3 font-semibold ${paymentMethod === "COD" ? "text-white" : "text-gray-400"}`}
            >
              Cash on Delivery
            </Text>
          </TouchableOpacity>
          <TouchableOpacity
            onPress={() => setPaymentMethod("MoMo")}
            className={`w-full flex-row items-center px-5 py-4 rounded-2xl border ${paymentMethod === "MoMo" ? "bg-[#FF8C42]/10 border-[#FF8C42]" : "bg-[#0a0a0a] border-gray-800"}`}
          >
            <CreditCard
              size={20}
              color={paymentMethod === "MoMo" ? "#FF8C42" : "#9ca3af"}
            />
            <Text
              className={`ml-3 font-semibold ${paymentMethod === "MoMo" ? "text-white" : "text-gray-400"}`}
            >
              MoMo Wallet
            </Text>
          </TouchableOpacity>
        </View>

        <View className="bg-[#0a0a0a] border border-gray-800 rounded-3xl p-5 mb-8">
          <View className="flex-row justify-between items-center mb-3">
            <Text className="text-gray-400 font-semibold">Subtotal</Text>
            <Text className="text-white font-medium">
              ₫{totalPrice.toLocaleString()}
            </Text>
          </View>
          <View className="flex-row justify-between items-center mb-3">
            <Text className="text-gray-400 font-semibold">Shipping</Text>
            <Text className="text-white font-medium">
              ₫{shippingFee.toLocaleString()}
            </Text>
          </View>
          <View className="border-t border-gray-800 pt-3 mt-1 flex-row justify-between items-center">
            <Text className="text-white font-bold text-lg">Total</Text>
            <Text className="text-[#FF8C42] font-bold text-2xl tracking-tight">
              ₫{totalWithShipping.toLocaleString()}
            </Text>
          </View>
        </View>
      </ScrollView>

      <View className="absolute bottom-0 left-0 right-0 bg-[#0a0a0a] border-t border-gray-800 px-6 pt-4 pb-8">
        <TouchableOpacity
          onPress={handlePlaceOrder}
          disabled={isSubmitting}
          className="w-full bg-[#FF8C42] py-4 rounded-2xl items-center shadow shadow-orange-500/20"
          style={{ opacity: isSubmitting ? 0.5 : 1 }}
        >
          <Text className="text-black font-bold text-lg">
            {isSubmitting ? "Placing Order..." : "Place Order"}
          </Text>
        </TouchableOpacity>
      </View>
    </KeyboardAvoidingView>
  );
}
