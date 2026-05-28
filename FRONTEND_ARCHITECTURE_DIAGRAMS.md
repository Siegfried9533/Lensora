# 📐 Frontend Architecture Diagrams - Lensora

## 1️⃣ Overall Application Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                      LENSORA FRONTEND APP                       │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │                   RootLayout                             │   │
│  │  - Splash Screen Handler                                │   │
│  │  - Font Loading                                          │   │
│  │  - Error Boundary (expo-router)                         │   │
│  └──────────────────────────────────────────────────────────┘   │
│                              ↓                                     │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │                   Theme Provider                         │   │
│  │  - useColorScheme() → Dark/Light Theme                  │   │
│  │  - DarkTheme / DefaultTheme                             │   │
│  └──────────────────────────────────────────────────────────┘   │
│                              ↓                                     │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │                   AuthProvider                           │   │
│  │  ┌────────────────────────────────────────────────────┐ │   │
│  │  │ Global State (Context)                             │ │   │
│  │  │ • User & Authentication                            │ │   │
│  │  │ • Cart Items & Cart Operations                     │ │   │
│  │  │ • Favorites & Toggle Methods                       │ │   │
│  │  │ • Orders & Order History                           │ │   │
│  │  │ • Rentals & Rental History                         │ │   │
│  │  │ • Loading & Error States                           │ │   │
│  │  └────────────────────────────────────────────────────┘ │   │
│  └──────────────────────────────────────────────────────────┘   │
│                              ↓                                     │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │         Navigation (Stack + Tabs + Groups)              │   │
│  │                                                          │   │
│  │  Stack Navigation Root                                 │   │
│  │  ├── (tabs) ← Bottom Tab Navigation                   │   │
│  │  ├── (auth) ← Auth Screens Group                      │   │
│  │  ├── modal  ← Modal Presentation                      │   │
│  │  ├── equipment/[id]   ← Dynamic Route                 │   │
│  │  ├── cart              ← Shopping Cart                 │   │
│  │  ├── checkout          ← Checkout Flow                 │   │
│  │  ├── orders/[id]       ← Order Details                │   │
│  │  └── payment-*         ← Payment Results               │   │
│  └──────────────────────────────────────────────────────────┘   │
│                              ↓                                     │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │                    Components/Screens                    │   │
│  └──────────────────────────────────────────────────────────┘   │
│                                                                   │
└─────────────────────────────────────────────────────────────────┘
```

---

## 2️⃣ Data Flow Architecture

```
                         USER INTERACTIONS
                              ↓
                    ┌──────────────────────┐
                    │    React Components  │
                    │  - Screens           │
                    │  - UI Elements       │
                    └──────────────────────┘
                              ↓
                    ┌──────────────────────┐
                    │   Custom Hooks       │
                    │  - useAuth()         │
                    │  - useAuthGuard()    │
                    │  - useColorScheme()  │
                    └──────────────────────┘
                              ↓
                    ┌──────────────────────┐
                    │  Context API         │
                    │  (AuthContext)       │
                    │  - State Management  │
                    │  - Dispatch Actions  │
                    └──────────────────────┘
                              ↓
                    ┌──────────────────────┐
                    │   API Service Layer  │
                    │  - authApi           │
                    │  - productApi        │
                    │  - cartApi           │
                    │  - orderApi          │
                    │  - etc.              │
                    └──────────────────────┘
                              ↓
                    ┌──────────────────────┐
                    │   HTTP Requests      │
                    │  (Axios / Fetch)     │
                    └──────────────────────┘
                              ↓
                    ┌──────────────────────┐
                    │   Backend API        │
                    │   (Java Spring Boot) │
                    └──────────────────────┘
                              ↓
                    ┌──────────────────────┐
                    │   Database / Cache   │
                    │   (PostgreSQL, etc)  │
                    └──────────────────────┘
                              ↓
                    ┌──────────────────────┐
                    │  Response Data       │
                    └──────────────────────┘
                              ↓
        (Update Context State) ← (Parse Response)
                              ↓
                    ┌──────────────────────┐
                    │ Re-render Components │
                    │ (Update UI)          │
                    └──────────────────────┘
