# 🎯 Design Patterns by Main Screens - Detailed Breakdown

## 1. Discovery/Shop Screen (`app/(tabs)/index.tsx`)

### Screen Purpose
Main shopping interface where users can browse, search, and filter products/assets.

### Design Patterns Used

#### 1.1 Search & Filter Pattern
```typescript
const [searchQuery, setSearchQuery] = useState('');
const [selectedCategory, setSelectedCategory] = useState<string | null>(null);
const [shopMode, setShopMode] = useState<'BUY' | 'RENT'>('BUY');
```
- Real-time search input
- Category dropdown filtering
- Mode toggle for BUY/RENT

#### 1.2 Memoized Filtering
```typescript
const filteredItems = useMemo(() => {
  return allItems.filter(item => {
    const matchesSearch = item.title.toLowerCase().includes(searchQuery.toLowerCase());
    const matchesCategory = selectedCategory ? item.categoryName === selectedCategory : true;
    return matchesSearch && matchesCategory;
  });
}, [searchQuery, selectedCategory, allItems]);
```
- Prevents recalculation on every render
- Only recalculates when dependencies change
- Performance optimization for large datasets

#### 1.3 Data Fetching with Loading State
```typescript
useEffect(() => {
  loadData();
}, [shopMode]);

const loadData = async () => {
  try {
    setLoading(true);
    setError(null);
    
    // Fetch categories
    const cats = await productApi.getCategoriesByType(backendType);
    setCategories(cats);
    
    // Fetch items
    const itemsData = shopMode === 'BUY' 
      ? await productApi.getAllProducts(0, 100)
      : await assetApi.getAllAssets(0, 100);
  } catch (e) {
    setError(e.message);
  } finally {
    setLoading(false);
  }
};
```
- Separation of concerns (API calls)
- Error handling
- Loading state management

#### 1.4 Context Integration
```typescript
const { user, favorites, toggleFavorite, cartItems, loadFavorites } = useAuth();

const handleToggleFavorite = async (id: string) => {
  if (!user) {
    router.push('/(auth)/login' as any);
    return;
  }
  await toggleFavorite(id, shopMode === 'BUY' ? 'PRODUCT' : 'ASSET');
};
```
- Access global auth state
- Call context methods
- Auth guard (redirect to login if needed)

#### 1.5 Navigation Pattern
```typescript
const router = useRouter();
// Navigate to detail screen
<Link href={`/equipment/${item.id}`}>
  {/* Item card */}
</Link>
```

#### 1.6 Conditional Rendering
```typescript
if (loading) {
  return <ActivityIndicator />;
}

if (error) {
  return <ErrorView />;
}

if (filteredItems.length === 0) {
  return <EmptyView />;
}

return <ItemsList />;
```

### State Management
- **Local State**: `searchQuery`, `selectedCategory`, `shopMode`, `loading`, `error`
- **Global State**: `user`, `favorites`, `cartItems` (from AuthContext)
- **Effects**: Load data on `shopMode` change

### Key Takeaways
✓ Efficient filtering with useMemo
✓ Multiple data sources (products & assets)
✓ User-friendly search interface
✓ Real-time updates
✓ Proper error handling

---

## 2. Equipment Detail Screen (`app/equipment/[id].tsx`)

### Screen Purpose
Display detailed information about a product or rental asset with ability to add to cart or favorites.

### Design Patterns Used

#### 2.1 Dynamic Route Parameter
```typescript
const { id } = useLocalSearchParams();
const stringId = typeof id === "string" ? id : Array.isArray(id) ? id[0] : "";
```
- Extract route parameter
- Type-safe parameter handling

#### 2.2 Polymorphic Product Handling
```typescript
const [equipment, setEquipment] = useState<
  (Product & { type: "PRODUCT" }) | (Asset & { type: "ASSET" }) | null
>(null);

const loadEquipment = async () => {
  try {
    // Try loading as product first
    const product = await productApi.getProductById(stringId);
    setEquipment({ ...product, type: "PRODUCT" });
  } catch (e) {
    // If fails, try as asset
    const asset = await assetApi.getAssetById(stringId);
    setEquipment({ ...asset, type: "ASSET" });
  }
};
```
- Handle both Product and Asset types
- Fallback mechanism (try product, then asset)
- Type discrimination with `type` field

