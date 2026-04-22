import React from 'react';
import { View, Text, TouchableOpacity } from 'react-native';
import { PackageOpen, Clock, Truck, CheckCircle2, ChevronRight, Home } from 'lucide-react-native';
import { useRouter } from 'expo-router';

export default function OrderStatusScreen() {
  const router = useRouter();

  // Mock tracking steps
  const steps = [
    { id: 1, title: 'Order Placed', desc: 'We have received your order', icon: Clock, completed: true },
    { id: 2, title: 'Processing', desc: 'Your items are being carefully packed', icon: PackageOpen, completed: true },
    { id: 3, title: 'Shipped', desc: 'Your order is on the way', icon: Truck, completed: false },
    { id: 4, title: 'Delivered', desc: 'The order has been handed to you', icon: CheckCircle2, completed: false },
  ];

  return (
    <View className="flex-1 bg-[#1a1a1a]">
      {/* Header */}
      <View className="px-6 pt-16 pb-4 border-b border-gray-800 items-center">
        <View className="w-16 h-16 rounded-full bg-green-500/20 items-center justify-center mb-3">
          <CheckCircle2 color="#4ade80" size={32} />
        </View>
        <Text className="text-2xl text-white font-bold mb-1">Order Confirmed!</Text>
        <Text className="text-gray-400 text-sm">Order #ORD-{Math.floor(Math.random() * 100000)}</Text>
      </View>

      <View className="flex-1 px-6 py-8">
        <Text className="text-sm font-bold text-gray-500 uppercase tracking-widest mb-6">Tracking Status</Text>
        
        <View className="space-y-0">
          {steps.map((step, index) => {
            const Icon = step.icon;
            const isLast = index === steps.length - 1;
            
            return (
              <View key={step.id} className="flex-row items-start relative opacity-100">
                 {!isLast && (
                    <View className={`absolute left-5 top-10 bottom-[-20px] w-0.5 ${step.completed ? 'bg-[#FF8C42]' : 'bg-gray-800'}`} />
                 )}
                 <View className={`w-10 h-10 rounded-full items-center justify-center z-10 ${step.completed ? 'bg-[#FF8C42]' : 'bg-gray-800'}`}>
                    <Icon size={18} color={step.completed ? 'black' : '#9ca3af'} />
                 </View>
                 <View className="ml-4 pb-10 flex-1">
                    <Text className={`font-bold ${step.completed ? 'text-white' : 'text-gray-500'}`}>{step.title}</Text>
                    <Text className={`${step.completed ? 'text-gray-400' : 'text-gray-600'} text-xs mt-1`}>{step.desc}</Text>
                 </View>
              </View>
            );
          })}
        </View>
      </View>

      <View className="absolute bottom-0 left-0 right-0 bg-[#0a0a0a] border-t border-gray-800 px-6 pt-4 pb-8 space-y-3">
        <TouchableOpacity 
          onPress={() => router.replace('/(tabs)/transactions' as any)}
          className="w-full bg-[#0a0a0a] border border-gray-800 py-4 rounded-2xl items-center flex-row justify-center"
        >
          <Text className="text-white font-bold text-sm mr-2">View in Transactions</Text>
          <ChevronRight size={16} color="white" />
        </TouchableOpacity>
        <TouchableOpacity 
          onPress={() => router.replace('/(tabs)/' as any)}
          className="w-full bg-[#FF8C42] py-4 rounded-2xl items-center flex-row justify-center"
        >
          <Home size={18} color="black" style={{ marginRight: 8 }} />
          <Text className="text-black font-bold text-sm">Return Home</Text>
        </TouchableOpacity>
      </View>
    </View>
  );
}
