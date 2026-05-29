# Lensora API — Báo Cáo Lỗi Test

**Ngày test:** 2026-05-30  
**Base URL:** `http://localhost:8080/api`  
**Tổng kết:** 62 PASS | 24 FAIL | 5 SKIP (tổng 91 TC chạy được)

---

## Tóm Tắt Theo Nhóm Lỗi

| Nhóm | Số TC | TC IDs |
|------|-------|--------|
| HTTP status code sai (400 thay vì 404) | 8 | PRD-006, AST-004, CRT-004, CRT-008, ORD-006, RNT-006, PAY-002, PAY-005 |
| Security / Authentication | 6 | AUT-003, AUT-004, AUT-006, AUT-007, AUT-009, AUT-010 |
| Business logic | 5 | CRT-006, FAV-003, ORD-003, ORD-007, ORD-008 |
| Thiết kế response sai | 2 | PAY-012, RNT-007/008 |
| Môi trường (mail/MoMo/Ollama) | 5 | AUT-016, AUT-017, PAY-001, PAY-004, CHB-001 |
| Validation bị che bởi lỗi hệ thống | 1 | CHB-003 |
| Skip (cần môi trường đặc biệt) | 5 | PAY-006, PAY-008, PAY-009, PAY-010, CHB-002 |

---

## Chi Tiết Các TC FAIL

### MODULE: Auth

---

#### TC-AUT-003 — Đăng ký bỏ trống email/password
| | |
|---|---|
| **Method** | `POST /api/auth/register` |
| **Auth** | No |
| **Request Body** | `{ "userName": "u3", "email": "", "password": "", "fullName": "U3" }` |
| **Expected** | `400` — lỗi validation |
| **Actual** | `401` — `"Chưa xác thực - vui lòng đăng nhập"` |
| **Nguyên nhân** | `SecurityConfig` chưa thêm `/api/auth/register` vào `permitAll()`. Spring Security chặn request trước khi đến controller nên validation không chạy. |
| **Fix** | Đảm bảo `/api/auth/**` đã có trong danh sách `permitAll` — kiểm tra lại pattern matcher. |

---

#### TC-AUT-004 — Đăng ký email sai định dạng
| | |
|---|---|
| **Method** | `POST /api/auth/register` |
| **Auth** | No |
| **Request Body** | `{ "userName": "u4", "email": "abc.com", "password": "Test@12345", "fullName": "U4" }` |
| **Expected** | `400` — lỗi format email |
| **Actual** | `401` — `"Chưa xác thực - vui lòng đăng nhập"` |
| **Nguyên nhân** | Cùng nguyên nhân TC-AUT-003 — Security filter chặn trước khi validation chạy. |
| **Fix** | Xem TC-AUT-003. |

---

#### TC-AUT-006 — Đăng nhập sai password
| | |
|---|---|
| **Method** | `POST /api/auth/login` |
| **Auth** | No |
| **Request Body** | `{ "email": "testrunner@lensora.test", "password": "WrongPassword" }` |
| **Expected** | `401` — sai mật khẩu |
| **Actual** | `400` — `"Email hoặc mật khẩu không hợp lệ"` |
| **Nguyên nhân** | Controller/Service ném exception với HTTP 400 thay vì 401 cho trường hợp sai credentials. |
| **Fix** | Sửa exception handler: sai credentials nên trả `401 Unauthorized`, không phải `400 Bad Request`. |

---

#### TC-AUT-007 — Đăng nhập email không tồn tại
| | |
|---|---|
| **Method** | `POST /api/auth/login` |
| **Auth** | No |
| **Request Body** | `{ "email": "nope@x.com", "password": "Test@12345" }` |
| **Expected** | `404` — tài khoản không tồn tại |
| **Actual** | `400` — `"Email hoặc mật khẩu không hợp lệ"` |
| **Nguyên nhân** | API không phân biệt giữa "email không tồn tại" và "sai password" (an toàn bảo mật), nhưng HTTP status nên là `401` hoặc `404` thay vì `400`. |
| **Fix** | Trả `401` thay vì `400`. Nếu muốn phân biệt thì trả `404` khi email không tồn tại, `401` khi sai password. |

---

