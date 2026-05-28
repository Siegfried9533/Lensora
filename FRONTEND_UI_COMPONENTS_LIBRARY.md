# 🧩 UI Components Library & Patterns - Lensora

## Component Structure & Styling Patterns

---

## 1. Button Components

### Primary Button (Orange CTA)
```typescript
<TouchableOpacity className="bg-[#FF8C42] px-8 py-4 rounded-full">
  <Text className="text-black font-bold text-center">Action</Text>
</TouchableOpacity>
```

| Property | Value | Note |
|----------|-------|------|
| Background | `#FF8C42` | Orange accent |
| Text Color | Black | High contrast |
| Text Weight | Bold (700) | Prominent |
| Padding H | 32px (px-8) | Wide buttons |
| Padding V | 16px (py-4) | Tall buttons |
| Border Radius | 999px | Pill-shaped |
| Min Touch | 44x44px | Accessibility |

### Secondary Button (Bordered)
```typescript
<TouchableOpacity className="border border-gray-800 px-6 py-3 rounded-full">
  <Text className="text-white font-semibold">Secondary</Text>
</TouchableOpacity>
```

### Icon Button
```typescript
<TouchableOpacity className="w-10 h-10 bg-[#0a0a0a] rounded-full items-center justify-center border border-gray-800">
  <Icon size={20} color="#9ca3af" />
</TouchableOpacity>
```

### Link Button
```typescript
<TouchableOpacity>
  <Text className="text-[#FF8C42] font-semibold">Learn More →</Text>
</TouchableOpacity>
```

---

## 2. Input Components

### Text Input
```typescript
<TextInput
  placeholder="Enter text..."
  placeholderTextColor="#4b5563"
  value={value}
  onChangeText={setValue}
  className="px-4 py-4 bg-[#0a0a0a] border border-gray-800 rounded-2xl text-white"
/>
```

| Property | Value |
|----------|-------|
| Background | `#0a0a0a` |
| Border | 1px `#374151` |
| Padding | 16px all |
| Border Radius | 16px |
| Text Color | White |
| Placeholder | `#4b5563` |

### Search Input
```typescript
<View className="relative">
  <View className="absolute left-4 top-4 z-10">
    <Search size={16} color="#6b7280" />
  </View>
  <TextInput
    placeholder="Search equipment..."
    placeholderTextColor="#4b5563"
    className="pl-11 pr-4 py-4 bg-[#0a0a0a] border border-gray-800 rounded-2xl text-white"
  />
</View>
```

### Password Input
```typescript
<View className="relative">
  <TextInput
    placeholder="Password"
    placeholderTextColor="#4b5563"
    secureTextEntry={!showPassword}
    className="pr-12 pl-4 py-4 bg-[#0a0a0a] border border-gray-800 rounded-2xl text-white"
  />
  <TouchableOpacity className="absolute right-4 top-4">
    <Eye size={20} color="#6b7280" />
  </TouchableOpacity>
</View>
```

---

## 3. Card Components

### Product Card
```typescript
<TouchableOpacity className="bg-[#0a0a0a] border border-gray-800 rounded-2xl overflow-hidden">
  <Image className="w-full h-48" source={{ uri: imageUrl }} />
  <View className="p-4">
    <Text className="text-white font-bold text-base">{title}</Text>
    <Text className="text-gray-400 text-xs mb-3">{category}</Text>
    <View className="flex-row justify-between items-center">
      <Text className="text-[#FF8C42] font-bold text-lg">${price}</Text>
      <Heart size={20} color={isFavorite ? '#FF8C42' : '#9ca3af'} />
    </View>
  </View>
</TouchableOpacity>
```

**Layout:**
```
┌──────────────────────┐
│                      │
│  [  Image Area   ]   │  ← 192px height
│                      │
├──────────────────────┤
│ Product Title        │  ← Bold, white
│ category             │  ← Gray, small
│                      │
│ $199.99        ❤️   │  ← Orange price, icon
└──────────────────────┘
```

### Info Card
```typescript
<View className="bg-[#0a0a0a] border border-gray-800 rounded-2xl p-4">
  <Text className="text-white font-bold mb-2">Title</Text>
  <Text className="text-gray-400 text-sm">Description text here...</Text>
</View>
```

### Stat Card
```typescript
<View className="bg-[#0a0a0a] border border-gray-800 rounded-2xl p-4 items-center">
  <Text className="text-2xl font-bold text-[#FF8C42] mb-2">42</Text>
  <Text className="text-gray-400 text-sm">Orders</Text>
</View>
```

