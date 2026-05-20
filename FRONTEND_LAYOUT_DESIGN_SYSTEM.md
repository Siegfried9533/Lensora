# 🎨 Frontend Layout & Design System - Lensora

## Tổng Quan Design System

Lensora sử dụng một design system hiện đại, tối giản, tập trung vào trải nghiệm người dùng di động.

### Technology Stack
- **Styling Framework**: Tailwind CSS + NativeWind
- **Color Scheme**: Dark Theme (Material Dark)
- **Icons**: Lucide React Native + Expo Vector Icons
- **Typography**: Custom font (Space Mono)
- **Responsive**: Mobile-first approach

---

## 1️⃣ Color Palette

### Theme Colors (Dark Mode)

```typescript
// Color Scheme
Primary Background:     #1a1a1a (Dark gray - main background)
Secondary Background:   #0a0a0a (Darker - cards, inputs)
Border Color:          #374151 (Light gray)
Text Primary:          #ffffff (White)
Text Secondary:        #9ca3af (Light gray)
Text Muted:            #6b7280 (Gray)

Accent Colors:
- Primary Accent:      #FF8C42 (Orange - buttons, highlights)
- Error:               #f87171 (Red)
- Success:             #4ade80 (Green)
- Warning:             #facc15 (Yellow)
```

### Tailwind Color Usage

```typescript
// Backgrounds
bg-[#1a1a1a]     // Main view background
bg-[#0a0a0a]     // Card background, inputs
bg-[#FF8C42]     // Primary buttons
bg-black/20      // Semi-transparent overlay

// Text Colors
text-white           // Primary text
text-gray-400        // Secondary text
text-gray-500        // Muted text
text-gray-600        // Very muted
text-gray-800        // Border-like
text-[#FF8C42]       // Accent text

// Borders
border-gray-800      // Light borders
border-gray-700      // Medium borders
border-gray-600      // Dark borders
border-red-500       // Error state
```

### Visual Color Reference

```
┌─────────────────────────────────────────┐
│ LENSORA COLOR PALETTE                   │
├─────────────────────────────────────────┤
│                                         │
│  ▓▓▓▓  #1a1a1a - Main Background       │
│  ░░░░  #0a0a0a - Card Background       │
│  ────  #374151 - Borders                │
│  ┏┓┏┓  #ffffff - Primary Text           │
│  ╳╳╳╳  #9ca3af - Secondary Text         │
│  ████  #FF8C42 - Primary Accent         │
│  ░░░░  #f87171 - Error/Danger           │
│  ░░░░  #4ade80 - Success                │
│                                         │
└─────────────────────────────────────────┘
```

---

## 2️⃣ Layout Structure

### Safe Area Handling

```typescript
import { useSafeAreaInsets } from 'react-native-safe-area-context';

const insets = useSafeAreaInsets();

// Header with safe area
<View style={{ paddingTop: insets.top + 16 }}>
  {/* Header content */}
</View>

// Bottom bar with safe area
<View style={{ paddingBottom: insets.bottom }}>
  {/* Bottom content */}
</View>
```

### Common Layout Patterns

#### Pattern 1: Screen with Header + ScrollView + Footer
```
┌──────────────────────────┐
│  📱 Safe Area Top        │
├──────────────────────────┤
│                          │
│  ← Header ────────────→  │
│                          │
├──────────────────────────┤
│                          │
│  ScrollView Content      │
│                          │
│  ├─ Search               │
│  ├─ Categories           │
│  ├─ Items Grid           │
│                          │
├──────────────────────────┤
│  🔘 Action Button        │
│                          │
│  📱 Safe Area Bottom     │
└──────────────────────────┘
```

#### Pattern 2: Header with Icon Buttons
```typescript
<View className="flex-row items-center justify-between px-6">
  <View>
    <Text className="text-3xl font-bold text-white">DISCOVERY</Text>
    <Text className="text-sm text-gray-400">Welcome!</Text>
  </View>
  
  {/* Shopping Cart Button with Badge */}
  <TouchableOpacity className="w-10 h-10 bg-[#0a0a0a] rounded-full relative border border-gray-800">
    <ShoppingCart size={20} color="#9ca3af" />
    <View className="absolute -top-1 -right-1 bg-red-500 w-4 h-4 rounded-full">
      <Text className="text-[10px] text-white font-bold">9+</Text>
    </View>
  </TouchableOpacity>
</View>
```

---

## 3️⃣ Component Styling Patterns

### Button Styles

