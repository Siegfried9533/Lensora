# 📊 Frontend Design Patterns - Feature Matrix & Comparison

## Pattern Usage Matrix by Screen

```
┌────────────────────────┬─────────────────────────────────────────────────────────┐
│ Screen                 │ Patterns Used                                           │
├────────────────────────┼─────────────────────────────────────────────────────────┤
│ Discovery/Shop         │ • Search & Filter                                       │
│ (index.tsx)            │ • useMemo (filtering)                                   │
│                        │ • useEffect (load data)                                 │
│                        │ • useState (local state)                                │
│                        │ • useAuth (cart/favorites)                              │
│                        │ • useRouter (navigation)                                │
│                        │ • Loading/Error states                                  │
│                        │ • Conditional rendering (empty state)                   │
├────────────────────────┼─────────────────────────────────────────────────────────┤
│ Equipment Detail       │ • Dynamic routing ([id])                                │
│ (equipment/[id].tsx)   │ • API service (fetch product/asset)                     │
│                        │ • Date picker (rental dates)                            │
│                        │ • Quantity selector                                     │
│                        │ • useState (local form state)                           │
│                        │ • useAuth (addToCart, toggleFavorite)                  │
│                        │ • Loading/Error states                                  │
│                        │ • Alert dialogs                                         │
├────────────────────────┼─────────────────────────────────────────────────────────┤
│ Cart                   │ • State from Context (cartItems)                        │
│ (cart.tsx)             │ • Quantity controls (+/- buttons)                       │
│                        │ • Item removal                                          │
│                        │ • useMemo (total calculation)                           │
│                        │ • Conditional rendering (empty state)                   │
│                        │ • useRouter (checkout navigation)                       │
│                        │ • Update/Remove operations                              │
├────────────────────────┼─────────────────────────────────────────────────────────┤
│ Checkout               │ • Form handling (multi-field)                           │
│ (checkout.tsx)         │ • Payment method selector (COD/MoMo)                    │
│                        │ • Address & Phone input                                 │
│                        │ • useMemo (price calculation)                           │
│                        │ • useLocalSearchParams (checkout data)                  │
│                        │ • useAuth (cartItems, user data)                        │
│                        │ • API calls (create order, payment)                     │
│                        │ • Loading/Error handling                                │
│                        │ • Navigation on success                                 │
├────────────────────────┼─────────────────────────────────────────────────────────┤
│ Order Details          │ • Dynamic routing ([id])                                │
│ (orders/[id].tsx)      │ • useAuthGuard (protected route)                        │
│                        │ • API service (fetch order)                             │
│                        │ • Order status display                                  │
│                        │ • Timeline/status steps                                 │
│                        │ • Loading/Error states                                  │
│                        │ • Action buttons (cancel, return, etc)                  │
├────────────────────────┼─────────────────────────────────────────────────────────┤
│ Profile                │ • useAuthGuard (protected)                              │
│ ((tabs)/profile.tsx)   │ • Avatar upload (ImagePicker)                           │
│                        │ • Menu items (nested navigation)                        │
│                        │ • useAuth (user data, logout)                           │
│                        │ • Link components (sub-routes)                          │
│                        │ • AsyncStorage (persistence)                            │
│                        │ • API call (updateAvatar)                               │
├────────────────────────┼─────────────────────────────────────────────────────────┤
│ Login                  │ • Form handling (email, password)                       │
│ ((auth)/login.tsx)     │ • Controlled inputs (useState)                          │
│                        │ • API call (authApi.login)                              │
│                        │ • Error handling & display                              │
│                        │ • Loading state (button disabled)                       │
│                        │ • AsyncStorage (save token)                             │
│                        │ • Context update (setUser, setToken)                    │
│                        │ • Navigation (on success)                               │
├────────────────────────┼─────────────────────────────────────────────────────────┤
│ Sign Up                │ • Form handling (email, password, username)             │
│ ((auth)/signup.tsx)    │ • Validation logic                                      │
│                        │ • API call (authApi.signup)                             │
│                        │ • Password strength indicator                           │
│                        │ • Error/Success messages                                │
│                        │ • Auto-login after signup                               │
│                        │ • Email verification flow                               │
├────────────────────────┼─────────────────────────────────────────────────────────┤
│ Notifications          │ • useEffect (real-time updates)                         │
│ (Tab icon & screen)    │ • useAuth (token, notifications)                        │
│                        │ • Badge display (unread count)                          │
│                        │ • setInterval (30s refresh)                             │
│                        │ • API call (getUnreadCount)                             │
│                        │ • Dynamic component (NotificationsTabIcon)              │
├────────────────────────┼─────────────────────────────────────────────────────────┤
│ Payment Success        │ • Navigation confirmation                               │
│ (payment-success.tsx)  │ • useAuth (loadOrders, loadRentals)                     │
│                        │ • Success state display                                 │
│                        │ • Buttons (view order, continue shopping)               │
├────────────────────────┼─────────────────────────────────────────────────────────┤
│ Payment Failed         │ • Error display                                         │
│ (payment-failed.tsx)   │ • Retry button                                          │
│                        │ • Return to checkout                                    │
│                        │ • Error message details                                 │
└────────────────────────┴─────────────────────────────────────────────────────────┘
```