```

---

## 3️⃣ AuthContext State Management

```
AuthContext
│
├─ Authentication
│  ├─ user: User | null
│  ├─ token: string | null
│  ├─ isLoading: boolean
│  ├─ error: string | null
│  ├─ login(email, password)
│  ├─ signup(email, password, userName)
│  └─ logout()
│
├─ Cart Management
│  ├─ cartItems: CartContextItem[]
│  ├─ addToCart(id, type, quantity?)
│  ├─ removeFromCart(cartItemId)
│  ├─ updateQuantity(cartItemId, amount)
│  ├─ clearCart()
│  └─ loadCart()
│
├─ Favorites Management
│  ├─ favorites: string[]
│  ├─ favoriteItems: FavoriteItem[]
│  ├─ toggleFavorite(id, type)
│  └─ loadFavorites()
│
├─ Orders Management
│  ├─ orders: Order[]
│  └─ loadOrders()
│
└─ Rentals Management
   ├─ rentals: Rental[]
   └─ loadRentals()
```

---

## 4️⃣ Navigation Structure

```
App
│
└── RootLayout (Stack)
    │
    ├── (tabs) - Bottom Tab Navigation
    │   ├── index.tsx             → Discovery/Shop Screen
    │   ├── chatbot.tsx           → Chatbot Screen
    │   ├── notifications.tsx     → Notifications Screen
    │   ├── profile.tsx           → User Profile Screen
    │   └── transactions.tsx      → Transactions Screen
    │
    ├── (auth) - Auth Group
    │   ├── login.tsx             → Login Screen
    │   └── signup.tsx            → Sign Up Screen
    │
    ├── equipment/[id].tsx        → Product/Asset Details
    ├── cart.tsx                  → Shopping Cart
    ├── checkout.tsx              → Checkout Screen
    ├── orders/[id].tsx           → Order Details
    ├── payment-success.tsx       → Payment Success
    ├── payment-failed.tsx        → Payment Failed
    ├── notifications.tsx         → Notifications List
    ├── verify-email.tsx          → Email Verification
    ├── oauth-success.tsx         → OAuth Success
    └── modal.tsx                 → Modal Screen (if needed)
```

---

## 5️⃣ API Service Layer Architecture

```
services/api/
│
├── config.ts
│   └── API base configuration
│
├── authApi.ts
│   ├── login(email, password)
│   ├── signup(email, password, userName)
│   ├── logout()
│   └── verifyEmail(token)
│
├── productApi.ts
│   ├── getAllProducts(page, size)
│   ├── getProductById(id)
│   ├── getCategoriesByType(type)
│   └── searchProducts(query)
│
├── assetApi.ts
│   ├── getAllAssets(page, size)
│   ├── getAssetById(id)
│   └── getAssetsByCategory(category)
│
├── cartApi.ts
│   ├── addToCart(itemId, type, quantity)
│   ├── removeFromCart(cartItemId)
│   ├── updateQuantity(cartItemId, quantity)
│   ├── getCartItems()
│   └── clearCart()
│
├── favoriteApi.ts
│   ├── toggleFavorite(itemId, type)
│   ├── getFavorites()
│   └── removeFavorite(favoriteId)
│
├── orderApi.ts
│   ├── createOrder(orderData)
│   ├── getOrders()
│   ├── getOrderById(id)
│   ├── cancelOrder(id)
│   └── getRentals()
│
├── paymentApi.ts
│   ├── initiatePayment(orderData)
│   ├── verifyPayment(transactionId)
│   └── getPaymentHistory()
│
├── notificationApi.ts
│   ├── getNotifications()
│   ├── getUnreadCount()
│   ├── markAsRead(notificationId)
│   └── deleteNotification(id)
│
├── chatbotApi.ts
│   ├── sendMessage(message)
│   ├── getConversation()
│   └── clearChat()
│
└── rentalApi.ts
    ├── getRentals()
    ├── createRental(rentalData)
    ├── cancelRental(id)
    └── extendRental(id, days)