#### Primary Button (CTA)
```typescript
<TouchableOpacity className="bg-[#FF8C42] px-8 py-4 rounded-full">
  <Text className="text-black font-bold text-center">Action</Text>
</TouchableOpacity>
```

**Specifications:**
- Background: `#FF8C42` (Orange)
- Text: Black, Bold
- Padding: `px-8 py-4` (horizontal/vertical)
- Border radius: `rounded-full`
- Shape: Pill-shaped button

#### Secondary Button
```typescript
<TouchableOpacity className="border border-gray-800 px-6 py-3 rounded-full">
  <Text className="text-white font-semibold">Secondary</Text>
</TouchableOpacity>
```

#### Subtle Button (Link-style)
```typescript
<TouchableOpacity>
  <Text className="text-[#FF8C42] font-semibold">Link Button</Text>
</TouchableOpacity>
```

### Input Styling

#### Text Input
```typescript
<TextInput
  placeholder="Search equipment..."
  placeholderTextColor="#4b5563"
  className="pl-11 pr-4 py-4 bg-[#0a0a0a] border border-gray-800 rounded-2xl text-white"
/>
```

**Specifications:**
- Background: `#0a0a0a`
- Border: `border-gray-800`, width 1px
- Border radius: `rounded-2xl` (large radius)
- Padding: `px-4 py-4`
- Text color: White
- Placeholder: Light gray (`#4b5563`)

#### Icon in Input
```typescript
<View className="relative">
  <View className="absolute left-4 top-4 z-10">
    <Search size={16} color="#6b7280" />
  </View>
  <TextInput
    className="pl-11 pr-4 py-4 bg-[#0a0a0a] border border-gray-800 rounded-2xl text-white"
  />
</View>
```

### Card Styling

#### Product Card
```typescript
<TouchableOpacity className="bg-[#0a0a0a] border border-gray-800 rounded-2xl overflow-hidden">
  {/* Image */}
  <Image className="w-full h-48" source={{ uri: imageUrl }} />
  
  {/* Content */}
  <View className="p-4">
    <Text className="text-white font-bold">{title}</Text>
    <Text className="text-gray-400 text-sm">{category}</Text>
    <View className="flex-row justify-between items-center mt-3">
      <Text className="text-[#FF8C42] font-bold text-lg">${price}</Text>
      <Heart size={20} color="#9ca3af" />
    </View>
  </View>
</TouchableOpacity>
```

**Specifications:**
- Background: `#0a0a0a`
- Border: `border-gray-800`
- Border radius: `rounded-2xl`
- Padding: `p-4` (uniform)
- Image: Full width, height 48 (192px)
- Overflow: Hidden (for rounded corners)

### Toggle/Switch Patterns

#### Buy/Rent Toggle
```typescript
<View className="flex-row mt-6 bg-[#0a0a0a] border border-gray-800 p-1 rounded-2xl">
  <TouchableOpacity
    className={`flex-1 py-3 rounded-xl items-center ${
      mode === 'BUY' ? 'bg-[#FF8C42]' : ''
    }`}
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
  >
    <Text className={`font-semibold ${
      mode === 'RENT' ? 'text-black' : 'text-gray-500'
    }`}>
      Rent
    </Text>
  </TouchableOpacity>
</View>
```

**Design:**
- Segmented control design
- Active tab: Orange background + black text
- Inactive tab: Transparent + gray text
- Small padding between segments

### Category Pills

```typescript
<ScrollView horizontal showsHorizontalScrollIndicator={false}>
  {/* All Categories Button */}
  <TouchableOpacity
    className={`px-5 py-3 rounded-full ${
      selected === null ? 'bg-[#FF8C42]' : 'bg-[#0a0a0a] border border-gray-800'
    }`}
  >
    <Text className={selected === null ? 'text-black font-bold' : 'text-white'}>
      All
    </Text>
  </TouchableOpacity>
  
  {/* Category Pills */}
  {categories.map(cat => (
    <TouchableOpacity
      key={cat.id}
      className={`px-5 py-3 rounded-full mx-1 ${
        selected === cat.name ? 'bg-[#FF8C42]' : 'bg-[#0a0a0a] border border-gray-800'
      }`}
    >
      <Text className={selected === cat.name ? 'text-black font-bold' : 'text-white'}>
        {cat.name}
      </Text>
    </TouchableOpacity>
  ))}
</ScrollView>
```

**Specifications:**
- Horizontal scroll
- Pill-shaped (fully rounded)
- Padding: `px-5 py-3`
- Gap: `mx-1` (margin between)
- No scroll indicator

