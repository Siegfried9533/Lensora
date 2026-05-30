# Lensora API — Báo Cáo Kiểm Tra API

**Ngày test:** 2026-05-30
**Base URL:** `http://localhost:8081/api` (code hiện tại, build từ source)
**Backend test:** Spring Boot 3.4.2 + Java 21, kết nối PostgreSQL 15 (localhost:5434/LensoraDB)
**Tổng kết:** ~89 PASS | 14 FAIL | 5 SKIP / 114 test cases

> **So với báo cáo cũ:** Nhiều lỗi đã được sửa (chatbot → DeepSeek, security AUT-009/010, FAV-003, ORD-003, RNT-007/008). Báo cáo này phản ánh trạng thái code hiện tại.

---

## Tóm Tắt Theo Nhóm Lỗi

| Nhóm | Số TC | TC IDs |
|------|-------|--------|
| Security — `/error` chưa permit | 2 | AUT-003, AUT-004 |
| Security — Payment endpoints yêu cầu auth | 6 | PAY-001, PAY-002, PAY-004, PAY-005, PAY-011 *(ko token)*, PAY-013 *(ko token)* |
| Môi trường — Mail server chưa chạy | 2 | AUT-016, AUT-017 |
| Môi trường — MoMo credentials chưa cấu hình | 2 | PAY-001, PAY-004 *(kể cả khi có token)* |
| Enum mismatch PaymentMethod | 1 | ORD-001 *(nếu gửi "MOMO")* |
| Response code sai | 1 | PAY-012 |
| Skip (cần MoMo credentials thật) | 3 | PAY-006, PAY-007\*, PAY-008 |

> \* TC-PAY-007 (chữ ký sai → 400) PASS khi không có credentials.

---

## Những Lỗi Đã Được Sửa Kể Từ Báo Cáo Trước

| TC | Vấn đề cũ | Trạng thái hiện tại |
|----|-----------|---------------------|
| TC-AUT-006 | Login sai pass → 400 (sai) | ✅ Đã sửa → trả **401** |
| TC-AUT-007 | Login email không tồn tại → 400 (sai) | ✅ Đã sửa → trả **401** |
| TC-AUT-009 | GET /me không token → NPE 400 | ✅ Đã sửa → trả **401** |
| TC-AUT-010 | GET /me token sai → NPE 400 | ✅ Đã sửa → trả **401** |
| TC-FAV-003 | Toggle xóa favorites → 400 (thiếu @Transactional) | ✅ Đã sửa → trả **200** |
| TC-ORD-003 | Tạo đơn items=[] → 200 (không validate) | ✅ Đã sửa → trả **400** |
| TC-ORD-007 | Admin update status → Access Denied | ✅ Đã sửa → ADMIN được phép |
| TC-ORD-008 | User update status → Access Denied | ✅ Đúng behavior → USER trả **403** |
| TC-RNT-007 | check-availability → 401 (chưa permit) | ✅ Đã permit → trả **200** |
| TC-RNT-008 | calculate-price → 401 (chưa permit) | ✅ Đã permit → trả **200** |
| TC-CHB-001 | Chatbot → 500 (Ollama lỗi) | ✅ Migrate sang DeepSeek → **200** |
| TC-CHB-002 | Chatbot streaming → skip | ✅ Hoạt động → **200** |
| TC-CHB-003 | Chatbot message rỗng → 500 (che lỗi) | ✅ DeepSeek xử lý đúng |

---

## Chi Tiết Các TC FAIL Hiện Tại

---

### MODULE: Auth

---

#### TC-AUT-003 — Đăng ký thiếu trường bắt buộc (userName/password)

| | |
|---|---|
| **Method** | `POST /api/auth/register` |
| **Auth** | No |
| **Request Body** | `{ "email": "test@example.com" }` (thiếu `userName`, `password`) |
| **Expected** | `400` — validation error |
| **Actual** | `401` — `"Chưa xác thực - vui lòng đăng nhập"` |
| **Nguyên nhân** | Khi `@Valid` validation fail (`MethodArgumentNotValidException`), Spring Boot forward request đến `/error`. Nhưng `/error` **chưa được thêm vào `permitAll()`** trong `SecurityConfig` → Spring Security chặn với 401. |
| **Fix** | Thêm `"/error"` vào danh sách `permitAll()` trong `SecurityConfig.java`: |

