import React, { useMemo } from 'react';
import { View, Text, ScrollView, Image, TouchableOpacity } from 'react-native';
import { ArrowLeft, Heart, Share2, Star, Shield, CheckCircle } from 'lucide-react-native';
import { useLocalSearchParams, useRouter } from 'expo-router';
import { PRODUCTS, ASSETS, CATEGORIES, getPrimaryImage } from '@/constants/mockData';
import { useAuth } from '@/context/AuthContext';

export default function EquipmentDetailScreen() {
  const router = useRouter();
  const { id } = useLocalSearchParams();
  const { user, favorites, toggleFavorite } = useAuth();
  
  const stringId = typeof id === 'string' ? id : Array.isArray(id) ? id[0] : '';
  
  const equipment = useMemo(() => {
    const productMatch = PRODUCTS.find(p => p.productId === stringId);
    if (productMatch) {
      return {
        id: productMatch.productId,
        title: productMatch.productName,
        price: productMatch.price,
        type: 'PRODUCT' as const,
        brand: productMatch.brand,
        desc: productMatch.description,
        categoryId: productMatch.categoryId
      };
    }
    
    const assetMatch = ASSETS.find(a => a.assetId === stringId);
    if (assetMatch) {
      return {
        id: assetMatch.assetId,
        title: assetMatch.modelName,
        price: assetMatch.dailyRate,
        type: 'ASSET' as const,
        brand: assetMatch.brand,
        desc: "Professional equipment available for rent.",
        categoryId: assetMatch.categoryId,
        status: assetMatch.status
      };
    }
    return null;
  }, [stringId]);

  if (!equipment) {
    return (
      <View className="flex-1 bg-[#1a1a1a] items-center justify-center">
        <Text className="text-white">Equipment not found.</Text>
        <TouchableOpacity onPress={() => router.back()} className="mt-4 px-4 py-2 bg-[#FF8C42] rounded-full">
          <Text className="text-black font-bold">Go Back</Text>
        </TouchableOpacity>
      </View>
    );
  }

  const isFaved = favorites.includes(equipment.id);
  const categoryName = CATEGORIES.find(c => c.categoryId === equipment.categoryId)?.categoryName || '';

  return (
    <View className="flex-1 bg-[#1a1a1a]">
      <ScrollView className="flex-1" contentContainerStyle={{ paddingBottom: 100 }}>
        {/* Image Header */}
        <View className="relative">
          <View className="aspect-square bg-black w-full border-b border-gray-800">
            <Image
              source={{ uri: getPrimaryImage(equipment.id, equipment.type) }}
              className="w-full h-full"
              resizeMode="contain"
            />
          </View>

          {/* Top Actions */}
          <View className="absolute top-12 left-0 right-0 px-4 flex-row justify-between">
            <TouchableOpacity
              onPress={() => router.back()}
              className="w-10 h-10 bg-black/60 rounded-full flex items-center justify-center border border-white/10"
            >
              <ArrowLeft size={20} color="white" />
            </TouchableOpacity>
            <View className="flex-row gap-2">
               <TouchableOpacity 
                onPress={() => {
                  if(!user) {
                     router.push('/(auth)/login' as any);
                     return;
                  }
                  toggleFavorite(equipment.id);
                }}
                className={`w-10 h-10 rounded-full flex items-center justify-center border ${isFaved ? 'bg-[#FF8C42] border-[#FF8C42]' : 'bg-black/40 border-white/20'}`}
              >
                <Heart size={20} color={isFaved ? 'black' : 'white'} fill={isFaved ? 'black' : 'none'} />
              </TouchableOpacity>
            </View>
          </View>

          {/* Badge */}
          <View className="absolute bottom-4 left-4">
            <View className="bg-[#FF8C42] px-3 py-1.5 rounded-full">
              <Text className="text-black text-xs font-bold uppercase">{equipment.type}</Text>
            </View>
          </View>
        </View>

        {/* Content */}
        <View className="px-6 py-6 space-y-4">
          <View className="mb-4">
            <Text className="text-xs text-gray-500 mb-2 uppercase tracking-wider font-semibold">
              {equipment.brand} • {categoryName}
            </Text>
            <Text className="text-3xl mb-4 tracking-tight text-white font-bold">{equipment.title}</Text>
            <View className="flex-row items-baseline gap-2 mb-4">
              <Text className="text-4xl text-[#FF8C42] font-semibold">₫{equipment.price.toLocaleString()}</Text>
              {equipment.type === 'ASSET' && <Text className="text-gray-400 text-sm">/ day</Text>}
            </View>
            <View className="flex-row items-center gap-4">
              <View className="flex-row items-center">
                <Star size={16} color="#FF8C42" fill="#FF8C42" className="mr-1" />
                <Text className="text-sm text-gray-400 ml-1">4.9 (128 reviews)</Text>
              </View>
            </View>
          </View>

          {/* Asset Status Warning */}
          {equipment.type === 'ASSET' && equipment.status !== 'AVAILABLE' && (
             <View className="bg-red-500/10 border border-red-500/20 p-4 rounded-xl mb-4">
                 <Text className="text-red-500 font-bold">Currently Rented Out</Text>
                 <Text className="text-gray-400 text-xs mt-1">This item will be available soon.</Text>
             </View>
          )}

          {/* Description */}
          <View className="bg-[#0a0a0a] border border-gray-800 rounded-3xl p-5 mb-4">
            <Text className="text-xs text-gray-500 uppercase tracking-wider mb-3 font-bold">Description</Text>
            <Text className="text-sm text-gray-300 leading-relaxed">{equipment.desc}</Text>
          </View>

          {/* Guarantee */}
          <View className="bg-[#FF8C42]/10 border border-[#FF8C42]/20 rounded-3xl p-5 flex-row items-center gap-3">
            <Shield size={32} color="#FF8C42" />
            <View className="flex-1">
              <Text className="text-sm text-white mb-1 font-semibold">{equipment.type === 'PRODUCT' ? 'Buyer Protection' : 'Verified Rental'}</Text>
              <Text className="text-xs text-gray-400">
                100% money-back guarantee if product doesn't match description
              </Text>
            </View>
          </View>
        </View>
      </ScrollView>

      {/* Bottom Actions */}
      <View className="absolute bottom-0 left-0 right-0 bg-[#0a0a0a] border-t border-gray-800 px-6 pt-4 pb-8 flex-row gap-3">
        <TouchableOpacity className="flex-1 py-4 bg-gray-900 border border-gray-800 rounded-2xl items-center justify-center">
          <Text className="text-white font-semibold">Contact Store</Text>
        </TouchableOpacity>
        <TouchableOpacity 
          className={`flex-1 py-4 rounded-2xl items-center justify-center ${equipment.type === 'ASSET' && equipment.status !== 'AVAILABLE' ? 'bg-gray-600' : 'bg-[#FF8C42]'}`}
          disabled={equipment.type === 'ASSET' && equipment.status !== 'AVAILABLE'}
        >
          <Text className={`${equipment.type === 'ASSET' && equipment.status !== 'AVAILABLE' ? 'text-gray-400' : 'text-black'} font-bold`}>
            {equipment.type === 'PRODUCT' ? 'Buy Now' : 'Rent Now'}
          </Text>
        </TouchableOpacity>
      </View>
    </View>
  );
}