### List Item Pattern

```typescript
<View className="flex-row items-center p-4 border-b border-gray-800">
  <View className="flex-1">
    <Text className="text-white font-semibold">{title}</Text>
    <Text className="text-gray-400 text-sm">{subtitle}</Text>
  </View>
  
  <ChevronRight size={20} color="#6b7280" />
</View>
```

---

## 4️⃣ Spacing System

### Standard Spacing Values (Tailwind)

```
px-1  = 4px      (horizontal padding)
px-2  = 8px
px-3  = 12px
px-4  = 16px
px-6  = 24px ← Most common
px-8  = 32px

py-1  = 4px      (vertical padding)
py-2  = 8px
py-3  = 12px
py-4  = 16px ← Most common
py-6  = 24px

p-4   = 16px all sides
p-6   = 24px all sides

mb-2  = 8px margin bottom
mb-4  = 16px margin bottom
mb-6  = 24px margin bottom
mt-6  = 24px margin top
```

### Common Spacing Patterns

```typescript
// Full-width screen with padding
<View className="px-6">
  {/* Content with 24px padding on sides */}
</View>

// Top padding with safe area
style={{ paddingTop: insets.top + 16 }}

// Item spacing
<View className="gap-3">  {/* 12px gap between children */}
  {items.map(item => <Item />)}
</View>

// Horizontal gap
<View className="flex-row gap-2">  {/* 8px gap */}
  {/* Items */}
</View>
```

---

## 5️⃣ Typography System

### Font Sizes

```typescript
// Tailwind Text Sizes
text-xs    = 12px
text-sm    = 14px
text-base  = 16px
text-lg    = 18px
text-xl    = 20px
text-2xl   = 24px
text-3xl   = 30px
text-4xl   = 36px

// Custom sizes
text-[10px]   = Exact size
text-[18px]   = Badge text, small info
```

### Font Weights

```typescript
font-thin    = 100
font-light   = 300
font-normal  = 400
font-medium  = 500
font-semibold = 600
font-bold    = 700 ← Most used
font-black   = 900
```

### Common Typography Patterns

#### Screen Title
```typescript
<Text className="text-3xl mb-1 text-white font-bold tracking-tight">
  DISCOVERY
</Text>
<Text className="text-sm text-gray-400">
  Welcome, User!
</Text>
```

#### Card Title
```typescript
<Text className="text-white font-bold">{title}</Text>
<Text className="text-gray-400 text-sm">{subtitle}</Text>
```

#### Action Text
```typescript
<Text className="text-black font-bold text-center">
  Action Label
</Text>
```

#### Meta Information
```typescript
<Text className="text-xs text-gray-500 uppercase tracking-wider">
  CATEGORY
</Text>
```

---

## 6️⃣ Border & Radius System

### Border Radius

```typescript
rounded-lg     = 8px
rounded-xl     = 12px
rounded-2xl    = 16px ← Most used for cards
rounded-3xl    = 18px
rounded-full   = 999px (pill-shaped buttons)
```

### Border Widths

```typescript
border         = 1px (default)
border-2       = 2px (not commonly used)
border-[3px]   = Custom size
```

### Border Colors

```typescript
border-gray-800    = Light border (#374151)
border-gray-700    = Medium border
border-gray-600    = Dark border
border-red-500     = Error state
border-[#FF8C42]   = Accent border
```

### Common Border Patterns

```typescript
// Card border
className="border border-gray-800 rounded-2xl"

// Input border
className="border border-gray-800 rounded-2xl"

// Divider
className="border-b border-gray-800"

// Top border only
className="border-t border-gray-800"
```

---

## 7️⃣ Flex Layout Patterns

### Flexbox Utilities

```typescript
// Direction & Alignment
flex-row              // Horizontal layout
flex-col              // Vertical layout (default)
items-center          // Vertical centering
justify-center        // Horizontal centering
justify-between       // Space between
justify-around        // Space around
gap-2                 // Space between items
gap-3                 // Gap between items
gap-4                 // Larger gap

// Sizing
flex-1                // Take available space
w-full                // 100% width
h-full                // 100% height
```

### Common Flex Patterns

#### Header with Title and Button
```typescript
<View className="flex-row items-center justify-between px-6">
  <Text className="text-2xl font-bold text-white">Title</Text>
  <TouchableOpacity className="w-10 h-10">
    <Icon />
  </TouchableOpacity>
</View>
```

#### Centered Content
```typescript
<View className="flex-1 items-center justify-center">
  <Icon size={40} />
  <Text className="text-xl text-white font-bold mt-4">Message</Text>
</View>
```

