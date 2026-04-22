import { useAuth } from "@/frontend/context/AuthContext";
import {
    assetApi,
    Category,
    productApi
} from "@/services/api/productApi";
import { Link, useRouter } from "expo-router";
import { Heart, Search, ShoppingCart } from "lucide-react-native";
import { useEffect, useMemo, useState } from "react";
import {
    ActivityIndicator,
    Image,
    ScrollView,
    Text,
    TextInput,
    TouchableOpacity,
    View,
} from "react-native";

interface DisplayItem {
  id: string;
  title: string;
  price: number;
  type: "PRODUCT" | "ASSET";
  categoryName: string;
  primaryImageUrl: string;
}

export default function DiscoveryScreen() {
  const router = useRouter();
  const { user, favorites, toggleFavorite, cartItems, loadFavorites } =
    useAuth();

  const cartItemCount = cartItems.reduce((sum, item) => sum + item.quantity, 0);

  const [searchQuery, setSearchQuery] = useState("");
  const [selectedCategory, setSelectedCategory] = useState<string | null>(null);
  const [shopMode, setShopMode] = useState<"BUY" | "RENT">("BUY");

  // State
  const [allItems, setAllItems] = useState<DisplayItem[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Load data
  useEffect(() => {
    loadData();
  }, [shopMode]);

  const loadData = async () => {
    try {
      setLoading(true);
      setError(null);

      // Load categories - map shopMode to backend type
      const backendType = shopMode === "BUY" ? "PRODUCT" : "ASSET";
      const cats = await productApi.getCategoriesByType(backendType);
      setCategories(cats);

      // Load products or assets
      if (shopMode === "BUY") {
        const productsData = await productApi.getAllProducts(0, 100);
        const items: DisplayItem[] = productsData.content.map((p) => ({
          id: p.productId,
          title: p.productName,
          price: p.price,
          type: "PRODUCT" as const,
          categoryName: p.categoryName,
          primaryImageUrl: p.primaryImageUrl,
        }));
        setAllItems(items);
      } else {
        const assetsData = await assetApi.getAllAssets(0, 100);
        const items: DisplayItem[] = assetsData.content.map((a) => ({
          id: a.assetId,
          title: a.modelName,
          price: a.dailyRate,
          type: "ASSET" as const,
          categoryName: a.categoryName,
          primaryImageUrl: a.primaryImageUrl,
        }));
        setAllItems(items);
      }
    } catch (e: any) {
      setError(e.message || "Failed to load data");
    } finally {
      setLoading(false);
    }
  };

  const filteredItems = useMemo(() => {
    return allItems.filter((item) => {
      const matchesSearch = item.title
        .toLowerCase()
        .includes(searchQuery.toLowerCase());
      const matchesCategory = selectedCategory
        ? item.categoryName === selectedCategory
        : true;
      return matchesSearch && matchesCategory;
    });
  }, [searchQuery, selectedCategory, allItems]);

  const handleToggleFavorite = async (id: string) => {
    if (!user) {
      router.push("/(auth)/login" as any);
      return;
    }
    await toggleFavorite(id, shopMode === "BUY" ? "PRODUCT" : "ASSET");
  };

  if (loading) {
    return (
      <View className="flex-1 bg-[#1a1a1a] items-center justify-center">
        <ActivityIndicator size="large" color="#FF8C42" />
        <Text className="text-gray-400 mt-4">Loading...</Text>
      </View>
    );
  }

  if (error) {
    return (
      <View className="flex-1 bg-[#1a1a1a] items-center justify-center p-6">
        <Text className="text-red-500 mb-4">{error}</Text>
        <TouchableOpacity
          onPress={loadData}
          className="bg-[#FF8C42] px-6 py-3 rounded-full"
        >
          <Text className="text-black font-bold">Retry</Text>
        </TouchableOpacity>
      </View>
    );
  }

  return (
    <ScrollView className="flex-1 bg-[#1a1a1a]">
      {/* Header */}
      <View className="px-6 pt-6 pb-4">
        <View className="flex-row items-center justify-between mb-6">
          <View>
            <Text className="text-3xl mb-1 text-white font-bold tracking-tight">
              DISCOVERY
            </Text>
            <Text className="text-sm text-gray-400">
              Welcome, {user ? user.userName : "Guest"}!
            </Text>
          </View>
          {user ? (
            <TouchableOpacity
              onPress={() => router.push("/cart" as any)}
              className="w-10 h-10 bg-[#0a0a0a] rounded-full flex items-center justify-center border border-gray-800 relative"
            >
              <ShoppingCart size={20} color="#9ca3af" />
              {cartItemCount > 0 && (
                <View className="absolute -top-1 -right-1 bg-red-500 w-4 h-4 rounded-full items-center justify-center">
                  <Text className="text-[10px] text-white font-bold">
                    {cartItemCount > 99 ? "99+" : cartItemCount}
                  </Text>
                </View>
              )}
            </TouchableOpacity>
          ) : (
            <Link href={"/(auth)/login" as any} asChild>
              <TouchableOpacity className="px-4 py-2 bg-[#FF8C42] rounded-full">
                <Text className="text-black font-semibold text-xs">
                  Sign In
                </Text>
              </TouchableOpacity>
            </Link>
          )}
        </View>

        {/* Search Bar */}
        <View className="relative">
          <View className="absolute left-4 top-4 z-10">
            <Search size={16} color="#6b7280" />
          </View>
          <TextInput
            placeholder="Search equipment..."
            placeholderTextColor="#4b5563"
            value={searchQuery}
            onChangeText={setSearchQuery}
            className="pl-11 pr-4 py-4 bg-[#0a0a0a] border border-gray-800 rounded-2xl text-white"
          />
        </View>

        {/* Buy/Rent Toggle */}
        <View className="flex-row mt-6 bg-[#0a0a0a] border border-gray-800 p-1 rounded-2xl">
          <TouchableOpacity
            onPress={() => {
              setShopMode("BUY");
              setSelectedCategory(null);
            }}
            className={`flex-1 py-3 rounded-xl items-center ${shopMode === "BUY" ? "bg-[#FF8C42]" : ""}`}
          >
            <Text
              className={`font-semibold ${shopMode === "BUY" ? "text-black" : "text-gray-500"}`}
            >
              Mua thiết bị
            </Text>
          </TouchableOpacity>
          <TouchableOpacity
            onPress={() => {
              setShopMode("RENT");
              setSelectedCategory(null);
            }}
            className={`flex-1 py-3 rounded-xl items-center ${shopMode === "RENT" ? "bg-[#FF8C42]" : ""}`}
          >
            <Text
              className={`font-semibold ${shopMode === "RENT" ? "text-black" : "text-gray-500"}`}
            >
              Thuê thiết bị
            </Text>
          </TouchableOpacity>
        </View>
      </View>

      {/* Categories Filter */}
      <View className="px-6 mb-8">
        <ScrollView
          horizontal
          showsHorizontalScrollIndicator={false}
          className="flex-row gap-3 overflow-visible pb-2"
        >
          <TouchableOpacity
            onPress={() => setSelectedCategory(null)}
            className={`px-5 py-3 mr-3 rounded-full flex-row ${selectedCategory === null ? "bg-[#FF8C42]" : "bg-[#0a0a0a] border border-gray-800"}`}
          >
            <Text
              className={`text-sm ${selectedCategory === null ? "text-black font-bold" : "text-white"}`}
            >
              All
            </Text>
          </TouchableOpacity>

          {categories.map((category) => (
            <TouchableOpacity
              key={category.categoryId}
              onPress={() => setSelectedCategory(category.categoryName)}
              className={`px-5 py-3 mr-3 rounded-full flex-row ${selectedCategory === category.categoryName ? "bg-[#FF8C42]" : "bg-[#0a0a0a] border border-gray-800"}`}
            >
              <Text
                className={`text-sm ${selectedCategory === category.categoryName ? "text-black font-bold" : "text-white"}`}
              >
                {category.categoryName}
              </Text>
            </TouchableOpacity>
          ))}
        </ScrollView>
      </View>

      {/* Banner */}
      <View className="px-6 mb-8">
        <View className="bg-[#FF8C42] rounded-3xl p-8 relative overflow-hidden">
          <View className="relative z-10">
            <Text className="text-xs tracking-widest mb-2 opacity-90 font-bold text-black">
              SPECIAL OFFER
            </Text>
            <Text className="text-2xl mb-2 tracking-tight text-white font-bold">
              Summer Sale
            </Text>
            <Text className="text-sm opacity-90 mb-4 text-white">
              Up to 30% off on selected items
            </Text>
            <TouchableOpacity
              onPress={() => router.push("/store" as any)}
              className="bg-black px-6 py-3 rounded-full self-start"
            >
              <Text className="text-white text-sm font-bold">Shop Now</Text>
            </TouchableOpacity>
          </View>
        </View>
      </View>

      {/* Featured Section */}
      <View className="px-6 pb-8">
        <View className="flex-row items-baseline justify-between mb-6">
          <Text className="text-xl tracking-tight text-white font-bold">
            {searchQuery || selectedCategory ? "Results" : "Featured"}
          </Text>
        </View>

        <View className="space-y-4">
          {filteredItems.map((item) => {
            const isFaved = favorites.includes(item.id);
            return (
              <TouchableOpacity
                key={item.id}
                onPress={() => router.push(`/equipment/${item.id}` as any)}
                className="block bg-[#0a0a0a] rounded-3xl overflow-hidden border border-gray-800 mb-4"
              >
                <View className="relative aspect-[4/3] w-full bg-gray-900">
                  <Image
                    source={{ uri: item.primaryImageUrl }}
                    className="w-full h-full"
                    resizeMode="cover"
                  />
                  <View className="absolute top-4 right-4 bg-white/20 px-3 py-1 rounded-full backdrop-blur-sm">
                    <Text className="text-white text-xs font-medium">
                      {item.type}
                    </Text>
                  </View>
                  <TouchableOpacity
                    onPress={(e) => {
                      e.stopPropagation();
                      if (!user) {
                        router.push("/(auth)/login" as any);
                        return;
                      }
                      handleToggleFavorite(item.id);
                    }}
                    className={`absolute top-4 left-4 w-9 h-9 rounded-full flex items-center justify-center border ${isFaved ? "bg-[#FF8C42] border-[#FF8C42]" : "bg-black/40 border-white/20"}`}
                  >
                    <Heart
                      size={16}
                      color={isFaved ? "black" : "white"}
                      fill={isFaved ? "black" : "none"}
                    />
                  </TouchableOpacity>
                </View>
                <View className="p-5">
                  <Text className="text-xs text-gray-500 mb-1 tracking-wide uppercase">
                    {item.categoryName}
                  </Text>
                  <Text className="text-xl mb-3 tracking-tight text-white font-bold">
                    {item.title}
                  </Text>
                  <View className="flex-row items-baseline gap-1">
                    <Text className="text-2xl text-[#FF8C42] font-semibold">
                      ₫{item.price.toLocaleString()}
                    </Text>
                    {item.type === "ASSET" && (
                      <Text className="text-sm text-gray-500">/day</Text>
                    )}
                  </View>
                </View>
              </TouchableOpacity>
            );
          })}
        </View>

        {filteredItems.length === 0 && (
          <View className="items-center py-12">
            <Text className="text-gray-500">No items found</Text>
          </View>
        )}
      </View>

      {/* Rental Section */}
      <View className="px-6 pb-8">
        <View className="bg-[#0a0a0a] border border-gray-800 rounded-3xl p-6">
          <View className="flex-row items-center justify-between mb-4">
            <View>
              <Text className="text-lg mb-1 tracking-tight text-white font-bold">
                Rental Service
              </Text>
              <Text className="text-xs text-gray-400">Try before you buy</Text>
            </View>
            <Text className="text-3xl">📷</Text>
          </View>
          <Text className="text-sm text-gray-400 mb-4">
            Rent professional equipment starting from ₫400,000/day
          </Text>
          <TouchableOpacity
            onPress={() => router.push("/rentals" as any)}
            className="w-full py-4 bg-white rounded-full items-center justify-center"
          >
            <Text className="text-black text-sm font-bold">Browse Rentals</Text>
          </TouchableOpacity>
        </View>
      </View>
    </ScrollView>
  );
}
