# 📋 So Sánh: TSX/React Native vs XML Android Layout

## Tổng Quan

| Khía Cạnh | TSX (React Native) | XML (Android Layout) |
|-----------|-------------------|---------------------|
| **Nền tảng** | Cross-platform (iOS/Android/Web) | Android only |
| **Ngôn ngữ** | TypeScript/JavaScript + JSX syntax | XML markup |
| **Logic** | Tích hợp sẵn trong component | Tách biệt (Java/Kotlin code) |
| **Styling** | Tailwind CSS, className, inline style | XML attributes, separate styles.xml |
| **State** | Tích hợp (useState, Context) | Không có (thay bằng ViewModel, LiveData) |
| **Reactivity** | Tự động re-render khi state thay đổi | Manual findViewById(), setText() |
| **Performance** | Optimized với React reconciliation | Direct view manipulation |
| **Learning Curve** | Medium (need JS + React knowledge) | Medium (Android-specific) |

---

## 1️⃣ Cấu Trúc File & Thư Mục

### React Native TSX

```
frontend/
├── app/
│   ├── (tabs)/
│   │   ├── index.tsx           ← One file = Screen + Logic + Styling
│   │   ├── profile.tsx
│   │   └── _layout.tsx
│   ├── equipment/
│   │   └── [id].tsx            ← Dynamic route parameter
│   ├── cart.tsx
│   └── checkout.tsx
└── components/
    ├── Button.tsx              ← Reusable component
    ├── Card.tsx
    └── Header.tsx
```

**Đặc điểm:**
- ✅ One file cho một screen/component
- ✅ Component và logic cùng một file
- ✅ Styling cùng file (className, inline styles)
- ✅ Dễ tìm và maintain

### Android XML Layout

```
android/
├── res/
│   ├── layout/
│   │   ├── activity_main.xml       ← Layout only
│   │   ├── activity_cart.xml
│   │   ├── fragment_profile.xml
│   │   └── item_product_card.xml
│   ├── values/
│   │   ├── colors.xml              ← Color constants
│   │   ├── strings.xml             ← String resources
│   │   ├── styles.xml              ← Style definitions
│   │   └── dimens.xml              ← Size constants
│   ├── values-dark/
│   │   └── colors.xml              ← Dark theme colors
│   └── drawable/
│       └── button_background.xml   ← Drawable resources
└── src/
    ├── MainActivity.kt              ← Logic (separate file)
    ├── CartActivity.kt
    └── ProfileFragment.kt
```

**Đặc điểm:**
- ❌ Layout tách biệt từ logic
- ❌ Styling tách biệt (styles.xml, colors.xml)
- ✅ Reuse được strings, colors, dimensions
- ✅ Resources quản lý tập trung

---

## 2️⃣ Ví Dụ So Sánh: Product Card

### React Native TSX Version

```typescript
// ProductCard.tsx - One file with everything

interface ProductCardProps {
  product: Product;
  onPress: () => void;
  isFavorite: boolean;
  onFavPress: () => void;
}

export function ProductCard({ 
  product, 
  onPress, 
  isFavorite, 
  onFavPress 
}: ProductCardProps) {
  // Logic included in component
  const handleFavoritePress = () => {
    onFavPress();
  };

  // Styling included as className strings
  return (
    <TouchableOpacity 
      onPress={onPress}
      className="bg-[#0a0a0a] border border-gray-800 rounded-2xl overflow-hidden mb-4"
    >
      {/* Image */}
      <Image 
        source={{ uri: product.image }}
        className="w-full h-48 bg-gray-900"
      />

      {/* Content */}
      <View className="p-4">
        <Text className="text-white font-bold text-base">
          {product.name}
        </Text>
        
        <Text className="text-gray-400 text-xs mb-3">
          {product.category}
        </Text>

        {/* Price & Favorite */}
        <View className="flex-row justify-between items-center">
          <Text className="text-[#FF8C42] font-bold text-lg">
            ${product.price}
          </Text>
          
          <TouchableOpacity onPress={handleFavoritePress}>
            <Heart 
              size={20} 
              color={isFavorite ? '#FF8C42' : '#9ca3af'}
              fill={isFavorite ? '#FF8C42' : 'none'}
            />
          </TouchableOpacity>
        </View>
      </View>
    </TouchableOpacity>
  );
}

// Usage
<ProductCard 
  product={product}
  onPress={() => navigateToDetail(product.id)}
  isFavorite={favorites.includes(product.id)}
  onFavPress={() => toggleFavorite(product.id)}
/>
```