---

## Pattern Frequency Analysis

### Most Used Patterns (Ranked)

| Rank | Pattern | Frequency | Importance | Complexity |
|------|---------|-----------|------------|-----------|
| 1 | **useState** | ████████████████████ 95% | ★★★★★ | ★★☆☆☆ |
| 2 | **useEffect** | ██████████████████ 85% | ★★★★★ | ★★★☆☆ |
| 3 | **useAuth (Context)** | ████████████████ 80% | ★★★★★ | ★★★☆☆ |
| 4 | **Conditional Rendering** | ████████████████ 80% | ★★★★★ | ★★☆☆☆ |
| 5 | **Try-Catch (Error Handling)** | ██████████████ 70% | ★★★★☆ | ★★☆☆☆ |
| 6 | **useRouter (Navigation)** | ██████████████ 70% | ★★★★☆ | ★★☆☆☆ |
| 7 | **useMemo** | ████████████ 60% | ★★★☆☆ | ★★★☆☆ |
| 8 | **API Services** | ████████████ 60% | ★★★★★ | ★★★★☆ |
| 9 | **useAuthGuard** | ██████████ 50% | ★★★★☆ | ★★★☆☆ |
| 10 | **Dynamic Routes** | ████████ 40% | ★★★☆☆ | ★★★☆☆ |
| 11 | **Date Picker** | ██████ 30% | ★★☆☆☆ | ★★☆☆☆ |
| 12 | **AsyncStorage** | ██████ 30% | ★★★★☆ | ★★☆☆☆ |
| 13 | **Form Handling** | ██████ 30% | ★★★☆☆ | ★★★☆☆ |
| 14 | **Component Composition** | ██████ 30% | ★★★★☆ | ★★★☆☆ |
| 15 | **Search & Filter** | ████ 20% | ★★★☆☆ | ★★★☆☆ |

Legend:
- **Frequency**: How often the pattern is used across screens
- **Importance**: How critical the pattern is to app functionality (★★★★★ = critical)
- **Complexity**: How difficult it is to understand and implement (★★★★★ = very complex)

---

## Pattern Combinations (Most Common)

### Combination 1: Data Fetching (Found in 80% of screens)
```typescript
useEffect(() => {            // ← useEffect
  const fetchData = async () => {
    try {                    // ← Try-Catch
      setLoading(true);      // ← useState
      const data = await api.fetch();
      setData(data);
    } catch (error) {
      setError(error.message);
    } finally {
      setLoading(false);
    }
  };
  
  fetchData();
}, [dependency]);            // ← Dependency tracking
```

### Combination 2: Form with Context Update (Found in 60% of screens)
```typescript
const [email, setEmail] = useState('');        // ← useState
const { login } = useAuth();                   // ← useAuth
const router = useRouter();                    // ← useRouter

const handleSubmit = async () => {
  try {                                        // ← Try-Catch
    await login(email, password);              // ← API call
    router.push('/(tabs)');                    // ← Navigation
  } catch (error) {
    setError(error.message);
  }
};
```

