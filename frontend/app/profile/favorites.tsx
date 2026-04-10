import React from 'react';
import { View, Text, TouchableOpacity, ScrollView, Image } from 'react-native';
import { ArrowLeft, Trash2 } from 'lucide-react-native';
import { useRouter } from 'expo-router';
import { useAuth } from '@/context/AuthContext';
import { PRODUCTS, ASSETS, getPrimaryImage } from '@/constants/mockData';

export default function FavoritesScreen() {
  const router = useRouter();
  const { favorites, toggleFavorite } = useAuth();

  const favoriteItems = [
    ...PRODUCTS.map(p => ({ ...p, id: p.productId, title: p.productName, type: 'PRODUCT' as const })),
    ...ASSETS.map(a => ({ ...a, id: a.assetId, title: a.modelName, type: 'ASSET' as const }))
  ].filter(i => favorites.includes(i.id));

  return (
    <View className="flex-1 bg-[#1a1a1a]">
      <View className="px-6 pt-16 pb-4 flex-row items-center border-b border-gray-800">
        <TouchableOpacity onPress={() => router.back()} className="mr-4">
          <ArrowLeft color="white" size={24} />
        </TouchableOpacity>
        <Text className="text-xl text-white font-bold flex-1">Favorites</Text>
      </View>

      <ScrollView className="p-6">
        {favoriteItems.length === 0 ? (
          <Text className="text-gray-400 text-center mt-10">No favorites added yet.</Text>
        ) : (
          favoriteItems.map(item => (
            <View key={item.id} className="mb-4 bg-[#0a0a0a] rounded-2xl border border-gray-800 flex-row items-center p-3">
              <Image 
                source={{ uri: getPrimaryImage(item.id, item.type) }}
                className="w-20 h-20 rounded-xl"
              />
              <View className="flex-1 ml-4 justify-center">
                <Text className="text-xs text-[#FF8C42] mb-1">{item.type}</Text>
                <Text className="text-white font-bold mb-1" numberOfLines={1}>{item.title}</Text>
              </View>
              <TouchableOpacity onPress={() => toggleFavorite(item.id)} className="p-3">
                <Trash2 color="#ef4444" size={20} />
              </TouchableOpacity>
            </View>
          ))
        )}
      </ScrollView>
    </View>
  );
}