#### TC-AUT-009 — Lấy thông tin user không có token
| | |
|---|---|
| **Method** | `GET /api/auth/me` |
| **Auth** | No (không gửi header Authorization) |
| **Expected** | `401` — Unauthorized |
| **Actual** | `400` — `"Cannot invoke UserDetails.getUsername() because userDetails is null"` |
| **Nguyên nhân** | `JwtAuthFilter` bỏ qua request không có token (gọi `filterChain.doFilter()` rồi `return`), nhưng controller vẫn cố gọi `SecurityContextHolder.getAuthentication()` và không kiểm tra null trước khi dùng. Kết quả là `NullPointerException` bị global handler bắt và trả `400`. |
| **Fix** | Trong controller `/auth/me`, thêm kiểm tra `authentication == null` hoặc `!authentication.isAuthenticated()` và throw `401`. Hoặc sửa `JwtAuthFilter` để trả `401` khi không có token ở endpoint cần auth. |

---

#### TC-AUT-010 — Lấy thông tin user token không hợp lệ
| | |
|---|---|
| **Method** | `GET /api/auth/me` |
| **Auth** | `Authorization: Bearer invalid_token_string` |
| **Expected** | `401` — token không hợp lệ |
| **Actual** | `400` — `"Cannot invoke UserDetails.getUsername() because userDetails is null"` |
| **Nguyên nhân** | Cùng TC-AUT-009 — `JwtAuthFilter` không set authentication khi token sai, controller NPE. |
| **Fix** | Xem TC-AUT-009. Ngoài ra, `JwtAuthFilter` nên log/handle exception khi parse token thất bại thay vì im lặng bỏ qua. |

---

#### TC-AUT-016 — Gửi lại email xác thực
| | |
|---|---|
| **Method** | `POST /api/auth/resend-verification` |
| **Auth** | No |
| **Request Body** | `{ "email": "testrunner@lensora.test" }` |
| **Expected** | `200` |
| **Actual** | `400` — `"Mail server connection failed. Couldn't connect to host, port: localhost, 1025"` |
| **Nguyên nhân** | **Môi trường** — Mail server (MailHog/Mailtrap) chưa được khởi động. Config `spring.mail.host=localhost:1025` không kết nối được. |
| **Fix** | Khởi động mail server local (ví dụ: `docker run -p 1025:1025 -p 8025:8025 mailhog/mailhog`) hoặc cấu hình SMTP thật trong `.env`. |

---

#### TC-AUT-017 — Quên mật khẩu
| | |
|---|---|
| **Method** | `POST /api/auth/forgot-password` |
| **Auth** | No |
| **Request Body** | `{ "email": "testrunner@lensora.test" }` |
| **Expected** | `200` |
| **Actual** | `400` — `"Mail server connection failed"` |
| **Nguyên nhân** | Cùng TC-AUT-016 — mail server không hoạt động. |
| **Fix** | Xem TC-AUT-016. |

---

### MODULE: Products

---

#### TC-PRD-006 — Chi tiết sản phẩm ID không tồn tại
| | |
|---|---|
| **Method** | `GET /api/products/{id}` |
| **Auth** | No |
| **Path** | `/api/products/00000000-0000-0000-0000-000000000000` |
| **Expected** | `404` |
| **Actual** | `400` — `{ "success": false, "message": "Không tìm thấy sản phẩm" }` |
| **Nguyên nhân** | Global exception handler map `NotFoundException` (hoặc custom exception) về HTTP `400` thay vì `404`. |
| **Fix** | Trong `GlobalExceptionHandler`, sửa annotation của handler "không tìm thấy X" thành `@ResponseStatus(HttpStatus.NOT_FOUND)` hoặc return `ResponseEntity.status(404)`. |

---

### MODULE: Assets

---

#### TC-AST-004 — Chi tiết tài sản ID không tồn tại
| | |
|---|---|
| **Method** | `GET /api/assets/{id}` |
| **Auth** | No |
| **Path** | `/api/assets/00000000-0000-0000-0000-000000000000` |
| **Expected** | `404` |
| **Actual** | `400` — `{ "success": false, "message": "Không tìm thấy thiết bị cho thuê" }` |
| **Nguyên nhân** | Cùng TC-PRD-006 — exception handler trả sai HTTP status. |
| **Fix** | Xem TC-PRD-006. |

---

### MODULE: Cart