#### List Item with Right Action
```typescript
<View className="flex-row items-center px-6 py-4">
  <View className="flex-1">
    <Text className="text-white font-bold">{title}</Text>
  </View>
  <TouchableOpacity>
    <ChevronRight />
  </TouchableOpacity>
</View>
```

---

## 8️⃣ Screen Layouts (Visual Structure)

### Discovery Screen Layout
```
┌─────────────────────────────────────┐
│ 🛒 DISCOVERY              🛍️ (badge)│  ← Header
├─────────────────────────────────────┤
│ ⌕ Search equipment...               │  ← Search
├─────────────────────────────────────┤
│ [Buy Equipment] [Rent Equipment]     │  ← Mode Toggle
├─────────────────────────────────────┤
│ [All] [Camera] [Lens] [Tripod] ...  │  ← Categories
├─────────────────────────────────────┤
│                                     │
│  ┌─────────┐  ┌─────────┐          │
│  │ Image   │  │ Image   │          │
│  │ Product │  │ Product │          │
│  │ $199    │  │ $149    │          │  ← Products Grid
│  └─────────┘  └─────────┘          │
│  ┌─────────┐  ┌─────────┐          │
│  │ Image   │  │ Image   │          │
│  │ Product │  │ Product │          │
│  │ $299    │  │ $99     │          │
│  └─────────┘  └─────────┘          │
│                                     │
└─────────────────────────────────────┘
```

### Cart Screen Layout
```
┌─────────────────────────────────────┐
│ ← Giỏ hàng           Xóa giỏ hàng   │  ← Header
├─────────────────────────────────────┤
│                                     │
│  ┌─────────────────────────────────┐│
│  │ [Image] Product Name      $199  ││
│  │         Qty: 1   ← (+) 1 (-)    ││  ← Cart Items
│  │         Remove                  ││
│  └─────────────────────────────────┘│
│  ┌─────────────────────────────────┐│
│  │ [Image] Product Name      $149  ││
│  │         Qty: 2   ← (+) 2 (-)    ││
│  │         Remove                  ││
│  └─────────────────────────────────┘│
│                                     │
├─────────────────────────────────────┤
│ Subtotal:           $348            │  ← Summary
│ Shipping:           $35             │
│ ──────────────────────────          │
│ Total:              $383            │
│                                     │
│ [Checkout]                          │  ← CTA
└─────────────────────────────────────┘
```

### Profile Screen Layout
```
┌─────────────────────────────────────┐
│                                     │
│     ┌─────────────┐                 │
│     │   Avatar    │                 │
│     │ [Edit Photo]│                 │  ← User Info
│     └─────────────┘                 │
│                                     │
│     John Doe                        │
│     john@example.com                │
│                                     │
├─────────────────────────────────────┤
│ [Orders: 5] [Rentals: 2] [★ 4.5]   │  ← Stats
├─────────────────────────────────────┤
│ 🛍️  My Equipment                    │
│ ❤️  Favorites                       │  ← Menu Items
│ ⚙️  Settings                        │
│ 🔒 Change Password                  │
│ 🚪 Logout                           │
├─────────────────────────────────────┤
│                                     │
└─────────────────────────────────────┘
```

### Login Screen Layout
```
┌─────────────────────────────────────┐
│                                     │
│                                     │
│   📸 LENSORA SHOP                   │  ← Logo/Title
│                                     │
│   [Email Input]                     │  ← Form Inputs
│   [Password Input]                  │
│                                     │
│   [Login Button]                    │  ← CTA
│                                     │
│   Don't have account? Sign Up →     │  ← Link
│                                     │
│                                     │
└─────────────────────────────────────┘
```

---

## 9️⃣ Responsive Design

### Mobile-First Approach

```typescript
// All sizes are designed for mobile first
// No media queries needed (React Native handles this)

// Screen width breakpoints (React Native)
<100px   = Extra small phone
100-300px = Small phone
300-400px = Regular phone
400-600px = Large phone
600+px   = Tablet
```

### Dynamic Sizing

```typescript
// Responsive padding
px-6      // 24px - standard for all screens
px-4      // 16px - smaller content

// Responsive font
text-sm   // 14px - labels
text-base // 16px - body text
text-lg   // 18px - subtitles
text-2xl  // 24px - section titles
text-3xl  // 30px - main titles

// Responsive image heights
h-48      // 192px - product images
h-32      // 128px - thumbnail
h-64      // 256px - banner
```

