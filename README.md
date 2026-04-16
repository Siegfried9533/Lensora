<div align="center">

# 📷 LENSORA

### *Nền tảng mua bán & cho thuê thiết bị nhiếp ảnh*

[![React Native](https://img.shields.io/badge/React%20Native-0.81-61DAFB?style=for-the-badge&logo=react&logoColor=black)](https://reactnative.dev/)
[![Expo](https://img.shields.io/badge/Expo-54-000020?style=for-the-badge&logo=expo&logoColor=white)](https://expo.dev/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.2-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-316192?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://www.docker.com/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.9-3178C6?style=for-the-badge&logo=typescript&logoColor=white)](https://www.typescriptlang.org/)

<br/>

> **Lensora** là ứng dụng di động hiện đại dành cho cộng đồng nhiếp ảnh, cho phép người dùng **mua bán sản phẩm** và **thuê thiết bị** nhiếp ảnh chuyên nghiệp một cách dễ dàng, nhanh chóng và an toàn.

<br/>

🎨 **[Xem bản thiết kế Figma](https://www.figma.com/design/zWRnV8ly0ogNpLNMXmGH9J/Giao-di%E1%BB%87n-mua-b%C3%A1n-thi%E1%BA%BFt-b%E1%BB%8B-nhi%E1%BA%BFp-%E1%BA%A3nh)**

</div>

---

## 📋 Mục lục

- [📖 Giới thiệu](#-giới-thiệu)
- [✨ Tính năng nổi bật](#-tính-năng-nổi-bật)
- [🏗 Kiến trúc hệ thống](#-kiến-trúc-hệ-thống)
- [🛠 Công nghệ sử dụng](#-công-nghệ-sử-dụng)
- [📂 Cấu trúc thư mục](#-cấu-trúc-thư-mục)
- [🚀 Hướng dẫn cài đặt](#-hướng-dẫn-cài-đặt)
- [🔐 Tài khoản dùng thử](#-tài-khoản-dùng-thử)
- [📡 API Reference](#-api-reference)
- [🗄 Database Schema](#-database-schema)
- [🤝 Đóng góp](#-đóng-góp)

---

## 📖 Giới thiệu

**Lensora** ra đời để phục vụ nhu cầu ngày càng tăng của cộng đồng nhiếp ảnh tại Việt Nam. Thay vì phải bỏ ra chi phí lớn để sở hữu thiết bị đắt tiền, người dùng có thể dễ dàng:

- 🛒 **Mua bán** máy ảnh, ống kính, phụ kiện với giá cả minh bạch
- 📅 **Thuê thiết bị** theo ngày với mức giá hợp lý (từ ₫400.000/ngày)
- ⭐ **Đánh giá & nhận xét** để xây dựng cộng đồng tin tưởng
- 💼 **Quản lý đơn hàng & lịch sử thuê** ngay trên điện thoại

Ứng dụng được thiết kế với giao diện tối (dark theme) hiện đại, tối ưu cho trải nghiệm di động và nhắm đến đối tượng là các nhiếp ảnh gia chuyên nghiệp lẫn nghiệp dư.

---

## ✨ Tính năng nổi bật

### 🔒 Xác thực người dùng
- Đăng ký / Đăng nhập bảo mật với **JWT Token**
- Phân quyền **User** và **Admin**
- Hệ thống **Trust Score** đánh giá độ uy tín của người dùng

### 🔍 Khám phá & Tìm kiếm
- Màn hình **Discovery** tổng hợp cả sản phẩm mua bán và cho thuê
- **Tìm kiếm theo từ khóa** theo thời gian thực
- **Lọc theo danh mục**: Máy ảnh, Ống kính, Phụ kiện, ...
- Banner **khuyến mãi** nổi bật (Summer Sale, v.v.)

### 🛒 Mua sắm (Store)
- Duyệt và xem chi tiết sản phẩm với hình ảnh chất lượng cao
- Thêm sản phẩm vào **Giỏ hàng**
- Theo dõi **trạng thái đơn hàng**: `PENDING → SHIPPED → DELIVERED`
- Quản lý lịch sử **Đơn mua** của cá nhân

### 📷 Thuê thiết bị (Rentals)
- Xem danh sách thiết bị cho thuê với **giá thuê theo ngày**
- Theo dõi trạng thái thuê: `PENDING → ACTIVE → COMPLETED`
- Hệ thống tính **phí cọc**, **phí phạt trả trễ** tự động
- Quản lý lịch sử thuê cá nhân

### 👤 Hồ sơ người dùng
- Xem và chỉnh sửa **thông tin cá nhân**
- Danh sách **yêu thích** (Favorites / Wishlist)
- Quản lý **thiết bị của tôi** (đăng bán / cho thuê)
- **Lịch sử giao dịch** toàn diện
- Cài đặt tài khoản & thông báo

### ⭐ Đánh giá & Nhận xét
- Đánh giá sao cho cả **sản phẩm mua** và **thiết bị thuê**
- Xem nhận xét từ cộng đồng trước khi quyết định

---

## 🏗 Kiến trúc hệ thống

```
┌─────────────────────────────────────────────────────┐
│                  LENSORA SYSTEM                      │
├─────────────┬──────────────────┬────────────────────┤
│   Mobile    │    REST API      │     Database        │
│   Client    │    Backend       │                     │
│             │                  │                     │
│  React      │  Spring Boot     │   PostgreSQL 15     │
│  Native +   │  Java 21         │                     │
│  Expo       │  Spring Security │   Port: 5433        │
│             │  JWT Auth        │                     │
│  Port: N/A  │  Port: 8080      │                     │
└─────────────┴──────────────────┴────────────────────┘
         Docker Compose orchestrates Backend + DB
```

---

## 🛠 Công nghệ sử dụng

### 📱 Frontend (Mobile)
| Công nghệ | Phiên bản | Mục đích |
|-----------|-----------|----------|
| React Native | 0.81.5 | Framework ứng dụng di động |
| Expo | ~54.0 | Build tools & native APIs |
| Expo Router | ~6.0 | File-based routing |
| NativeWind | ^2.0 | Tailwind CSS cho React Native |
| TypeScript | ~5.9 | Kiểm tra kiểu dữ liệu tĩnh |
| Lucide React Native | ^1.7 | Bộ icon hiện đại |
| React Native Reanimated | ~4.1 | Animation mượt mà |

### ⚙️ Backend (API Server)
| Công nghệ | Phiên bản | Mục đích |
|-----------|-----------|----------|
| Spring Boot | 3.4.2 | Framework backend chính |
| Java | 21 (LTS) | Ngôn ngữ lập trình |
| Spring Security | - | Bảo mật & xác thực |
| JJWT | 0.12.5 | Tạo và xác minh JWT Token |
| Spring Data JPA | - | ORM & tương tác database |
| Lombok | - | Giảm boilerplate code |
| PostgreSQL Driver | - | Kết nối cơ sở dữ liệu |

### 🗄 Infrastructure
| Công nghệ | Mục đích |
|-----------|----------|
| PostgreSQL 15 | Hệ quản trị cơ sở dữ liệu |
| Docker & Docker Compose | Container hóa & triển khai |

---

## 📂 Cấu trúc thư mục

```
Lensora/
├── 📱 frontend/                    # Ứng dụng di động React Native
│   ├── app/                        # Màn hình & định tuyến (Expo Router)
│   │   ├── (auth)/                 # Nhóm màn hình xác thực
│   │   │   ├── login.tsx           # Màn hình đăng nhập
│   │   │   └── signup.tsx          # Màn hình đăng ký
│   │   ├── (tabs)/                 # Màn hình chính (Bottom Tab)
│   │   │   ├── index.tsx           # Trang Discovery (Trang chủ)
│   │   │   ├── notifications.tsx   # Thông báo
│   │   │   ├── profile.tsx         # Hồ sơ cá nhân
│   │   │   └── transactions.tsx    # Lịch sử giao dịch
│   │   ├── equipment/
│   │   │   └── [id].tsx            # Chi tiết sản phẩm (Dynamic Route)
│   │   ├── orders/
│   │   │   └── [id].tsx            # Chi tiết đơn hàng
│   │   ├── profile/
│   │   │   ├── favorites.tsx       # Danh sách yêu thích
│   │   │   ├── my-equipment.tsx    # Thiết bị của tôi
│   │   │   ├── personal-info.tsx   # Thông tin cá nhân
│   │   │   └── settings.tsx        # Cài đặt tài khoản
│   │   ├── rentals/
│   │   │   └── index.tsx           # Danh sách thiết bị cho thuê
│   │   └── store/
│   │       └── index.tsx           # Cửa hàng mua sắm
│   ├── components/                 # UI Components tái sử dụng
│   ├── constants/
│   │   ├── mockData.ts             # Dữ liệu mẫu (mock)
│   │   ├── types.ts                # TypeScript interfaces/types
│   │   └── Colors.ts               # Hằng số màu sắc
│   ├── context/
│   │   └── AuthContext.tsx         # Global state xác thực
│   ├── assets/                     # Hình ảnh, font chữ
│   ├── tailwind.config.js          # Cấu hình Tailwind/NativeWind
│   └── package.json
│
├── ⚙️ backend/                     # API Server Spring Boot
│   └── src/main/java/com/example/backend/
│       ├── controller/
│       │   └── AuthController.java # REST endpoints xác thực
│       ├── entity/                 # JPA Entities (Database Models)
│       │   ├── User.java
│       │   ├── Product.java        # Sản phẩm mua bán
│       │   ├── Asset.java          # Thiết bị cho thuê
│       │   ├── Rental.java         # Hợp đồng thuê
│       │   ├── Order.java          # Đơn hàng
│       │   ├── OrderItem.java      # Chi tiết đơn hàng
│       │   ├── Cart.java           # Giỏ hàng
│       │   ├── Category.java       # Danh mục
│       │   ├── Review.java         # Đánh giá
│       │   ├── Image.java          # Hình ảnh sản phẩm
│       │   └── Customer.java       # Thông tin khách hàng
│       ├── dto/                    # Data Transfer Objects
│       ├── repository/             # Spring Data JPA Repositories
│       ├── services/               # Business Logic
│       ├── config/
│       │   ├── SecurityConfig.java         # Cấu hình Spring Security
│       │   └── JwtAuthenticationFilter.java # JWT Filter
│       └── exceptions/
│           └── GlobalExceptionHandler.java  # Xử lý lỗi toàn cục
│
└── 🐳 docker-compose.yml           # Cấu hình Docker Compose
```

---

## 🚀 Hướng dẫn cài đặt

### Yêu cầu hệ thống

| Công cụ | Phiên bản tối thiểu |
|---------|---------------------|
| Node.js | >= 18.x |
| npm | >= 9.x |
| Java (JDK) | 21 |
| Docker Desktop | Latest |
| Expo Go (điện thoại) | Latest |

---

### 🐳 Khởi chạy Backend & Database (Docker)

Đây là cách nhanh nhất để chạy toàn bộ hệ thống backend.

```bash
# Clone repository
git clone https://github.com/Siegfried9533/Lensora.git
cd Lensora

# Khởi chạy PostgreSQL + Backend bằng Docker Compose
docker-compose up -d

# Kiểm tra trạng thái container
docker-compose ps
```

> 💡 Backend sẽ chạy tại `http://localhost:8080` và PostgreSQL tại `localhost:5433`

Để dừng:
```bash
docker-compose down
```

---

### 🔧 Khởi chạy Backend thủ công (không dùng Docker)

> ⚠️ Yêu cầu PostgreSQL đang chạy trên `localhost:5433` với database `LensoraDB`

**Trên Windows:**
```bat
# Khởi chạy
run-backend.bat

# Khởi chạy lại (nếu đang chạy)
restart-backend.bat

# Dừng
stop-backend.bat
```

**Trên macOS/Linux:**
```bash
cd backend
./mvnw spring-boot:run
```

---

### 📱 Khởi chạy Frontend (Mobile App)

```bash
# Di chuyển vào thư mục frontend
cd frontend

# Cài đặt dependencies
npm install

# Khởi chạy Expo Dev Server
npm start
# hoặc
npx expo start
```

Sau khi server khởi chạy:

| Nền tảng | Cách mở |
|----------|---------|
| 📱 **Điện thoại thật** | Quét mã QR bằng app **Expo Go** |
| 🤖 **Android Emulator** | Nhấn phím `a` trong terminal |
| 🍎 **iOS Simulator** | Nhấn phím `i` trong terminal |
| 🌐 **Trình duyệt** | Nhấn phím `w` trong terminal |

---

## 🔐 Tài khoản dùng thử

> ⚠️ **Lưu ý:** Hiện tại frontend đang sử dụng **Mock Data** để minh họa giao diện và luồng hoạt động. Dữ liệu thực sẽ được kết nối với API backend trong phiên bản tiếp theo.

| Vai trò | Email | Mật khẩu | Quyền hạn |
|---------|-------|----------|-----------|
| 👤 **User** | `test@example.com` | `password123` | Mua bán, thuê thiết bị, đánh giá |
| 🛡️ **Admin** | `john@example.com` | `password123` | Toàn quyền quản trị hệ thống |

---

## 📡 API Reference

Base URL: `http://localhost:8080`

### 🔒 Authentication (`/api/auth`)

| Method | Endpoint | Mô tả | Auth |
|--------|----------|-------|------|
| `POST` | `/api/auth/signup` | Đăng ký tài khoản mới | ❌ |
| `POST` | `/api/auth/login` | Đăng nhập, nhận JWT Token | ❌ |
| `GET` | `/api/auth/me` | Lấy thông tin người dùng hiện tại | ✅ JWT |

#### Request Body — Đăng ký
```json
{
  "userName": "nguyenvana",
  "email": "nguyenvana@example.com",
  "password": "securePassword123"
}
```

#### Request Body — Đăng nhập
```json
{
  "email": "nguyenvana@example.com",
  "password": "securePassword123"
}
```

#### Response — JWT Token
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": 1,
  "email": "nguyenvana@example.com",
  "role": "USER"
}
```

> 📌 Thêm JWT token vào header của các request yêu cầu xác thực:
> `Authorization: Bearer <token>`

---

## 🗄 Database Schema

Hệ thống sử dụng **PostgreSQL** với các bảng chính sau:

```
┌─────────────┐     ┌─────────────────┐     ┌─────────────┐
│    Users    │────▶│    Customers    │────▶│   Rentals   │
│─────────────│     │─────────────────│     │─────────────│
│ userId (PK) │     │ customerId (PK) │     │ rentalId    │
│ userName    │     │ userId (FK)     │     │ userId (FK) │
│ email       │     └─────────────────┘     │ assetId(FK) │
│ password    │                             │ startDate   │
│ role        │                             │ endDate     │
└─────────────┘                             │ status      │
                                            └─────────────┘
┌─────────────┐     ┌─────────────────┐
│  Products   │     │    Assets       │
│─────────────│     │─────────────────│
│ productId   │     │ assetId (PK)    │
│ categoryId  │     │ categoryId (FK) │
│ productName │     │ modelName       │
│ brand       │     │ brand           │
│ price       │     │ dailyRate       │
│ stockQty    │     │ status          │
└─────────────┘     │ seriNumber      │
                    └─────────────────┘
┌─────────────┐     ┌─────────────────┐
│   Orders    │     │   Categories    │
│─────────────│     │─────────────────│
│ orderId     │     │ categoryId (PK) │
│ userId      │     │ categoryName    │
│ orderDate   │     │ type            │
│ totalAmount │     └─────────────────┘
│ status      │
└─────────────┘
```

**Trạng thái đơn hàng (Orders):**
`PENDING` → `SHIPPED` → `DELIVERED` | `CANCELLED`

**Trạng thái thuê (Rentals):**
`PENDING` → `ACTIVE` → `COMPLETED` | `CANCELLED`

**Trạng thái thiết bị (Assets):**
`AVAILABLE` | `RENTED` | `MAINTENANCE`

---

## 🤝 Đóng góp

Chúng tôi hoan nghênh mọi đóng góp từ cộng đồng! Để đóng góp:

1. **Fork** repository này
2. Tạo **branch** mới: `git checkout -b feature/ten-tinh-nang`
3. **Commit** thay đổi: `git commit -m 'feat: thêm tính năng XYZ'`
4. **Push** lên branch: `git push origin feature/ten-tinh-nang`
5. Tạo **Pull Request**

---

<div align="center">

**Được xây dựng với ❤️ bởi nhóm Lensora**

*Thiết kế UI: [Figma](https://www.figma.com/design/zWRnV8ly0ogNpLNMXmGH9J/Giao-di%E1%BB%87n-mua-b%C3%A1n-thi%E1%BA%BFt-b%E1%BB%8B-nhi%E1%BA%BFp-%E1%BA%A3nh) | Photos: [Unsplash](https://unsplash.com) | Icons: [shadcn/ui](https://ui.shadcn.com/)*

</div>