### Combination 3: List with Filtering (Found in 40% of screens)
```typescript
const [items, setItems] = useState([]);        // ← useState
const [filter, setFilter] = useState('');      // ← useState
const { user } = useAuth();                    // ← useAuth

const filteredItems = useMemo(() => {          // ← useMemo
  return items.filter(item => 
    item.name.includes(filter)
  );
}, [items, filter]);

useEffect(() => {                              // ← useEffect
  loadItems();
}, []);
```

---

## Anti-Patterns to Avoid

```
❌ AVOID: Prop Drilling
   Instead: Use Context API → useAuth()

❌ AVOID: API calls in useEffect without dependencies
   Instead: Include proper dependency array

❌ AVOID: Direct state mutation
   Instead: Create new state object

❌ AVOID: Complex component without splitting
   Instead: Break into smaller components

❌ AVOID: Unhandled errors
   Instead: Try-catch + user feedback

❌ AVOID: Magic numbers/strings
   Instead: Use constants

❌ AVOID: Inline functions in render
   Instead: useCallback or extract function

❌ AVOID: Not cleaning up effects
   Instead: Return cleanup function from useEffect
```

---

## Pattern Maturity Levels

### Level 1: Basic (Entry-level concepts)
- ✓ useState
- ✓ useEffect
- ✓ Conditional rendering
- ✓ Event handlers
- ✓ Props passing

### Level 2: Intermediate (Most of Lensora)
- ✓ Context API
- ✓ Custom hooks
- ✓ useRouter
- ✓ Try-catch error handling
- ✓ useMemo
- ✓ API service layer
- ✓ Async/await

### Level 3: Advanced (Future optimization)
- ○ Higher-order components
- ○ Render props
- ○ React Query / SWR
- ○ State machines
- ○ Advanced TypeScript generics
- ○ Performance monitoring
- ○ Web workers
- ○ Suspense

---

## Dependencies Between Patterns

```
┌─────────────────────────────────────────┐
│     FOUNDATIONAL PATTERNS               │
│  (Required before other patterns)       │
│                                         │
│  • React Basics                         │
│  • TypeScript Types                     │
│  • Component Structure                  │
│  • Props & State (useState)             │
└─────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────┐
│    INTERMEDIATE PATTERNS                │
│  (Built on foundational)                │
│                                         │
│  • useEffect                            │
│  • Conditional Rendering                │
│  • Try-Catch Error Handling             │
│  • Custom Hooks                         │
└─────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────┐
│    ADVANCED PATTERNS                    │
│  (Combining intermediate)               │
│                                         │
│  • Context API                          │
│  • API Service Layer                    │
│  • useMemo optimization                 │
│  • Route Guards (useAuthGuard)          │
│  • Dynamic Routing                      │
└─────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────┐
│    APPLICATION-SPECIFIC                 │
│  (Project-specific implementations)     │
│                                         │
│  • AuthContext                          │
│  • Search & Filter                      │
│  • Cart Management                      │
│  • Checkout Flow                        │
└─────────────────────────────────────────┘
```

---

## When to Use Each Pattern

### useState
- **When**: Need to manage UI state, form inputs, toggles
- **Why**: Direct state management for component
- **Example**: Loading spinner, form input, visibility toggle
- **Avoid**: Global state, data that needs to be shared across many components

### useEffect
- **When**: Side effects, data fetching, subscriptions, cleanup
- **Why**: Handle effects after render, manage lifecycle
- **Example**: Fetch data on mount, cleanup listeners, update document title
- **Avoid**: Use with empty dependencies unless intentional

### useContext / AuthContext
- **When**: Global state that many components need
- **Why**: Avoid prop drilling, centralize state
- **Example**: User auth, cart items, app preferences
- **Avoid**: Local UI state (use useState instead)

### useMemo
- **When**: Expensive calculations, filtering large lists
- **Why**: Prevent unnecessary recalculations
- **Example**: Filter items, compute totals, sort lists
- **Avoid**: Every calculation (causes performance overhead)

