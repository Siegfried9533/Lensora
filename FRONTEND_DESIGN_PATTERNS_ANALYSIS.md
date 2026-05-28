# Phân Tích Design Patterns - Frontend Lensora

## 📋 Tổng Quan
Frontend của Lensora được xây dựng với **React Native** + **Expo** + **TypeScript**, sử dụng nhiều design patterns hiện đại để quản lý state, routing, và UI.

---

## 🎯 Các Design Patterns Chính

### 1. **File-Based Routing (Expo Router)**
**Vị trí:** `app/` folder structure
**Mục đích:** Quản lý navigation dựa trên file/folder structure
**Ứng dụng:**
```
app/
├── (tabs)/          → Tab-based navigation
├── (auth)/          → Auth screens group
├── equipment/[id].tsx → Dynamic route parameter
├── cart.tsx
├── checkout.tsx
└── orders/[id].tsx
```

**Ưu điểm:**
- Routing tự động dựa trên cấu trúc file
- Hỗ trợ nested routes, dynamic segments
- Automatic linking

---

### 2. **Context API + Provider Pattern**
**File:** `context/AuthContext.tsx`
**Mục đích:** Centralized state management cho toàn app
**Trạng thái được quản lý:**
- **Authentication**: user, token, login, logout, signup
- **Cart**: cartItems, addToCart, removeFromCart, updateQuantity
- **Favorites**: favoriteItems, toggleFavorite, loadFavorites
- **Orders**: orders list, loadOrders
- **Rentals**: rentals list, loadRentals
- **Profile**: updateAvatar, changePassword

**Cấu trúc:**
```typescript
interface AuthContextType {
  user: User | null;
  token: string | null;
  isLoading: boolean;
  error: string | null;
  // ... methods
}

export function AuthProvider({ children }) {
  // State management + API calls
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
```

**Ưu điểm:**
- Tránh prop drilling
- Single source of truth
- Easy to persist/restore state

---

### 3. **Custom Hooks Pattern**
**Files:** `hooks/useAuthGuard.ts`, `components/useColorScheme.ts`

#### 3.1 **useAuthGuard Hook**
**Mục đích:** Bảo vệ các screens yêu cầu authentication
```typescript
export function useAuthGuard(): UseAuthGuardResult {
  const router = useRouter();
  const { token, user, isLoading } = useAuth();
  
  const requiresAuth = (options?: AuthGuardOptions) => {
    if (!isAuthenticated) {
      router.replace('/(auth)/login');
    }
  };
  
  return { isAuthenticated, isLoading, requiresAuth };
}
```

**Sử dụng:**
- Profile screens
- Orders screens
- Rentals screens

#### 3.2 **useColorScheme Hook**
**Mục đích:** Quản lý dark/light theme
**Sử dụng:** Tab layout, theme provider

---

### 4. **API Service Layer Pattern**
**Folder:** `services/api/`
**Files:**
- `authApi.ts` - Authentication
- `productApi.ts` - Products & Assets
- `cartApi.ts` - Cart operations
- `favoriteApi.ts` - Favorites
- `orderApi.ts` - Orders
- `paymentApi.ts` - Payment
- `notificationApi.ts` - Notifications
- `rentalApi.ts` - Rentals
- `chatbotApi.ts` - Chatbot
- `config.ts` - API configuration

**Pattern:**
```typescript
// Example: productApi
export interface Product { ... }
export interface Category { ... }

class ProductApi {
  async getAllProducts(page, size) { ... }
  async getProductById(id) { ... }
  async getCategoriesByType(type) { ... }
}
```

**Lợi ích:**
- Tách biệt logic API từ components
- Reusable API calls
- Centralized API configuration
- Easier testing & mocking

---

### 5. **Component Composition Pattern**
**Ví dụ:** `(tabs)/_layout.tsx` - NotificationsTabIcon component

```typescript
function NotificationsTabIcon({ color }: { color: string }) {
  const { token } = useAuth();
  const [unreadCount, setUnreadCount] = useState(0);

  // Fetch unread count
  // Auto-refresh every 30s
  // Render badge with count

  return (
    <View>
      <Bell size={24} color={color} />
      {unreadCount > 0 && <Badge />}
    </View>
  );
}
```

**Ưu điểm:**
- Reusable component
- Encapsulated logic
- Responsive to data changes

---

### 6. **Conditional Rendering & State Patterns**
**Ứng dụng chính:** Discovery screen, Cart screen

```typescript
// Loading state
if (loading) {
  return <ActivityIndicator />;
}

// Error state
if (error) {
  return <ErrorView />;
}

// Empty state
if (cartItems.length === 0) {
  return <EmptyCartView />;
}

// Normal state
return <NormalView />;
```

---

### 7. **Memoization Pattern**
**Ứng dụng:** 
- `index.tsx` - Filtered items calculation
- `checkout.tsx` - Total price calculation