---

## 4. List Components

### List Item
```typescript
<View className="px-6 py-4 border-b border-gray-800 flex-row items-center">
  <View className="flex-1">
    <Text className="text-white font-bold">{title}</Text>
    <Text className="text-gray-400 text-sm">{subtitle}</Text>
  </View>
  <ChevronRight size={20} color="#6b7280" />
</View>
```

### List Item with Avatar
```typescript
<View className="px-6 py-4 flex-row items-center border-b border-gray-800">
  <Image className="w-12 h-12 rounded-full mr-4" source={{ uri: avatar }} />
  <View className="flex-1">
    <Text className="text-white font-bold">{name}</Text>
    <Text className="text-gray-400 text-sm">{subtitle}</Text>
  </View>
</View>
```

### List Item with Toggle
```typescript
<View className="px-6 py-4 flex-row items-center border-b border-gray-800">
  <View className="flex-1">
    <Text className="text-white font-bold">{title}</Text>
  </View>
  <Switch value={enabled} onValueChange={setEnabled} />
</View>
```

### Cart Item
```typescript
<View className="flex-row px-6 py-4 border-b border-gray-800 items-center">
  <Image className="w-16 h-16 rounded-lg mr-4" source={{ uri: imageUrl }} />
  
  <View className="flex-1">
    <Text className="text-white font-bold text-sm">{name}</Text>
    <Text className="text-gray-400 text-xs mb-2">{category}</Text>
    <Text className="text-[#FF8C42] font-bold">${price}</Text>
  </View>
  
  <View className="items-center">
    <TouchableOpacity className="mb-1">
      <Plus size={16} color="#9ca3af" />
    </TouchableOpacity>
    <Text className="text-white text-sm font-bold">{quantity}</Text>
    <TouchableOpacity className="mt-1">
      <Minus size={16} color="#9ca3af" />
    </TouchableOpacity>
  </View>
</View>
```

---

## 5. Badge Components

### Notification Badge
```typescript
<View className="absolute -top-1 -right-1 bg-red-500 w-4 h-4 rounded-full items-center justify-center">
  <Text className="text-[10px] text-white font-bold">9+</Text>
</View>
```

### Category Badge
```typescript
<View className="bg-[#FF8C42]/20 px-3 py-1 rounded-full">
  <Text className="text-[#FF8C42] text-xs font-semibold">Electronics</Text>
</View>
```

### Status Badge
```typescript
<View className="bg-green-500/20 px-3 py-1 rounded-full">
  <Text className="text-green-500 text-xs font-bold">In Stock</Text>
</View>
```

---

## 6. Toggle/Switch Components

### Buy/Rent Toggle
```typescript
<View className="flex-row bg-[#0a0a0a] border border-gray-800 p-1 rounded-2xl">
  <TouchableOpacity
    className={`flex-1 py-3 rounded-xl items-center ${
      mode === 'BUY' ? 'bg-[#FF8C42]' : ''
    }`}
    onPress={() => setMode('BUY')}
  >
    <Text className={`font-semibold ${
      mode === 'BUY' ? 'text-black' : 'text-gray-500'
    }`}>
      Buy
    </Text>
  </TouchableOpacity>
  
  <TouchableOpacity
    className={`flex-1 py-3 rounded-xl items-center ${
      mode === 'RENT' ? 'bg-[#FF8C42]' : ''
    }`}
    onPress={() => setMode('RENT')}
  >
    <Text className={`font-semibold ${
      mode === 'RENT' ? 'text-black' : 'text-gray-500'
    }`}>
      Rent
    </Text>
  </TouchableOpacity>
</View>
```

---

## 7. Filter/Category Components

### Category Pills (Horizontal Scroll)
```typescript
<ScrollView horizontal showsHorizontalScrollIndicator={false} className="pb-2">
  {/* All Button */}
  <TouchableOpacity
    className={`px-5 py-3 rounded-full mr-3 ${
      selected === null ? 'bg-[#FF8C42]' : 'bg-[#0a0a0a] border border-gray-800'
    }`}
    onPress={() => setSelected(null)}
  >
    <Text className={selected === null ? 'text-black font-bold' : 'text-white'}>
      All
    </Text>
  </TouchableOpacity>
  
  {/* Category Buttons */}
  {categories.map(cat => (
    <TouchableOpacity
      key={cat.id}
      className={`px-5 py-3 rounded-full mr-3 ${
        selected === cat.name ? 'bg-[#FF8C42]' : 'bg-[#0a0a0a] border border-gray-800'
      }`}
      onPress={() => setSelected(cat.name)}
    >
      <Text className={selected === cat.name ? 'text-black font-bold' : 'text-white'}>
        {cat.name}
      </Text>
    </TouchableOpacity>
  ))}
</ScrollView>
```

