import React from 'react';
import { View, Text, ScrollView, TouchableOpacity, Image } from 'react-native';
import { ArrowLeft, Camera } from 'lucide-react-native';
import { useRouter } from 'expo-router';
import { ASSETS, CATEGORIES, getPrimaryImage } from '@/constants/mockData';

export default function RentalsScreen() {
  const router = useRouter();

  return (
    <View className="flex-1 bg-[#1a1a1a]">
      <View className="px-6 pt-16 pb-4 flex-row items-center border-b border-gray-800">
        <TouchableOpacity onPress={() => router.back()} className="mr-4">
          <ArrowLeft color="white" size={24} />
        </TouchableOpacity>
        <Text className="text-xl text-white font-bold flex-1">Rentals</Text>
        <Camera color="white" size={24} />
      </View>

      <ScrollView className="p-6">
        <Text className="text-gray-400 mb-6">Choose equipment for your next shoot.</Text>
        
        {ASSETS.map(asset => {
          const categoryName = CATEGORIES.find(c => c.categoryId === asset.categoryId)?.categoryName;
          const isRented = asset.status !== 'AVAILABLE';
          
          return (
            <TouchableOpacity 
              key={asset.assetId} 
              disabled={isRented}
              onPress={() => router.push(`/equipment/${asset.assetId}` as any)}
              className={`mb-4 flex-row bg-[#0a0a0a] border border-gray-800 rounded-3xl overflow-hidden ${isRented ? 'opacity-50' : ''}`}
            >
              <Image 
                source={{ uri: getPrimaryImage(asset.assetId, 'ASSET') }} 
                className="w-32 h-full" 
                resizeMode="cover"
              />
              <View className="flex-1 p-4">
                <Text className="text-xs text-gray-400 uppercase mb-1 tracking-wider">{categoryName}</Text>
                <Text className="text-lg font-bold text-white mb-2">{asset.modelName}</Text>
                <Text className="text-[#FF8C42] font-semibold mb-2">₫{asset.dailyRate.toLocaleString()} <Text className="text-gray-500 text-xs">/day</Text></Text>
                
                <View className={`self-start px-3 py-1 rounded-full ${isRented ? 'bg-red-500/20' : 'bg-green-500/20'}`}>
                  <Text className={`text-xs font-semibold ${isRented ? 'text-red-500' : 'text-green-500'}`}>
                    {asset.status}
                  </Text>
                </View>
              </View>
            </TouchableOpacity>
          )
        })}
      </ScrollView>
    </View>
  );
}