```typescript
const filteredItems = useMemo(() => {
  return allItems.filter(item => {
    const matchesSearch = item.title
      .toLowerCase()
      .includes(searchQuery.toLowerCase());
    const matchesCategory = selectedCategory 
      ? item.categoryName === selectedCategory 
      : true;
    return matchesSearch && matchesCategory;
  });
}, [searchQuery, selectedCategory, allItems]);
```

**Lợi ích:**
- Avoid expensive recalculations
- Performance optimization

---

### 8. **Search & Filter Pattern**
**File:** `(tabs)/index.tsx` - Discovery Screen

```typescript
const [searchQuery, setSearchQuery] = useState('');
const [selectedCategory, setSelectedCategory] = useState(null);
const [shopMode, setShopMode] = useState('BUY' | 'RENT');

const filteredItems = useMemo(() => {
  return allItems.filter(/* conditions */);
}, [searchQuery, selectedCategory, allItems]);
```

**Features:**
- Real-time search
- Category filtering
- Shop mode toggle (BUY/RENT)

---

### 9. **Loading & Error States Pattern**
**Ứng dụng:** Hầu hết các screens

```typescript
const [loading, setLoading] = useState(true);
const [error, setError] = useState<string | null>(null);

useEffect(() => {
  loadData();
}, [dependency]);

const loadData = async () => {
  try {
    setLoading(true);
    // API calls
  } catch (e) {
    setError(e.message);
  } finally {
    setLoading(false);
  }
};
```

---

### 10. **Dynamic Form Handling**
**File:** `checkout.tsx`

```typescript
const [paymentMethod, setPaymentMethod] = useState<'COD' | 'MoMo'>('COD');
const [shippingAddress, setShippingAddress] = useState('');
const [phone, setPhone] = useState('');
const [selectedProvince, setSelectedProvince] = useState('');

const totalPrice = useMemo(() => {
  return checkoutData.total + shippingFee;
}, [checkoutData, shippingFee]);
```

---

### 11. **Date Picker Pattern**
**File:** `equipment/[id].tsx` - Rental date selection

```typescript
const [startDate, setStartDate] = useState(new Date());
const [endDate, setEndDate] = useState(new Date(Date.now() + 24*60*60*1000));
const [showStartPicker, setShowStartPicker] = useState(false);
const [showEndPicker, setShowEndPicker] = useState(false);

// DateTimePicker component integration
<DateTimePicker
  value={tempDate}
  mode="date"
  onChange={handleDateChange}
/>
```

---

### 12. **Polymorphic Component Pattern**
**Ứng dụng:** Handling Product vs Asset

```typescript
interface DisplayItem {
  id: string;
  type: 'PRODUCT' | 'ASSET';
  productName?: string;
  assetName?: string;
  // ... other fields
}

// Single item component works for both types
const getItemName = (item) => {
  return item.productName || item.assetName || "Unknown";
};
```

---

### 13. **Data Persistence Pattern**
**File:** `context/AuthContext.tsx`

```typescript
const TOKEN_KEY = 'camera_shop_token';
const USER_KEY = 'camera_shop_user';

// Persist auth data
await AsyncStorage.setItem(TOKEN_KEY, token);
await AsyncStorage.setItem(USER_KEY, JSON.stringify(user));

// Restore on mount
const loadStoredAuth = async () => {
  const storedToken = await AsyncStorage.getItem(TOKEN_KEY);
  const storedUser = await AsyncStorage.getItem(USER_KEY);
  // ...
};
```

---

### 14. **Real-Time Update Pattern**
**File:** `(tabs)/_layout.tsx` - Notification badge

```typescript
useEffect(() => {
  if (!token) return;

  const loadUnreadCount = async () => {
    const count = await notificationApi.getUnreadCount(token);
    setUnreadCount(count);
  };

  loadUnreadCount();

  // Refresh every 30 seconds
  const interval = setInterval(loadUnreadCount, 30000);
  return () => clearInterval(interval);
}, [token]);
```

**Ưu điểm:**
- Auto-refresh data
- Cleanup on unmount
- Real-time user feedback

---

### 15. **Type-Safe Navigation Pattern**
**File:** `_layout.tsx`

```typescript
<Stack.Screen
  name="equipment/[id]"
  options={{ 
    headerShown: false, 
    animation: "slide_from_right" 
  }}
/>

// Usage with type safety
router.push(`/equipment/${id}` as any);
```

---

### 16. **Tab Navigation with Badge Pattern**
**File:** `(tabs)/_layout.tsx`

```typescript
<Tabs screenOptions={{
  tabBarActiveTintColor: '#FF8C42',
  tabBarInactiveTintColor: '#6b7280',
  tabBarStyle: {
    backgroundColor: '#0a0a0a',
    borderTopColor: '#1a1a1a',
  }
}}>
  <Tabs.Screen
    name="index"
    options={{
      title: "Shop",
      tabBarIcon: ({ color }) => <Compass size={24} color={color} />
    }}
  />
  <Tabs.Screen
    name="chatbot"
    options={{
      tabBarIcon: ({ color }) => <Bot size={24} color={color} />
    }}
  />
  <Tabs.Screen
    name="notifications"
    options={{
      tabBarIcon: NotificationsTabIcon // Component with badge
    }}
  />
</Tabs>
```