#### 2.3 Date Picker Pattern (for rentals)
```typescript
const [startDate, setStartDate] = useState<Date>(new Date());
const [endDate, setEndDate] = useState<Date>(
  new Date(Date.now() + 24 * 60 * 60 * 1000)
);
const [showStartPicker, setShowStartPicker] = useState(false);
const [showEndPicker, setShowEndPicker] = useState(false);

<DateTimePicker
  value={tempDate}
  mode="date"
  onChange={handleDateChange}
/>
```
- Manage rental date range
- Separate state for temporary selection
- Show/hide picker modal

#### 2.4 Quantity Selector
```typescript
const [quantity, setQuantity] = useState(1);

const handleQuantityChange = (amount: number) => {
  setQuantity(prev => Math.max(1, prev + amount));
};

// UI
<TouchableOpacity onPress={() => handleQuantityChange(-1)}>
  <Minus size={20} color="white" />
</TouchableOpacity>
<Text>{quantity}</Text>
<TouchableOpacity onPress={() => handleQuantityChange(1)}>
  <Plus size={20} color="white" />
</TouchableOpacity>
```

#### 2.5 Context Actions
```typescript
const { user, toggleFavorite, addToCart } = useAuth();

const handleAddToCart = async () => {
  try {
    await addToCart(
      stringId,
      equipment.type,
      equipment.type === 'ASSET' ? rentalDays : quantity
    );
    Alert.alert('Success', 'Added to cart');
  } catch (error) {
    Alert.alert('Error', error.message);
  }
};
```

#### 2.6 Conditional Modal Display
```typescript
<Modal visible={showStartPicker}>
  <DateTimePicker
    value={tempDate}
    mode="date"
    onChange={(event, selectedDate) => {
      // Handle change
    }}
  />
</Modal>
```

### State Management
- **Local State**: `quantity`, `startDate`, `endDate`, `showStartPicker`, `isFavorite`
- **Global State**: `user` (for auth check), `addToCart`, `toggleFavorite`
- **Effects**: Load equipment on component mount

### Key Takeaways
✓ Handles multiple item types (Product/Asset)
✓ Complex form state management
✓ Date picker for rental selection
✓ Error recovery with fallback loading
✓ Integration with global state for cart/favorites

---

## 3. Shopping Cart Screen (`app/cart.tsx`)

### Screen Purpose
Display items in cart, manage quantities, and proceed to checkout.

### Design Patterns Used

#### 3.1 Global State Access
```typescript
const { cartItems, updateQuantity, removeFromCart, clearCart } = useAuth();
```
- All cart data from context
- Centralized cart operations

#### 3.2 Memoized Calculation
```typescript
const totalPrice = cartItems.reduce(
  (sum, item) => sum + (item.price || 0) * item.quantity,
  0,
);
```
- Calculate total price
- Efficient with reduce

#### 3.3 Conditional Rendering (Empty State)
```typescript
if (cartItems.length === 0) {
  return (
    <View className="flex-1 bg-[#1a1a1a]">
      <View className="flex-1 justify-center items-center p-6">
        <ShoppingBag size={40} color="#4b5563" />
        <Text className="text-xl text-white font-bold mb-2">Giỏ hàng trống</Text>
        <TouchableOpacity
          onPress={() => router.push("/(tabs)" as any)}
          className="bg-[#FF8C42] px-8 py-4 rounded-full"
        >
          <Text className="text-black font-bold">Start Shopping</Text>
        </TouchableOpacity>
      </View>
    </View>
  );
}
```
- Empty state with icon, message, CTA
- Encourage user action

#### 3.4 List Item Operations
```typescript
const handleQuantityChange = async (itemId: string, amount: number) => {
  await updateQuantity(itemId, amount);
};

const handleRemoveItem = async (itemId: string) => {
  await removeFromCart(itemId);
};
```
- Update quantity via context
- Remove items via context

#### 3.5 Navigation to Checkout
```typescript
const handleCheckout = () => {
  if (cartItems.length === 0) return;
  router.push("/checkout" as any);
};
```

### State Management
- **Local State**: None (all from context)
- **Global State**: `cartItems`, `updateQuantity`, `removeFromCart`, `clearCart`
- **Calculations**: Total price with reduce

### Key Takeaways
✓ Simplified with context-based state
✓ Clear empty state UX
✓ Efficient quantity/removal operations
✓ Smooth navigation flow to checkout

---

## 4. Checkout Screen (`app/checkout.tsx`)

### Screen Purpose
Handle order information collection and payment processing.

### Design Patterns Used

