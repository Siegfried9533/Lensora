import { useAuth } from "@/frontend/context/AuthContext";
import { useRouter } from "expo-router";
import {
    ArrowLeft,
    Minus,
    Package,
    Plus,
    ShoppingBag,
    Trash2,
} from "lucide-react-native";
import {
    Image,
    ScrollView,
    Text,
    TouchableOpacity,
    View
} from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";

export default function CartScreen() {
  const router = useRouter();
  const insets = useSafeAreaInsets();
  const { cartItems, updateQuantity, removeFromCart, clearCart } = useAuth();

  const totalPrice = cartItems.reduce(
    (sum, item) => sum + (item.price || 0) * item.quantity,
    0,
  );

  const handleCheckout = () => {
    if (cartItems.length === 0) return;
    router.push("/checkout" as any);
  };

  const getItemName = (item: (typeof cartItems)[0]) => {
    return item.productName || item.assetName || "Unknown Item";
  };

  const getItemImage = (item: (typeof cartItems)[0]) => {
    return item.primaryImageUrl || null;
  };

  if (cartItems.length === 0) {
    return (
      <View className="flex-1 bg-[#1a1a1a]">
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
          <Text className="text-xl text-white font-bold flex-1">Giỏ hàng</Text>
        </View>

        <View className="flex-1 justify-center items-center p-6">
          <View className="w-24 h-24 rounded-full bg-black/20 border border-gray-800 items-center justify-center mb-6">
            <ShoppingBag size={40} color="#4b5563" />
          </View>
          <Text className="text-xl text-white font-bold mb-2">
            Giỏ hàng trống
          </Text>
          <Text className="text-gray-400 text-center mb-8">
            Looks like you haven't added any equipment to your cart yet.
          </Text>
          <TouchableOpacity
            onPress={() => router.push("/(tabs)" as any)}
            className="bg-[#FF8C42] px-8 py-4 rounded-full"
          >
            <Text className="text-black font-bold">Start Shopping</Text>
          </TouchableOpacity>
        </View>
      </View>
    );
  }

  return (
    <View className="flex-1 bg-[#1a1a1a]">
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
        <Text className="text-xl text-white font-bold flex-1">Giỏ hàng</Text>
        <TouchableOpacity onPress={() => clearCart()}>
          <Text className="text-red-500 font-semibold">Xóa giỏ hàng</Text>
        </TouchableOpacity>
      </View>

      <ScrollView className="flex-1 py-4">
        {cartItems.map((item) => (
          <View
            key={item.cartItemId}
            className="mx-6 mb-4 bg-[#0a0a0a] rounded-3xl p-4 border border-gray-800 flex-row items-center"
          >
            {getItemImage(item) ? (
              <Image
                source={{ uri: getItemImage(item)! }}
                className="w-20 h-20 rounded-2xl bg-black"
                resizeMode="contain"
              />
            ) : (
              <View className="w-20 h-20 rounded-2xl bg-gray-900 items-center justify-center">
                <Package size={24} color="#4b5563" />
              </View>
            )}
            <View className="flex-1 ml-4 justify-center">
              <Text className="text-[10px] text-gray-500 uppercase mb-1 font-bold">
                {item.type}
              </Text>
              <Text className="text-white font-bold mb-1" numberOfLines={2}>
                {getItemName(item)}
              </Text>
              <Text className="text-[#FF8C42] font-semibold mb-3">
                ₫{(item.price || 0).toLocaleString()}{" "}
                {item.type === "ASSET" ? "/day" : ""}
              </Text>

              {/* Quantity controls */}
              <View className="flex-row items-center justify-between">
                <View className="flex-row items-center bg-[#1a1a1a] rounded-full border border-gray-800">
                  <TouchableOpacity
                    className="w-11 h-11 items-center justify-center"
                    onPress={() =>
                      item.quantity > 1
                        ? updateQuantity(item.cartItemId, -1)
                        : removeFromCart(item.cartItemId)
                    }
                  >
                    <Minus size={14} color="white" />
                  </TouchableOpacity>
                  <Text className="text-white font-semibold flex-1 text-center min-w-[24px]">
                    {item.quantity}
                  </Text>
                  <TouchableOpacity
                    className="w-11 h-11 items-center justify-center"
                    onPress={() => updateQuantity(item.cartItemId, 1)}
                  >
                    <Plus size={14} color="white" />
                  </TouchableOpacity>
                </View>

                <TouchableOpacity
                  onPress={() => removeFromCart(item.cartItemId)}
                  className="w-8 h-8 rounded-full bg-red-500/10 items-center justify-center"
                >
                  <Trash2 size={16} color="#ef4444" />
                </TouchableOpacity>
              </View>
            </View>
          </View>
        ))}
      </ScrollView>

      {/* Bottom Checkout Bar */}
      <View className="bg-[#0a0a0a] border-t border-gray-800 px-6 pt-6 pb-10 rounded-t-3xl shadow-lg">
        <View className="flex-row justify-between items-end mb-4">
          <Text className="text-gray-400 font-semibold">Tổng cộng</Text>
          <Text className="text-3xl font-bold text-white tracking-tight">
            ₫{totalPrice.toLocaleString()}
          </Text>
        </View>
        <TouchableOpacity
          onPress={handleCheckout}
          className="w-full bg-[#FF8C42] py-4 rounded-2xl items-center shadow shadow-orange-500/20"
        >
          <Text className="text-black font-bold text-lg">Thanh toán</Text>
        </TouchableOpacity>
      </View>
    </View>
  );
}