**Đặc điểm:**
- ✅ Component tự chứa tất cả: UI, Logic, Styling
- ✅ Props-based (reactive)
- ✅ Inline styling với className
- ✅ Event handlers tích hợp
- ✅ Reusable và composable

### Android XML Version

#### Layout File (item_product_card.xml)
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout 
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:background="@color/card_background"
    android:padding="@dimen/padding_medium">

    <!-- Image -->
    <ImageView
        android:id="@+id/productImage"
        android:layout_width="match_parent"
        android:layout_height="@dimen/product_image_height"
        android:scaleType="centerCrop"
        android:contentDescription="@string/product_image" />

    <!-- Content Container -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:layout_marginTop="@dimen/padding_small">

        <!-- Product Name -->
        <TextView
            android:id="@+id/productName"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:textSize="@dimen/text_large"
            android:textColor="@color/text_primary"
            android:textStyle="bold"
            android:text="Product Name" />

        <!-- Category -->
        <TextView
            android:id="@+id/productCategory"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:textSize="@dimen/text_small"
            android:textColor="@color/text_secondary"
            android:layout_marginTop="@dimen/padding_extra_small"
            android:text="Category" />

        <!-- Price & Favorite Container -->
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:layout_marginTop="@dimen/padding_small"
            android:gravity="center_vertical">

            <!-- Price -->
            <TextView
                android:id="@+id/productPrice"
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:textSize="@dimen/text_xlarge"
                android:textColor="@color/accent_orange"
                android:textStyle="bold"
                android:text="$199" />

            <!-- Favorite Button -->
            <ImageButton
                android:id="@+id/favoriteButton"
                android:layout_width="@dimen/icon_size"
                android:layout_height="@dimen/icon_size"
                android:src="@drawable/ic_heart"
                android:contentDescription="@string/add_favorite"
                android:background="?attr/selectableItemBackgroundBorderless" />

        </LinearLayout>
    </LinearLayout>
</LinearLayout>
```

#### Style File (styles.xml)
```xml
<resources>
    <style name="ProductCardStyle">
        <item name="android:background">@color/card_background</item>
        <item name="android:padding">@dimen/padding_medium</item>
    </style>

    <style name="ProductNameStyle">
        <item name="android:textSize">@dimen/text_large</item>
        <item name="android:textColor">@color/text_primary</item>
        <item name="android:textStyle">bold</item>
    </style>
</resources>
```

#### Color File (colors.xml)
```xml
<resources>
    <color name="card_background">#0a0a0a</color>
    <color name="text_primary">#ffffff</color>
    <color name="text_secondary">#9ca3af</color>
    <color name="accent_orange">#FF8C42</color>
</resources>
```

#### Kotlin Code (ProductCardAdapter.kt)
```kotlin
class ProductCardAdapter(
    private val products: List<Product>,
    private val onProductClick: (Product) -> Unit,
    private val onFavoriteClick: (Product) -> Unit
) : RecyclerView.Adapter<ProductCardViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ProductCardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product_card, parent, false)
        return ProductCardViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductCardViewHolder, position: Int) {
        val product = products[position]
        holder.bind(product, onProductClick, onFavoriteClick)
    }

    override fun getItemCount() = products.size
}

class ProductCardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val image = itemView.findViewById<ImageView>(R.id.productImage)
    private val name = itemView.findViewById<TextView>(R.id.productName)
    private val category = itemView.findViewById<TextView>(R.id.productCategory)
    private val price = itemView.findViewById<TextView>(R.id.productPrice)
    private val favoriteButton = itemView.findViewById<ImageButton>(R.id.favoriteButton)

    fun bind(
        product: Product,
        onProductClick: (Product) -> Unit,
        onFavoriteClick: (Product) -> Unit
    ) {
        // Set data
        name.text = product.name
        category.text = product.category
        price.text = "$${product.price}"
        
        // Load image
        Glide.with(itemView.context)
            .load(product.image)
            .into(image)

        // Set click listeners
        itemView.setOnClickListener { onProductClick(product) }
        favoriteButton.setOnClickListener { onFavoriteClick(product) }
    }
}
```

**Đặc điểm:**
- ❌ Layout tách biệt từ logic (XML + Kotlin)
- ❌ Styling tách biệt (colors.xml, styles.xml)
- ✅ Manual data binding trong ViewHolder
- ✅ Manual resource management

---

## 3️⃣ So Sánh Cụ Thể

### A. Layout Structure

#### React Native (TSX)
```typescript
// Nested JSX syntax - familiar to web developers
<View className="bg-[#0a0a0a] p-4 rounded-2xl">
  <Image source={{ uri: imageUrl }} className="w-full h-48" />
  <View className="flex-row justify-between items-center">
    <Text className="text-white font-bold">Product</Text>
    <Heart size={20} color="#FF8C42" />
  </View>