```

---

## 6️⃣ Component Hierarchy

```
RootLayout
├── ErrorBoundary
├── ThemeProvider
│   └── AuthProvider
│       └── Stack Navigation
│           ├── (tabs)
│           │   ├── TabLayout
│           │   │   └── NotificationsTabIcon (with badge)
│           │   ├── DiscoveryScreen
│           │   │   ├── SearchInput
│           │   │   ├── CategoryFilter
│           │   │   ├── ShopModeToggle
│           │   │   └── ItemList
│           │   ├── ChatbotScreen
│           │   ├── NotificationsScreen
│           │   ├── ProfileScreen
│           │   │   ├── AvatarUpload
│           │   │   ├── UserInfo
│           │   │   ├── MenuList
│           │   │   └── LogoutButton
│           │   └── TransactionsScreen
│           │
│           ├── (auth)
│           │   ├── LoginScreen
│           │   │   ├── EmailInput
│           │   │   ├── PasswordInput
│           │   │   └── LoginButton
│           │   └── SignupScreen
│           │       ├── EmailInput
│           │       ├── PasswordInput
│           │       ├── UsernameInput
│           │       └── SignupButton
│           │
│           ├── EquipmentDetailScreen
│           │   ├── ImageCarousel
│           │   ├── ProductInfo
│           │   ├── PriceDisplay
│           │   ├── RatingStars
│           │   ├── DatePicker (for rentals)
│           │   ├── QuantitySelector
│           │   ├── AddToCartButton
│           │   └── FavoriteButton
│           │
│           ├── CartScreen
│           │   ├── CartItemList
│           │   │   └── CartItem
│           │   │       ├── ItemImage
│           │   │       ├── ItemDetails
│           │   │       ├── QuantityControls
│           │   │       └── RemoveButton
│           │   ├── OrderSummary
│           │   └── CheckoutButton
│           │
│           ├── CheckoutScreen
│           │   ├── OrderReview
│           │   ├── AddressInput
│           │   ├── PhoneInput
│           │   ├── PaymentMethodSelector
│           │   ├── ShippingCalculator
│           │   └── SubmitButton
│           │
│           ├── OrderDetailsScreen
│           │   ├── OrderHeader
│           │   ├── ItemsList
│           │   ├── Timeline
│           │   └── ActionButtons
│           │
│           └── Payment Result Screens
│               ├── PaymentSuccessScreen
│               └── PaymentFailedScreen
```

---

## 7️⃣ State Management Flow (Example: Add to Cart)

```
User Action: Click "Add to Cart"
    ↓
CartScreen Component
    ↓
Call: addToCart(productId, 'PRODUCT', quantity)
    ↓
AuthContext Hook: useAuth()
    ↓
addToCart Function:
    ├─ setLoading(true)
    ├─ Call: cartApi.addToCart(...)
    │   ├─ Make API request
    │   ├─ Parse response
    │   └─ Return cartItem
    ├─ Update: setCartItems([...old, new])
    ├─ setError(null)
    └─ setLoading(false)
    ↓
Components subscribing to cart:
    ├─ CartScreen → Update display
    ├─ TabLayout → Update cart count badge
    └─ Others → Re-render as needed
    ↓
persist to AsyncStorage (auto in useEffect)
    ↓
UI Updates with new state
```

---

## 8️⃣ Authentication Flow

```
Start
│
├─→ Check AsyncStorage for stored token
│   │
│   ├─ Token exists? → Restore user & set authenticated
│   │
│   └─ No token? → Go to LoginScreen
│
├─→ LoginScreen
│   ├─ User enters email/password
│   ├─ Call: authApi.login(email, pass)
│   ├─ Response: { token, user }
│   ├─ Save to context state
│   ├─ Save to AsyncStorage
│   └─ Navigate to home
│
├─→ SignupScreen
│   ├─ User enters credentials
│   ├─ Call: authApi.signup(...)
│   ├─ Response: { token, user }
│   ├─ Auto login + navigate
│   └─ Send verification email
│
├─→ AuthContext useEffect
│   ├─ Listen to token changes
│   ├─ If token → Set as authenticated
│   ├─ If !token → Redirect to login
│   └─ Handle auth errors
│
└─→ useAuthGuard Hook
    ├─ Check if user authenticated
    ├─ If not → Show alert
    ├─ If yes → Allow access
    └─ Used in: Profile, Orders, Rentals