### useRouter
- **When**: Programmatic navigation, conditional navigation
- **Why**: Navigate in response to events, not just links
- **Example**: After login, after checkout, conditional redirects
- **Avoid**: Use Link component for static navigation

### Custom Hooks (useAuthGuard, useColorScheme)
- **When**: Reusable logic across components
- **Why**: Extract logic, compose hooks, DRY principle
- **Example**: Auth protection, theme detection, form validation
- **Avoid**: Overly simple logic, single-use hooks

### API Services
- **When**: All API communication
- **Why**: Centralized, mockable, consistent error handling
- **Example**: productApi.getProducts(), authApi.login()
- **Avoid**: Fetch directly in components

### Try-Catch
- **When**: Async operations, API calls, risky code
- **Why**: Handle errors gracefully, prevent crashes
- **Example**: API calls, JSON parsing, file operations
- **Avoid**: Swallowing errors silently, not providing feedback

### Conditional Rendering
- **When**: Different UI based on state
- **Why**: Show appropriate content for each state
- **Example**: Loading spinner, error message, empty state
- **Avoid**: Rendering all content and hiding with CSS

---

## Testing Patterns

### Unit Test Examples

#### Testing Custom Hook
```typescript
// useAuthGuard hook test
test('redirects to login if not authenticated', () => {
  const { result } = renderHook(() => useAuthGuard());
  
  expect(result.current.isAuthenticated).toBe(false);
  // Mock router and verify redirect
});
```

#### Testing Component with Context
```typescript
// Component using useAuth
test('displays user name from context', () => {
  render(
    <AuthProvider value={mockAuthContext}>
      <ProfileScreen />
    </AuthProvider>
  );
  
  expect(screen.getByText('John Doe')).toBeTruthy();
});
```

#### Testing API Service
```typescript
// API service test
test('login success returns token', async () => {
  jest.mock('services/api/authApi');
  
  const result = await authApi.login('test@test.com', 'pass');
  
  expect(result.token).toBeDefined();
});
```

---

## Performance Optimization Checklist

- ✓ Use useMemo for expensive calculations
- ✓ Use useCallback for event handlers (future if needed)
- ✓ Implement pagination for large lists
- ✓ Lazy load images
- ✓ Cache API responses (in context)
- ✓ Avoid inline object creation in renders
- ✓ Use FlatList/ScrollView with proper keyExtractor
- ✓ Profile with React DevTools
- ✓ Minimize bundle size
- ✓ Code split routes (expo-router does this)

---

## Scalability Considerations

### Current State (Perfect for current size)
- ✓ Context API handles state well
- ✓ Service layer is scalable
- ✓ Hooks are composable
- ✓ Component structure is clean

### As App Grows (100+ screens)
- Consider: Redux, Zustand, or Mobx for complex state
- Implement: React Query for advanced caching
- Add: Error boundaries per route
- Monitor: Performance with profilers

### Enterprise Scale
- Add: Redux DevTools for debugging
- Implement: Advanced caching strategies
- Use: Type-safe API generation
- Monitor: Analytics and error tracking

---

## Summary Statistics

```
Total Design Patterns: 16+
Most Used: useState, useEffect, useAuth
Complexity Range: ★☆☆☆☆ to ★★★★☆
Best For: Mobile shopping app
Architecture Style: Component-based + Hooks + Context
Frontend Framework: React Native + Expo
State Management: Context API (fits well)
```

---

## Quick Decision Tree

```
Need to manage state?
├─ Local to component? → useState
├─ Used by many components? → Context API
└─ Side effects? → useEffect

Need to navigate?
├─ Based on user action? → useRouter
└─ Static link? → Link component

Need to fetch data?
├─ Use → API service layer
├─ Handle errors with → try-catch
└─ Show feedback with → useState (loading, error)

Need to optimize?
├─ Expensive calculation? → useMemo
├─ Passing functions down? → useCallback (future)
└─ Large lists? → Pagination

Need to protect route?
├─ Use → useAuthGuard
└─ Redirect → router.replace

Need to share logic?
├─ Multiple components? → Custom Hook
└─ Single component? → Extract function
```
