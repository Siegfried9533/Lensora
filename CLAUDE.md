# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

Lensora — a camera-equipment marketplace (buy / sell / rent) with two independent modules:

- `backend/` — Spring Boot 3.4.2 REST API (Java 21), PostgreSQL, Flyway, JWT auth.
- `frontend/` — native Android app (**Java + XML Views**, not Kotlin/Compose) calling the backend via Retrofit.

Note: the Maven artifact and Java package are `com.camerashop` (legacy "Camera Shop" name) even though the product is "Lensora". The Android package is `com.example.my_mobile_app`.

## Commands

The repo root `README.md` is the authoritative source for setup. `backend/README.md` is **stale** (it claims Java 17 / MySQL / port 3306 / VNPay — all wrong); do not trust it.

Backend (run from `backend/`):
```bash
# Full stack (API + Postgres) in Docker
docker compose up -d --build
# If host port 5433 is taken:
DB_PORT=5434 docker compose up -d --build

# Local run: Postgres in Docker, Spring Boot on the host (spring-dotenv loads backend/.env)
docker compose up -d db
./mvnw spring-boot:run

# Tests (note: no test classes exist yet, so this currently runs nothing)
./mvnw test
./mvnw test -Dtest=SomeClassName#someMethod   # single test, once tests exist

# Package a jar
./mvnw clean package

curl http://localhost:8080/api/health          # health check
```

Frontend (run from `frontend/`):
```bash
./gradlew assembleDebug          # build debug APK
./gradlew installDebug           # build + install to a connected device/emulator
./gradlew test                   # JVM unit tests
./gradlew connectedAndroidTest   # instrumented tests (needs a running emulator/device)
```

## Environment & secrets

- **Backend secrets are env-driven, never hardcoded.** `application.properties` reads everything via `${VAR:default}`. `spring-dotenv` auto-loads `backend/.env` on local runs — create it with `cp .env.example .env` and fill real values (DB password, `APP_JWT_SECRET`, GHN, MoMo, `DEEPSEEK_API_KEY`).
- **Frontend base URL** comes from `frontend/local.properties` key `api.base.url`, injected as `BuildConfig.BASE_URL` (read via `api/ApiConstants.java`). Default fallback is the emulator loopback `http://10.0.2.2:8080/api/`. For a physical device, set it to the backend host's LAN IP.

## Architecture

### Backend (`com.camerashop`)
Conventional layered Spring MVC. Entry point `CameraShopApplication.java`. Per feature: `controller/` → `service/` → `repository/` (JPA) over `entity/`, with `dto/` at the boundary. Cross-cutting: `filter/JwtAuthFilter`, `config/SecurityConfig`, `exception/GlobalExceptionHandler`.

Two architectural facts that aren't obvious from a single file:

- **Flyway owns the schema; JPA only validates it.** `spring.jpa.hibernate.ddl-auto=validate` plus Flyway migrations in `src/main/resources/db/migration/` (`V1__Baseline.sql`, `V2__Create_application_schema.sql`). Adding/changing an entity field **requires a new `V*__*.sql` migration** — otherwise the app fails to boot on schema validation. Hibernate will not auto-create columns.
- **External integrations live behind services with graceful fallbacks.** `GHNService` (shipping fees + province/district/ward lookup) calls the GHN API and **falls back to `provinces.open-api.vn`** when GHN has no token or errors. `MoMoService` handles payment create + IPN. `ChatbotService` calls **DeepSeek** (`deepseek.*` config — the project migrated off Ollama; the `OLLAMA_*` vars listed in the root README are obsolete).

### Frontend (`com.example.my_mobile_app`)
Activity-based UI, one package per feature under `ui/` (`auth`, `home`, `cart`, `checkout`, `orders`, `rentals`, `equipment`, `chatbot`, `payment`, `profile`, …). `model/` holds plain data classes; `util/` holds helpers (`TokenManager`, `PriceFormatter`, `LocaleHelper`).

The frontend↔backend contract is the part that spans files:

- Each Retrofit interface in `api/` mirrors one backend `@RestController` at `/api/...` (e.g. `OrderService` ↔ `OrderController`, `PaymentService` ↔ `PaymentController` + `ShippingController`). When you change an endpoint's path/shape on one side, update the matching interface on the other.
- All responses are wrapped in `api/ApiResponse<T>` (`success` / `message` / `data`); callers check `b.success && b.data != null` before using data.
- `api/ApiClient` is a singleton Retrofit holder; its `AuthInterceptor` attaches `Authorization: Bearer <jwt>` from `TokenManager` automatically, so individual calls don't pass the token.
- Screens extend `ui/BaseActivity`, which provides `requireLogin()`, `showLoading()/hideLoading()`, and `showError()`. Follow that pattern for new activities.

### Localization
UI strings are bilingual: `res/values/strings.xml` (English) and `res/values-vi/strings.xml` (Vietnamese, the primary UI language). **Add/change strings in both files together** — code references `R.string.*`, never literals.

## Local automation

`.claude/settings.local.json` defines a PostToolUse hook (`.claude/hooks/restart-backend.sh`) that **auto-restarts the Spring Boot backend whenever a file under `backend/` is edited**. It relaunches `./mvnw spring-boot:run` detached; logs go to `backend/backend-run.log`. It only restarts the app process — Postgres and a valid `backend/.env` must already be up. Editing several backend files in one turn triggers several restarts (expected).