---

## 🔟 Empty & Loading States

### Loading State
```typescript
<View className="flex-1 bg-[#1a1a1a] items-center justify-center">
  <ActivityIndicator size="large" color="#FF8C42" />
  <Text className="text-gray-400 mt-4">Loading...</Text>
</View>
```

### Empty State
```typescript
<View className="flex-1 justify-center items-center p-6">
  <View className="w-24 h-24 rounded-full bg-black/20 border border-gray-800 items-center justify-center mb-6">
    <ShoppingBag size={40} color="#4b5563" />
  </View>
  <Text className="text-xl text-white font-bold mb-2">Giỏ hàng trống</Text>
  <Text className="text-gray-400 text-center mb-8">
    Looks like you haven't added anything yet.
  </Text>
  <TouchableOpacity className="bg-[#FF8C42] px-8 py-4 rounded-full">
    <Text className="text-black font-bold">Start Shopping</Text>
  </TouchableOpacity>
</View>
```

### Error State
```typescript
<View className="flex-1 bg-[#1a1a1a] items-center justify-center p-6">
  <Text className="text-red-500 mb-4 text-center">{error}</Text>
  <TouchableOpacity onPress={retry} className="bg-[#FF8C42] px-6 py-3 rounded-full">
    <Text className="text-black font-bold">Retry</Text>
  </TouchableOpacity>
</View>
```

---

## 1️⃣1️⃣ Design Best Practices Applied

### ✅ Consistency
- Same color scheme throughout
- Consistent button styles
- Uniform spacing (6px grid system)
- Same typography patterns

### ✅ Visual Hierarchy
- Large titles (30-36px)
- Medium subtitles (20-24px)
- Regular body (14-16px)
- Small meta (12px)

### ✅ Accessibility
- Sufficient contrast (white on dark)
- Large touch targets (44px minimum)
- Clear interactive elements
- Readable font sizes

### ✅ Performance
- Minimal re-renders
- Optimized images
- Efficient layouts
- No unnecessary renders

### ✅ User Experience
- Quick feedback (loading states)
- Clear error messages
- Empty state guidance
- Smooth animations (fade, slide)

---

## 1️⃣2️⃣ Design Tokens Reference

```typescript
// Comprehensive Design Tokens

COLORS: {
  // Primary
  primary: '#FF8C42',           // Orange - actions, highlights
  
  // Backgrounds
  bg_primary: '#1a1a1a',        // Main background
  bg_secondary: '#0a0a0a',      // Cards, inputs
  
  // Text
  text_primary: '#ffffff',      // Main text
  text_secondary: '#9ca3af',    // Secondary text
  text_muted: '#6b7280',        // Muted text
  
  // States
  error: '#f87171',             // Error/danger
  success: '#4ade80',           // Success
  warning: '#facc15',           // Warning
  
  // Borders
  border_light: '#374151',      // Light borders
}

SPACING: {
  xs: 4,
  sm: 8,
  md: 12,
  lg: 16,
  xl: 24,
  xxl: 32,
}

RADIUS: {
  sm: 8,
  md: 12,
  lg: 16,
  full: 999,
}

FONTS: {
  xs: 12,
  sm: 14,
  base: 16,
  lg: 18,
  xl: 20,
  xxl: 24,
  xxxl: 30,
}

SHADOWS: {
  sm: {
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.18,
    shadowRadius: 1.0,
    elevation: 1,
  },
}
```

---

## Summary

**Lensora Layout & Design System:**

| Aspek | Chi Tiết |
|-------|---------|
| **Color Scheme** | Dark mode (#1a1a1a, #0a0a0a), Orange accent (#FF8C42) |
| **Typography** | Tailwind CSS, Space Mono font |
| **Spacing** | 6px grid system (4px, 8px, 12px, 16px, 24px, 32px) |
| **Border Radius** | 8px, 12px, 16px, 999px (pill buttons) |
| **Buttons** | Pill-shaped, orange background, black text, 16px padding |
| **Cards** | Dark background, light border, 16px border radius, 16px padding |
| **Inputs** | Dark background, light border, icon support, 16px padding |
| **Layout** | Flexbox, mobile-first, safe area handling |
| **Icons** | Lucide React Native, 20-24px typical size |
| **Animations** | Fade, slide transitions between screens |
| **State UX** | Loading spinner, empty state, error message, retry button |

**Key Design Principle:**
Minimalist dark theme với orange accent, tập trung vào tương tác đơn giản, rõ ràng, dễ sử dụng trên di động.