```

---

## 9️⃣ Performance Optimization Patterns

```
Component Render Path
│
├─ Props Change?
│   └─ useMemo → Compare dependencies
│       ├─ If deps same → Return cached value
│       ├─ If deps changed → Recalculate
│       └─ Prevents expensive re-renders
│
├─ State Updates?
│   └─ useEffect → Handle side effects
│       ├─ Fetch data on mount
│       ├─ Cleanup on unmount
│       └─ Dependency tracking
│
├─ List Rendering?
│   └─ FlatList / ScrollView (React Native)
│       ├─ Lazy load items
│       ├─ Virtual scrolling
│       └─ Memory efficient
│
└─ API Calls?
    └─ Service Layer Pattern
        ├─ Centralized
        ├─ Easy to cache
        └─ Easy to mock
```

---

## 🔟 Error Handling Architecture

```
Try-Catch Block
    │
    ├─ Success Path
    │  ├─ Parse response
    │  ├─ Update state
    │  ├─ Clear errors
    │  └─ Return data
    │
    └─ Error Path
       ├─ Log error
       ├─ Set error message
       ├─ Show to user
       │  ├─ Alert dialog
       │  ├─ Toast notification
       │  ├─ Error message on screen
       │  └─ Retry button
       └─ Handle gracefully
           ├─ Fallback UI
           ├─ Default values
           └─ User guidance
```

---

## 1️⃣1️⃣ Styling Architecture

```
Styling System
│
├─ Tailwind CSS + NativeWind
│   ├─ Utility classes
│   ├─ Responsive design
│   └─ Dark mode support
│
├─ Theme System
│   ├─ Colors constant
│   ├─ useColorScheme hook
│   └─ Dynamic theming
│
├─ Component Styles
│   ├─ className="..."
│   ├─ style={{ }}
│   └─ Combination of both
│
└─ Icon System
    ├─ Lucide React Native
    ├─ Expo Vector Icons
    └─ Custom SVGs
```

---

## 1️⃣2️⃣ Data Persistence

```
App Lifecycle
│
├─ App Start
│   ├─ Check AsyncStorage
│   ├─ TOKEN_KEY → Restore token
│   ├─ USER_KEY → Restore user
│   └─ Restore auth state
│
├─ User Action (Auth)
│   ├─ Save token
│   ├─ Save user
│   └─ Auto-update on changes
│
├─ App Pause/Resume
│   ├─ Preserve state
│   ├─ Re-validate token
│   └─ Refresh data if needed
│
└─ App Close
    └─ Data persists in AsyncStorage
```

---

## Pattern Relationship Matrix

```
                    | Navigation | State | Components | API  | Styling
────────────────────┼────────────┼───────┼────────────┼──────┼─────────
Routing             |     ✓      |       |            |      |
Context API         |            |   ✓   |      ✓     |      |
Hooks               |            |   ✓   |      ✓     |      |
Custom Hooks        |     ✓      |   ✓   |      ✓     |      |
Components          |            |       |      ✓     |      |    ✓
API Services        |            |       |            |  ✓   |
Error Handling      |            |   ✓   |      ✓     |  ✓   |
Performance Opt     |            |   ✓   |      ✓     |      |
Styling             |            |       |            |      |    ✓
```

---

## 📌 Key Takeaways

1. **Modular Architecture**: Services, Hooks, Context separation
2. **Data Centralization**: All global state in AuthContext
3. **Component Composition**: Small, reusable components
4. **Performant**: Memoization, lazy loading, efficient renders
5. **Type Safe**: Full TypeScript coverage
6. **User Friendly**: Proper loading, error, empty states
7. **Maintainable**: Clear patterns and organization
8. **Scalable**: Easy to add new features without refactoring