#### 4.1 Complex Form State Management
```typescript
const [paymentMethod, setPaymentMethod] = useState<"COD" | "MoMo">("COD");
const [shippingAddress, setShippingAddress] = useState("");
const [phone, setPhone] = useState("");
const [selectedProvince, setSelectedProvince] = useState("");
const [selectedDistrict, setSelectedDistrict] = useState("");
```
- Multiple form fields
- Type-safe form state

#### 4.2 Route Parameter Parsing
```typescript
const params = useLocalSearchParams();
const checkoutData = useMemo<CheckoutData | null>(() => {
  if (params.data && typeof params.data === "string") {
    try {
      return JSON.parse(params.data);
    } catch (e) {
      console.error("Failed to parse checkout data:", e);
      return null;
    }
  }
  return null;
}, [params.data]);
```
- Support direct checkout flow
- Parse complex route data
- Error handling for malformed data

#### 4.3 Memoized Price Calculation
```typescript
const totalPrice = useMemo(() => {
  if (checkoutData) {
    return checkoutData.total;
  }
  
  const itemsTotal = checkoutItems.reduce(
    (sum, item) => sum + item.price * item.quantity,
    0,
  );
  
  return itemsTotal + shippingFee;
}, [checkoutData, checkoutItems, shippingFee]);
```
- Recalculate only when dependencies change
- Support both direct checkout and cart checkout

#### 4.4 Conditional Checkout Type
```typescript
const isDirectCheckout = checkoutData !== null;
const checkoutItems = checkoutData?.items || cartItems;
const checkoutType = checkoutData?.type; // 'BUY_NOW' | 'RENT_NOW'
```
- Support multiple checkout flows
- Differentiate between product purchase and rental

#### 4.5 Form Submission with Validation
```typescript
const handleSubmit = async () => {
  // Validation
  if (!shippingAddress.trim()) {
    Alert.alert("Error", "Please enter shipping address");
    return;
  }
  if (!phone.trim()) {
    Alert.alert("Error", "Please enter phone number");
    return;
  }
  
  try {
    setIsSubmitting(true);
    
    // Create order
    const order = await orderApi.createOrder({
      items: checkoutItems,
      shippingAddress,
      phone,
      type: isDirectCheckout ? checkoutType : 'BUY_NOW'
    });
    
    // Initiate payment if needed
    if (paymentMethod === 'MoMo') {
      const payment = await paymentApi.initiatePayment({
        orderId: order.orderId,
        amount: totalPrice,
        method: 'MoMo'
      });
      Linking.openURL(payment.redirectUrl);
    } else {
      // COD - redirect to success
      router.push('/payment-success' as any);
    }
  } catch (error) {
    Alert.alert('Error', error.message);
  } finally {
    setIsSubmitting(false);
  }
};
```

### State Management
- **Local State**: Form inputs, submission state
- **Global State**: `cartItems`, `user`, `token`, `clearCart`, `loadOrders`
- **Route Params**: Optional checkout data
- **Side Effects**: Form validation, API calls

### Key Takeaways
✓ Flexible checkout flow (cart or direct)
✓ Form validation with user feedback
✓ Multiple payment methods
✓ Error recovery with try-catch
✓ Dynamic price calculation

---

## 5. Profile Screen (`app/(tabs)/profile.tsx`)

### Screen Purpose
Display user information, favorites, settings, and profile options.

### Design Patterns Used

#### 5.1 Route Protection with Custom Hook
```typescript
const { requiresAuth } = useAuthGuard();

useEffect(() => {
  requiresAuth();
}, []);
```
- Ensures only authenticated users access
- Redirects to login if needed

#### 5.2 Global State Access
```typescript
const { user, logout, favorites, updateAvatar } = useAuth();
```
- User data from context
- Logout action
- Avatar update
- Favorites list

#### 5.3 Image Picker Integration
```typescript
const pickImage = async () => {
  const permissionResult = await ImagePicker.requestMediaLibraryPermissionsAsync();
  
  if (permissionResult.granted === false) {
    Alert.alert("Permission required", "Allow access to photos");
    return;
  }
  
  const result = await ImagePicker.launchImageLibraryAsync({
    mediaTypes: ImagePicker.MediaTypeOptions.Images,
    allowsEditing: true,
    aspect: [1, 1],
    quality: 0.7,
  });
  
  if (!result.canceled) {
    const imageUri = result.assets[0].uri;
    try {
      await updateAvatar(imageUri);
      Alert.alert("Success", "Avatar updated");
    } catch (error) {
      Alert.alert("Error", error.message);
    }
  }
};
```
- Request permissions
- Launch image picker
- Handle selection
- Upload via context