---

#### TC-CRT-004 — Thêm vào giỏ hàng itemId không tồn tại
| | |
|---|---|
| **Method** | `POST /api/cart/add` |
| **Auth** | Yes |
| **Request Body** | `{ "itemId": "00000000-0000-0000-0000-000000000000", "type": "PRODUCT", "quantity": 1 }` |
| **Expected** | `404` |
| **Actual** | `400` — `{ "success": false, "message": "Không tìm thấy sản phẩm" }` |
| **Nguyên nhân** | Cùng nguyên nhân TC-PRD-006. |
| **Fix** | Xem TC-PRD-006. |

---

#### TC-CRT-006 — Cập nhật số lượng = 0
| | |
|---|---|
| **Method** | `PUT /api/cart/{id}/quantity` |
| **Auth** | Yes |
| **Request Body** | `{ "quantity": 0 }` |
| **Expected** | `400` — từ chối quantity không hợp lệ |
| **Actual** | `200` — cập nhật thành công với `quantity: 0` |
| **Nguyên nhân** | Service/controller không validate `quantity > 0` trước khi lưu. |
| **Fix** | Thêm validation: `if (quantity <= 0) throw new BadRequestException("Số lượng phải lớn hơn 0")` hoặc dùng `@Min(1)` trên DTO. |

---

#### TC-CRT-008 — Xóa cart item không tồn tại
| | |
|---|---|
| **Method** | `DELETE /api/cart/{id}` |
| **Auth** | Yes |
| **Path** | `/api/cart/00000000-0000-0000-0000-000000000000` |
| **Expected** | `404` |
| **Actual** | `400` — `{ "success": false, "message": "Không tìm thấy sản phẩm trong giỏ hàng" }` |
| **Nguyên nhân** | Cùng TC-PRD-006. |
| **Fix** | Xem TC-PRD-006. |

---

### MODULE: Favorites

---

#### TC-FAV-003 — Toggle xóa khỏi danh sách yêu thích
| | |
|---|---|
| **Method** | `POST /api/favorites/toggle` |
| **Auth** | Yes |
| **Request Body** | `{ "itemId": "<productId>", "type": "PRODUCT" }` (gọi lần 2 sau khi đã add) |
| **Expected** | `200` — item bị xóa khỏi favorites |
| **Actual** | `400` — `{ "success": false, "message": "Executing an update/delete query" }` |
| **Nguyên nhân** | Repository method thực hiện DELETE/UPDATE query nhưng **thiếu `@Transactional`** trên service method hoặc repository method. Spring JPA yêu cầu `@Transactional` cho modifying queries. |
| **Fix** | Thêm `@Transactional` vào service method `toggleFavorite()` và/hoặc thêm `@Modifying @Transactional` vào repository method DELETE. |

---

### MODULE: Orders

---

#### TC-ORD-003 — Tạo đơn hàng với items rỗng
| | |
|---|---|
| **Method** | `POST /api/orders` |
| **Auth** | Yes |
| **Request Body** | `{ "shippingAddress": "...", "paymentMethod": "COD", "shippingFee": 0, "items": [] }` |
| **Expected** | `400` — không cho phép tạo đơn không có sản phẩm |
| **Actual** | `200` — tạo thành công đơn hàng với `totalAmount: 0` và `items: []` |
| **Nguyên nhân** | Service không validate danh sách items trước khi tạo đơn hàng. |
| **Fix** | Thêm validation: `if (items == null || items.isEmpty()) throw new BadRequestException("Đơn hàng phải có ít nhất 1 sản phẩm")`. |

---

#### TC-ORD-006 — Chi tiết đơn hàng ID không tồn tại
| | |
|---|---|
| **Method** | `GET /api/orders/{id}` |
| **Auth** | Yes |
| **Path** | `/api/orders/00000000-0000-0000-0000-000000000000` |
| **Expected** | `404` |
| **Actual** | `400` — `{ "success": false, "message": "Không tìm thấy đơn hàng" }` |
| **Nguyên nhân** | Cùng TC-PRD-006. |
| **Fix** | Xem TC-PRD-006. |

---

