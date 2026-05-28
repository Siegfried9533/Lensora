# 🎯 Quick Reference - Design Patterns Tóm Tắt

## Danh Sách Các Design Patterns Sử Dụng

### 1. **Routing Patterns**
- 📍 **File-Based Routing** (Expo Router)
  - Location: `app/` structure
  - Example: `app/(tabs)/index.tsx` → `/(tabs)` route
  - Dynamic routes: `app/equipment/[id].tsx`

- 📍 **Route Groups**
  - `(tabs)` - Tab-based screens
  - `(auth)` - Authentication screens
  - `(modal)` - Modal screens

---

### 2. **State Management Patterns**

#### 2.1 Global State (Context API)
```
AuthContext
├── Authentication: user, token, login(), logout()
├── Cart: cartItems, addToCart(), removeFromCart()
├── Favorites: favoriteItems, toggleFavorite()
├── Orders: orders[], loadOrders()
└── Rentals: rentals[], loadRentals()
```

#### 2.2 Local State (useState)
- Form inputs (checkout.tsx)
- UI state (modals, tabs)
- Filter state (searchQuery, selectedCategory)
- Loading/error states

#### 2.3 Data Persistence
- AsyncStorage for token & user persistence
- Auto-restore on app launch

---

### 3. **Hook Patterns**

| Hook | Purpose | File |
|------|---------|------|
| `useAuth()` | Access global auth context | context/AuthContext |
| `useAuthGuard()` | Protect routes, redirect to login | hooks/useAuthGuard |
| `useColorScheme()` | Dark/light theme detection | components/useColorScheme |
| `useMemo()` | Memoize expensive calculations | Multiple screens |
| `useEffect()` | Side effects, data fetching | All screens |
| `useState()` | Local state management | All screens |
| `useRouter()` | Navigation | expo-router |
| `useLocalSearchParams()` | Route parameters | Dynamic screens |

---

### 4. **Component Patterns**

#### 4.1 Smart Components (Containers)
- Fetch data, manage state
- Example: `index.tsx`, `equipment/[id].tsx`

#### 4.2 Dumb Components (Presentational)
- Display data, handle events
- Example: `NotificationsTabIcon`, `Badge`

#### 4.3 Composition
- NotificationsTabIcon composed in TabLayout
- Reusable across app

---

### 5. **API/Service Patterns**

```typescript
services/api/
├── authApi.ts      → login(), signup(), logout()
├── productApi.ts   → getProducts(), getProductById()
├── assetApi.ts     → getAssets(), getAssetById()
├── cartApi.ts      → addItem(), removeItem()
├── favoriteApi.ts  → toggleFavorite(), getFavorites()
├── orderApi.ts     → createOrder(), getOrders()
├── paymentApi.ts   → initiatePayment()
├── chatbotApi.ts   → sendMessage()
└── config.ts       → API configuration
```

**Pattern Benefits:**
- ✅ Separation of concerns
- ✅ Easy mocking for tests
- ✅ Centralized error handling
- ✅ Consistent API interface

---

### 6. **Navigation Patterns**

#### 6.1 Stack Navigation
```typescript
<Stack.Screen name="(tabs)" options={{ headerShown: false }} />
<Stack.Screen name="modal" options={{ presentation: "modal" }} />
<Stack.Screen name="equipment/[id]" options={{ animation: "slide_from_right" }} />
```

#### 6.2 Tab Navigation
```typescript
<Tabs>
  <Tabs.Screen name="index" />      // Shop/Discovery
  <Tabs.Screen name="chatbot" />    // Chatbot
  <Tabs.Screen name="notifications" /> // Notifications
  <Tabs.Screen name="profile" />    // Profile
</Tabs>
```

#### 6.3 Dynamic Navigation
```typescript
router.push(`/equipment/${productId}`);
router.push("/checkout");
router.replace("/(auth)/login");
```

---

### 7. **Data Flow Patterns**

#### 7.1 Fetch → Loading → Error → Display
```typescript
const [loading, setLoading] = useState(true);
const [error, setError] = useState(null);
const [data, setData] = useState([]);

useEffect(() => {
  loadData();
}, []);

const loadData = async () => {
  try {
    setLoading(true);
    const result = await api.fetch();
    setData(result);
  } catch (e) {
    setError(e.message);
  } finally {
    setLoading(false);
  }
};

if (loading) return <Spinner />;
if (error) return <ErrorView />;
if (data.length === 0) return <EmptyView />;
return <DataView data={data} />;
```

#### 7.2 Real-time Updates
```typescript
useEffect(() => {
  const interval = setInterval(async () => {
    const newData = await api.fetch();
    setData(newData);
  }, 30000); // Every 30 seconds

  return () => clearInterval(interval);
}, []);
```

---

### 8. **UI Patterns**

#### 8.1 Search & Filter
- Real-time search input
- Category dropdown
- Mode toggle (BUY/RENT)
- Memoized filtering

#### 8.2 Empty States
- Show when cartItems.length === 0
- Show when items.length === 0
- With icon, message, CTA button

#### 8.3 Error States
- Display error message
- Provide retry button
- Log for debugging

#### 8.4 Loading States
- ActivityIndicator spinner
- Skeleton screens (future)
- Disabled buttons during submission

---

### 9. **Form Patterns**

#### 9.1 Controlled Inputs
```typescript
const [email, setEmail] = useState('');
const [password, setPassword] = useState('');

<TextInput
  value={email}
  onChangeText={setEmail}
/>
```