#### 5.4 Menu Navigation
```typescript
const menuItems = [
  { label: 'My Equipment', icon: Heart, href: '/profile/my-equipment' },
  { label: 'Favorites', icon: Star, href: '/profile/favorites' },
  { label: 'Settings', icon: Settings, href: '/profile/settings' },
  { label: 'Change Password', icon: Lock, href: '/profile/change-password' },
];

<ScrollView>
  {menuItems.map(item => (
    <Link href={item.href}>
      <View className="flex-row items-center p-4 border-b border-gray-800">
        <item.icon size={20} color="#FF8C42" />
        <Text className="ml-4 text-white">{item.label}</Text>
        <ChevronRight className="ml-auto" />
      </View>
    </Link>
  ))}
</ScrollView>
```

#### 5.5 Logout Flow
```typescript
const handleLogout = () => {
  Alert.alert('Logout', 'Are you sure?', [
    { text: 'Cancel', style: 'cancel' },
    {
      text: 'Logout',
      onPress: async () => {
        await logout();
        router.replace('/(auth)/login' as any);
      },
      style: 'destructive',
    },
  ]);
};
```
- Confirmation dialog
- Async logout
- Navigate to login

### State Management
- **Local State**: None
- **Global State**: `user`, `logout`, `favorites`, `updateAvatar`
- **Side Effects**: Auth guard on mount

### Key Takeaways
✓ Protected route with custom hook
✓ Image upload integration
✓ Nested navigation
✓ Logout confirmation
✓ Centered around global state

---

## 6. Login Screen (`app/(auth)/login.tsx`)

### Screen Purpose
Authenticate user with email and password.

### Design Patterns Used

#### 6.1 Controlled Form Inputs
```typescript
const [email, setEmail] = useState('');
const [password, setPassword] = useState('');
const [isSubmitting, setIsSubmitting] = useState(false);
const [error, setError] = useState<string | null>(null);

<TextInput
  value={email}
  onChangeText={setEmail}
  placeholder="Email"
  secureTextEntry={false}
/>

<TextInput
  value={password}
  onChangeText={setPassword}
  placeholder="Password"
  secureTextEntry={true}
/>
```

#### 6.2 API Integration with Error Handling
```typescript
const { login } = useAuth();
const router = useRouter();

const handleLogin = async () => {
  if (!email.trim() || !password.trim()) {
    setError("Please fill all fields");
    return;
  }
  
  try {
    setIsSubmitting(true);
    setError(null);
    
    const success = await login(email, password);
    
    if (success) {
      router.replace('/(tabs)' as any);
    }
  } catch (err: any) {
    setError(err.message || 'Login failed');
  } finally {
    setIsSubmitting(false);
  }
};
```

#### 6.3 Form Validation
```typescript
const isFormValid = email.trim().length > 0 && 
                    password.trim().length > 0;

<TouchableOpacity
  onPress={handleLogin}
  disabled={isSubmitting || !isFormValid}
  className={`py-4 rounded-full ${
    isFormValid && !isSubmitting
      ? 'bg-[#FF8C42]'
      : 'bg-gray-600'
  }`}
>
  <Text className="text-black font-bold text-center">
    {isSubmitting ? 'Logging in...' : 'Login'}
  </Text>
</TouchableOpacity>
```

#### 6.4 Navigation Link
```typescript
<Link href="/(auth)/signup">
  <Text className="text-[#FF8C42]">Don't have an account? Sign Up</Text>
</Link>
```

### State Management
- **Local State**: `email`, `password`, `isSubmitting`, `error`
- **Global State**: `login` method from context
- **Navigation**: `useRouter`

### Key Takeaways
✓ Simple form handling
✓ Input validation
✓ Error display
✓ Loading state
✓ Seamless navigation to home

---

## 7. Notifications Tab Icon (`app/(tabs)/_layout.tsx`)

### Screen Purpose
Display notification bell icon with unread count badge in bottom navigation.

### Design Patterns Used