---

## 📊 Design Pattern Summary Table

| Pattern | Vị Trí | Mục Đích | Ưu Điểm |
|---------|--------|---------|--------|
| File-Based Routing | `app/` | Navigation | Automatic, scalable |
| Context API | `context/` | Global state | Centralized, no prop drilling |
| Custom Hooks | `hooks/` | Logic reuse | DRY, composable |
| Service Layer | `services/api/` | API calls | Separation of concerns |
| Composition | Components | UI building | Reusable, maintainable |
| Memoization | `useMemo` | Performance | Avoid recalculations |
| State Machine | useState | Local state | Predictable |
| Error Handling | try/catch | Safety | User-friendly UX |
| Data Persistence | AsyncStorage | Offline | Better UX |
| Real-time Updates | useEffect intervals | Live data | Engagement |

---

## 🎨 Styling Architecture

### Technologies:
- **Tailwind CSS** + **NativeWind** for React Native
- **TypeScript** for type safety
- **Lucide React Native** for icons

### Theme:
- **Dark theme** by default
- Primary color: `#FF8C42` (Orange)
- Background: `#0a0a0a`, `#1a1a1a`
- Accent colors: gray-400, gray-800

---

## 🔄 Data Flow Architecture

```
Components
    ↓
Custom Hooks (useAuth, useAuthGuard)
    ↓
Context API (AuthContext)
    ↓
Service Layer (API services)
    ↓
AsyncStorage (Data persistence)
    ↓
Backend API
```

---

## 🌟 Key Features by Screen

### Home/Discovery (`(tabs)/index.tsx`)
- ✅ Search functionality
- ✅ Category filtering
- ✅ Shop mode toggle (BUY/RENT)
- ✅ Memoized filtering
- ✅ Favorite toggling

### Equipment Detail (`equipment/[id].tsx`)
- ✅ Dynamic product/asset loading
- ✅ Date picker for rentals
- ✅ Quantity selector
- ✅ Add to cart/favorites
- ✅ Error handling

### Cart (`cart.tsx`)
- ✅ Quantity management
- ✅ Item removal
- ✅ Empty state
- ✅ Total calculation
- ✅ Checkout flow

### Checkout (`checkout.tsx`)
- ✅ Payment method selection
- ✅ Address input
- ✅ Shipping fee calculation
- ✅ Order creation
- ✅ Payment redirect

### Notifications (`(tabs)/_layout.tsx`)
- ✅ Real-time unread count
- ✅ Badge display
- ✅ Auto-refresh (30s interval)
- ✅ Dynamic tab icon

### Profile (`(tabs)/profile.tsx`)
- ✅ Avatar upload
- ✅ Personal info
- ✅ Favorites list
- ✅ Settings
- ✅ Change password

---

## 💡 Observations

### Strengths:
1. ✅ Clear separation of concerns (services, hooks, context)
2. ✅ TypeScript for type safety
3. ✅ Responsive UI with TailwindCSS
4. ✅ Global state management reduces prop drilling
5. ✅ Custom hooks for authentication guards
6. ✅ Proper loading/error states
7. ✅ Data persistence with AsyncStorage

### Areas for Enhancement:
1. 🔄 Consider adding Redux/Zustand for more complex state management (if needed)
2. 🔄 Add route-level loading skeletons
3. 🔄 Implement error boundaries for better error handling
4. 🔄 Add retry logic for failed API calls
5. 🔄 Consider adding react-query for advanced caching

---

## 📚 Pattern Dependencies

```
App Root
├── RootLayout (ErrorBoundary, SplashScreen)
├── ThemeProvider (Dark/Light)
├── AuthProvider (Global state)
│   ├── useAuth() - Access context
│   ├── useAuthGuard() - Protected routes
│   └── Service APIs
│       ├── authApi
│       ├── productApi
│       ├── cartApi
│       ├── favoriteApi
│       ├── orderApi
│       └── ...
└── Navigation
    ├── Stack (Root navigation)
    ├── Tabs (Bottom navigation)
    ├── Groups ((auth), (tabs))
    └── Dynamic Routes ([id])
```

---

## 🚀 Best Practices Implemented

1. **Type Safety**: Full TypeScript coverage
2. **DRY**: Reusable hooks and services
3. **Single Responsibility**: Components focus on UI
4. **State Colocate**: Local state stays local, global state in Context
5. **Error Handling**: Try/catch with user-friendly messages
6. **Performance**: Memoization, lazy loading
7. **UX**: Loading states, empty states, error states
8. **Testability**: Separated API layer makes testing easier