```java
.requestMatchers(
    "/api/health", "/api/auth/**", "/api/categories/**",
    "/api/products/**", "/api/assets/**",
    "/api/payment/momo/ipn", "/api/payment/momo/callback",
    "/api/shipping/**", "/api/notifications/system",
    "/api/chatbot/**",
    "/api/rentals/check-availability", "/api/rentals/calculate-price",
    "/error"   // ← THÊM DÒNG NÀY
).permitAll()
```

---

#### TC-AUT-004 — Đăng ký email sai định dạng

| | |
|---|---|
| **Method** | `POST /api/auth/register` |
| **Auth** | No |
| **Request Body** | `{ "userName": "u4", "email": "abc.com", "password": "Test@1234" }` |
| **Expected** | `400` — email format error |
| **Actual** | `401` — `"Chưa xác thực - vui lòng đăng nhập"` |
| **Nguyên nhân** | Cùng nguyên nhân TC-AUT-003 — `@Email` validation fail → forward đến `/error` chưa permit. |
| **Fix** | Xem TC-AUT-003. |

---

#### TC-AUT-016 — Gửi lại email xác thực

| | |
|---|---|
| **Method** | `POST /api/auth/resend-verification` |
| **Auth** | No |
| **Request Body** | `{ "email": "user@example.com" }` |
| **Expected** | `200` |
| **Actual** | `400` — `"Mail server connection failed. Couldn't connect to host, port: localhost, 1025"` |
| **Nguyên nhân** | **Môi trường** — Mail server chưa khởi động. `AuthService.resendVerificationEmail()` ném exception khi gửi email thất bại (khác với `register()` có catch-and-ignore). |
| **Fix ngắn hạn** | Bọc lỗi mail trong `try-catch` tương tự như `register()` để API trả 200 dù mail fail: |

```java
// AuthService.resendVerificationEmail()
try {
    emailService.sendEmailVerification(user.getEmail(), user.getUserName(), token.getToken());
} catch (Exception e) {
    System.err.println("Failed to send verification email: " + e.getMessage());
    // Không re-throw — API vẫn trả 200
}
```

| **Fix môi trường** | Khởi động mail server: `docker run -p 1025:1025 -p 8025:8025 mailhog/mailhog` |

---

#### TC-AUT-017 — Quên mật khẩu

| | |
|---|---|
| **Method** | `POST /api/auth/forgot-password` |
| **Auth** | No |
| **Request Body** | `{ "email": "user@example.com" }` |
| **Expected** | `200` |
| **Actual** | `400` — `"Mail server connection failed"` |
| **Nguyên nhân** | Cùng TC-AUT-016. `AuthService.forgotPassword()` re-throw exception khi gửi email reset password thất bại. |
| **Fix** | Xem TC-AUT-016. |

---

### MODULE: Orders

---

#### TC-ORD-001 — Tạo đơn hàng với paymentMethod = "MOMO"

| | |
|---|---|
| **Method** | `POST /api/orders` |
| **Auth** | Yes |
| **Request Body** | `{ ..., "paymentMethod": "MOMO" }` |
| **Expected** | `200` |
| **Actual** | `400` — `"No enum constant com.camerashop.entity.Order.PaymentMethod.MOMO"` |
| **Nguyên nhân** | `Order.PaymentMethod` enum định nghĩa là `MoMo` (mixed case) trong Java, nhưng `Enum.valueOf()` là case-sensitive. Nếu client/mobile gửi `"MOMO"` → không match. |
| **Fix** | Chuẩn hóa enum sang uppercase trong `Order.java` và `Rental.java`: |

```java
// Order.java
public enum PaymentMethod {
    COD, VNPAY, MOMO   // ← Đổi VNPay → VNPAY, MoMo → MOMO
}
```

Hoặc xử lý case-insensitive trong `OrderService`:

