import { useAuth } from "@/frontend/context/AuthContext";
import { useRouter } from "expo-router";
import { ArrowLeft, Package, Trash2 } from "lucide-react-native";
import { useEffect, useState } from "react";
import {
    ActivityIndicator,
    Image,
    ScrollView,
    Text,
    TouchableOpacity,
    View,
} from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";

export default function FavoritesScreen() {
  const router = useRouter();
  const insets = useSafeAreaInsets();
  const { favoriteItems, toggleFavorite, user, loadFavorites } = useAuth();
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadFavoritesData();
  }, []);

  const loadFavoritesData = async () => {
    try {
      setLoading(true);
      await loadFavorites();
    } catch (e) {
      console.error("Error loading favorites:", e);
    } finally {
      setLoading(false);
    }
  };

  const handleRemoveFavorite = async (
    id: string,
    type: "PRODUCT" | "ASSET",
  ) => {
    await toggleFavorite(id, type);
  };

  if (!user) {
    return (
      <View className="flex-1 bg-[#1a1a1a] items-center justify-center p-6">
        <Text className="text-white text-lg mb-4">
          Vui lòng đăng nhập để xem yêu thích
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
        <Text className="text-xl text-white font-bold flex-1">Yêu thích</Text>
      </View>

      <ScrollView className="p-6">
        {favoriteItems.length === 0 ? (
          <View className="items-center py-12">
            <Text className="text-gray-400 text-center">
              Chưa có sản phẩm yêu thích
            </Text>
          </View>
        ) : (
          favoriteItems.map((item) => (
            <TouchableOpacity
              key={item.favoriteId}
              onPress={() => router.push(`/equipment/${item.id}` as any)}
              className="mb-4 bg-[#0a0a0a] rounded-2xl border border-gray-800 flex-row items-center p-3"
            >
              {item.primaryImageUrl ? (
                <Image
                  source={{ uri: item.primaryImageUrl }}
                  className="w-20 h-20 rounded-xl bg-gray-900"
                  resizeMode="cover"
                />
              ) : (
                <View className="w-20 h-20 rounded-xl bg-gray-900 items-center justify-center">
                  <Package color="#4b5563" size={24} />
                </View>
              )}
              <View className="flex-1 ml-4 justify-center">
                <Text className="text-xs text-[#FF8C42] mb-1">
                  {item.type === "PRODUCT" ? "Sản phẩm" : "Thiết bị thuê"}
                </Text>
                <Text className="text-white font-bold mb-1" numberOfLines={1}>
                  {item.productName || item.assetName}
                </Text>
                {item.price ? (
                  <Text className="text-[#FF8C42] text-sm">
                    ₫{item.price.toLocaleString()}
                    {item.type === "ASSET" ? "/ngày" : ""}
                  </Text>
                ) : null}
              </View>
              <TouchableOpacity
                onPress={() => handleRemoveFavorite(item.id, item.type)}
                className="p-3"
              >
                <Trash2 color="#ef4444" size={20} />
              </TouchableOpacity>
            </TouchableOpacity>
          ))
        )}
      </ScrollView>
    </View>
  );
}