</View>
```

**Ưu điểm:**
- ✅ Compact, readable
- ✅ Full programming power (conditions, loops, etc.)
- ✅ No need for separate view binding

#### Android XML
```xml
<!-- Hierarchical XML structure -->
<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:background="@color/card_background"
    android:padding="@dimen/padding_medium">
    
    <ImageView
        android:id="@+id/image"
        android:layout_width="match_parent"
        android:layout_height="@dimen/image_height" />
    
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal">
        
        <TextView android:id="@+id/name" ... />
        <ImageButton android:id="@+id/favorite" ... />
    </LinearLayout>
</LinearLayout>
```

**Ưu điểm:**
- ✅ Declarative, clear hierarchy
- ✅ Separate concerns (structure vs logic)
- ❌ Verbose, repetitive
- ❌ No programming logic allowed

---

### B. Styling

#### React Native (TSX)
```typescript
// Option 1: Tailwind Classes
<Text className="text-white font-bold text-lg">Title</Text>

// Option 2: Inline Styles
<Text style={{ color: '#FF8C42', fontWeight: 'bold', fontSize: 18 }}>
  Title
</Text>

// Option 3: Stylesheet Object
const styles = StyleSheet.create({
  title: {
    color: '#FF8C42',
    fontWeight: 'bold',
    fontSize: 18,
  },
});
<Text style={styles.title}>Title</Text>

// All in one file!
```

**Flow:**
```
className/style → Tailwind → Generated CSS → React Native → Native View
```

#### Android XML
```xml
<!-- Option 1: Inline Attributes -->
<TextView
    android:textColor="#FF8C42"
    android:textStyle="bold"
    android:textSize="18sp" />

<!-- Option 2: Reference to styles.xml -->
<TextView style="@style/TitleStyle" />

<!-- Option 3: Reference to colors.xml and dimens.xml -->
<TextView
    android:textColor="@color/accent_orange"
    android:textSize="@dimen/text_large" />

<!-- Styles defined in separate file (styles.xml) -->
```

**Flow:**
```
XML Attributes → colors.xml, dimens.xml → ResourceInflater → Android Views
```

**Comparison Table:**
| Aspek | TSX | XML |
|-------|-----|-----|
| **Flexibility** | Very flexible | Limited (static values) |
| **Code Reuse** | Component composition | Style inheritance |
| **Type Safety** | Full TypeScript support | No type checking |
| **Runtime Changes** | Easy (state-based) | Difficult (programmatic) |
| **Learning** | Web developer friendly | Android specific |

---

### C. Logic & State Management

#### React Native (TSX)
```typescript
export default function CartScreen() {
  // State management integrated
  const { cartItems, updateQuantity, removeFromCart } = useAuth();
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Logic inside component
  const totalPrice = useMemo(() => {
    return cartItems.reduce((sum, item) => sum + item.price * item.quantity, 0);
  }, [cartItems]);

  const handleCheckout = async () => {
    setIsSubmitting(true);
    try {
      await createOrder(cartItems);
      router.push('/payment-success');
    } catch (error) {
      Alert.alert('Error', error.message);
    } finally {
      setIsSubmitting(false);
    }
  };

  // Everything in one component
  return (
    <ScrollView>
      {cartItems.map(item => (
        <CartItem
          key={item.id}
          item={item}
          onRemove={() => removeFromCart(item.id)}
          onQuantityChange={(qty) => updateQuantity(item.id, qty)}
        />
      ))}
      <Text>Total: ${totalPrice}</Text>
      <Button onPress={handleCheckout} disabled={isSubmitting}>
        Checkout
      </Button>
    </ScrollView>
  );
}
```

**Đặc điểm:**
- ✅ State (useState, Context)
- ✅ Logic (computed values, handlers)
- ✅ Reactive (automatic re-render)
- ✅ Async/await support
- ✅ All in one component

#### Android (Kotlin + XML)
```kotlin
// Activity/Fragment - logic separate from XML
class CartActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCartBinding
    private lateinit var viewModel: CartViewModel
    private val cartAdapter = CartAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup ViewModel (state management)
        viewModel = ViewModelProvider(this).get(CartViewModel::class.java)

        // Observe state changes (LiveData)
        viewModel.cartItems.observe(this) { items ->
            cartAdapter.submitList(items)
            updateTotalPrice()
        }

        // Setup RecyclerView
        binding.recyclerView.adapter = cartAdapter

        // Setup button handler
        binding.checkoutButton.setOnClickListener {
            handleCheckout()
        }
    }

    private fun updateTotalPrice() {
        val total = viewModel.cartItems.value?.sumOf { 
            it.price * it.quantity 
        } ?: 0.0
        binding.totalPrice.text = "Total: $$total"
    }

    private fun handleCheckout() {
        binding.checkoutButton.isEnabled = false
        viewModel.createOrder { success ->
            if (success) {
                startActivity(Intent(this, PaymentSuccessActivity::class.java))
            } else {
                showError("Order failed")
            }
            binding.checkoutButton.isEnabled = true
        }
    }
}

