# Lensora

Lensora là project ứng dụng mua, bán và thuê thiết bị máy ảnh, gồm 2 phần chính:

```text
Lensora/
├── backend/
└── frontend/
```

- `backend/`: Spring Boot REST API, PostgreSQL, JWT authentication, Flyway migration.
- `frontend/`: Android app Java/XML dùng Retrofit để gọi API backend.

## Yêu cầu môi trường

- Java 21
- Docker và Docker Compose
- Android Studio hoặc Android SDK/Gradle
- Android Emulator hoặc thiết bị Android thật

## Chạy Backend

Backend và PostgreSQL được cấu hình trong [backend/docker-compose.yml](backend/docker-compose.yml).

```bash
cd backend
docker compose up -d --build
```

Backend chạy tại:

```text
http://localhost:8080
```

Health check:

```bash
curl http://localhost:8080/api/health
```

Nếu port PostgreSQL mặc định `5433` bị trùng, chạy bằng port khác:

```bash
cd backend
DB_PORT=5434 docker compose up -d --build
```

Thông tin DB mặc định trong Docker:

```text
Database: LensoraDB
Username: postgres
Password: Hoalt@2005
Host port: 5433
Container port: 5432
```

## Chạy Backend Local Bằng Maven

Nếu chỉ chạy DB bằng Docker và chạy Spring Boot ngoài Docker:

```bash
cd backend
DB_PORT=5434 docker compose up -d db
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5434/LensoraDB \
SPRING_DATASOURCE_USERNAME=postgres \
SPRING_DATASOURCE_PASSWORD='Hoalt@2005' \
bash ./mvnw spring-boot:run
```

Chạy test backend:

```bash
cd backend
bash ./mvnw test
```

## Chạy Frontend Android

Build debug APK:

```bash
cd frontend
./gradlew assembleDebug
```

API base URL của Android app nằm tại:

```text
frontend/app/src/main/java/com/example/my_mobile_app/api/ApiConstants.java
```

Mặc định:

```java
public static final String BASE_URL = "http://10.0.2.2:8080/api/";
```

Giá trị này đúng khi chạy Android Emulator và backend ở cùng máy.

Nếu chạy trên điện thoại thật hoặc backend nằm ở máy khác, đổi `10.0.2.2` thành IP LAN của máy chạy backend, ví dụ:

```java
public static final String BASE_URL = "http://192.168.1.25:8080/api/";
```

Điều kiện khi dùng điện thoại thật:

- Điện thoại và máy backend phải cùng mạng Wi-Fi/LAN.
- Firewall của máy backend phải mở port `8080`.
- Backend đã cấu hình `server.address=0.0.0.0`, nên có thể nhận request từ máy khác.

## Tài Khoản Test

Sau khi backend khởi động và seed dữ liệu, có thể dùng:

```text
User:  test@example.com / password123
Admin: john@example.com / password123
```

## API Chính

Base URL:

```text
http://localhost:8080/api
```

Một số endpoint thường dùng:

```text
GET  /api/health
POST /api/auth/login
POST /api/auth/register
GET  /api/auth/me
GET  /api/products
GET  /api/categories
GET  /api/cart
GET  /api/favorites
GET  /api/orders
GET  /api/rentals
POST /api/chatbot/chat-sync
```

Các API cần đăng nhập dùng header:

```text
Authorization: Bearer <jwt-token>
```

## Cấu Hình Dịch Vụ Ngoài

Các biến môi trường backend có thể cấu hình khi cần:

```text
SPRING_DATASOURCE_URL
SPRING_DATASOURCE_USERNAME
SPRING_DATASOURCE_PASSWORD
APP_JWT_SECRET
SPRING_MAIL_HOST
SPRING_MAIL_PORT
APP_GHN_TOKEN
APP_GHN_SHOP_ID
APP_MOMO_PARTNER_CODE
APP_MOMO_ACCESS_KEY
APP_MOMO_SECRET_KEY
OLLAMA_BASE_URL
OLLAMA_MODEL
OLLAMA_API_KEY
```

Các giá trị mặc định nằm trong:

```text
backend/src/main/resources/application.properties
```

## Cấu Trúc Thư Mục

```text
backend/
├── src/main/java/com/camerashop/
│   ├── config/
│   ├── controller/
│   ├── dto/
│   ├── entity/
│   ├── filter/
│   ├── repository/
│   ├── service/
│   └── util/
├── src/main/resources/
│   ├── application.properties
│   └── db/migration/
├── docker-compose.yml
├── Dockerfile
└── pom.xml

frontend/
├── app/src/main/java/com/example/my_mobile_app/
│   ├── api/
│   ├── model/
│   ├── ui/
│   └── util/
├── app/src/main/res/
│   ├── layout/
│   ├── drawable/
│   ├── menu/
│   └── values/
├── app/build.gradle.kts
└── settings.gradle.kts
```

## Lệnh Dừng Docker Backend

```bash
cd backend
docker compose down
```