**Specifications:**
- Pill-shaped buttons
- Horizontal scroll
- No scroll indicator
- Active: Orange bg + black text
- Inactive: Dark bg + gray border + white text

---

## 8. Header Components

### Simple Header
```typescript
<View
  className="px-6 pb-4 flex-row items-center border-b border-gray-800"
  style={{ paddingTop: insets.top + 16 }}
>
  <TouchableOpacity className="mr-4">
    <ArrowLeft color="white" size={24} />
  </TouchableOpacity>
  <Text className="text-xl text-white font-bold flex-1">Title</Text>
  <TouchableOpacity>
    <MoreVertical size={24} color="white" />
  </TouchableOpacity>
</View>
```

### Discovery Header (with Stats)
```typescript
<View className="px-6 pt-6 pb-4">
  <View className="flex-row items-center justify-between mb-6">
    <View>
      <Text className="text-3xl mb-1 text-white font-bold">DISCOVERY</Text>
      <Text className="text-sm text-gray-400">Welcome, {userName}!</Text>
    </View>
    <TouchableOpacity className="w-10 h-10 bg-[#0a0a0a] rounded-full border border-gray-800 items-center justify-center relative">
      <ShoppingCart size={20} color="#9ca3af" />
      {cartCount > 0 && (
        <View className="absolute -top-1 -right-1 bg-red-500 w-4 h-4 rounded-full items-center justify-center">
          <Text className="text-[10px] text-white font-bold">{cartCount}</Text>
        </View>
      )}
    </TouchableOpacity>
  </View>
</View>
```

---

## 9. Empty State Components

### Generic Empty State
```typescript
<View className="flex-1 justify-center items-center p-6">
  {/* Icon Circle */}
  <View className="w-24 h-24 rounded-full bg-black/20 border border-gray-800 items-center justify-center mb-6">
    <ShoppingBag size={40} color="#4b5563" />
  </View>
  
  {/* Title */}
  <Text className="text-xl text-white font-bold mb-2">
    Cart is Empty
  </Text>
  
  {/* Description */}
  <Text className="text-gray-400 text-center mb-8">
    You haven't added anything to your cart yet. Start shopping!
  </Text>
  
  {/* CTA Button */}
  <TouchableOpacity className="bg-[#FF8C42] px-8 py-4 rounded-full">
    <Text className="text-black font-bold">Start Shopping</Text>
  </TouchableOpacity>
</View>
```

---

## 10. Loading State Components

### Activity Indicator
```typescript
<View className="flex-1 bg-[#1a1a1a] items-center justify-center">
  <ActivityIndicator size="large" color="#FF8C42" />
  <Text className="text-gray-400 mt-4">Loading...</Text>
</View>
```

### Skeleton Loader (Card)
```typescript
<View className="bg-[#0a0a0a] border border-gray-800 rounded-2xl p-4 mb-4">
  <View className="w-full h-6 bg-gray-700 rounded-lg mb-3" />
  <View className="w-3/4 h-4 bg-gray-700 rounded-lg" />
</View>
```

---

## 11. Dialog/Modal Components

### Alert Dialog
```typescript
Alert.alert(
  'Confirm Action',
  'Are you sure you want to proceed?',
  [
    { text: 'Cancel', style: 'cancel' },
    {
      text: 'Delete',
      onPress: () => handleDelete(),
      style: 'destructive',
    },
  ]
);
```

### Confirmation Modal
```typescript
<Modal
  visible={isVisible}
  transparent={true}
  animationType="fade"
>
  <View className="flex-1 bg-black/50 justify-center items-center p-6">
    <View className="bg-[#1a1a1a] rounded-2xl p-6 w-full">
      <Text className="text-xl text-white font-bold mb-4">Confirm</Text>
      <Text className="text-gray-400 mb-6">Confirm action?</Text>
      <View className="flex-row gap-3">
        <TouchableOpacity className="flex-1 border border-gray-800 py-3 rounded-full">
          <Text className="text-white text-center font-semibold">Cancel</Text>
        </TouchableOpacity>
        <TouchableOpacity className="flex-1 bg-[#FF8C42] py-3 rounded-full">
          <Text className="text-black text-center font-bold">Confirm</Text>
        </TouchableOpacity>
      </View>
    </View>
  </View>
</Modal>
```