// ViewModel - state management separated
class CartViewModel : ViewModel() {
    private val _cartItems = MutableLiveData<List<CartItem>>()
    val cartItems: LiveData<List<CartItem>> = _cartItems

    fun createOrder(callback: (Boolean) -> Unit) {
        // API call logic
    }
}
```

**Đặc điểm:**
- ✅ ViewModel for state (separation of concerns)
- ✅ LiveData for reactive updates
- ❌ Manual subscription management
- ❌ Manual view updates
- ❌ View binding required
- ❌ More boilerplate code

---

### D. Button Handling

#### React Native (TSX)
```typescript
// Simple, inline
<TouchableOpacity
  onPress={() => handlePress()}
  disabled={isLoading}
  className="bg-[#FF8C42] px-8 py-4 rounded-full"
>
  <Text className="text-black font-bold">
    {isLoading ? 'Loading...' : 'Submit'}
  </Text>
</TouchableOpacity>

// Or extracted
const handlePress = async () => {
  setLoading(true);
  try {
    await doSomething();
  } finally {
    setLoading(false);
  }
};
```

**Flow:**
```
Press → onPress handler → setState → Re-render
```

#### Android XML
```xml
<!-- XML (layout) -->
<Button
    android:id="@+id/submitButton"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:text="@string/submit" />
```

```kotlin
// Kotlin Code (Activity)
binding.submitButton.setOnClickListener {
    handlePress()
}

private fun handlePress() {
    binding.submitButton.isEnabled = false
    binding.submitButton.text = "Loading..."
    
    lifecycleScope.launch {
        try {
            doSomething()
        } finally {
            binding.submitButton.isEnabled = true
            binding.submitButton.text = getString(R.string.submit)
        }
    }
}
```

**Difference:**
| Aspek | TSX | XML + Kotlin |
|-------|-----|------------|
| **Location** | Same file | Separate files |
| **Text Update** | Automatic (via state) | Manual (setText) |
| **Disabled State** | Props-based | Manual (setEnabled) |
| **Re-render** | Automatic | Manual management |

---

### E. Data Binding

#### React Native (TSX)
```typescript
// Automatic - component re-renders when props/state changes
interface Props {
  product: Product;
  isFavorite: boolean;
}

export function ProductCard({ product, isFavorite }: Props) {
  return (
    <View>
      <Text>{product.name}</Text>
      <Heart 
        fill={isFavorite ? '#FF8C42' : 'none'}
        // Automatically updated when isFavorite prop changes
      />
    </View>
  );
}

// Usage
const [isFavorite, setIsFavorite] = useState(false);
<ProductCard product={product} isFavorite={isFavorite} />
// When isFavorite changes → component re-renders automatically
```

**Flow:**
```
Props Change → React Reconciliation → Component Re-render → UI Update
```

#### Android (Kotlin + XML)
```kotlin
// Manual data binding
class ProductViewHolder(itemView: View) {
    private val name = itemView.findViewById<TextView>(R.id.name)
    private val favoriteButton = itemView.findViewById<ImageButton>(R.id.favorite)

    fun bind(product: Product, isFavorite: Boolean) {
        // Manual setText
        name.text = product.name
        
        // Manual setImageResource
        favoriteButton.setImageResource(
            if (isFavorite) R.drawable.ic_heart_filled 
            else R.drawable.ic_heart_outline
        )
    }
}

// Need to call bind() again when data changes
// OR use Data Binding Library for more automatic approach
```

**Alternative: Android Data Binding**
```xml
<!-- XML with binding expressions -->
<?xml version="1.0" encoding="utf-8"?>
<layout xmlns:android="http://schemas.android.com/apk/res/android">
    <data>
        <variable
            name="product"
            type="com.example.Product" />
    </data>
    <LinearLayout ...>
        <TextView
            android:text="@{product.name}" />
    </LinearLayout>
