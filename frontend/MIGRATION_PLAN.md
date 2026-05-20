# Camera Shop — Migration Plan: React Native (Expo) → Native Android (Java + XML)

> **Plan này sẽ được lưu tại:** `My_Mobile_App/MIGRATION_PLAN.md` (bước đầu tiên khi thực thi sẽ copy file này sang đó).

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:subagent-driven-development` (recommended) hoặc `superpowers:executing-plans` để triển khai theo từng task. Steps dùng checkbox (`- [ ]`).

---

## Context

Dự án `frontend/` hiện tại là một app React Native + Expo (TypeScript, NativeWind/Tailwind, Expo Router) với ~30 màn hình kết nối tới Spring Boot backend (`/api`). User muốn:

1. **Port toàn bộ giao diện sang native Android XML + Java** (giữ nguyên 100% UI/UX, chỉ thay ngôn ngữ).
2. Sử dụng `My_Mobile_App/` (scaffold Android Studio mặc định, package `com.example.my_mobile_app`) làm nơi viết code mới.
3. Cập nhật `docker-compose.yml`: chỉ giữ **db + backend**, bỏ frontend (frontend Android sẽ chạy ngoài Docker).
4. Tuân thủ `CLAUDE.md` của repo (Think Before Coding, Simplicity First, Surgical Changes, Goal-Driven Execution).

**Goal:** App Android native chạy được, đăng nhập/đăng ký/duyệt sản phẩm/mua-thuê/giỏ hàng/thanh toán/đơn hàng/chatbot/thông báo/profile — tất cả gọi đúng REST endpoint của backend hiện tại.

**Architecture:**
- **1 Activity / 1 màn hình** (theo lựa chọn của user). Navigation bằng `Intent` + `startActivity`. Bottom navigation (5 tab) implement bằng `BottomNavigationView` lặp lại trong 5 Activity tab + chuyển bằng Intent với `FLAG_ACTIVITY_REORDER_TO_FRONT` để giữ stack.
- **MVC nhẹ:** Activity = Controller, layout XML = View, model POJO + Retrofit Service = Model. Không dùng MVVM/LiveData (quá phức tạp với người mới + scope lớn).
- **Networking:** Retrofit2 + OkHttp + Gson. Auth Interceptor đọc token từ `SharedPreferences`.
- **Image loading:** Glide.
- **Async:** Retrofit `enqueue()` callback (background → main thread tự động).
- **State chia sẻ:** `SharedPreferences` (token, user) + reload từ API mỗi khi cần (cart, favorites, orders, notifications). Không có global ViewModel/Repository singleton phức tạp.

**Tech Stack:**
- Java 11, AGP 8.13, compileSdk 36, minSdk 24, targetSdk 36
- AndroidX AppCompat 1.6.1, Material Components 1.10.0, ConstraintLayout 2.1.4
- **Thêm mới:** Retrofit 2.11.0, OkHttp 4.12.0, Gson 2.11.0, Glide 4.16.0, OkHttp logging-interceptor

**Design tokens** (lấy từ NativeWind theme hiện tại):
- Primary bg: `#1a1a1a` | Card bg: `#0a0a0a` | Accent: `#FF8C42`
- Text: `#FFFFFF` | Muted: `#9ca3af` | Border: `#374151`
- Status: red `#EF4444`, green `#22c55e`, blue `#3b82f6`

---

## File Structure (target)

