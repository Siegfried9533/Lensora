import React from 'react';
import { View, Text, TouchableOpacity, ScrollView } from 'react-native';
import { ArrowLeft, Package, ChevronRight } from 'lucide-react-native';
import { useRouter } from 'expo-router';
import { useAuth } from '@/context/AuthContext';
import { RENTALS, ASSETS, ORDERS } from '@/constants/mockData';

export default function MyEquipmentScreen() {
  const router = useRouter();
  const { user } = useAuth();

  const userRentals = RENTALS.filter(r => r.userId === user?.userId);
  const userOrders = ORDERS.filter(o => o.userId === user?.userId);

  return (
    <View className="flex-1 bg-[#1a1a1a]">
      <View className="px-6 pt-16 pb-4 flex-row items-center border-b border-gray-800">
        <TouchableOpacity onPress={() => router.back()} className="mr-4">
          <ArrowLeft color="white" size={24} />
        </TouchableOpacity>
        <Text className="text-xl text-white font-bold flex-1">My Equipment</Text>
      </View>

      <ScrollView className="p-6">
        <Text className="text-lg font-bold text-white mb-4 flex-row items-center">
          Active Rentals
        </Text>
        
        {userRentals.length === 0 ? (
          <Text className="text-gray-400">You don't have any active rentals.</Text>
        ) : (
          userRentals.map(rental => {
            const asset = ASSETS.find(a => a.assetId === rental.assetId);
            return (
              <TouchableOpacity 
                key={rental.rentalId} 
                onPress={() => router.push(`/rentals/detail/${rental.rentalId}` as any)}
                className="bg-[#0a0a0a] border border-gray-800 rounded-2xl p-4 mb-4"
              >
                <View className="flex-row items-center justify-between mb-3">
                  <Text className="text-white font-bold">{asset?.modelName || 'Unknown Asset'}</Text>
                  <View className="bg-green-500/20 px-2 py-1 rounded">
                    <Text className="text-green-500 text-[10px] uppercase font-bold">{rental.status}</Text>
                  </View>
                </View>
                <Text className="text-gray-400 text-sm mb-1">Start: {new Date(rental.startDate).toLocaleDateString()}</Text>
                
                <View className="flex-row justify-between items-center mt-3 pt-3 border-t border-gray-800">
                  <Text className="text-gray-500 text-xs">View Agreement</Text>
                  <ChevronRight size={16} color="#FF8C42" />
                </View>
              </TouchableOpacity>
            )
          })
        )}

        <View className="mt-8">
          <Text className="text-lg font-bold text-white mb-4">Purchased Products</Text>
          {userOrders.length === 0 ? (
            <View className="border border-dashed border-gray-800 rounded-2xl p-6 items-center flex-col justify-center bg-[#0a0a0a]/50">
              <Package color="#4b5563" size={32} className="mb-3" />
              <Text className="text-gray-500 text-center">You haven't bought any products yet.</Text>
            </View>
          ) : (
            userOrders.map(order => (
               <TouchableOpacity 
                 key={order.orderId}
                 onPress={() => router.push(`/orders/${order.orderId}` as any)}
                 className="bg-[#0a0a0a] border border-gray-800 rounded-2xl p-4 mb-4 flex-row items-center justify-between"
               >
                  <View>
                     <Text className="text-white font-bold mb-1">Order #{order.orderId}</Text>
                     <Text className="text-gray-400 text-xs">{new Date(order.orderDate).toLocaleDateString()} • {order.status}</Text>
                  </View>
                  <ChevronRight size={20} color="#FF8C42" />
               </TouchableOpacity>
            ))
          )}
        </View>
      </ScrollView>
    </View>
  );
}