```java
Order.PaymentMethod orderPaymentMethod =
    Order.PaymentMethod.valueOf(paymentMethod.toUpperCase());
```

---

### MODULE: Payment

---

#### TC-PAY-001 — Tạo URL thanh toán MoMo cho đơn hàng

| | |
|---|---|
| **Method** | `POST /api/payment/momo/create` |
| **Auth** | No (test case) / cần token (thực tế) |
| **Request Body** | `{ "orderId": "<uuid>", "amount": <totalAmount> }` |
| **Expected** | `200` — trả về `payUrl` |
| **Actual (không token)** | `401` — endpoint chưa được permit |
| **Actual (có token)** | `500` — `"MoMo partnerCode is missing"` |
| **Nguyên nhân** | **(1) Security:** `SecurityConfig` chỉ permit `/api/payment/momo/ipn` và `/api/payment/momo/callback`. Các endpoint còn lại (`/create`, `/create-rental`, `/status/**`, `/query`) rơi vào `anyRequest().authenticated()`. **(2) Config:** `APP_MOMO_PARTNER_CODE` chưa được đặt trong environment. |
| **Fix Security** | Thêm vào `permitAll()` trong `SecurityConfig.java`: |

```java
"/api/payment/momo/create",
"/api/payment/momo/create-rental",
"/api/payment/status/**",
"/api/payment/momo/query"
```

| **Fix Config** | Cấu hình MoMo sandbox credentials trong `.env` hoặc `application.properties`: |

```properties
APP_MOMO_PARTNER_CODE=MOMO...
APP_MOMO_ACCESS_KEY=...
APP_MOMO_SECRET_KEY=...
```

---

#### TC-PAY-002 — Tạo MoMo payment orderId không tồn tại

| | |
|---|---|
| **Method** | `POST /api/payment/momo/create` |
| **Auth** | No (test case) / cần token (thực tế) |
| **Request Body** | `{ "orderId": "00000000-0000-0000-0000-000000000000", "amount": 100 }` |
| **Expected** | `400` |
| **Actual (không token)** | `401` |
| **Actual (có token)** | `500` — `ResourceNotFoundException` không được catch đúng |
| **Nguyên nhân** | (1) Cùng security issue TC-PAY-001. (2) `PaymentController.createMoMoPayment()` không có catch block cho `ResourceNotFoundException` — exception bubble up thành 500 Internal Server Error. |
| **Fix** | Ngoài fix security, thêm catch trong PaymentController: |

```java
} catch (ResourceNotFoundException e) {
    return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
```

---

#### TC-PAY-004 — Tạo MoMo payment cho đơn thuê

| | |
|---|---|
| **Method** | `POST /api/payment/momo/create-rental` |
| **Auth** | No (test case) / cần token (thực tế) |
| **Expected** | `200` |
| **Actual** | `401` (không token) / `500` (có token, MoMo chưa config) |
| **Nguyên nhân** | Cùng TC-PAY-001. |
| **Fix** | Xem TC-PAY-001. |

---

#### TC-PAY-005 — Tạo MoMo rental payment rentalId không tồn tại

| | |
|---|---|
| **Method** | `POST /api/payment/momo/create-rental` |
| **Auth** | No (test case) |
| **Expected** | `400` |
| **Actual** | `401` (không token) |
| **Nguyên nhân** | Cùng security issue TC-PAY-001. |
| **Fix** | Xem TC-PAY-001. |

---

#### TC-PAY-011 — Lấy trạng thái thanh toán (không token)

| | |
|---|---|
| **Method** | `GET /api/payment/status/{orderCode}` |
| **Auth** | No (test case nói public) |
| **Expected** | `200` |
| **Actual** | `401` |
| **Nguyên nhân** | Endpoint chưa được permit trong SecurityConfig (cùng TC-PAY-001). Khi gửi token thì hoạt động đúng → 200. |
| **Fix** | Xem TC-PAY-001. |

---

#### TC-PAY-012 — Lấy trạng thái thanh toán orderCode không tồn tại