```
My_Mobile_App/
├── MIGRATION_PLAN.md                  ← copy của plan này
├── app/
│   ├── build.gradle.kts                ← thêm dependencies
│   └── src/main/
│       ├── AndroidManifest.xml         ← khai báo permissions + 30 activities
│       ├── java/com/example/my_mobile_app/
│       │   ├── MainActivity.java       ← splash/route: login? → LoginActivity, else → HomeActivity
│       │   ├── api/
│       │   │   ├── ApiClient.java          ← Retrofit singleton + AuthInterceptor
│       │   │   ├── ApiConstants.java       ← BASE_URL
│       │   │   ├── ApiResponse.java        ← {success, data, message}
│       │   │   ├── AuthService.java        ← Retrofit interface
│       │   │   ├── ProductService.java
│       │   │   ├── AssetService.java
│       │   │   ├── CartService.java
│       │   │   ├── OrderService.java
│       │   │   ├── RentalService.java
│       │   │   ├── PaymentService.java
│       │   │   ├── FavoriteService.java
│       │   │   ├── NotificationService.java
│       │   │   └── ChatbotService.java     ← non-stream version (streaming sẽ dùng OkHttp raw)
│       │   ├── model/                  ← POJO mirror các interface TypeScript
│       │   │   ├── User.java, AuthResponse.java
│       │   │   ├── Product.java, Asset.java, Category.java
│       │   │   ├── CartItem.java, Favorite.java
│       │   │   ├── Order.java, OrderItem.java
│       │   │   ├── Rental.java
│       │   │   ├── Notification.java
│       │   │   ├── PaymentResult.java, ShippingFee.java
│       │   │   ├── Province.java, District.java, Ward.java
│       │   │   └── ChatMessage.java
│       │   ├── util/
│       │   │   ├── TokenManager.java       ← SharedPreferences wrapper
│       │   │   ├── UserManager.java        ← cache User trong SharedPreferences (JSON)
│       │   │   ├── PriceFormatter.java     ← format VND
│       │   │   ├── DateUtils.java
│       │   │   └── BottomNavHelper.java    ← gắn BottomNavigationView vào Activity
│       │   └── ui/
│       │       ├── BaseActivity.java       ← common loading dialog, error snackbar
│       │       ├── auth/
│       │       │   ├── LoginActivity.java
│       │       │   ├── SignupActivity.java
│       │       │   ├── ForgotPasswordActivity.java
│       │       │   ├── ResetPasswordActivity.java
│       │       │   └── VerifyEmailActivity.java
│       │       ├── home/
│       │       │   ├── HomeActivity.java                  (tab 1 - Discovery)
│       │       │   └── ProductCardAdapter.java
│       │       ├── transactions/
│       │       │   ├── TransactionsActivity.java          (tab 2)
│       │       │   ├── OrderListAdapter.java
│       │       │   └── RentalListAdapter.java
│       │       ├── chatbot/
│       │       │   ├── ChatbotActivity.java               (tab 3, streaming SSE qua OkHttp)
│       │       │   └── ChatMessageAdapter.java
│       │       ├── notifications/
│       │       │   ├── NotificationsActivity.java         (tab 4)
│       │       │   └── NotificationAdapter.java
│       │       ├── profile/
│       │       │   ├── ProfileActivity.java               (tab 5)
│       │       │   ├── SettingsActivity.java
│       │       │   ├── PersonalInfoActivity.java
│       │       │   ├── ChangePasswordActivity.java
│       │       │   ├── FavoritesActivity.java
│       │       │   ├── MyEquipmentActivity.java
│       │       │   ├── PrivacySecurityActivity.java
│       │       │   ├── SpendingStatsActivity.java
│       │       │   └── HelpActivity.java
│       │       ├── equipment/
│       │       │   └── EquipmentDetailActivity.java
│       │       ├── cart/
│       │       │   ├── CartActivity.java
│       │       │   └── CartItemAdapter.java
│       │       ├── checkout/
│       │       │   └── CheckoutActivity.java
│       │       ├── orders/
│       │       │   └── OrderDetailActivity.java
│       │       ├── rentals/
│       │       │   ├── RentalListActivity.java
│       │       │   └── RentalDetailActivity.java
│       │       ├── payment/
│       │       │   ├── PaymentSuccessActivity.java
│       │       │   ├── PaymentFailedActivity.java
│       │       │   └── OrderStatusActivity.java
│       │       └── store/
│       │           └── StoreActivity.java
│       └── res/
│           ├── layout/                 ← ~35 file XML (1 cho mỗi screen + item layouts)
│           ├── menu/bottom_nav_menu.xml
│           ├── drawable/               ← bg_button, bg_input, bg_card, bg_chip, ic_*
│           ├── values/
│           │   ├── colors.xml          ← thêm 15+ color
│           │   ├── strings.xml         ← thêm ~80 string Vietnamese
│           │   ├── dimens.xml          ← spacing chuẩn
│           │   ├── themes.xml          ← Material3 dark + accent
│           │   └── styles.xml          ← Button.Primary, Input, CardOrange...
│           └── values-night/themes.xml
└── (root)
    └── ../docker-compose.yml          ← cập nhật: bỏ service frontend (nếu có)
```

---

# PHASE 0 — Plan persistence & Docker compose

### Task 0.1: Lưu plan vào project

**Files:**
- Create: `My_Mobile_App/MIGRATION_PLAN.md`

- [ ] **Step 1**: Copy nội dung của plan này (file `binary-frolicking-koala.md`) vào `My_Mobile_App/MIGRATION_PLAN.md`. Đây phải là việc đầu tiên để user có thể tham chiếu trong suốt quá trình.

- [ ] **Step 2**: Commit
```bash
git add My_Mobile_App/MIGRATION_PLAN.md
git commit -m "docs: add Android migration plan"
```

### Task 0.2: Cập nhật docker-compose.yml

**Files:**
- Modify: `docker-compose.yml`

- [ ] **Step 1**: Đọc `docker-compose.yml` hiện tại. File hiện tại chỉ có `db` (postgres:15) + `backend` (Spring Boot, port 8080) — KHÔNG có service frontend nào. Verify bằng `cat docker-compose.yml`.

