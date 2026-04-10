import React, { useState, useMemo } from 'react';
import { View, Text, TextInput, TouchableOpacity, ScrollView, Image } from 'react-native';
import { Search, Heart, ShoppingCart } from 'lucide-react-native';
import { Link, useRouter } from 'expo-router';
import { useAuth } from '@/context/AuthContext';
import { CATEGORIES, PRODUCTS, ASSETS, getPrimaryImage } from '@/constants/mockData';

export default function DiscoveryScreen() {
  const router = useRouter();
  const { user, favorites, toggleFavorite } = useAuth();
  
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedCategory, setSelectedCategory] = useState<string | null>(null);

  // Combine Products and Assets for the Featured view natively
  const allItems = useMemo(() => {
    const pItems = PRODUCTS.map(p => ({
      ...p,
      id: p.productId,
      title: p.productName,
      type: 'PRODUCT' as const,
      categoryName: CATEGORIES.find(c => c.categoryId === p.categoryId)?.categoryName || '',
    }));
    
    const aItems = ASSETS.map(a => ({
      ...a,
      id: a.assetId,
      title: a.modelName,
      price: a.dailyRate, // Use daily rate as display price
      type: 'ASSET' as const,
      categoryName: CATEGORIES.find(c => c.categoryId === a.categoryId)?.categoryName || '',
    }));

    return [...pItems, ...aItems];
  }, []);

  const filteredItems = useMemo(() => {
    return allItems.filter(item => {
      const matchesSearch = item.title.toLowerCase().includes(searchQuery.toLowerCase());
      const matchesCategory = selectedCategory ? CATEGORIES.find(c => c.categoryId === item.categoryId)?.categoryId === selectedCategory : true;
      return matchesSearch && matchesCategory;
    });
  }, [searchQuery, selectedCategory, allItems]);

  return (
    <ScrollView className="flex-1 bg-[#1a1a1a]">
      {/* Header */}
      <View className="px-6 pt-6 pb-4">
        <View className="flex-row items-center justify-between mb-6">
          <View>
            <Text className="text-3xl mb-1 text-white font-bold tracking-tight">DISCOVERY</Text>
            <Text className="text-sm text-gray-400">Welcome, {user ? user.userName : 'Guest'}!</Text>
          </View>
          {user ? (
            <TouchableOpacity className="w-10 h-10 bg-[#0a0a0a] rounded-full flex items-center justify-center border border-gray-800">
              <ShoppingCart size={20} color="#9ca3af" />
            </TouchableOpacity>
          ) : (
            <Link href={"/(auth)/login" as any} asChild>
              <TouchableOpacity className="px-4 py-2 bg-[#FF8C42] rounded-full">
                <Text className="text-black font-semibold text-xs">Sign In</Text>
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
      </View>

      {/* Categories Filter */}
      <View className="px-6 mb-8">
        <ScrollView horizontal showsHorizontalScrollIndicator={false} className="flex-row gap-3 overflow-visible pb-2">
          <TouchableOpacity
            onPress={() => setSelectedCategory(null)}
            className={`px-5 py-3 mr-3 rounded-full flex-row transition-colors ${selectedCategory === null ? 'bg-[#FF8C42]' : 'bg-[#0a0a0a] border border-gray-800'}`}
          >
            <Text className={`text-sm ${selectedCategory === null ? 'text-black font-bold' : 'text-white'}`}>All</Text>
          </TouchableOpacity>
          
          {CATEGORIES.map((category) => (
            <TouchableOpacity
              key={category.categoryId}
              onPress={() => setSelectedCategory(category.categoryId)}
              className={`px-5 py-3 mr-3 rounded-full flex-row transition-colors ${selectedCategory === category.categoryId ? 'bg-[#FF8C42]' : 'bg-[#0a0a0a] border border-gray-800'}`}
            >
              <Text className={`text-sm ${selectedCategory === category.categoryId ? 'text-black font-bold' : 'text-white'}`}>
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
            <Text className="text-xs tracking-widest mb-2 opacity-90 font-bold text-black">SPECIAL OFFER</Text>
            <Text className="text-2xl mb-2 tracking-tight text-white font-bold">Summer Sale</Text>
            <Text className="text-sm opacity-90 mb-4 text-white">Up to 30% off on selected items</Text>
            <TouchableOpacity onPress={() => router.push('/store' as any)} className="bg-black px-6 py-3 rounded-full self-start">
              <Text className="text-white text-sm font-semibold">Shop Now</Text>
            </TouchableOpacity>
          </View>
        </View>
      </View>

      {/* Featured Section */}
      <View className="px-6 pb-8">
        <View className="flex-row items-baseline justify-between mb-6">
          <Text className="text-xl tracking-tight text-white font-bold">
            {searchQuery || selectedCategory ? 'Results' : 'Featured'}
          </Text>
        </View>

        <View className="space-y-4">
          {filteredItems.map((item) => {
            const isFaved = favorites.includes(item.id);
            return (
              <TouchableOpacity 
                key={item.id} 
                onPress={() => router.push(`/equipment/${item.id}` as any)}
                className="block bg-[#0a0a0a] rounded-3xl overflow-hidden border border-gray-800 mb-4 transition-all"
              >
                <View className="relative aspect-[4/3] w-full bg-gray-900">
                  <Image
                    source={{ uri: getPrimaryImage(item.id, item.type) }}
                    className="w-full h-full"
                    resizeMode="cover"
                  />
                  <View className="absolute top-4 right-4 bg-white/20 px-3 py-1 rounded-full backdrop-blur-sm">
                    <Text className="text-white text-xs font-medium">{item.type}</Text>
                  </View>
                  <TouchableOpacity 
                    onPress={(e) => {
                        e.stopPropagation();
                        if(!user) {
                           router.push('/(auth)/login' as any);
                           return;
                        }
                        toggleFavorite(item.id);
                    }}
                    className={`absolute top-4 left-4 w-9 h-9 rounded-full flex items-center justify-center border ${isFaved ? 'bg-[#FF8C42] border-[#FF8C42]' : 'bg-black/40 border-white/20'}`}
                  >
                    <Heart size={16} color={isFaved ? 'black' : 'white'} fill={isFaved ? 'black' : 'none'} />
                  </TouchableOpacity>
                </View>
                <View className="p-5">
                  <Text className="text-xs text-gray-500 mb-1 tracking-wide uppercase">
                    {item.categoryName}
                  </Text>
                  <Text className="text-xl mb-3 tracking-tight text-white font-bold">{item.title}</Text>
                  <View className="flex-row items-baseline gap-1">
                    <Text className="text-2xl text-[#FF8C42] font-semibold">₫{item.price.toLocaleString()}</Text>
                    {item.type === 'ASSET' && <Text className="text-sm text-gray-500">/day</Text>}
                  </View>
                </View>
              </TouchableOpacity>
            );
          })}
        </View>
      </View>

      {/* Rental Section */}
      <View className="px-6 pb-8">
        <View className="bg-[#0a0a0a] border border-gray-800 rounded-3xl p-6">
          <View className="flex-row items-center justify-between mb-4">
            <View>
              <Text className="text-lg mb-1 tracking-tight text-white font-bold">Rental Service</Text>
              <Text className="text-xs text-gray-400">Try before you buy</Text>
            </View>
            <Text className="text-3xl">📷</Text>
          </View>
          <Text className="text-sm text-gray-400 mb-4">
            Rent professional equipment starting from ₫400,000/day
          </Text>
          <TouchableOpacity onPress={() => router.push('/rentals' as any)} className="w-full py-4 bg-white rounded-full items-center justify-center">
            <Text className="text-black text-sm font-semibold">Browse Rentals</Text>
          </TouchableOpacity>
        </View>
      </View>
    </ScrollView>
  );
}
