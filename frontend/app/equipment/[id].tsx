<<<<<<< HEAD
import { useAuth } from '@/context/AuthContext';
=======
import { useAuth } from "@/frontend/context/AuthContext";
>>>>>>> LTH
import {
    Asset,
    assetApi,
    Product,
    productApi,
} from "@/services/api/productApi";
import DateTimePicker from "@react-native-community/datetimepicker";
import { useLocalSearchParams, useRouter } from "expo-router";
import {
    ArrowLeft,
    Calendar,
    Check,
    Heart,
    Minus,
    Plus,
    Share2,
    Shield,
    ShoppingCart,
    Star
} from "lucide-react-native";
import { useEffect, useState } from "react";
import {
    ActivityIndicator,
    Alert,
    Image,
    Modal,
    Platform,
    ScrollView,
    Text,
    TouchableOpacity,
    View,
} from "react-native";

export default function EquipmentDetailScreen() {
  const router = useRouter();
  const { id } = useLocalSearchParams();
  const { user, toggleFavorite, addToCart } = useAuth();

  const stringId = typeof id === "string" ? id : Array.isArray(id) ? id[0] : "";

  const [equipment, setEquipment] = useState<
    (Product & { type: "PRODUCT" }) | (Asset & { type: "ASSET" }) | null
  >(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [quantity, setQuantity] = useState(1);
  const [isFavorite, setIsFavorite] = useState(false);

  // Rental date states
  const [startDate, setStartDate] = useState<Date>(new Date());
  const [endDate, setEndDate] = useState<Date>(
    new Date(Date.now() + 24 * 60 * 60 * 1000),
  );
  const [showStartPicker, setShowStartPicker] = useState(false);
  const [showEndPicker, setShowEndPicker] = useState(false);
  const [pickerMode, setPickerMode] = useState<"start" | "end" | null>(null);
  const [tempDate, setTempDate] = useState<Date>(new Date());

  useEffect(() => {
    loadEquipment();
  }, [stringId]);

  const loadEquipment = async () => {
    try {
      setLoading(true);
      setError(null);

      try {
        const product = await productApi.getProductById(stringId);
        setEquipment({ ...product, type: "PRODUCT" });
        return;
      } catch (e) {
        // Not a product, try asset
      }

      const asset = await assetApi.getAssetById(stringId);
      setEquipment({ ...asset, type: "ASSET" });
    } catch (e: any) {
      setError(e.message || "Failed to load equipment");
    } finally {
      setLoading(false);
    }
  };

  const handleToggleFavorite = async () => {
    if (!user || !equipment) {
      router.push("/(auth)/login" as any);
      return;
    }
    await toggleFavorite(
      equipment.type === "PRODUCT" ? equipment.productId : equipment.assetId,
      equipment.type,
    );
    setIsFavorite(!isFavorite);
  };

  const calculateRentalDays = () => {
    const diffTime = Math.abs(endDate.getTime() - startDate.getTime());
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    return Math.max(1, diffDays);
  };

  const calculateRentalTotal = () => {
    if (!equipment || equipment.type !== "ASSET") return 0;
    const days = calculateRentalDays();
    return (equipment as Asset).dailyRate * days;
  };

  const handleAddToCart = async () => {
    if (!user || !equipment) {
      router.push("/(auth)/login" as any);
      return;
    }

    try {
      const itemId =
        equipment.type === "PRODUCT" ? equipment.productId : equipment.assetId;
      await addToCart(
        itemId,
        equipment.type,
        equipment.type === "PRODUCT" ? quantity : 1,
      );
      Alert.alert(
        "Success",
        `${equipment.type === "PRODUCT" ? equipment.productName : equipment.modelName} has been added to your cart.`,
      );
    } catch (e: any) {
      Alert.alert("Error", e.message || "Failed to add to cart");
    }
  };

  const handleBuyNow = async () => {
    if (!user || !equipment || equipment.type !== "PRODUCT") {
      router.push("/(auth)/login" as any);
      return;
    }

    const checkoutData = {
      type: "BUY_NOW",
      items: [
        {
          id: equipment.productId,
          type: "PRODUCT" as const,
          name: equipment.productName,
          price: equipment.price,
          quantity: quantity,
          imageUrl: equipment.primaryImageUrl,
        },
      ],
      total: equipment.price * quantity,
    };

    router.push({
      pathname: "/checkout" as any,
      params: { data: JSON.stringify(checkoutData) },
    });
  };

  const handleRentNow = async () => {
    if (!user || !equipment || equipment.type !== "ASSET") {
      router.push("/(auth)/login" as any);
      return;
    }

    if (startDate >= endDate) {
      Alert.alert("Invalid Dates", "Return date must be after start date");
      return;
    }

    const today = new Date();
    today.setHours(0, 0, 0, 0);
    if (startDate < today) {
      Alert.alert("Invalid Date", "Start date cannot be in the past");
      return;
    }

    const rentalDays = calculateRentalDays();
    const totalAmount = calculateRentalTotal();

    const checkoutData = {
      type: "RENT_NOW",
      items: [
        {
          id: equipment.assetId,
          type: "ASSET" as const,
          name: equipment.modelName,
          price: (equipment as Asset).dailyRate,
          quantity: 1,
          imageUrl: equipment.primaryImageUrl,
          startDate: startDate.toISOString(),
          endDate: endDate.toISOString(),
          rentalDays: rentalDays,
        },
      ],
      total: totalAmount,
      rentalInfo: {
        startDate: startDate.toISOString(),
        endDate: endDate.toISOString(),
        days: rentalDays,
      },
    };

    router.push({
      pathname: "/checkout" as any,
      params: { data: JSON.stringify(checkoutData) },
    });
  };

  const openDatePicker = (mode: "start" | "end") => {
    setPickerMode(mode);
    setTempDate(mode === "start" ? startDate : endDate);
    setShowStartPicker(mode === "start");
    setShowEndPicker(mode === "end");
  };

  const closeDatePicker = () => {
    setShowStartPicker(false);
    setShowEndPicker(false);
    setPickerMode(null);
  };

  const confirmDateSelection = () => {
    if (!pickerMode) return;

    const minDate = new Date();
    minDate.setHours(0, 0, 0, 0);

    if (pickerMode === "start") {
      if (tempDate < minDate) {
        Alert.alert("Invalid Date", "Cannot select a date in the past");
        return;
      }
      setStartDate(tempDate);
      if (tempDate >= endDate) {
        const newEndDate = new Date(tempDate);
        newEndDate.setDate(newEndDate.getDate() + 1);
        setEndDate(newEndDate);
      }
    } else {
      if (tempDate <= startDate) {
        Alert.alert("Invalid Date", "Return date must be after start date");
        return;
      }
      setEndDate(tempDate);
    }
    closeDatePicker();
  };

  const onStartDateChange = (event: any, selectedDate?: Date) => {
    if (Platform.OS === "android") {
      setShowStartPicker(false);
      if (selectedDate) {
        const minDate = new Date();
        minDate.setHours(0, 0, 0, 0);
        if (selectedDate < minDate) {
          Alert.alert("Invalid Date", "Cannot select a date in the past");
          return;
        }
        setStartDate(selectedDate);
        if (selectedDate >= endDate) {
          const newEndDate = new Date(selectedDate);
          newEndDate.setDate(newEndDate.getDate() + 1);
          setEndDate(newEndDate);
        }
      }
    } else {
      if (selectedDate) setTempDate(selectedDate);
    }
  };

  const onEndDateChange = (event: any, selectedDate?: Date) => {
    if (Platform.OS === "android") {
      setShowEndPicker(false);
      if (selectedDate) {
        if (selectedDate <= startDate) {
          Alert.alert("Invalid Date", "Return date must be after start date");
          return;
        }
        setEndDate(selectedDate);
      }
    } else {
      if (selectedDate) setTempDate(selectedDate);
    }
  };

  const formatDate = (date: Date) => {
    return date.toLocaleDateString("vi-VN", {
      weekday: "short",
      day: "numeric",
      month: "short",
      year: "numeric",
    });
  };

  if (loading) {
    return (
      <View className="flex-1 bg-[#1a1a1a] items-center justify-center">
        <ActivityIndicator size="large" color="#FF8C42" />
      </View>
    );
  }

  if (error || !equipment) {
    return (
      <View className="flex-1 bg-[#1a1a1a] items-center justify-center">
        <Text className="text-white mb-4">
          {error || "Equipment not found"}
        </Text>
        <TouchableOpacity
          onPress={() => router.back()}
          className="px-4 py-2 bg-[#FF8C42] rounded-full"
        >
          <Text className="text-black font-bold">Go Back</Text>
        </TouchableOpacity>
      </View>
    );
  }

  const isProduct = equipment.type === "PRODUCT";
  const title = isProduct ? equipment.productName : equipment.modelName;
  const price = isProduct ? equipment.price : equipment.dailyRate;
  const status = (equipment as Asset).status;

  // Mock specifications data
  const specifications = isProduct
    ? [
        { label: "Sensor", value: "50MP Full-Frame Exmor RS" },
        { label: "ISO Range", value: "100-32000" },
        { label: "Video", value: "8K 30fps / 4K 120fps" },
        { label: "Storage", value: "Dual CFexpress Type A" },
      ]
    : [];

  const whatsIncluded = [
    "Camera Body",
    "Battery",
    "Charger",
    "Strap",
    "Manual",
  ];

  return (
    <View className="flex-1 bg-[#1a1a1a]">
      <ScrollView
        className="flex-1"
        contentContainerStyle={{ paddingBottom: 120 }}
      >
        {/* Image Header */}
        <View className="relative h-[400px] bg-black">
          <Image
            source={{ uri: equipment.primaryImageUrl }}
            className="w-full h-full"
            resizeMode="cover"
          />

          {/* Top Actions */}
          <View className="absolute top-12 left-0 right-0 px-4 flex-row justify-between">
            <TouchableOpacity
              onPress={() => router.back()}
              className="w-11 h-11 bg-black/60 rounded-full flex items-center justify-center border border-white/10"
            >
              <ArrowLeft size={20} color="white" />
            </TouchableOpacity>
            <View className="flex-row gap-2">
              <TouchableOpacity className="w-11 h-11 bg-black/60 rounded-full flex items-center justify-center border border-white/10">
                <Share2 size={20} color="white" />
              </TouchableOpacity>
              <TouchableOpacity
                onPress={handleToggleFavorite}
                className={`w-11 h-11 rounded-full flex items-center justify-center border ${
                  isFavorite
                    ? "bg-[#FF8C42] border-[#FF8C42]"
                    : "bg-black/60 border-white/20"
                }`}
              >
                <Heart
                  size={20}
                  color={isFavorite ? "black" : "white"}
                  fill={isFavorite ? "black" : "none"}
                />
              </TouchableOpacity>
            </View>
          </View>

          {/* Badge */}
          <View className="absolute bottom-4 left-4">
            <View className="bg-[#FF8C42] px-4 py-2 rounded-full">
              <Text className="text-black text-xs font-bold uppercase">
                Brand New
              </Text>
            </View>
          </View>
        </View>

        {/* Content */}
        <View className="px-6 py-6">
          {/* Category */}
          <Text className="text-xs text-gray-500 mb-2 uppercase tracking-wider font-semibold">
            {equipment.brand}
          </Text>

          {/* Title & Price */}
          <Text className="text-3xl mb-3 tracking-tight text-white font-bold">
            {title}
          </Text>
          <View className="flex-row items-baseline gap-2 mb-4">
            <Text className="text-3xl text-[#FF8C42] font-semibold">
              ₫{price.toLocaleString()}
            </Text>
            {!isProduct && <Text className="text-gray-400 text-sm">/day</Text>}
          </View>

          {/* Rating */}
          <View className="flex-row items-center gap-4 mb-6">
            <View className="flex-row items-center">
              <Star size={16} color="#FF8C42" fill="#FF8C42" />
              <Text className="text-sm text-gray-400 ml-1">4.9 (234)</Text>
            </View>
            <Text className="text-sm text-gray-500">Sold: 67</Text>
          </View>

          {/* Description */}
          <View className="bg-[#0a0a0a] border border-gray-800 rounded-2xl p-5 mb-4">
            <Text className="text-xs text-gray-500 uppercase tracking-wider mb-3 font-bold">
              Description
            </Text>
            <Text className="text-sm text-gray-300 leading-relaxed">
              {equipment.description}
            </Text>
          </View>

          {/* Specifications */}
          {specifications.length > 0 && (
            <View className="bg-[#0a0a0a] border border-gray-800 rounded-2xl p-5 mb-4">
              <Text className="text-xs text-gray-500 uppercase tracking-wider mb-4 font-bold">
                Specifications
              </Text>
              <View className="space-y-3">
                {specifications.map((spec, index) => (
                  <View
                    key={index}
                    className="flex-row justify-between items-center border-b border-gray-800 pb-3 last:border-0 last:pb-0"
                  >
                    <Text className="text-gray-400 text-sm">{spec.label}</Text>
                    <Text className="text-white text-sm">{spec.value}</Text>
                  </View>
                ))}
              </View>
            </View>
          )}

          {/* What's Included */}
          <View className="bg-[#0a0a0a] border border-gray-800 rounded-2xl p-5 mb-4">
            <Text className="text-xs text-gray-500 uppercase tracking-wider mb-4 font-bold">
              What's Included
            </Text>
            <View className="space-y-2">
              {whatsIncluded.map((item, index) => (
                <View key={index} className="flex-row items-center">
                  <Check size={16} color="#FF8C42" />
                  <Text className="text-white text-sm ml-2">{item}</Text>
                </View>
              ))}
            </View>
          </View>

          {/* Rental Section */}
          {!isProduct && (
            <View className="bg-gradient-to-r from-blue-900/30 to-purple-900/30 border border-blue-500/20 rounded-2xl p-5 mb-4">
              <Text className="text-sm text-gray-400 mb-2">
                Also Available for Rent
              </Text>
              <Text className="text-2xl text-[#FF8C42] font-semibold mb-3">
                ₫{(equipment as Asset).dailyRate.toLocaleString()}{" "}
                <Text className="text-sm text-gray-400">/day</Text>
              </Text>
              <TouchableOpacity className="w-full py-3 bg-[#1a3a5c] rounded-xl items-center">
                <Text className="text-white font-semibold">
                  View Rental Details
                </Text>
              </TouchableOpacity>
            </View>
          )}

          {/* Buyer Protection */}
          <View className="bg-[#FF8C42]/10 border border-[#FF8C42]/20 rounded-2xl p-5 mb-4">
            <View className="flex-row items-start gap-3">
              <Shield size={28} color="#FF8C42" className="mt-0.5" />
              <View className="flex-1">
                <Text className="text-sm text-white font-semibold mb-1">
                  Buyer Protection
                </Text>
                <Text className="text-xs text-gray-400 leading-relaxed">
                  100% money-back guarantee if product doesn't match description
                </Text>
              </View>
            </View>
          </View>
        </View>
      </ScrollView>

      {/* Bottom Actions */}
      <View className="absolute bottom-0 left-0 right-0 bg-[#0a0a0a] border-t border-gray-800 px-6 pt-4 pb-8">
        {isProduct ? (
          <>
            <View className="flex-row items-center justify-between mb-4">
              <Text className="text-white font-semibold">Quantity</Text>
              <View className="flex-row items-center bg-[#1a1a1a] rounded-full border border-gray-800">
                <TouchableOpacity
                  className="w-10 h-10 items-center justify-center"
                  onPress={() => setQuantity((prev) => Math.max(1, prev - 1))}
                >
                  <Minus size={16} color="white" />
                </TouchableOpacity>
                <Text className="text-white font-semibold px-4 min-w-[40px] text-center">
                  {quantity}
                </Text>
                <TouchableOpacity
                  className="w-10 h-10 items-center justify-center"
                  onPress={() => setQuantity((prev) => prev + 1)}
                >
                  <Plus size={16} color="white" />
                </TouchableOpacity>
              </View>
            </View>

            <View className="flex-row gap-3">
              <TouchableOpacity
                onPress={handleAddToCart}
                className="w-14 h-14 bg-[#1a1a1a] border border-gray-800 rounded-2xl items-center justify-center"
              >
                <ShoppingCart size={24} color="white" />
              </TouchableOpacity>
              <TouchableOpacity
                onPress={handleBuyNow}
                className="flex-1 h-14 bg-[#FF8C42] rounded-2xl items-center justify-center"
              >
                <Text className="text-black font-bold text-lg">Mua ngay</Text>
              </TouchableOpacity>
            </View>
          </>
        ) : (
          <>
            {/* Date Pickers */}
            <View className="flex-row gap-3 mb-4">
              <View className="flex-1">
                <Text className="text-xs text-gray-400 mb-2">Start Date</Text>
                <TouchableOpacity
                  onPress={() => openDatePicker("start")}
                  className="flex-row items-center bg-[#1a1a1a] border border-gray-800 rounded-2xl px-4 py-3"
                >
                  <Calendar size={16} color="#FF8C42" />
                  <Text className="text-white ml-2 flex-1">
                    {formatDate(startDate)}
                  </Text>
                </TouchableOpacity>
              </View>

              <View className="flex-1">
                <Text className="text-xs text-gray-400 mb-2">Return Date</Text>
                <TouchableOpacity
                  onPress={() => openDatePicker("end")}
                  className="flex-row items-center bg-[#1a1a1a] border border-gray-800 rounded-2xl px-4 py-3"
                >
                  <Calendar size={16} color="#FF8C42" />
                  <Text className="text-white ml-2 flex-1">
                    {formatDate(endDate)}
                  </Text>
                </TouchableOpacity>
              </View>
            </View>

            {/* Rental Summary */}
            <View className="bg-[#1a1a1a] border border-gray-800 rounded-2xl p-4 mb-4">
              <View className="flex-row justify-between items-center mb-2">
                <Text className="text-gray-400 text-sm">Rental Period</Text>
                <Text className="text-white font-semibold">
                  {calculateRentalDays()} day(s)
                </Text>
              </View>
              <View className="flex-row justify-between items-center mb-2">
                <Text className="text-gray-400 text-sm">Daily Rate</Text>
                <Text className="text-white">₫{price.toLocaleString()}</Text>
              </View>
              <View className="border-t border-gray-800 mt-2 pt-3 flex-row justify-between items-center">
                <Text className="text-white font-bold">Total</Text>
                <Text className="text-[#FF8C42] font-bold text-xl">
                  ₫{calculateRentalTotal().toLocaleString()}
                </Text>
              </View>
            </View>

            {/* Rent Button */}
            <TouchableOpacity
              onPress={handleRentNow}
              disabled={status !== "AVAILABLE"}
              className={`w-full h-14 rounded-2xl items-center justify-center ${
                status !== "AVAILABLE" ? "bg-gray-600" : "bg-[#FF8C42]"
              }`}
            >
              <Text
                className={`font-bold text-lg ${status !== "AVAILABLE" ? "text-gray-400" : "text-black"}`}
              >
                {status !== "AVAILABLE" ? "Unavailable" : "Rent Now"}
              </Text>
            </TouchableOpacity>
          </>
        )}
      </View>

      {/* Date Picker Modal */}
      <Modal
        visible={pickerMode !== null}
        transparent
        animationType="fade"
        onRequestClose={closeDatePicker}
      >
        <View className="flex-1 justify-end bg-black/60">
          <View className="bg-[#1a1a1a] rounded-t-3xl border-t border-gray-800 pb-8">
            <View className="w-full items-center py-4">
              <View className="w-12 h-1 bg-gray-700 rounded-full" />
            </View>

            <View className="flex-row items-center justify-between px-6 pb-4 border-b border-gray-800">
              <Text className="text-lg text-white font-semibold">
                {pickerMode === "start" ? "Chọn ngày bắt đầu" : "Chọn ngày trả"}
              </Text>
              <TouchableOpacity onPress={closeDatePicker} className="p-2">
                <Text className="text-gray-400 text-2xl">×</Text>
              </TouchableOpacity>
            </View>

            <View className="px-6 py-4 items-center bg-[#0a0a0a] mx-6 my-4 rounded-2xl border border-gray-800">
              <Text className="text-xs text-gray-400 uppercase tracking-wider mb-2">
                {pickerMode === "start" ? "Start Date" : "Return Date"}
              </Text>
              <Text className="text-2xl text-white font-bold">
                {formatDatePickerDate(tempDate)}
              </Text>
            </View>

            <View className="items-center py-4">
              <DateTimePicker
                value={tempDate}
                mode="date"
                display="spinner"
                onChange={onStartDateChange}
                textColor="white"
                themeVariant="dark"
                minimumDate={
                  pickerMode === "start"
                    ? new Date()
                    : new Date(startDate.getTime() + 24 * 60 * 60 * 1000)
                }
              />
            </View>

            <TouchableOpacity
              onPress={confirmDateSelection}
              className="mx-6 mt-4 py-4 bg-[#FF8C42] rounded-2xl items-center"
            >
              <Text className="text-black font-bold text-base">Xác nhận</Text>
            </TouchableOpacity>
          </View>
        </View>
      </Modal>
    </View>
  );
}

const formatDatePickerDate = (date: Date) => {
  return date.toLocaleDateString("vi-VN", {
    weekday: "long",
    day: "numeric",
    month: "long",
    year: "numeric",
  });
};