</layout>
```

```kotlin
// Binding setup
val binding: ProductLayoutBinding = ProductLayoutBinding.inflate(layoutInflater)
binding.product = product
// But updates still need ViewModel + LiveData
```

---

## 4️⃣ Danh Sách So Sánh Chi Tiết

### Component Features

```
┌──────────────────────────┬──────────────────┬──────────────────┐
│ Feature                  │ TSX (React Native)│ XML (Android)    │
├──────────────────────────┼──────────────────┼──────────────────┤
│ File Organization        │ One file per     │ Layout + Code    │
│                          │ component        │ separate         │
├──────────────────────────┼──────────────────┼──────────────────┤
│ Styling Location         │ Same file        │ colors.xml,      │
│                          │ (className)      │ styles.xml       │
├──────────────────────────┼──────────────────┼──────────────────┤
│ Logic Integration        │ Component        │ Separate         │
│                          │ integrated       │ Activity/        │
│                          │                  │ ViewModel        │
├──────────────────────────┼──────────────────┼──────────────────┤
│ State Management         │ useState,        │ ViewModel,       │
│                          │ Context          │ LiveData         │
├──────────────────────────┼──────────────────┼──────────────────┤
│ Data Binding             │ Automatic        │ Manual/Data      │
│                          │ (props/state)    │ Binding lib      │
├──────────────────────────┼──────────────────┼──────────────────┤
│ Event Handlers           │ Inline           │ setOnClickListener│
├──────────────────────────┼──────────────────┼──────────────────┤
│ Re-render Strategy       │ Automatic        │ Manual (observe) │
├──────────────────────────┼──────────────────┼──────────────────┤
│ Async Support            │ async/await      │ lifecycleScope   │
├──────────────────────────┼──────────────────┼──────────────────┤
│ Learning Curve           │ Web dev skills   │ Android specific │
├──────────────────────────┼──────────────────┼──────────────────┤
│ Type Safety              │ Full TypeScript  │ Some Java/Kotlin │
├──────────────────────────┼──────────────────┼──────────────────┤
│ Developer Experience     │ Faster dev       │ More verbose     │
├──────────────────────────┼──────────────────┼──────────────────┤
│ Cross-platform           │ iOS/Android/Web  │ Android only     │
└──────────────────────────┴──────────────────┴──────────────────┘
```

---

## 5️⃣ View Component Mapping

### Common Components

```
React Native TSX          ↔  Android XML/Widget
─────────────────────────────────────────────────
View                      ↔  ViewGroup (Linear, Constraint, etc)
Text                      ↔  TextView
TextInput                 ↔  EditText
TouchableOpacity          ↔  Button, ImageButton, CardView
Image                     ↔  ImageView
ScrollView                ↔  ScrollView
FlatList/SectionList      ↔  RecyclerView
Modal                     ↔  Dialog, AlertDialog
ActivityIndicator         ↔  ProgressBar
Switch                    ↔  Switch, CheckBox
```

### Example Mappings

#### View Container
```typescript
// React Native TSX
<View className="flex-1 bg-[#1a1a1a] px-6 py-4">
  {/* children */}
</View>
```

```xml
<!-- Android XML -->
<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/bg_primary"
    android:paddingStart="24dp"
    android:paddingEnd="24dp"
    android:paddingTop="16dp"
    android:paddingBottom="16dp">
    <!-- children -->
</LinearLayout>
```

#### Text Component
```typescript
// React Native TSX
<Text className="text-white font-bold text-lg">
  Hello World
</Text>
```

```xml
<!-- Android XML -->
<TextView
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="Hello World"
    android:textColor="@color/text_primary"
    android:textStyle="bold"
    android:textSize="18sp" />
```

#### Input Component
```typescript
// React Native TSX
<TextInput
  placeholder="Enter text..."
  value={value}
  onChangeText={setValue}
  className="bg-[#0a0a0a] text-white p-4 rounded-2xl border border-gray-800"
/>
```

```xml
<!-- Android XML -->
<EditText
    android:id="@+id/input"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:hint="Enter text..."
    android:background="@drawable/edit_text_background"
    android:textColor="@color/text_primary"
    android:padding="16dp" />
```

```xml
<!-- edit_text_background.xml (drawable) -->
<shape xmlns:android="http://schemas.android.com/apk/res/android">
    <solid android:color="@color/card_background" />
    <stroke
        android:color="@color/border_light"
        android:width="1dp" />
    <corners android:radius="16dp" />
</shape>
```

---

## 6️⃣ Workflow Comparison

### React Native Development Flow
```
1. Write TSX component
   ├─ UI (JSX)
   ├─ Logic (handlers, state)
   └─ Styling (className/inline)

