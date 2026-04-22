import { useAuth } from "@/frontend/context/AuthContext";
import { Product, productApi } from "@/services/api/productApi";
import { useRouter } from "expo-router";
import { ArrowLeft, ShoppingCart } from "lucide-react-native";
import { useEffect, useState } from "react";
import {
    ActivityIndicator,
    Image,
    ScrollView,
    Text,
    TouchableOpacity,
    View,
} from "react-native";

export default function StoreScreen() {
  const router = useRouter();
  const { cartItems, user } = useAuth();

  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const cartItemCount = cartItems.reduce((sum, item) => sum + item.quantity, 0);

  useEffect(() => {
    loadProducts();
  }, []);

  const loadProducts = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await productApi.getAllProducts(0, 100);
      setProducts(data.content);
    } catch (e: any) {
      setError(e.message || "Failed to load products");
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <View className="flex-1 bg-[#1a1a1a] items-center justify-center">
        <ActivityIndicator size="large" color="#FF8C42" />
      </View>
    );
  }

  if (error) {
    return (
      <View className="flex-1 bg-[#1a1a1a] items-center justify-center p-6">
        <Text className="text-red-500 mb-4">{error}</Text>
        <TouchableOpacity
          onPress={loadProducts}
          className="bg-[#FF8C42] px-6 py-3 rounded-full"
        >
          <Text className="text-black font-bold">Retry</Text>
        </TouchableOpacity>
      </View>
    );
  }

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
          Shop Equipment
        </Text>
        <TouchableOpacity
          onPress={() => {
            if (!user) {
              router.push("/(auth)/login" as any);
            } else {
              router.push("/cart" as any);
            }
          }}
          className="relative"
        >
          <ShoppingCart color="white" size={24} />
          {cartItemCount > 0 && user && (
            <View className="absolute -top-2 -right-2 bg-red-500 w-4 h-4 rounded-full items-center justify-center">
              <Text className="text-[10px] text-white font-bold">
                {cartItemCount}
              </Text>
            </View>
          )}
        </TouchableOpacity>
      </View>

      <ScrollView className="p-6">
        <View className="flex-row flex-wrap justify-between">
          {products.map((product) => {
            return (
              <TouchableOpacity
                key={product.productId}
                onPress={() =>
                  router.push(`/equipment/${product.productId}` as any)
                }
                className="w-[48%] mb-6 bg-[#0a0a0a] rounded-2xl border border-gray-800 overflow-hidden"
              >
                <Image
                  source={{ uri: product.primaryImageUrl }}
                  className="w-full h-32"
                  resizeMode="cover"
                />
                <View className="p-3">
                  <Text className="text-[10px] text-gray-500 uppercase mb-1">
                    {product.categoryName}
                  </Text>
                  <Text className="text-white font-bold mb-2" numberOfLines={1}>
                    {product.productName}
                  </Text>
                  <Text className="text-[#FF8C42] font-semibold">
                    ₫{product.price.toLocaleString()}
                  </Text>
                  <Text className="text-xs text-gray-400 mt-1">
                    Stock: {product.stockQuantity}
                  </Text>
                </View>
              </TouchableOpacity>
            );
          })}
        </View>
      </ScrollView>
    </View>
  );
}