---

## 12. Form Components

### Form Input Group
```typescript
<View className="mb-6">
  <Text className="text-sm text-gray-400 mb-2">Email</Text>
  <TextInput
    placeholder="your@email.com"
    placeholderTextColor="#4b5563"
    value={email}
    onChangeText={setEmail}
    className="px-4 py-4 bg-[#0a0a0a] border border-gray-800 rounded-2xl text-white"
  />
</View>
```

### Checkbox
```typescript
<TouchableOpacity
  onPress={() => setChecked(!checked)}
  className="flex-row items-center mb-4"
>
  <View className={`w-5 h-5 border-2 rounded mr-3 items-center justify-center ${
    checked ? 'bg-[#FF8C42] border-[#FF8C42]' : 'border-gray-600'
  }`}>
    {checked && <Check size={14} color="black" />}
  </View>
  <Text className="text-white">{label}</Text>
</TouchableOpacity>
```

---

## 13. Divider Components

### Simple Divider
```typescript
<View className="h-px bg-gray-800 my-4" />
```

### Labeled Divider
```typescript
<View className="flex-row items-center my-4">
  <View className="flex-1 h-px bg-gray-800" />
  <Text className="text-gray-400 text-sm mx-3">OR</Text>
  <View className="flex-1 h-px bg-gray-800" />
</View>
```

---

## 14. Icon Components

### Icon with Label
```typescript
<View className="items-center">
  <View className="w-12 h-12 rounded-full bg-[#0a0a0a] border border-gray-800 items-center justify-center mb-2">
    <Heart size={24} color="#FF8C42" />
  </View>
  <Text className="text-white text-sm font-semibold">Favorites</Text>
</View>
```

### Icon Button with Badge
```typescript
<View className="relative">
  <TouchableOpacity className="w-10 h-10 bg-[#0a0a0a] rounded-full border border-gray-800 items-center justify-center">
    <Bell size={20} color="#9ca3af" />
  </TouchableOpacity>
  <View className="absolute -top-1 -right-1 bg-red-500 w-4 h-4 rounded-full items-center justify-center">
    <Text className="text-[10px] text-white font-bold">3</Text>
  </View>
</View>
```

---

## 15. Typography Presets

### Screen Title
```typescript
<Text className="text-3xl mb-1 text-white font-bold tracking-tight">
  DISCOVERY
</Text>
```

### Section Title
```typescript
<Text className="text-xl text-white font-bold mb-4">
  Recent Orders
</Text>
```

### Card Title
```typescript
<Text className="text-base text-white font-bold mb-1">
  Product Name
</Text>
```

### Subtitle
```typescript
<Text className="text-sm text-gray-400 mb-2">
  Product Category
</Text>
```

### Caption
```typescript
<Text className="text-xs text-gray-500 uppercase tracking-wider">
  LABEL
</Text>
```

### Error Text
```typescript
<Text className="text-sm text-red-500 mt-2">
  {errorMessage}
</Text>
```

---

## Component Usage Checklist

| Component | Used In | Frequency |
|-----------|---------|-----------|
| Primary Button | All screens | ★★★★★ |
| TextInput | Auth, Checkout | ★★★★☆ |
| Product Card | Discovery, Search | ★★★★★ |
| List Item | Profile, Orders | ★★★★☆ |
| Category Pills | Discovery | ★★★☆☆ |
| Empty State | Cart, Favorites | ★★★☆☆ |
| Loading Spinner | All data screens | ★★★★★ |
| Header | All screens | ★★★★★ |
| Badge | Notifications, Cart | ★★★★☆ |
| Modal | Confirmations | ★★☆☆☆ |

---

## Design System Summary

**Consistent Components:**
- All buttons follow same pattern (orange + black)
- All inputs follow same styling (dark bg + gray border)
- All cards follow same pattern (dark bg + border + padding)
- All lists follow same pattern (flex-row + border-bottom)

**Spacing Consistency:**
- Header padding: 16-24px
- Card padding: 16px
- Item padding: 16px
- Gap between items: 12-16px

**Color Consistency:**
- Primary action: Always `#FF8C42`
- Text: Always white/gray (never other colors for text)
- Borders: Always gray shades
- Backgrounds: Dark shades only

This consistent approach makes the UI predictable and easy to maintain!