2. Component renders
   ├─ Evaluate JSX
   ├─ Apply styles
   └─ Create views

3. User interaction
   ├─ Event handler called
   ├─ State updated
   └─ Component re-renders (automatic)

4. Done!
```

### Android Development Flow
```
1. Create XML layout
   ├─ Define views
   └─ Set attributes

2. Create styles.xml
   └─ Define reusable styles

3. Create Kotlin Activity/Fragment
   ├─ Find views (findViewById)
   ├─ Setup listeners
   └─ Manage state (ViewModel)

4. Create ViewModel
   ├─ Manage state (LiveData)
   ├─ Perform operations
   └─ Emit changes

5. User interaction
   ├─ Listener called
   ├─ ViewModel updated
   ├─ LiveData changed
   └─ Observer notified
   └─ Manual UI update (setText, setImageResource, etc)

6. Done!
```

---

## 7️⃣ Code Comparison: Full Screen Example

### React Native Search Screen

```typescript
// SearchScreen.tsx - SINGLE FILE
import { useState, useMemo } from 'react';
import { View, Text, TextInput, ScrollView, TouchableOpacity } from 'react-native';
import { Search, X } from 'lucide-react-native';

interface Props {
  items: Product[];
  onItemSelect: (item: Product) => void;
}

export default function SearchScreen({ items, onItemSelect }: Props) {
  // STATE
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedCategory, setSelectedCategory] = useState<string | null>(null);

  // LOGIC
  const filteredItems = useMemo(() => {
    return items.filter(item => {
      const matchesSearch = item.name.toLowerCase().includes(searchQuery.toLowerCase());
      const matchesCategory = selectedCategory 
        ? item.category === selectedCategory 
        : true;
      return matchesSearch && matchesCategory;
    });
  }, [searchQuery, selectedCategory, items]);

  const categories = [...new Set(items.map(i => i.category))];

  // EVENT HANDLERS
  const handleClearSearch = () => setSearchQuery('');

  // RENDER
  return (
    <ScrollView className="flex-1 bg-[#1a1a1a]">
      {/* HEADER */}
      <View className="px-6 pt-6 pb-4">
        <Text className="text-3xl text-white font-bold mb-4">Search</Text>

        {/* SEARCH INPUT */}
        <View className="relative mb-4">
          <View className="absolute left-4 top-3 z-10">
            <Search size={16} color="#6b7280" />
          </View>
          <TextInput
            placeholder="Search items..."
            placeholderTextColor="#4b5563"
            value={searchQuery}
            onChangeText={setSearchQuery}
            className="pl-11 pr-11 py-3 bg-[#0a0a0a] border border-gray-800 rounded-2xl text-white"
          />
          {searchQuery && (
            <TouchableOpacity 
              className="absolute right-4 top-3"
              onPress={handleClearSearch}
            >
              <X size={16} color="#6b7280" />
            </TouchableOpacity>
          )}
        </View>

        {/* CATEGORY FILTER */}
        <ScrollView horizontal showsHorizontalScrollIndicator={false}>
          <TouchableOpacity
            className={`px-5 py-2 rounded-full mr-2 ${
              selectedCategory === null 
                ? 'bg-[#FF8C42]' 
                : 'bg-[#0a0a0a] border border-gray-800'
            }`}
            onPress={() => setSelectedCategory(null)}
          >
            <Text className={selectedCategory === null ? 'text-black font-bold' : 'text-white'}>
              All
            </Text>
          </TouchableOpacity>

          {categories.map(cat => (
            <TouchableOpacity
              key={cat}
              className={`px-5 py-2 rounded-full mr-2 ${
                selectedCategory === cat 
                  ? 'bg-[#FF8C42]' 
                  : 'bg-[#0a0a0a] border border-gray-800'
              }`}
              onPress={() => setSelectedCategory(cat)}
            >
              <Text className={selectedCategory === cat ? 'text-black font-bold' : 'text-white'}>
                {cat}
              </Text>
            </TouchableOpacity>
          ))}
        </ScrollView>
      </View>

      {/* RESULTS */}
      <View className="px-6">
        {filteredItems.map(item => (
          <TouchableOpacity
            key={item.id}
            onPress={() => onItemSelect(item)}
            className="py-4 border-b border-gray-800"
          >
            <Text className="text-white font-bold">{item.name}</Text>
            <Text className="text-gray-400 text-sm">{item.category}</Text>
          </TouchableOpacity>
        ))}
      </View>
    </ScrollView>
  );
}

