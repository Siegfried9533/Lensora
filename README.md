# Camera Shop Backend

A full-featured e-commerce backend for a camera equipment marketplace built with Spring Boot.

## Table of Contents
- [Overview](#overview)
- [Tech Stack](#tech-stack)
- [Features](#features)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Installation](#installation)
  - [Database Setup](#database-setup)
  - [Running the Application](#running-the-application)
- [API Endpoints](#api-endpoints)
- [Authentication](#authentication)
- [Testing](#testing)
- [Deployment](#deployment)

## Overview

This is the backend service for a camera equipment marketplace that allows users to browse, rent, and purchase photography equipment. The backend provides RESTful APIs for user authentication, product management, shopping cart, order processing, rental services, and payment integration.

## Tech Stack

- **Java 17**
- **Spring Boot 3.2.4**
- **Spring Security + JWT**
- **Spring Data JPA**
- **MySQL**
- **Maven**

## Features

- 🔐 JWT Authentication & Authorization
- 👤 User Registration & Login
- 🛍️ Product Catalog Management
- 🛒 Shopping Cart Functionality
- 📦 Order Processing
- 📱 Rental Services
- 💰 Payment Integration (MoMo, VNPay)
- 🚚 Shipping Integration (GHN)
- ❤️ Favorites/Wishlist
- 📧 Email Verification
- 🔔 Notification System
- 📊 Admin Dashboard APIs

## Project Structure

```
src/main/java/com/camerashop/
├── CameraShopApplication.java
├── config/
│   ├── DataInitializer.java
│   ├── NotificationScheduler.java
│   ├── OAuth2SuccessHandler.java
│   └── SecurityConfig.java
├── controller/
│   ├── AssetController.java
│   ├── AuthController.java
│   ├── CartController.java
│   ├── CategoryController.java
│   ├── FavoriteController.java
│   ├── NotificationController.java
│   ├── OrderController.java
│   ├── PaymentController.java
│   ├── ProductController.java
│   ├── RentalController.java
│   └── ShippingController.java
├── dto/
├── entity/
├── filter/
│   └── JwtAuthFilter.java
├── repository/
├── service/
└── util/
    └── JwtUtil.java
```

## Getting Started

### Prerequisites

- Java 17
- Maven 3.8+
- MySQL 8.0+

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/minhphuong150505/Mobile-App_Backend.git
   cd Mobile-App_Backend/Backend
   ```

2. Install dependencies:
   ```bash
   mvn clean compile
   ```

### Database Setup

1. Start MySQL server

2. Create database:
   ```sql
   mysql -u root -p
   CREATE DATABASE camera_shop;
   ```

### Running the Application

```bash
mvn spring-boot:run
```

The server will start on port 8080.

## API Endpoints

Base URL: `http://localhost:8080/api`

### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - User login
- `POST /api/auth/refresh` - Refresh token

### Products
- `GET /api/products` - Get all products
- `GET /api/products/{id}` - Get product by ID
- `POST /api/products` - Create new product (ADMIN)
- `PUT /api/products/{id}` - Update product (ADMIN)
- `DELETE /api/products/{id}` - Delete product (ADMIN)

### Assets
- `GET /api/assets` - Get all assets
- `GET /api/assets/{id}` - Get asset by ID
- `POST /api/assets` - Create new asset (ADMIN)
- `PUT /api/assets/{id}` - Update asset (ADMIN)

### Cart
- `GET /api/cart` - Get user's cart
- `POST /api/cart` - Add item to cart
- `PUT /api/cart/{id}` - Update cart item
- `DELETE /api/cart/{id}` - Remove item from cart

### Orders
- `GET /api/orders` - Get user's orders
- `GET /api/orders/{id}` - Get order by ID
- `POST /api/orders` - Create new order
- `PUT /api/orders/{id}/cancel` - Cancel order

### Rentals
- `GET /api/rentals` - Get user's rentals
- `POST /api/rentals` - Create new rental
- `PUT /api/rentals/{id}/return` - Return rented item

### Favorites
- `GET /api/favorites` - Get user's favorites
- `POST /api/favorites` - Add item to favorites
- `DELETE /api/favorites/{id}` - Remove item from favorites

## Authentication

This application uses JWT (JSON Web Token) for authentication:

1. Register or login to get a JWT token
2. Include the token in the Authorization header:
   ```
   Authorization: Bearer <your-token-here>
   ```

## Testing

Run tests with Maven:

```bash
mvn test
```

## Deployment

For production deployment:

1. Update `application.properties` with production database credentials
2. Build the JAR file:
   ```bash
   mvn clean package
   ```
3. Run the JAR:
   ```bash
   java -jar target/camera-shop-backend-*.jar
   ```

## Test Users

After first run, the database will be seeded with test users:

- **User**: testuser / password123
- **Admin**: johndoe / password123

---

Built with ❤️ using Spring Boot