#### 7.1 Custom Component for Icon with Badge
```typescript
function NotificationsTabIcon({ color }: { color: string }) {
  const { token } = useAuth();
  const [unreadCount, setUnreadCount] = useState(0);
  
  useEffect(() => {
    if (!token) return;

    const loadUnreadCount = async () => {
      try {
        const count = await notificationApi.getUnreadCount(token);
        setUnreadCount(typeof count === 'number' ? count : 0);
      } catch (error) {
        console.error('Failed to load unread count:', error);
      }
    };

    loadUnreadCount();

    // Refresh every 30 seconds
    const interval = setInterval(loadUnreadCount, 30000);
    return () => clearInterval(interval);
  }, [token]);

  return (
    <View style={{ position: 'relative' }}>
      <Bell size={24} color={color} />
      {unreadCount > 0 && (
        <View style={{
          position: 'absolute',
          right: -8,
          top: -4,
          backgroundColor: '#FF8C42',
          borderRadius: 8,
          minWidth: 16,
          height: 16,
          alignItems: 'center',
          justifyContent: 'center',
        }}>
          <Text style={{
            color: 'black',
            fontSize: 9,
            fontWeight: 'bold',
          }}>
            {unreadCount > 9 ? '9+' : unreadCount}
          </Text>
        </View>
      )}
    </View>
  );
}
```

#### 7.2 Real-Time Updates with Polling
```typescript
useEffect(() => {
  loadUnreadCount();
  const interval = setInterval(loadUnreadCount, 30000);
  return () => clearInterval(interval);
}, [token]);
```
- Auto-refresh every 30 seconds
- Cleanup interval on unmount
- Check token before fetching

#### 7.3 Conditional Badge Display
```typescript
{unreadCount > 0 && (
  <View style={/* badge styles */}>
    <Text>{unreadCount > 9 ? '9+' : unreadCount}</Text>
  </View>
)}
```

### State Management
- **Local State**: `unreadCount`
- **Global State**: `token` from auth context
- **Effects**: Auto-refresh polling

### Key Takeaways
✓ Dynamic component updates
✓ Real-time notification count
✓ Auto-refresh pattern
✓ Badge overflow handling (9+)
✓ Resource cleanup with interval

---

## 8. Tab Navigation Layout (`app/(tabs)/_layout.tsx`)

### Screen Purpose
Main navigation hub with bottom tabs for different app sections.

### Design Patterns Used

#### 8.1 Tab Screen Definition
```typescript
<Tabs screenOptions={{
  tabBarActiveTintColor: '#FF8C42',
  tabBarInactiveTintColor: '#6b7280',
  tabBarStyle: {
    backgroundColor: '#0a0a0a',
    borderTopColor: '#1a1a1a',
    borderTopWidth: 1,
  }
}}>
  <Tabs.Screen
    name="index"
    options={{
      title: "Shop",
      tabBarIcon: ({ color }) => <Compass size={24} color={color} />
    }}
  />
  {/* More tabs */}
</Tabs>
```

#### 8.2 Dynamic Tab Icons
```typescript
<Tabs.Screen
  name="notifications"
  options={{
    tabBarIcon: NotificationsTabIcon // Custom component
  }}
/>
```

#### 8.3 Theme Integration
```typescript
const colorScheme = useColorScheme();
const tabBarTintColor = colorScheme === 'dark' ? '#FF8C42' : '#...'
```

### State Management
- **Local State**: Tab state (managed by Expo Router)
- **Global State**: `token` for auth guard checks
- **Theme**: Dynamic color scheme

### Key Takeaways
✓ Clean tab organization
✓ Custom dynamic icons
✓ Consistent styling
✓ Easy to add new tabs

---

## Summary Across All Screens

| Screen | Primary Pattern | Secondary Patterns | Complexity |
|--------|-----------------|-------------------|-----------|
| Discovery | Search & Filter | Memoization, API calls | Medium |
| Equipment Detail | Dynamic routing | Polymorphism, Date picker | High |
| Cart | List management | Calculation, Conditional rendering | Low |
| Checkout | Form handling | Validation, Multiple flows | High |
| Profile | Route protection | Image upload, Navigation | Medium |
| Login | Form submission | Auth integration, Navigation | Low |
| Notifications | Real-time updates | Polling, Badge display | Medium |
| Tab Layout | Component composition | Dynamic icons, Theme | Low |

---

## Most Critical Patterns for Each Screen

1. **Discovery**: Efficient filtering with useMemo
2. **Equipment Detail**: Proper error handling with fallback loading
3. **Cart**: Global state management simplicity
4. **Checkout**: Complex form state + validation
5. **Profile**: Route protection + file upload
6. **Login**: Clean form handling + error feedback
7. **Notifications**: Auto-refresh polling pattern
8. **Tab Layout**: Dynamic component updates