// USAGE
<SearchScreen items={products} onItemSelect={handleSelect} />
```

**Stats:**
- Lines: ~120
- Files: 1
- Includes: UI + Logic + Styling + State

---

### Android XML + Kotlin Version

#### activity_search.xml - LAYOUT ONLY
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:background="@color/bg_primary">

    <!-- Header -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="24dp">

        <TextView
            android:id="@+id/headerTitle"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Search"
            android:textSize="30sp"
            android:textColor="@color/text_primary"
            android:textStyle="bold"
            android:layout_marginBottom="16dp" />

        <!-- Search Input -->
        <EditText
            android:id="@+id/searchInput"
            android:layout_width="match_parent"
            android:layout_height="48dp"
            android:hint="Search items..."
            android:background="@drawable/edit_text_background"
            android:paddingStart="40dp"
            android:paddingEnd="40dp"
            android:textColor="@color/text_primary"
            android:textColorHint="@color/text_muted" />

        <!-- Category Filter -->
        <HorizontalScrollView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="16dp"
            android:scrollbars="none">

            <LinearLayout
                android:id="@+id/categoryContainer"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:orientation="horizontal" />
        </HorizontalScrollView>
    </LinearLayout>

    <!-- Results List -->
    <RecyclerView
        android:id="@+id/resultsList"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1"
        android:paddingStart="24dp"
        android:paddingEnd="24dp" />
</LinearLayout>
```

#### item_search_result.xml - LIST ITEM LAYOUT
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:paddingVertical="16dp"
    android:paddingStart="0dp"
    android:paddingEnd="0dp"
    android:background="?attr/selectableItemBackground">

    <TextView
        android:id="@+id/itemName"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:textSize="16sp"
        android:textColor="@color/text_primary"
        android:textStyle="bold" />

    <TextView
        android:id="@+id/itemCategory"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:textSize="12sp"
        android:textColor="@color/text_secondary"
        android:layout_marginTop="4dp" />

    <View
        android:layout_width="match_parent"
        android:layout_height="1dp"
        android:background="@color/border_light"
        android:layout_marginTop="16dp" />
</LinearLayout>
```

#### colors.xml - COLORS
```xml
<resources>
    <color name="bg_primary">#1a1a1a</color>
    <color name="card_bg">#0a0a0a</color>
    <color name="text_primary">#ffffff</color>
    <color name="text_secondary">#9ca3af</color>
    <color name="text_muted">#6b7280</color>
    <color name="border_light">#374151</color>
    <color name="accent_orange">#FF8C42</color>
</resources>
```

#### dimens.xml - SIZES
```xml
<resources>
    <dimen name="padding_small">8dp</dimen>
    <dimen name="padding_medium">16dp</dimen>
    <dimen name="padding_large">24dp</dimen>
    <dimen name="text_small">12sp</dimen>
    <dimen name="text_medium">16sp</dimen>
    <dimen name="text_large">30sp</dimen>
</resources>
```

#### SearchActivity.kt - LOGIC
```kotlin
class SearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchBinding
    private lateinit var adapter: SearchResultAdapter
    private val viewModel: SearchViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup adapter
        adapter = SearchResultAdapter { item ->
            navigateToDetail(item)
        }
        binding.resultsList.adapter = adapter

        // Setup search input
        binding.searchInput.addTextChangedListener { text ->
            viewModel.updateSearchQuery(text.toString())
        }

        // Observe state
        viewModel.filteredItems.observe(this) { items ->
            adapter.submitList(items)
        }

        // Setup category filter
        viewModel.categories.observe(this) { categories ->
            setupCategoryFilters(categories)
        }
    }

    private fun setupCategoryFilters(categories: List<String>) {
        binding.categoryContainer.removeAllViews()

        // Add "All" button
        addCategoryButton("All") {
            viewModel.updateSelectedCategory(null)
        }

        // Add category buttons
        categories.forEach { category ->
            addCategoryButton(category) {
                viewModel.updateSelectedCategory(category)
            }
        }
    }

    private fun addCategoryButton(label: String, onClickListener: () -> Unit) {
        val button = MaterialButton(this).apply {
            text = label
            setOnClickListener { onClickListener() }
        }
        binding.categoryContainer.addView(button)
    }

    private fun navigateToDetail(item: Product) {
        startActivity(Intent(this, DetailActivity::class.java).apply {
            putExtra("itemId", item.id)
        })
    }
}
```

#### SearchViewModel.kt - STATE MANAGEMENT
```kotlin
class SearchViewModel(private val repository: ProductRepository) : ViewModel() {
    private val _filteredItems = MutableLiveData<List<Product>>()
    val filteredItems: LiveData<List<Product>> = _filteredItems

