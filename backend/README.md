# Lensora Backend

REST API for the Lensora camera-equipment marketplace (buy / sell / rent), built with Spring Boot.

> The Java package and Maven artifact are `com.camerashop` (legacy "Camera Shop" name); the product is **Lensora**. The repo root `README.md` covers running the full stack — this file documents the backend in detail.

## Tech Stack

- **Java 21**
- **Spring Boot 3.4.2** (Web, Data JPA, Security, Validation, Mail, OAuth2 Client)
- **PostgreSQL** + **Flyway** (versioned migrations)
- **JWT** (`io.jsonwebtoken` jjwt 0.12.5)
- **spring-dotenv** (loads `backend/.env` on local runs)
- **Maven** (wrapper `./mvnw`), **Docker**

## Features

- 🔐 JWT authentication & authorization (+ Google OAuth2 login)
- 👤 Registration / login with email verification
- 🛍️ Product & asset (rental equipment) catalog
- 🛒 Cart, ❤️ favorites, ⭐ reviews
- 📦 Order processing and 📱 rentals
- 💰 Payment integration: **MoMo** (sandbox)
- 🚚 Shipping integration: **GHN**, with a `provinces.open-api.vn` fallback for province/district/ward lookups
- 🔔 Notifications + scheduled jobs
- 🤖 Chatbot powered by **DeepSeek**

## Project Structure

```
src/main/java/com/camerashop/
├── CameraShopApplication.java     # entry point
├── config/                        # SecurityConfig, schedulers, OAuth2 success handler, data init
├── controller/                    # REST controllers (one per feature)
├── dto/                           # request/response DTOs (+ dto/chatbot)
├── entity/                        # JPA entities
├── exception/                     # GlobalExceptionHandler
├── filter/                        # JwtAuthFilter
├── repository/                    # Spring Data JPA repositories
├── service/                       # business logic + integrations (GHNService, MoMoService, ChatbotService, ...)
└── util/
src/main/resources/
├── application.properties         # all config via ${ENV:default}
└── db/migration/                  # Flyway: V1__Baseline.sql, V2__Create_application_schema.sql
```

## Getting Started

### Prerequisites

- Java 21
- Docker & Docker Compose (recommended for PostgreSQL)
- Maven is not required globally — use the bundled `./mvnw`

### Configuration

Secrets are **env-driven, never hardcoded**. Copy the template and fill real values:

```bash
cp .env.example .env
```

`spring-dotenv` loads `backend/.env` automatically on local runs. Key variables (see `application.properties` for the full list and defaults):

| Variable | Description |
|----------|-------------|
| `SPRING_DATASOURCE_URL` | JDBC URL (default `jdbc:postgresql://localhost:5433/LensoraDB`) |
| `SPRING_DATASOURCE_USERNAME` / `SPRING_DATASOURCE_PASSWORD` | DB credentials |
| `APP_JWT_SECRET` | JWT signing secret (≥ 32 chars) |
| `APP_JWT_EXPIRATION_MS` | Token lifetime (default `86400000` = 24h) |
| `APP_FRONTEND_URL` | Frontend URL used in email links |
| `SPRING_MAIL_*` | SMTP host/port/credentials for email verification |
| `APP_GHN_API_URL` | GHN API base URL. Use `https://online-gateway.ghn.vn/shiip/public-api/` for orders that should appear in the production GHN portal. |
| `APP_GHN_TOKEN` / `APP_GHN_SHOP_ID` / `APP_GHN_DISTRICT_ID` | GHN shipping credentials |
| `APP_GHN_FROM_NAME` / `APP_GHN_FROM_PHONE` / `APP_GHN_FROM_ADDRESS` / `APP_GHN_FROM_WARD_CODE` / `APP_GHN_FROM_DISTRICT_ID` | Pickup address sent when creating GHN shipping orders |
| `APP_MOMO_PARTNER_CODE` / `APP_MOMO_ACCESS_KEY` / `APP_MOMO_SECRET_KEY` | MoMo payment credentials |
| `DEEPSEEK_BASE_URL` / `DEEPSEEK_MODEL` / `DEEPSEEK_API_KEY` | Chatbot LLM config |

### Database

Default Docker Postgres (from `docker-compose.yml`):

```
Database: LensoraDB
User/Pass: postgres / Hoalt@2005
Host port: 5433  (override with DB_PORT)  →  container 5432
```

Schema is managed by **Flyway**, not Hibernate: `spring.jpa.hibernate.ddl-auto=validate`. Hibernate validates the schema at boot and will **fail to start** if an entity doesn't match the migrated tables. Any schema change requires a new `src/main/resources/db/migration/V*__*.sql` migration.

### Running

```bash
# Full stack (API + Postgres) in Docker, from the repo root or backend/
docker compose up -d --build
# Port 5433 already in use? pick another:
DB_PORT=5434 docker compose up -d --build

# Local dev: Postgres in Docker, Spring Boot on the host (loads backend/.env)
docker compose up -d db
./mvnw spring-boot:run
```

Server runs on `http://localhost:8080` (bound to `0.0.0.0`, so other devices on the LAN can reach it).

```bash
curl http://localhost:8080/api/health
```

## API

Base URL: `http://localhost:8080/api`. Authenticated endpoints require:

```
Authorization: Bearer <jwt-token>
```

Controllers (each under `/api/<feature>`): `auth`, `products`, `categories`, `assets`, `cart`, `favorites`, `orders`, `rentals`, `reviews`, `notifications`, `payment`, `shipping`, `chatbot`, `health`.

Commonly used endpoints:

```
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

# Shipping (GHN + open-api fallback)
GET  /api/shipping/provinces
GET  /api/shipping/districts/{provinceId}
GET  /api/shipping/wards/{districtId}
POST /api/shipping/calculate
GET  /api/shipping/track/{orderCode}

# Payment (MoMo)
POST /api/payment/momo/create
POST /api/payment/momo/create-rental
GET  /api/payment/status/{orderCode}

# Chatbot (DeepSeek)
POST /api/chatbot/chat-sync
```

## Authentication

JWT (HMAC-SHA256). Subject = userId (UUID); claims include username, email, role; default expiry 24h. Local registration requires email verification; OAuth2 (Google) logins are auto-verified.

## Testing

```bash
./mvnw test
```

> There are no test classes yet, so this currently compiles and runs nothing. H2 is wired as a test-scope dependency for when tests are added.

## Test Accounts

Seeded on first run:

| Email | Password | Role |
|-------|----------|------|
| testuser@lensora.com | 123456 | USER |
| admin@lensora.com | 123456 | ADMIN |

## Deployment

```bash
# Docker
docker compose up -d --build
docker compose logs -f
docker compose down            # add -v to drop the data volume

# Jar
./mvnw clean package
java -jar target/backend-0.0.1-SNAPSHOT.jar
```

## Notes

- MoMo IPN (`/api/payment/momo/ipn`) needs a public URL — use a tunnel (e.g. ngrok) for local testing.
- GHN shipping needs a registered shop + token; without one, the service falls back to `provinces.open-api.vn` for address lookups and to a flat default for fee calculation.