#### TC-ORD-007 — Cập nhật trạng thái đơn hàng (SHIPPED)
| | |
|---|---|
| **Method** | `PATCH /api/orders/{orderId}/status` |
| **Auth** | Yes (user thường) |
| **Request Body** | `{ "status": "SHIPPED" }` |
| **Expected** | `200` |
| **Actual** | `400` — `{ "message": "Access Denied" }` |
| **Nguyên nhân** | Endpoint `PATCH /orders/{id}/status` được bảo vệ chỉ cho ADMIN hoặc có `@PreAuthorize("hasRole('ADMIN')")`, nhưng test case thiết kế cho cả USER có thể gọi. |
| **Fix** | Nếu chỉ ADMIN được update status: cập nhật test case (đổi precondition thành cần token ADMIN). Nếu USER được tự cancel đơn của mình: sửa SecurityConfig/controller để phân quyền đúng theo từng status transition. |

---

#### TC-ORD-008 — Cập nhật trạng thái đơn hàng (CANCELLED)
| | |
|---|---|
| **Method** | `PATCH /api/orders/{orderId}/status` |
| **Auth** | Yes (user thường) |
| **Request Body** | `{ "status": "CANCELLED" }` |
| **Expected** | `200` |
| **Actual** | `400` — `{ "message": "Access Denied" }` |
| **Nguyên nhân** | Cùng TC-ORD-007. |
| **Fix** | Xem TC-ORD-007. |

---

### MODULE: Rentals

---

#### TC-RNT-006 — Chi tiết đơn thuê ID không tồn tại
| | |
|---|---|
| **Method** | `GET /api/rentals/{id}` |
| **Auth** | Yes |
| **Path** | `/api/rentals/00000000-0000-0000-0000-000000000000` |
| **Expected** | `404` |
| **Actual** | `400` — `{ "success": false, "message": "Không tìm thấy đơn thuê" }` |
| **Nguyên nhân** | Cùng TC-PRD-006. |
| **Fix** | Xem TC-PRD-006. |

---

#### TC-RNT-007 — Kiểm tra tài sản còn trống (check-availability)
| | |
|---|---|
| **Method** | `GET /api/rentals/check-availability?assetId=...&startDate=...&endDate=...` |
| **Auth** | Test case ghi "No" — thực tế endpoint yêu cầu auth |
| **Expected** | `200` |
| **Actual** | `401` — `"Chưa xác thực - vui lòng đăng nhập"` |
| **Nguyên nhân** | Endpoint không được liệt kê trong `permitAll()` của `SecurityConfig`. Test case file thiết kế Auth=No nhưng backend yêu cầu token. |
| **Fix** | **Chọn một trong hai:** (1) Thêm `/api/rentals/check-availability` vào `permitAll()` — hợp lý vì đây là thông tin public để user xem trước khi đăng nhập. (2) Cập nhật test case: đổi Auth thành Yes. |

---

#### TC-RNT-008 — Tính giá thuê (calculate-price)
| | |
|---|---|
| **Method** | `POST /api/rentals/calculate-price` |
| **Auth** | Test case ghi "No" — thực tế endpoint yêu cầu auth |
| **Request Body** | `{ "assetId": "...", "startDate": "2026-08-01", "endDate": "2026-08-05" }` |
| **Expected** | `200` |
| **Actual** | `401` — `"Chưa xác thực - vui lòng đăng nhập"` |
| **Nguyên nhân** | Cùng TC-RNT-007. |
| **Fix** | Xem TC-RNT-007. |

---

### MODULE: Payment

---

#### TC-PAY-001 — Tạo thanh toán MoMo (order hợp lệ)
| | |
|---|---|
| **Method** | `POST /api/payment/momo/create` |
| **Auth** | Yes |
| **Request Body** | `{ "orderId": "<uuid>", "amount": 145030000, "orderInfo": "...", "requestType": "captureWallet" }` |
| **Expected** | `200` — trả về `payUrl` |
| **Actual** | `500` — `"Tạo URL thanh toán MoMo thất bại: The merchant configuration is incorrect or the account is inactive."` |
| **Nguyên nhân** | **Môi trường** — Tài khoản MoMo sandbox (`MOMOBKUN20180810`) đã hết hiệu lực hoặc cấu hình sai. |
| **Fix** | Đăng nhập MoMo developer portal, tạo/lấy lại sandbox credentials mới và cập nhật `app.momo.partner-code`, `app.momo.access-key`, `app.momo.secret-key` trong `application.properties`. |

