import React from 'react';
import { View, Text, ScrollView, TouchableOpacity, Image } from 'react-native';
import { ArrowLeft, ShoppingCart } from 'lucide-react-native';
import { useRouter } from 'expo-router';
import { PRODUCTS, CATEGORIES, getPrimaryImage } from '@/constants/mockData';

export default function StoreScreen() {
  const router = useRouter();

  return (
    <View className="flex-1 bg-[#1a1a1a]">
      <View className="px-6 pt-16 pb-4 flex-row items-center border-b border-gray-800">
        <TouchableOpacity onPress={() => router.back()} className="mr-4">
          <ArrowLeft color="white" size={24} />
        </TouchableOpacity>
        <Text className="text-xl text-white font-bold flex-1">Shop Equipment</Text>
        <ShoppingCart color="white" size={24} />
      </View>

      <ScrollView className="p-6">
        <View className="flex-row flex-wrap justify-between">
          {PRODUCTS.map(product => {
            const categoryName = CATEGORIES.find(c => c.categoryId === product.categoryId)?.categoryName;
            return (
              <TouchableOpacity 
                key={product.productId} 
                onPress={() => router.push(`/equipment/${product.productId}` as any)}
                className="w-[48%] mb-6 bg-[#0a0a0a] rounded-2xl border border-gray-800 overflow-hidden"
              >
                <Image 
                  source={{ uri: getPrimaryImage(product.productId, 'PRODUCT') }} 
                  className="w-full h-32" 
                  resizeMode="cover"
                />
                <View className="p-3">
                  <Text className="text-[10px] text-gray-500 uppercase mb-1">{categoryName}</Text>
                  <Text className="text-white font-bold mb-2" numberOfLines={1}>{product.productName}</Text>
                  <Text className="text-[#FF8C42] font-semibold">₫{product.price.toLocaleString()}</Text>
                  <Text className="text-xs text-gray-400 mt-1">Stock: {product.stockQuantity}</Text>
                </View>
              </TouchableOpacity>
            )
          })}
        </View>
      </ScrollView>
    </View>
  );
}
