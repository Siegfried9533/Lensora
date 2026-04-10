import React from 'react';
import { View, Text, ScrollView, TouchableOpacity, Image } from 'react-native';
import { ArrowLeft, Calendar, Camera, CreditCard, ShieldAlert } from 'lucide-react-native';
import { useLocalSearchParams, useRouter } from 'expo-router';
import { RENTALS, ASSETS, getPrimaryImage } from '@/constants/mockData';

export default function RentalDetailScreen() {
  const router = useRouter();
  const { id } = useLocalSearchParams();
  const stringId = typeof id === 'string' ? id : Array.isArray(id) ? id[0] : '';
  
  const rental = RENTALS.find(r => r.rentalId === stringId);
  const asset = rental ? ASSETS.find(a => a.assetId === rental.assetId) : null;

  if (!rental || !asset) {
    return (
      <View className="flex-1 bg-[#1a1a1a] items-center justify-center">
        <Text className="text-white">Rental contract not found.</Text>
        <TouchableOpacity onPress={() => router.back()} className="mt-4 px-4 py-2 bg-[#FF8C42] rounded-full">
          <Text className="text-black font-bold">Go Back</Text>
        </TouchableOpacity>
      </View>
    );
  }

  return (
    <View className="flex-1 bg-[#1a1a1a]">
      <View className="px-6 pt-16 pb-4 flex-row items-center border-b border-gray-800 bg-[#1a1a1a]">
        <TouchableOpacity onPress={() => router.back()} className="mr-4">
          <ArrowLeft color="white" size={24} />
        </TouchableOpacity>
        <Text className="text-xl text-white font-bold flex-1">Rental Agreement</Text>
        <View className="bg-[#FF8C42]/20 px-2.5 py-1 rounded-full">
            <Text className="text-[#FF8C42] text-xs font-bold uppercase">{rental.status}</Text>
        </View>
      </View>

      <ScrollView className="flex-1 p-6">
        <View className="bg-[#0a0a0a] border border-gray-800 rounded-3xl overflow-hidden mb-6">
           <Image 
             source={{ uri: getPrimaryImage(asset.assetId, 'ASSET') }}
             className="w-full h-48 bg-gray-900"
             resizeMode="cover"
           />
           <View className="p-5">
             <Text className="text-gray-500 text-xs font-bold mb-1 uppercase tracking-wider">{asset.brand}</Text>
             <Text className="text-white text-xl font-bold mb-2">{asset.modelName}</Text>
             <Text className="text-gray-400 text-xs flex-row"><Camera size={12} color="#9ca3af" /> S/N: {asset.seriNumber}</Text>
           </View>
        </View>

        <View className="bg-[#0a0a0a] border border-gray-800 rounded-2xl p-5 mb-4 space-y-4">
           <Text className="text-white font-bold mb-2">Duration</Text>
           <View className="flex-row items-center justify-between border-b border-gray-800 pb-4">
              <View>
                 <Text className="text-gray-500 text-xs mb-1">Start Date</Text>
                 <Text className="text-white text-sm font-semibold">{new Date(rental.startDate).toLocaleDateString()}</Text>
              </View>
              <ArrowLeft color="#4b5563" size={16} style={{transform: [{rotate: '180deg'}]}} />
              <View>
                 <Text className="text-gray-500 text-xs mb-1">End Date</Text>
                 <Text className="text-white text-sm font-semibold">{new Date(rental.endDate).toLocaleDateString()}</Text>
              </View>
           </View>
        </View>

        <View className="bg-[#0a0a0a] border border-gray-800 rounded-2xl p-5 mb-8 space-y-4">
           <Text className="text-white font-bold mb-2">Financials</Text>
           
           <View className="flex-row justify-between items-center mb-2">
              <Text className="text-gray-400 text-sm">Deposit Fee</Text>
              <Text className="text-white font-semibold">₫{rental.depositFee.toLocaleString()}</Text>
           </View>
           
           <View className="flex-row justify-between items-center mb-2">
              <Text className="text-gray-400 text-sm">Rental Bill</Text>
              <Text className="text-white font-semibold">₫{rental.totalRentFee.toLocaleString()}</Text>
           </View>
           
           {rental.penaltyFee > 0 && (
             <View className="flex-row justify-between items-center mb-2 px-3 py-2 bg-red-500/10 rounded-lg">
                <Text className="text-red-400 text-sm flex-row items-center"><ShieldAlert size={14} color="#f87171" className="mr-1" /> Penalty Fee</Text>
                <Text className="text-red-400 font-semibold">₫{rental.penaltyFee.toLocaleString()}</Text>
             </View>
           )}

           <View className="flex-row justify-between items-center pt-3 border-t border-gray-800">
              <Text className="text-white font-bold text-lg">Total Cost</Text>
              <Text className="text-[#FF8C42] font-bold text-xl">₫{(rental.totalRentFee + rental.penaltyFee).toLocaleString()}</Text>
           </View>
        </View>
      </ScrollView>
    </View>
  );
}