---

#### TC-PAY-002 — Tạo MoMo payment orderId không tồn tại
| | |
|---|---|
| **Method** | `POST /api/payment/momo/create` |
| **Auth** | Yes |
| **Request Body** | `{ "orderId": "00000000-...", "amount": 1000000, ... }` |
| **Expected** | `404` |
| **Actual** | `400` — `"Không tìm thấy đơn hàng: 00000000-..."` |
| **Nguyên nhân** | Cùng TC-PRD-006 — exception handler trả 400. |
| **Fix** | Xem TC-PRD-006. |

---

#### TC-PAY-004 — Tạo MoMo payment cho đơn thuê (hợp lệ)
| | |
|---|---|
| **Method** | `POST /api/payment/momo/create-rental` |
| **Auth** | Yes |
| **Request Body** | `{ "rentalId": "<uuid>", "orderInfo": "..." }` |
| **Expected** | `200` — trả về `payUrl` |
| **Actual** | `500` — MoMo sandbox inactive |
| **Nguyên nhân** | Cùng TC-PAY-001 — môi trường MoMo sandbox. |
| **Fix** | Xem TC-PAY-001. |

---

#### TC-PAY-005 — Tạo MoMo rental payment rentalId không tồn tại
| | |
|---|---|
| **Method** | `POST /api/payment/momo/create-rental` |
| **Auth** | Yes |
| **Request Body** | `{ "rentalId": "00000000-...", "orderInfo": "Test" }` |
| **Expected** | `404` |
| **Actual** | `400` — `"Không tìm thấy đơn thuê: 00000000-..."` |
| **Nguyên nhân** | Cùng TC-PRD-006. |
| **Fix** | Xem TC-PRD-006. |

---

#### TC-PAY-012 — Lấy trạng thái thanh toán orderCode sai
| | |
|---|---|
| **Method** | `GET /api/payment/status/{orderCode}` |
| **Auth** | Yes |
| **Path** | `/api/payment/status/INVALID_CODE` |
| **Expected** | `404` |
| **Actual** | `200` — `{ "success": true, "data": { "success": false, "message": "Không tìm thấy thanh toán" } }` |
| **Nguyên nhân** | Controller trả `HTTP 200` với body lồng `success: false` bên trong `data`. Vi phạm quy ước API — khi không tìm thấy tài nguyên nên trả `HTTP 404`. |
| **Fix** | Sửa controller: khi không tìm thấy payment transaction và không tìm thấy order → trả `ResponseEntity.status(404).body(ApiResponse.error("Không tìm thấy thanh toán"))`. |

---

### MODULE: Chatbot

---

#### TC-CHB-001 — Chat sync với chatbot
| | |
|---|---|
| **Method** | `POST /api/chatbot/chat-sync` |
| **Auth** | Yes |
| **Request Body** | `{ "message": "Tôi muốn tìm máy ảnh Canon EOS", "conversationId": null, "userId": null }` |
| **Expected** | `200` — câu trả lời từ chatbot |
| **Actual** | `500` — `"Lỗi chatbot: Lỗi Ollama (401): unauthorized"` |
| **Nguyên nhân** | **Môi trường** — `OLLAMA_API_KEY` không được cấu hình hoặc sai. Config hiện tại: `ollama.api-key=` (rỗng). |
| **Fix** | Cấu hình `OLLAMA_API_KEY` hợp lệ trong `.env` hoặc `application.properties`. Hoặc nếu dùng Ollama local (không cần key) thì đổi `ollama.base-url` về `http://localhost:11434`. |

---

#### TC-CHB-003 — Chat sync message rỗng
| | |
|---|---|
| **Method** | `POST /api/chatbot/chat-sync` |
| **Auth** | Yes |
| **Request Body** | `{ "message": "", "conversationId": null, "userId": null }` |
| **Expected** | `400` — validation lỗi message rỗng |
| **Actual** | `500` — `"Lỗi chatbot: Lỗi Ollama (401): unauthorized"` |
| **Nguyên nhân** | Lỗi Ollama (401) xảy ra trước khi validation `message` được kiểm tra, che mất lỗi validation thực sự. Ngoài ra thiếu `@NotBlank` validation trên trường `message`. |
| **Fix** | (1) Thêm `@NotBlank` vào trường `message` trong ChatRequest DTO để validation chạy trước khi gọi Ollama. (2) Sau khi fix môi trường Ollama (TC-CHB-001), kiểm tra lại TC này. |

