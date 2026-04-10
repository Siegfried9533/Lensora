import React, { useState } from 'react';
import { View, Text, ScrollView, Image, TouchableOpacity } from 'react-native';
import { Package, Clock, CheckCircle, XCircle, Calendar } from 'lucide-react-native';

export default function TransactionsScreen() {
  const [activeTab, setActiveTab] = useState<"orders" | "rentals">("orders");

  const orders = [
    {
      id: "ORD-2024-001",
      title: "Canon EF 24-70mm f/2.8L",
      image: "https://images.unsplash.com/photo-1772771841348-a9fb229fd81b?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxtaXJyb3JsZXNzJTIwY2FtZXJhJTIwbW9kZXJufGVufDF8fHx8MTc3NTAxNDQ5NXww&ixlib=rb-4.1.0&q=80&w=1080",
      date: "25/03/2026",
      price: "28.500.000",
      status: "delivered",
      statusText: "Delivered",
    },
    {
      id: "ORD-2024-002",
      title: "Sony A7 III Body",
      image: "https://images.unsplash.com/photo-1674668920910-85b8d3c187ca?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxwcmVtaXVtJTIwY2FtZXJhJTIwZ2VhciUyMGx1eHVyeXxlbnwxfHx8fDE3NzUwMTUzNjN8MA&ixlib=rb-4.1.0&q=80&w=1080",
      date: "30/03/2026",
      price: "42.000.000",
      status: "shipping",
      statusText: "In Transit",
    },
    {
      id: "ORD-2024-003",
      title: "Godox AD600 Pro",
      image: "https://images.unsplash.com/photo-1769699167704-33f5c2bbf3e5?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxzdHVkaW8lMjBsaWdodGluZyUyMGVxdWlwbWVudHxlbnwxfHx8fDE3NzUwMTQ0OTV8MA&ixlib=rb-4.1.0&q=80&w=1080",
      date: "02/04/2026",
      price: "18.000.000",
      status: "cancelled",
      statusText: "Cancelled",
    },
  ];

  const rentals = [
    {
      id: "RNT-2024-001",
      title: "Leica M11 Black",
      image: "https://images.unsplash.com/photo-1725779318629-eda3e096eb86?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHx2aW50YWdlJTIwY2FtZXJhJTIwYWVzdGhldGljJTIwZGFya3xlbnwxfHx8fDE3NzUwMTUzNjN8MA&ixlib=rb-4.1.0&q=80&w=1080",
      startDate: "01/04/2026",
      endDate: "05/04/2026",
      days: 4,
      pricePerDay: "2.500.000",
      total: "10.000.000",
      status: "active",
      statusText: "Active",
    },
    {
      id: "RNT-2024-002",
      title: "Hasselblad X2D 100C",
      image: "https://images.unsplash.com/photo-1511140973288-19bf21d7e771?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxwaG90b2dyYXBoeSUyMGVxdWlwbWVudCUyMG1pbmltYWwlMjBibGFjayUyMGJhY2tncm91bmR8ZW58MXx8fHwxNzc1MDE1MzYzfDA&ixlib=rb-4.1.0&q=80&w=1080",
      startDate: "28/03/2026",
      endDate: "30/03/2026",
      days: 2,
      pricePerDay: "3.000.000",
      total: "6.000.000",
      status: "completed",
      statusText: "Completed",
    },
    {
      id: "RNT-2024-003",
      title: "Sony α1 + 24-70 GM",
      image: "https://images.unsplash.com/photo-1585548601784-e319505354bb?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxwcmVtaXVtJTIwY2FtZXJhJTIwZ2VhciUyMGx1eHVyeXxlbnwxfHx8fDE3NzUwMTUzNjN8MA&ixlib=rb-4.1.0&q=80&w=1080",
      startDate: "10/04/2026",
      endDate: "13/04/2026",
      days: 3,
      pricePerDay: "1.800.000",
      total: "5.400.000",
      status: "pending",
      statusText: "Pending",
    },
  ];

  const getStatusIcon = (status: string, color: string) => {
    switch (status) {
      case "active":
      case "shipping":
        return <Clock size={16} color={color} />;
      case "completed":
      case "delivered":
        return <CheckCircle size={16} color={color} />;
      case "pending":
        return <Package size={16} color={color} />;
      case "cancelled":
        return <XCircle size={16} color={color} />;
      default:
        return <Package size={16} color={color} />;
    }
  };

  const getStatusStyle = (status: string) => {
    switch (status) {
      case "active":
      case "shipping":
        return { bg: "bg-blue-500/20", border: "border-blue-500/30", text: "text-blue-400", color: "#60a5fa" };
      case "completed":
      case "delivered":
        return { bg: "bg-green-500/20", border: "border-green-500/30", text: "text-green-400", color: "#4ade80" };
      case "pending":
        return { bg: "bg-yellow-500/20", border: "border-yellow-500/30", text: "text-yellow-400", color: "#facc15" };
      case "cancelled":
        return { bg: "bg-red-500/20", border: "border-red-500/30", text: "text-red-400", color: "#f87171" };
      default:
        return { bg: "bg-gray-500/20", border: "border-gray-500/30", text: "text-gray-400", color: "#9ca3af" };
    }
  };

  return (
    <View className="flex-1 bg-[#1a1a1a]">
      {/* Header */}
      <View className="px-6 pt-10 pb-4 bg-[#1a1a1a] z-10 w-full">
        <Text className="text-3xl mb-6 text-white font-bold tracking-tight">TRANSACTIONS</Text>

        {/* Tabs */}
        <View className="flex-row gap-3">
          <TouchableOpacity
            onPress={() => setActiveTab("orders")}
            className={`flex-1 py-3 rounded-2xl items-center justify-center transition-all ${
              activeTab === "orders"
                ? "bg-[#FF8C42]"
                : "bg-[#0a0a0a] border border-gray-800"
            }`}
          >
            <Text className={activeTab === "orders" ? "text-black font-semibold" : "text-gray-400"}>Đơn hàng</Text>
          </TouchableOpacity>
          <TouchableOpacity
            onPress={() => setActiveTab("rentals")}
            className={`flex-1 py-3 rounded-2xl items-center justify-center transition-all ${
              activeTab === "rentals"
                ? "bg-[#FF8C42]"
                : "bg-[#0a0a0a] border border-gray-800"
            }`}
          >
            <Text className={activeTab === "rentals" ? "text-black font-semibold" : "text-gray-400"}>Đơn thuê</Text>
          </TouchableOpacity>
        </View>
      </View>

      {/* Content */}
      <ScrollView className="px-6 flex-1 pt-4">
        {activeTab === "orders" ? (
          <View className="space-y-4 pb-8">
            {orders.map((order) => {
              const statusStyle = getStatusStyle(order.status);
              return (
                <View
                  key={order.id}
                  className="bg-[#0a0a0a] border border-gray-800 rounded-3xl overflow-hidden mb-4"
                >
                  <View className="flex-row gap-4 p-4">
                    <View className="w-24 h-24 rounded-xl overflow-hidden flex-shrink-0 bg-gray-900 border border-gray-800">
                      <Image
                        source={{ uri: order.image }}
                        className="w-full h-full"
                        resizeMode="cover"
                      />
                    </View>
                    <View className="flex-1">
                      <Text className="text-sm text-white font-semibold mb-1 w-full" numberOfLines={1}>{order.title}</Text>
                      <Text className="text-xs text-gray-500 mb-2">{order.id}</Text>
                      <View className={`flex-row items-center gap-1.5 px-2.5 py-1 rounded-full border self-start ${statusStyle.bg} ${statusStyle.border}`}>
                        {getStatusIcon(order.status, statusStyle.color)}
                        <Text className={`text-xs ml-1 ${statusStyle.text}`}>{order.statusText}</Text>
                      </View>
                    </View>
                  </View>
                  <View className="px-4 pb-4 mt-2">
                    <View className="flex-row justify-between mb-2">
                      <Text className="text-gray-500 text-sm">Order Date</Text>
                      <Text className="text-gray-300 text-sm">{order.date}</Text>
                    </View>
                    <View className="flex-row justify-between items-end pt-2 border-t border-gray-800">
                      <Text className="text-gray-500 text-sm">Total</Text>
                      <Text className="text-[#FF8C42] text-lg font-bold">₫{order.price}</Text>
                    </View>
                  </View>
                </View>
              );
            })}
          </View>
        ) : (
          <View className="space-y-4 pb-8">
            {rentals.map((rental) => {
              const statusStyle = getStatusStyle(rental.status);
              return (
                <View
                  key={rental.id}
                  className="bg-[#0a0a0a] border border-gray-800 rounded-3xl overflow-hidden mb-4"
                >
                  <View className="flex-row gap-4 p-4">
                    <View className="w-24 h-24 rounded-xl overflow-hidden flex-shrink-0 bg-gray-900 border border-gray-800">
                      <Image
                        source={{ uri: rental.image }}
                        className="w-full h-full"
                        resizeMode="cover"
                      />
                    </View>
                    <View className="flex-1">
                      <Text className="text-sm text-white font-semibold mb-1 w-full" numberOfLines={1}>{rental.title}</Text>
                      <Text className="text-xs text-gray-500 mb-2">{rental.id}</Text>
                      <View className={`flex-row items-center gap-1.5 px-2.5 py-1 rounded-full border self-start ${statusStyle.bg} ${statusStyle.border}`}>
                        {getStatusIcon(rental.status, statusStyle.color)}
                        <Text className={`text-xs ml-1 ${statusStyle.text}`}>{rental.statusText}</Text>
                      </View>
                    </View>
                  </View>
                  <View className="px-4 pb-4 mt-2">
                    <View className="flex-row justify-between mb-2">
                      <Text className="text-gray-500 text-sm">Rental Period</Text>
                      <View className="flex-row items-center">
                        <Calendar size={12} color="#d1d5db" />
                        <Text className="text-gray-300 text-sm ml-1">{rental.days} days</Text>
                      </View>
                    </View>
                    <View className="flex-row justify-between mb-2">
                      <Text className="text-gray-500 text-sm">Duration</Text>
                      <Text className="text-gray-300 text-sm">{rental.startDate} - {rental.endDate}</Text>
                    </View>
                    <View className="flex-row justify-between mb-2">
                      <Text className="text-gray-500 text-sm">Daily Rate</Text>
                      <Text className="text-gray-300 text-sm">₫{rental.pricePerDay}</Text>
                    </View>
                    <View className="flex-row justify-between items-end pt-2 border-t border-gray-800">
                      <Text className="text-gray-500 text-sm">Total</Text>
                      <Text className="text-[#FF8C42] text-lg font-bold">₫{rental.total}</Text>
                    </View>
                  </View>
                </View>
              );
            })}
          </View>
        )}
      </ScrollView>
    </View>
  );
}