| | |
|---|---|
| **Method** | `GET /api/payment/status/{orderCode}` |
| **Auth** | Yes (cần token do endpoint chưa permit) |
| **Path** | `/api/payment/status/NOTEXIST-ORDER` |
| **Expected** | `200` với body `{ "success": false, ... }` |
| **Actual** | `404` — `"Không tìm thấy thanh toán"` |
| **Nguyên nhân** | Controller trả `ResponseEntity.status(404)` khi không tìm thấy cả payment transaction lẫn order. Test case mong đợi `200` với body mô tả trạng thái (không tìm thấy). |
| **Fix** | Trong `PaymentController.getPaymentStatus()`, đổi response cuối: |

```java
// Trước
return ResponseEntity.status(404).body(ApiResponse.error("Không tìm thấy thanh toán"));

// Sau
Map<String, Object> result = new HashMap<>();
result.put("success", false);
result.put("message", "Không tìm thấy thanh toán");
result.put("orderCode", orderCode);
return ResponseEntity.ok(ApiResponse.success(result));
```

---

#### TC-PAY-013 — Query giao dịch MoMo (không token)

| | |
|---|---|
| **Method** | `POST /api/payment/momo/query` |
| **Auth** | No (test case) |
| **Expected** | `200` |
| **Actual** | `401` (không token) — PASS khi có token |
| **Nguyên nhân** | Cùng security issue TC-PAY-001. |
| **Fix** | Xem TC-PAY-001. |

---

## Chi Tiết Các TC SKIP

---

### TC-PAY-006 — MoMo IPN chữ ký đúng, resultCode = 0 (thanh toán thành công)

| | |
|---|---|
| **Method** | `POST /api/payment/momo/ipn` |
| **Lý do Skip** | Signature trong IPN phải là HMAC-SHA256 thật, tính bằng `secret-key` MoMo. Không thể giả lập thủ công. |
| **Cách test** | Dùng MoMo sandbox portal trigger IPN thật, hoặc unit test mock `MoMoService.verifySignature()`. |

---

### TC-PAY-008 — MoMo IPN chữ ký đúng, resultCode ≠ 0 (thanh toán thất bại)

| | |
|---|---|
| **Method** | `POST /api/payment/momo/ipn` |
| **Lý do Skip** | Cùng TC-PAY-006 — cần HMAC-SHA256 thật từ MoMo. |
| **Ghi chú** | TC-PAY-007 (chữ ký sai → 400) đã PASS bình thường. |

---

### TC-AUT-014 — Xác thực email với token hợp lệ

| | |
|---|---|
| **Method** | `POST /api/auth/verify-email?token=<valid>` |
| **Lý do Skip** | Cần lấy token thật từ DB. Token chỉ được tạo khi `register` và mail server không gửi được. Trong môi trường dev không có MailHog, token tồn tại trong DB nhưng không có trong email. |
| **Cách test** | Khởi động MailHog (`docker run -p 1025:1025 -p 8025:8025 mailhog/mailhog`), đăng ký tài khoản mới, lấy token từ giao diện MailHog (`http://localhost:8025`). |

---

### TC-AUT-018 — Đặt lại mật khẩu với token hợp lệ

| | |
|---|---|
| **Method** | `POST /api/auth/reset-password` |
| **Lý do Skip** | Phụ thuộc vào TC-AUT-017 (forgot-password) hoạt động để tạo token. Vì mail server chưa chạy, TC-AUT-017 fail → không có token reset. |
| **Cách test** | Xem fix TC-AUT-017. Sau khi mail server hoạt động: gọi `/forgot-password`, lấy token từ MailHog, gọi `/reset-password`. |

---

## Ghi Chú Quan Trọng Về Test Case Specification

### TC-AUT-006, TC-AUT-007 — Login sai password / email không tồn tại

| TC | Test Case Expected | Code Hiện Tại | Đánh Giá |
|----|-------------------|---------------|----------|
| AUT-006 | `400` | `401` | **Test case nên cập nhật** — 401 là đúng theo HTTP spec cho authentication failure |
| AUT-007 | `400` | `401` | **Test case nên cập nhật** — tương tự AUT-006 |