- [ ] **Step 2**: Nếu có bất kỳ service `frontend`/`expo`/`web` nào → xoá. Nếu không → ghi note vào commit message rằng "không có frontend service, đã verify".

- [ ] **Step 3**: Verify backend có `extra_hosts: host.docker.internal:host-gateway` để Android emulator có thể gọi `10.0.2.2:8080`. Nếu chưa có → giữ nguyên (không sửa nếu không cần).

- [ ] **Step 4**: Commit
```bash
git add docker-compose.yml
git commit -m "chore(docker): keep only db + backend, frontend runs outside docker"
```

### Task 0.3: Verify backend chạy

- [ ] `docker compose up -d db backend`
- [ ] `curl http://localhost:8080/api/products?page=0&size=1` — kỳ vọng JSON response (status 200 hoặc 401 nếu route yêu cầu auth, KHÔNG được 502/connection refused).
- [ ] Ghi lại IP máy host (`hostname -I | awk '{print $1}'`) để dùng cho `BASE_URL` ở Phase 1.

---

# PHASE 1 — Project setup (dependencies, theme, base classes)

### Task 1.1: Thêm dependencies vào Gradle

**Files:**
- Modify: `My_Mobile_App/app/build.gradle.kts` (hoặc `.gradle` nếu Groovy)

- [ ] **Step 1**: Mở file build.gradle của module app, thêm vào block `dependencies { ... }`:

```kotlin
implementation("com.squareup.retrofit2:retrofit:2.11.0")
implementation("com.squareup.retrofit2:converter-gson:2.11.0")
implementation("com.squareup.okhttp3:okhttp:4.12.0")
implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
implementation("com.github.bumptech.glide:glide:4.16.0")
annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
implementation("androidx.recyclerview:recyclerview:1.3.2")
implementation("androidx.cardview:cardview:1.0.0")
implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
implementation("com.google.android.material:material:1.12.0")
```

- [ ] **Step 2**: Sync Gradle. Verify build success: `./gradlew :app:assembleDebug` (chỉ cần compile, không cần run).

- [ ] **Step 3**: Commit `chore(android): add Retrofit/Glide/Material dependencies`.

### Task 1.2: Permissions + AndroidManifest base

**Files:**
- Modify: `My_Mobile_App/app/src/main/AndroidManifest.xml`

- [ ] Thêm vào trước `<application>`:
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
```

- [ ] Thêm `android:usesCleartextTraffic="true"` vào `<application>` (vì backend dev là HTTP).

- [ ] Commit `chore(android): declare network permissions`.

### Task 1.3: Color/String/Dimen resources

**Files:**
- Modify: `app/src/main/res/values/colors.xml`, `strings.xml`, `themes.xml`
- Create: `app/src/main/res/values/dimens.xml`, `styles.xml`

- [ ] **colors.xml** — thêm full palette (orange accent, gray scale 100→900, status colors). Reference đầy đủ trong báo cáo Phase 0 phía trên.

- [ ] **strings.xml** — thêm tất cả label tiếng Việt từ React Native screens (Đăng nhập, Đăng ký, Khám phá, Giỏ hàng, Thanh toán, Yêu thích, Mật khẩu, ...).

- [ ] **dimens.xml** — spacing chuẩn:
```xml
<dimen name="spacing_xs">4dp</dimen>
<dimen name="spacing_sm">8dp</dimen>
<dimen name="spacing_md">16dp</dimen>
<dimen name="spacing_lg">24dp</dimen>
<dimen name="spacing_xl">32dp</dimen>
<dimen name="radius_card">16dp</dimen>
<dimen name="radius_button">12dp</dimen>
<dimen name="text_caption">12sp</dimen>
<dimen name="text_body">14sp</dimen>
<dimen name="text_title">18sp</dimen>
<dimen name="text_heading">24sp</dimen>
```

- [ ] **themes.xml** — đặt `colorPrimary=@color/orange`, background `@color/bg_dark_primary`, force dark mode.

- [ ] **styles.xml** — `Button.Primary`, `Button.Outline`, `Input.Box`, `Text.Heading`, `Text.Body`, `Text.Muted`, `Text.Price`.

- [ ] Commit `feat(theme): add design tokens matching frontend theme`.

### Task 1.4: Drawable shapes

**Files:**
- Create: `app/src/main/res/drawable/bg_button_primary.xml`, `bg_input.xml`, `bg_card.xml`, `bg_chip.xml`, `bg_chip_selected.xml`, `bg_search.xml`, `bg_circle_dark.xml`

- [ ] Mỗi file là `<shape>` với `corner radius` + `solid color` + (optional) `stroke`. Ví dụ:
```xml
<!-- bg_button_primary.xml -->
<shape xmlns:android="http://schemas.android.com/apk/res/android" android:shape="rectangle">
    <solid android:color="@color/orange"/>
    <corners android:radius="@dimen/radius_button"/>