    private val _categories = MutableLiveData<List<String>>()
    val categories: LiveData<List<String>> = _categories

    private val _searchQuery = MutableLiveData<String>("")
    private val _selectedCategory = MutableLiveData<String?>(null)

    init {
        loadCategories()
        observeFilterChanges()
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateSelectedCategory(category: String?) {
        _selectedCategory.value = category
    }

    private fun loadCategories() {
        viewModelScope.launch {
            val cats = repository.getCategories()
            _categories.value = cats
        }
    }

    private fun observeFilterChanges() {
        combine(
            _searchQuery.asFlow(),
            _selectedCategory.asFlow()
        ) { query, category ->
            filterItems(query, category)
        }.launchIn(viewModelScope)
    }

    private suspend fun filterItems(query: String, category: String?) {
        val items = repository.getProducts()
        val filtered = items.filter { item ->
            val matchesSearch = item.name.contains(query, ignoreCase = true)
            val matchesCategory = category?.let { item.category == it } ?: true
            matchesSearch && matchesCategory
        }
        _filteredItems.postValue(filtered)
    }
}
```

#### SearchResultAdapter.kt - ADAPTER
```kotlin
class SearchResultAdapter(
    private val onItemClick: (Product) -> Unit
) : ListAdapter<Product, SearchResultAdapter.ViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_search_result, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, onItemClick)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameText = itemView.findViewById<TextView>(R.id.itemName)
        private val categoryText = itemView.findViewById<TextView>(R.id.itemCategory)

        fun bind(item: Product, onItemClick: (Product) -> Unit) {
            nameText.text = item.name
            categoryText.text = item.category
            itemView.setOnClickListener { onItemClick(item) }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Product>() {
            override fun areItemsTheSame(old: Product, new: Product) =
                old.id == new.id

            override fun areContentsTheSame(old: Product, new: Product) =
                old == new
        }
    }
}
```

**Stats:**
- Lines: ~400+
- Files: 6 (Activity, ViewModel, Adapter, Layout, Colors, Dimens)
- Includes: UI (XML) + Logic (Kotlin) + State (ViewModel) + Styling (Resources) + Adapter

---

## 8️⃣ Summary Table

```
┌────────────────────┬──────────────────────────┬─────────────────────────┐
│ Aspect             │ React Native (TSX)       │ Android (XML + Kotlin)  │
├────────────────────┼──────────────────────────┼─────────────────────────┤
│ Files Needed       │ 1                        │ 5-6+                    │
│ Lines of Code      │ 120                      │ 400+                    │
│ Setup Time         │ Fast                     │ Slow                    │
│ Readability        │ High (familiar syntax)   │ Medium (verbose)        │
│ Maintainability    │ Easy (all in one)        │ Complex (multiple files)│
│ Type Safety        │ Full TypeScript          │ Partial (Java/Kotlin)   │
│ State Management   │ Built-in (useState)      │ External (ViewModel)    │
│ Reactivity         │ Automatic                │ Manual (LiveData)       │
│ Learning Curve     │ Easy (web dev skills)    │ Medium (Android-specific)
│ Cross-platform     │ iOS/Android/Web          │ Android only            │
│ Hot Reload         │ Yes (fast)               │ Partial                 │
│ Performance        │ Good (optimized)         │ Good (native)           │
│ Developer Speed    │ Fast iteration           │ Slower iteration        │
│ Best For           │ Quick prototyping        │ Production apps         │
│                    │ Cross-platform apps      │ Complex Android UI      │
└────────────────────┴──────────────────────────┴─────────────────────────┘
```

---

## Key Takeaways

### React Native TSX ✅
**Giống:** Tính toán, logic, event handling
**Khác:** Cách tổ chức, styling, syntax

**Ưu điểm:**
- Nhanh hơn để develop
- Code ít hơn
- Reusable components
- Hot reload
- Cross-platform
- Familiar to web devs

**Nhược điểm:**
- Tương đối mới
- Community nhỏ hơn Android
- Performance có thể kém hơn native

### Android XML + Kotlin ✅
**Giống:** Tính toán, logic, event handling
**Khác:** Cách tổ chức (tách biệt), styling, state management

**Ưu điểm:**
- True native performance
- Large ecosystem
- Android features
- Well-established

**Nhược điểm:**
- Code nhiều hơn (boilerplate)
- Tách biệt file (khó maintain)
- Slow iteration
- Android-only
- Learning curve cao

**Kết luận:** React Native tốt hơn cho multi-platform apps, Android tốt hơn cho native Android features.