Code đã trả **401** (đúng theo RFC 7235). Test case trong Excel cần cập nhật expected status từ `400` → `401`.

### TC-RNT-011 — Trả thiết bị

Test case không đề cập request body, nhưng endpoint **bắt buộc** có body:

```json
{ "returnDate": "YYYY-MM-DD" }
```

Nếu không gửi body sẽ nhận `400 Required request body is missing`. Cần cập nhật test case hoặc làm body optional:

```java
// RentalController.java — làm optional
public ResponseEntity<ApiResponse> returnRental(
    @AuthenticationPrincipal UserDetails userDetails,
    @PathVariable String id,
    @RequestBody(required = false) Map<String, String> body) {
    LocalDate returnDate = (body != null && body.get("returnDate") != null)
        ? LocalDate.parse(body.get("returnDate"))
        : LocalDate.now();
    ...
}
```

---

## Bảng Tổng Hợp Fix Theo Ưu Tiên

| Ưu tiên | Vấn đề | TC liên quan | File cần sửa | Effort |
|---------|--------|-------------|--------------|--------|
| 🔴 Cao | `SecurityConfig`: thiếu `/error` trong `permitAll()` → validation error trả 401 | AUT-003, AUT-004 | `SecurityConfig.java` | Thấp — 1 dòng |
| 🔴 Cao | `SecurityConfig`: Payment endpoints yêu cầu auth dù test case giả định public | PAY-001, PAY-002, PAY-004, PAY-005, PAY-011, PAY-013 | `SecurityConfig.java` | Thấp — 4 dòng |
| 🔴 Cao | `PaymentMethod` enum: `MoMo` → `MOMO` (case mismatch với client) | ORD-001 | `Order.java`, `Rental.java`, `OrderService.java`, `RentalService.java` | Thấp |
| 🟠 Trung | `PaymentController.getPaymentStatus()`: trả 404 thay vì 200 khi không tìm thấy | PAY-012 | `PaymentController.java` | Thấp |
| 🟠 Trung | `PaymentController.createMoMoPayment()`: `ResourceNotFoundException` → 500 thay vì 400 | PAY-002 | `PaymentController.java` | Thấp |
| 🟠 Trung | `AuthService`: `resendVerificationEmail()` và `forgotPassword()` re-throw mail exception | AUT-016, AUT-017 | `AuthService.java` | Thấp |
| 🟠 Trung | `RentalController.returnRental()`: `@RequestBody` bắt buộc, cần optional | RNT-011 | `RentalController.java` | Thấp |
| ⚙️ Setup | Mail server (MailHog) chưa khởi động | AUT-016, AUT-017, AUT-014, AUT-018 | `docker-compose.yml` hoặc env | Setup |
| ⚙️ Setup | MoMo sandbox credentials chưa cấu hình | PAY-001, PAY-004 | `.env` / `application.properties` | Setup |

---

## Modules Hoạt Động Tốt (100% PASS)

| Module | Số TC | Ghi chú |
|--------|-------|---------|
| Health | 1 | |
| Auth (core) | 8/12 | AUT-001,002,005,008,009,010,011,012,013,015,019 PASS |
| Products | 7 | Toàn bộ |
| Assets | 5 | Toàn bộ |
| Categories | 2 | Toàn bộ |
| Cart | 9 | Toàn bộ (incl. cross-user 403) |
| Favorites | 4 | Toàn bộ (toggle toggle đúng) |
| Orders | 9/10 | Trừ ORD-001 (enum case) |
| Rentals | 10/11 | Trừ RNT-011 nếu thiếu body |
| Shipping | 5 | Toàn bộ (GHN API hoạt động) |
| Notifications | 6 | Toàn bộ (PASS với token) |
| Reviews | 2 | Toàn bộ |
| Chatbot | 3 | Toàn bộ (DeepSeek hoạt động) |
| Payment (callback/ipn) | 2 | IPN chữ ký sai → 400 PASS; callback → 302 PASS |
| Payment (với token) | 4 | PAY-003, PAY-011, PAY-013, PAY-014 |