---

## Chi Tiết Các TC SKIP

### TC-PAY-006 — MoMo IPN chữ ký đúng, resultCode=0
| | |
|---|---|
| **Method** | `POST /api/payment/momo/ipn` |
| **Lý do Skip** | Signature trong IPN payload phải là **HMAC-SHA256 thật** được tính bằng `secret-key` của MoMo. Không thể giả lập thủ công mà không có secret key hợp lệ. |
| **Cách test** | Dùng MoMo sandbox portal để trigger IPN thật, hoặc viết unit test mock `MoMoService.verifySignature()`. |

---

### TC-PAY-008 — MoMo IPN chữ ký đúng, resultCode != 0
| | |
|---|---|
| **Method** | `POST /api/payment/momo/ipn` |
| **Lý do Skip** | Cùng TC-PAY-006 — cần HMAC-SHA256 thật. |
| **Cách test** | Xem TC-PAY-006. |

---

### TC-PAY-009 — MoMo Callback thành công
| | |
|---|---|
| **Method** | `GET /api/payment/momo/callback` |
| **Lý do Skip** | Endpoint trả `HTTP 302 Redirect` về frontend URL (`http://localhost:8081/payment-success`). curl theo redirect nhưng không thể verify UI behavior. |
| **Cách test** | Test bằng browser hoặc dùng `curl -L` kết hợp kiểm tra `Location` header trong response. |

---

### TC-PAY-010 — MoMo Callback chữ ký sai
| | |
|---|---|
| **Method** | `GET /api/payment/momo/callback` |
| **Lý do Skip** | Cùng TC-PAY-009 — response là redirect, không phải JSON. |
| **Cách test** | Xem TC-PAY-009. |

---

### TC-CHB-002 — Chat async (streaming)
| | |
|---|---|
| **Method** | `POST /api/chatbot/chat` |
| **Lý do Skip** | Endpoint trả **Server-Sent Events (SSE) stream** — `curl` nhận được stream text, không phải JSON thuần, khó assert tự động. |
| **Cách test** | Dùng `curl -N` hoặc test bằng browser/Postman với SSE support. Cũng phụ thuộc vào Ollama API key hợp lệ (TC-CHB-001). |

---

## Bảng Tổng Hợp Fix Theo Ưu Tiên

| Ưu tiên | Vấn đề | TC liên quan | Effort |
|---------|--------|-------------|--------|
| 🔴 Cao | Global exception handler trả 400 thay vì 404 cho "not found" | PRD-006, AST-004, CRT-004, CRT-008, ORD-006, RNT-006, PAY-002, PAY-005 | Thấp — 1 chỗ fix |
| 🔴 Cao | JwtAuthFilter không xử lý null → NPE | AUT-009, AUT-010 | Thấp |
| 🔴 Cao | `@Transactional` thiếu trong Favorites toggle delete | FAV-003 | Thấp |
| 🟠 Trung | Login trả sai HTTP status (400 thay vì 401/404) | AUT-006, AUT-007 | Thấp |
| 🟠 Trung | Order cho phép items=[] | ORD-003 | Thấp |
| 🟠 Trung | Cart cho phép quantity=0 | CRT-006 | Thấp |
| 🟠 Trung | Payment status trả 200 khi không tìm thấy | PAY-012 | Thấp |
| 🟠 Trung | check-availability / calculate-price cần làm rõ auth requirement | RNT-007, RNT-008 | Thấp |
| 🟠 Trung | ORD update status bị Access Denied với USER | ORD-007, ORD-008 | Trung — cần review phân quyền |
| 🟡 Thấp | Validation message="" bị che bởi lỗi Ollama | CHB-003 | Thấp |
| ⚙️ Môi trường | Mail server chưa chạy | AUT-016, AUT-017 | Setup |
| ⚙️ Môi trường | MoMo sandbox credentials hết hạn | PAY-001, PAY-004 | Setup |
| ⚙️ Môi trường | Ollama API key không hợp lệ | CHB-001 | Setup |