#### 9.2 Multi-step Forms (Checkout)
```typescript
// Step 1: Cart review
// Step 2: Shipping address
// Step 3: Payment method
// Step 4: Order confirmation
```

#### 9.3 Date Pickers
```typescript
<DateTimePicker
  value={date}
  mode="date"
  onChange={handleDateChange}
/>
```

---

### 10. **Authentication Patterns**

#### 10.1 Protected Routes
```typescript
// Using useAuthGuard hook
const { isAuthenticated } = useAuthGuard();

useEffect(() => {
  if (!isAuthenticated) {
    router.replace('/(auth)/login');
  }
}, []);
```

#### 10.2 Auth Flow
```
Login → Token stored → Restore on app launch → Set global user
```

#### 10.3 Session Management
```typescript
const TOKEN_KEY = 'camera_shop_token';
const USER_KEY = 'camera_shop_user';

// Persist
await AsyncStorage.setItem(TOKEN_KEY, token);

// Restore
const storedToken = await AsyncStorage.getItem(TOKEN_KEY);
```

---

### 11. **Performance Patterns**

#### 11.1 Memoization
```typescript
// Memoize filtered items
const filteredItems = useMemo(() => {
  return items.filter(/* conditions */);
}, [items, searchQuery, category]);

// Memoize total calculation
const total = useMemo(() => {
  return items.reduce((sum, item) => sum + item.price, 0);
}, [items]);
```

#### 11.2 Lazy Loading
- Route segments loaded on demand
- Dynamic imports (future optimization)

---

### 12. **Error Handling Patterns**

#### 12.1 Try-Catch
```typescript
try {
  const result = await api.call();
  setData(result);
} catch (error) {
  setError(error.message || 'Something went wrong');
  Alert.alert('Error', error.message);
}
```

#### 12.2 Error Boundaries
```typescript
export { ErrorBoundary } from 'expo-router';

// Catches errors in navigation tree
```

#### 12.3 User Feedback
- Toast alerts
- Alert dialogs
- Error messages on screen

---

### 13. **Styling Patterns**

#### 13.1 Tailwind + NativeWind
```typescript
<View className="flex-1 bg-[#1a1a1a] p-6">
  <Text className="text-xl text-white font-bold">Title</Text>
</View>
```

#### 13.2 Theme Variables
```typescript
Colors = {
  light: { ... },
  dark: { ... }
}

// Usage
backgroundColor: Colors[colorScheme].background
```

#### 13.3 Icon Library
```typescript
import { Heart, ShoppingCart, Bell } from 'lucide-react-native';

<Heart size={24} color="white" />
```

---

### 14. **Testing-Friendly Patterns**

#### 14.1 Mockable Services
```typescript
// Easy to mock in tests
jest.mock('@/services/api/productApi');
productApi.getAllProducts.mockResolvedValue(mockData);
```

#### 14.2 Separated Logic
- Business logic in services
- UI in components
- State in context/hooks

#### 14.3 Type Safety
- TypeScript interfaces
- Reduced runtime errors
- Better IDE support

---

## 📊 Pattern Distribution

| Category | Count | Examples |
|----------|-------|----------|
| Navigation | 3 | Stack, Tabs, Routes |
| State Management | 3 | Context, useState, AsyncStorage |
| Hooks | 8+ | useAuth, useMemo, useEffect, etc |
| Data Flow | 4 | Fetch, Loading, Error, Display |
| Components | 3 | Smart, Dumb, Composed |
| UI/UX | 5 | Empty, Error, Loading, Forms, Search |
| Styling | 2 | Tailwind, Theme |

---

## 🔗 Cross-Pattern Relationships

```
Authentication
├── useAuthGuard() → Protected routes
├── AuthContext → Global user state
└── AsyncStorage → Session persistence

Product Discovery
├── productApi.ts → API service
├── useMemo → Filtered items
├── useEffect → Load data
└── useState → Local UI state

Shopping Flow
├── cartApi.ts → Cart operations
├── AuthContext → Store cart state
├── Checkout form → Payment handling
└── orderApi.ts → Create order

User Profile
├── useAuthGuard() → Protect screen
├── AuthContext → User data
├── AsyncStorage → Avatar persistence
└── updateAvatar() → API call
```

---

## 🎓 Learning Resource Map

### For Beginners:
1. Start with State Management (Context API)
2. Learn Navigation (File-based routing)
3. Understand Hooks (useEffect, useState)
4. Practice with Simple Components

### For Intermediate:
1. Advanced Hook patterns
2. Performance optimization (useMemo)
3. Error handling strategies
4. API layer abstraction

### For Advanced:
1. Custom hook composition
2. Higher-order components
3. Render prop pattern
4. Advanced state machines

---

## 🚀 Most Used Patterns (By Frequency)

1. **useState** - Used in almost every component
2. **useEffect** - Data fetching, side effects
3. **useAuth** (Context) - Global state access
4. **Conditional Rendering** - Loading/error/empty states
5. **Try-Catch** - Error handling
6. **useMemo** - Performance optimization
7. **File-based Routing** - Navigation
8. **Custom Hooks** - Logic extraction
9. **API Services** - Data fetching abstraction
10. **Tailwind CSS** - Styling

---

## 📝 Notes

- All patterns follow **React best practices**
- TypeScript ensures **type safety**
- Separation of concerns keeps code **maintainable**
- Context API is suitable for this app's **state complexity**
- Performance is optimized with **memoization**
- User experience enhanced with proper **loading/error states**