</shape>
```

- [x] Commit.

### Task 1.5: Utility classes

**Files:**
- Create: `util/TokenManager.java`, `UserManager.java`, `PriceFormatter.java`, `DateUtils.java`, `BottomNavHelper.java`

- [ ] **TokenManager**: methods `saveToken(Context, String)`, `getToken(Context): String`, `clear(Context)`. Backed bởi `SharedPreferences("camera_shop_prefs", MODE_PRIVATE)` key `auth_token`.

- [ ] **UserManager**: lưu User dạng JSON (Gson) trong cùng SharedPreferences key `current_user`.

- [ ] **PriceFormatter**: `format(double): String` → "₫25.000.000" (NumberFormat tiếng Việt-VN, prefix ₫).

- [ ] **DateUtils**: parse ISO-8601 → format dd/MM/yyyy + countdown ngày cho rental.

- [ ] **BottomNavHelper.attachTo(Activity, int currentItemId)**: attach 5 menu items, switch activity bằng Intent (giữ singleTask flag để tránh stack lồng nhau).

- [ ] Commit `feat(util): add token/user managers, price/date formatters`.

### Task 1.6: BaseActivity

**Files:**
- Create: `ui/BaseActivity.java`

- [ ] Methods: `showLoading()`, `hideLoading()` (dùng ProgressDialog hoặc full-screen overlay), `showError(String)` (Snackbar), `showSuccess(String)`, `requireLogin()` (check token, redirect → LoginActivity nếu null).

- [x] Commit.

---

# PHASE 2 — API layer

### Task 2.1: ApiConstants + ApiClient

**Files:**
- Create: `api/ApiConstants.java`, `api/ApiClient.java`, `api/ApiResponse.java`

- [ ] **ApiConstants.BASE_URL** = `"http://10.0.2.2:8080/api/"` (cho emulator). Comment ghi chú thay IP nếu test trên máy thật.

- [ ] **ApiResponse<T>**: `boolean success; T data; String message;`

- [ ] **ApiClient**: singleton trả về `Retrofit` với:
  - `OkHttpClient` có `HttpLoggingInterceptor` (BODY level — chỉ debug build) + `AuthInterceptor` thêm `Authorization: Bearer <token>` (đọc từ TokenManager).
  - Timeout 30s connect, 30s read.
  - GsonConverterFactory.

- [x] Commit.

### Task 2.2: Model POJOs

**Files:**
- Create: `model/User.java`, `AuthResponse.java`, `Product.java`, `Asset.java`, `Category.java`, `CartItem.java`, `Favorite.java`, `Order.java`, `OrderItem.java`, `Rental.java`, `Notification.java`, `PaymentResult.java`, `ShippingFee.java`, `Province.java`, `District.java`, `Ward.java`, `ChatMessage.java`, `PaginatedResponse.java`

- [ ] Mỗi class: public fields + empty constructor + Gson `@SerializedName` nếu tên field khác convention. Mirror chính xác các interface TypeScript trong `frontend/services/api/*.ts`.

- [ ] **PaginatedResponse<T>**: `List<T> content; int totalPages; int totalElements; int number; int size;` (Spring Boot pagination format).

- [ ] Commit `feat(model): add POJO mirrors of frontend types`.

### Task 2.3: Retrofit service interfaces

**Files:** Create 10 file `api/*Service.java`.

Mỗi service map 1-1 với file TypeScript tương ứng. Ví dụ AuthService:

```java
public interface AuthService {
    @POST("auth/login")
    Call<ApiResponse<AuthResponse>> login(@Body LoginRequest body);

    @POST("auth/register")
    Call<ApiResponse<AuthResponse>> register(@Body RegisterRequest body);

    @GET("auth/me")
    Call<ApiResponse<User>> getCurrentUser();

    @PUT("auth/avatar")
    Call<ApiResponse<User>> updateAvatar(@Body Map<String, String> body);

    @POST("auth/change-password")
    Call<ApiResponse<Void>> changePassword(@Body ChangePasswordRequest body);

    @POST("auth/forgot-password")
    Call<ApiResponse<Void>> forgotPassword(@Body Map<String, String> body);

    @POST("auth/reset-password")
    Call<ApiResponse<Void>> resetPassword(@Body Map<String, String> body);
}
```

- [ ] Lặp lại pattern cho ProductService, AssetService, CartService, OrderService, RentalService, FavoriteService, NotificationService, PaymentService, ChatbotService — đối chiếu từng endpoint với file TypeScript tương ứng.

- [ ] Tạo các DTO request nhỏ (LoginRequest, RegisterRequest, ChangePasswordRequest, AddToCartRequest, CreateOrderRequest, CreateRentalRequest, CreateMoMoPaymentRequest) trong package `api/dto/`.

- [ ] Commit theo từng service (10 commits) hoặc 1 commit "feat(api): add all Retrofit service interfaces".

### Task 2.4: Smoke-test API connection

**Files:**
- Modify: `MainActivity.java` (tạm)

- [ ] Trong `onCreate`, gọi `ApiClient.get().create(ProductService.class).getAllProducts(0,1).enqueue(...)`. Log response. Mục tiêu: thấy data thật trong Logcat.

- [ ] Sau khi verify OK → revert MainActivity về trạng thái routing splash (Phase 3.0).

- [ ] Commit nếu cần debugging.

---

# PHASE 3 — Auth screens

### Task 3.0: MainActivity routing

**Files:**
- Modify: `MainActivity.java`, `res/layout/activity_main.xml`

- [ ] **activity_main.xml**: ConstraintLayout có ProgressBar ở giữa (splash 1 giây).

- [ ] **MainActivity**: `onCreate` → check `TokenManager.getToken()`. Nếu có → call `getCurrentUser()`:
  - Success → `startActivity(HomeActivity)` + finish
  - Fail (401) → clear token → `startActivity(LoginActivity)` + finish
  Nếu không có token → `LoginActivity` ngay.

- [ ] Commit.

### Task 3.1: LoginActivity

**Files:**
- Create: `ui/auth/LoginActivity.java`, `res/layout/activity_login.xml`
- Modify: `AndroidManifest.xml` (đăng ký activity)

- [ ] **activity_login.xml**: replicate UI từ `frontend/app/(auth)/login.tsx`. Dùng `ScrollView` > `LinearLayout vertical` > back button → title "Chào mừng trở lại" → subtitle → input email (icon Mail bên trái) → input password (icon Lock + toggle eye) → "Quên mật khẩu?" link → button "Đăng nhập" → divider → text "Chưa có tài khoản? Đăng ký". Background `@color/bg_dark_secondary`.

- [ ] **LoginActivity.java**: `findViewById` → set click listeners → call `AuthService.login()` → on success: `TokenManager.save()` + `UserManager.save()` → `startActivity(HomeActivity)` + finish. On error: Snackbar tiếng Việt theo cùng pattern lỗi như TS file (translate "Invalid email or password" → "Email hoặc mật khẩu không đúng").

- [ ] **AndroidManifest**: thêm `<activity android:name=".ui.auth.LoginActivity"/>`.

- [ ] Verify thủ công: chạy app trên emulator, login bằng tài khoản test, vào HomeActivity (tạm placeholder).

- [ ] Commit `feat(auth): implement LoginActivity`.

### Task 3.2: SignupActivity

**Files:** `ui/auth/SignupActivity.java`, `res/layout/activity_signup.xml`

- [ ] Replicate `frontend/app/(auth)/signup.tsx`: input userName, email, password, confirm password, button "Đăng ký". Validation client (password length ≥ 6, confirm match).

- [ ] Sau khi register thành công → save token/user → vào HomeActivity.

- [ ] Đăng ký trong AndroidManifest. Commit.

### Task 3.3: ForgotPasswordActivity + ResetPasswordActivity + VerifyEmailActivity

- [ ] 3 activity tương ứng 3 file TS. ResetPasswordActivity nhận deep-link token (nếu có) — implement đơn giản: user copy paste token vào ô. (Deep link Android cần intent-filter custom — nếu user không yêu cầu thì skip, ghi TODO trong code.)

- [ ] Commit `feat(auth): implement password recovery flow`.

---

# PHASE 4 — Home / Discovery (tab 1)

### Task 4.1: HomeActivity layout + bottom nav

**Files:**
- Create: `res/layout/activity_home.xml`, `res/menu/bottom_nav_menu.xml`, `ui/home/HomeActivity.java`, `ui/home/ProductCardAdapter.java`, `res/layout/item_product_card.xml`, `res/layout/item_category_chip.xml`

- [ ] **bottom_nav_menu.xml**: 5 item (Khám phá, Giao dịch, Chatbot, Thông báo, Hồ sơ) với icon Material.

- [ ] **activity_home.xml**: 
  - Top: header logo + cart icon (badge số lượng) + favorites icon
  - Search EditText (icon kính lúp)
  - Toggle BUY/RENT (2 button trong segmented control style)
  - Horizontal RecyclerView categories chip
  - Vertical RecyclerView (GridLayoutManager 2 cột) product cards
  - Bottom: BottomNavigationView (current = Khám phá)

- [ ] **item_product_card.xml**: CardView dark bg, ImageView (Glide), favorite heart (top-left overlay), type badge (top-right), category caption, product name, price + "/day" nếu là ASSET.

- [ ] **ProductCardAdapter**: RecyclerView.Adapter generic — wrap cả Product và Asset thành class chung `DisplayItem` (id, title, price, type, categoryName, primaryImageUrl). Click item → `EquipmentDetailActivity` với extra `id` + `type`.

- [ ] **HomeActivity.java**:
  - `onCreate`: load categories (lưu trong list nội bộ), load products + assets (2 call song song, merge thành `List<DisplayItem>`).
  - Toggle BUY/RENT → filter list rồi `notifyDataSetChanged`.
  - Search → debounce (Handler postDelayed 300ms) → filter local hoặc gọi API search.
  - Pull-to-refresh: SwipeRefreshLayout wrap RecyclerView.

- [ ] AndroidManifest: register `HomeActivity` với `android:launchMode="singleTask"`.

- [ ] Verify: thấy danh sách sản phẩm thật từ backend.

- [ ] Commit `feat(home): implement Discovery tab`.

---

# PHASE 5 — Equipment Detail + Cart + Checkout + Payment

### Task 5.1: EquipmentDetailActivity

**Files:** `ui/equipment/EquipmentDetailActivity.java`, `res/layout/activity_equipment_detail.xml`

- [ ] Layout: ScrollView > image carousel (ViewPager2 nếu nhiều ảnh) > title + price > favorite + share button > description > spec list (LinearLayout key/value) > nếu ASSET: date pickers (start/end) + tính tổng tiền > button "Thêm vào giỏ" / "Thuê ngay".

- [ ] Activity: load by `id` extra, gọi đúng API (product hay asset dựa vào extra `type`). Date picker dùng `MaterialDatePicker`.

- [ ] Commit.

### Task 5.2: CartActivity

**Files:** `ui/cart/CartActivity.java`, `CartItemAdapter.java`, `res/layout/activity_cart.xml`, `item_cart.xml`

- [ ] List CartItem: hình + tên + giá + spinner số lượng (- / +) + nút xoá.
- [ ] Bottom bar sticky: tổng tiền + button "Thanh toán" → CheckoutActivity.
- [ ] Empty state.
- [ ] Commit.

### Task 5.3: CheckoutActivity

**Files:** `ui/checkout/CheckoutActivity.java`, `res/layout/activity_checkout.xml`

- [ ] Form địa chỉ: tỉnh / huyện / xã (3 Spinner nối nhau, gọi PaymentService getProvinces/getDistricts/getWards).
- [ ] Số nhà / ghi chú (EditText).
- [ ] Phương thức thanh toán: radio (COD / MoMo).
- [ ] Hiển thị summary + phí ship (gọi `calculateShippingFee`).
- [ ] Nút "Đặt hàng": call `createOrder()`. Nếu MoMo → `createMoMoPayment()` → mở `payUrl` bằng `Intent.ACTION_VIEW` (Chrome Custom Tabs nếu có thư viện, không thì browser).
- [ ] Sau khi quay lại app: navigate sang `OrderStatusActivity` để poll trạng thái.
- [ ] Commit.

### Task 5.4: PaymentSuccess / PaymentFailed / OrderStatus

**Files:** 3 activity + 3 layout đơn giản.

- [ ] **PaymentSuccessActivity**: tick xanh, text "Thanh toán thành công", button "Xem đơn hàng" → OrderDetailActivity.
- [ ] **PaymentFailedActivity**: icon X đỏ, text lý do, button "Thử lại".
- [ ] **OrderStatusActivity**: poll `getPaymentStatus(orderCode)` mỗi 3s (Handler) tối đa 30 lần → redirect Success/Failed.
- [ ] Commit.

---

# PHASE 6 — Transactions tab (tab 2)

### Task 6.1: TransactionsActivity

**Files:** `ui/transactions/TransactionsActivity.java`, `OrderListAdapter.java`, `RentalListAdapter.java`, `res/layout/activity_transactions.xml`, `item_order.xml`, `item_rental.xml`

- [x] TabLayout 2 tab: "Đơn hàng" / "Thuê thiết bị". Mỗi tab là 1 RecyclerView trong ViewPager2 (hoặc đơn giản: 2 RecyclerView, hide/show).
- [x] Item: status chip (PENDING/SHIPPED/DELIVERED/CANCELLED), tổng tiền, ngày đặt. Click → OrderDetailActivity / RentalDetailActivity.
- [x] BottomNavigationView highlight tab "Giao dịch".
- [ ] Commit.

### Task 6.2: OrderDetailActivity + RentalDetailActivity

**Files:** 2 activity + 2 layout.

- [x] OrderDetail: list items, địa chỉ ship, status timeline, tổng tiền, nút "Theo dõi" (mở `trackOrder()` URL hoặc inline). Nút "Huỷ" nếu PENDING.
- [x] RentalDetail: thông tin asset, ngày thuê/trả, depositFee, totalRentFee, status.
- [ ] Commit.

---

# PHASE 7 — Chatbot tab (tab 3)

### Task 7.1: ChatbotActivity

**Files:** `ui/chatbot/ChatbotActivity.java`, `ChatMessageAdapter.java`, `res/layout/activity_chatbot.xml`, `item_message_user.xml`, `item_message_bot.xml`

- [x] Layout: RecyclerView (messages) bên trên, input EditText + send button bên dưới (sticky bottom). Bottom nav highlight Chatbot.

- [x] Adapter: 2 view type — message user (bubble cam, align right) vs bot (bubble xám, align left).

- [x] **Streaming SSE**: dùng OkHttp raw (không qua Retrofit) gọi POST `/chatbot/stream` với body `{messages: [...]}`. Đọc `responseBody.source().readUtf8Line()` trong loop, parse SSE format `data: <chunk>\n\n`. Append vào message bot cuối cùng và `notifyItemChanged`.

- [x] Cancellation: lưu `Call` reference, cancel khi activity destroy.

- [x] Commit `feat(chatbot): implement streaming chat with OkHttp SSE`.

---

# PHASE 8 — Notifications tab (tab 4)

### Task 8.1: NotificationsActivity

**Files:** `ui/notifications/NotificationsActivity.java`, `NotificationAdapter.java`, `res/layout/activity_notifications.xml`, `item_notification.xml`

- [x] List notifications: icon (theo type: ORDER/PAYMENT/SYSTEM…), title, message, time, dot đỏ nếu chưa đọc.
- [x] Click item → markAsRead + navigate (nếu có deeplink trong notification).
- [x] Toolbar: action "Đánh dấu tất cả đã đọc".
- [x] SwipeRefresh để reload.
- [x] Bottom nav highlight Thông báo. Badge số chưa đọc trên icon.
- [ ] Commit.

---

# PHASE 9 — Profile tab (tab 5) + sub-screens

### Task 9.1: ProfileActivity

**Files:** `ui/profile/ProfileActivity.java`, `res/layout/activity_profile.xml`

- [x] Header: avatar (Glide circle), tên, email, trustScore.
- [x] List menu items (LinearLayout vertical hoặc RecyclerView): Personal Info, Settings, Favorites, My Equipment, Spending Stats, Privacy & Security, Help, Đăng xuất (đỏ).
- [ ] Mỗi item → activity tương ứng.
- [x] Đăng xuất: clear token/user → restart app vào LoginActivity.
- [x] Bottom nav highlight Hồ sơ. Commit.

### Task 9.2 → 9.9: Sub-screens (8 màn hình)

Mỗi sub-screen là 1 task riêng. Đều theo pattern: Activity + layout XML, gọi API, hiển thị/sửa, commit.

- [ ] **9.2 PersonalInfoActivity** — form sửa userName + avatar (image picker). Avatar upload: dùng `ActivityResultContracts.PickVisualMedia`, encode base64 hoặc upload qua endpoint `updateAvatar` (string URL — nếu cần upload file thật thì cần endpoint upload riêng, ghi TODO).
- [ ] **9.3 ChangePasswordActivity** — 3 field (old, new, confirm) + button.
- [ ] **9.4 FavoritesActivity** — RecyclerView grid giống HomeActivity, dùng `FavoriteService.getFavorites()`. Click → EquipmentDetail.
- [ ] **9.5 MyEquipmentActivity** — danh sách asset của user (nếu role là cho thuê).
- [ ] **9.6 SpendingStatsActivity** — đơn giản hoá: hiển thị tổng chi tiêu tháng/năm (tính client từ orders đã load).
- [ ] **9.7 PrivacySecurityActivity** — toggle tĩnh (placeholder, vì backend không có endpoint settings).
- [ ] **9.8 HelpActivity** — FAQ tĩnh dạng ExpandableListView hoặc list TextView.
- [ ] **9.9 SettingsActivity** — toggle dark mode (no-op vì theme đã dark), language (no-op), version info, link Terms/Privacy.

- [ ] Mỗi sub-screen: 1 commit.

---

# PHASE 10 — Final integration & verification

### Task 10.1: BottomNav consistency check

- [ ] Đảm bảo cả 5 tab activity có cùng BottomNavigationView ở dưới và `BottomNavHelper` chuyển đúng activity, không chồng stack vô hạn (test: bấm Discovery → Profile → Discovery → Profile, back chỉ về Home rồi exit).

### Task 10.2: Flow end-to-end thủ công

Chạy backend (`docker compose up -d db backend`), mở Android Studio chạy emulator (API 34, Pixel 6).

- [ ] **Auth**: đăng ký user mới → đăng xuất → đăng nhập lại → forgot password (nhập email, kiểm tra UI flow, không cần email thực sự gửi).
- [ ] **Home**: thấy danh sách sản phẩm, chuyển BUY/RENT, search, click chip category, pull-to-refresh.
- [ ] **Detail → Cart → Checkout → Payment**: thêm 1 product vào giỏ, vào giỏ, đổi số lượng, vào checkout, chọn địa chỉ + COD, tạo đơn → thấy success.
- [ ] **MoMo**: chọn MoMo → mở browser → (test sandbox) → quay lại app → OrderStatus poll → success/failed.
- [ ] **Rental**: chọn 1 asset, chọn ngày, thuê → thấy trong Transactions tab Rental.
- [ ] **Chatbot**: gõ câu hỏi → thấy stream từng token.
- [ ] **Notifications**: thấy list, đánh dấu đã đọc, badge update.
- [ ] **Profile**: vào tất cả 8 sub-screen, không crash, hiển thị đúng dữ liệu.

- [ ] Ghi vào `My_Mobile_App/MIGRATION_PLAN.md` section "Verification log" những gì đã pass và những gì còn lỗi.

### Task 10.3: Lint + build release

- [ ] `./gradlew :app:lint` — fix các warning critical.
- [ ] `./gradlew :app:assembleDebug` — must succeed.
- [ ] (Optional) `./gradlew :app:assembleRelease` (cần keystore — skip nếu chưa có).

### Task 10.4: Cleanup & final commit

- [ ] Xoá log debug thừa, comment TODO không cần.
- [ ] Update `My_Mobile_App/MIGRATION_PLAN.md` checkbox tổng tiến độ.
- [ ] Commit `chore: final cleanup, migration complete`.

---

## Verification Section

**Automated:**
- `./gradlew :app:assembleDebug` — không lỗi compile.
- `./gradlew :app:lint` — không có error level.

**Manual end-to-end** (xem Task 10.2 chi tiết):
1. Backend up: `docker compose up -d db backend` + `curl localhost:8080/api/products?page=0&size=1`.
2. Emulator API 34 + chạy app từ Android Studio.
3. Đi qua flow đầy đủ: Auth → Home → Detail → Cart → Checkout → Order → Chatbot → Notifications → Profile.
4. Quan sát Logcat tag `OkHttp` để verify request/response thật.
5. So sánh từng màn hình bằng mắt với screenshot React Native — UI phải GIỐNG hệt (màu, spacing, font, icon position).

**Cross-check với CLAUDE.md:**
- Think Before Coding: mỗi Activity bắt đầu bằng comment ngắn ghi mục đích + endpoint backend gọi.
- Simplicity First: không tạo Repository/UseCase/DI framework; chỉ Activity ↔ Service trực tiếp.
- Surgical Changes: chỉ sửa file thuộc `My_Mobile_App/` + `docker-compose.yml`. Không động vào `frontend/` hay `backend/`.
- Goal-Driven: mỗi Task đều có "verify" step rõ ràng.

---

## Risks & notes

- **MoMo payment**: cần MoMo sandbox key trong backend `.env` để test thật. Nếu không có, dừng ở bước "mở payUrl" và verify thủ công.
- **Chatbot streaming SSE**: backend phải emit đúng format `data: <chunk>\n\n` với `Content-Type: text/event-stream`. Nếu backend không stream → fallback ChatbotActivity dùng endpoint non-stream (`chatNonStream`).
- **Image upload (avatar)**: backend hiện tại chỉ nhận URL string. Việc upload file ảnh sẽ cần endpoint mới — trong scope này chỉ implement chọn ảnh + lưu URI local + (TODO) upload sau.
- **Deep link reset password**: cần custom intent-filter `myapp://reset?token=...` nếu muốn click link email mở app. Hiện tại implement nhập token thủ công.
- **30+ activity** là khối lượng rất lớn — đề xuất review checkpoint sau mỗi PHASE thay vì chờ end.

---

## Execution Handoff

Sau khi user duyệt plan này:

1. **Recommended:** Dùng `superpowers:subagent-driven-development` — dispatch 1 subagent / Phase với plan này làm reference. Review giữa mỗi Phase.
2. **Alternative:** Inline execution với `superpowers:executing-plans` — checkpoint sau mỗi Phase 1, 3, 5, 7, 9, 10.

Tôi đề xuất Option 1 vì khối lượng lớn (30+ activity) sẽ ngốn context nhanh nếu inline.
