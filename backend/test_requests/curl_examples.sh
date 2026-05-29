#!/bin/bash
# =============================================================
# Lensora API - curl test examples
# Usage: bash curl_examples.sh
# Trước tiên chạy TC-AUT-005 (login) để lấy TOKEN, rồi set biến TOKEN
# =============================================================

BASE_URL="http://localhost:8080/api"
TOKEN="YOUR_JWT_TOKEN_HERE"  # Thay bằng token thật sau khi login

# ===== HEALTH =====
echo "=== TC-HLT-001: Health check ==="
curl -s -X GET "$BASE_URL/health" | python3 -m json.tool

# ===== AUTH =====
echo -e "\n=== TC-AUT-001: Đăng ký hợp lệ ==="
curl -s -X POST "$BASE_URL/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "userName": "testuser",
    "email": "testuser@example.com",
    "password": "Password123!",
    "fullName": "Test User",
    "phoneNumber": "0901234567"
  }' | python3 -m json.tool

echo -e "\n=== TC-AUT-005: Đăng nhập ==="
curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "testuser@example.com",
    "password": "Password123!"
  }' | python3 -m json.tool

echo -e "\n=== TC-AUT-008: Lấy thông tin user (cần TOKEN) ==="
curl -s -X GET "$BASE_URL/auth/me" \
  -H "Authorization: Bearer $TOKEN" | python3 -m json.tool

echo -e "\n=== TC-AUT-009: Lấy thông tin user (không token) ==="
curl -s -X GET "$BASE_URL/auth/me" | python3 -m json.tool

# ===== PRODUCTS =====
echo -e "\n=== TC-PRD-001: Danh sách sản phẩm ==="
curl -s -X GET "$BASE_URL/products" | python3 -m json.tool

echo -e "\n=== TC-PRD-002: Danh sách sản phẩm (phân trang) ==="
curl -s -X GET "$BASE_URL/products?page=1&size=5" | python3 -m json.tool

echo -e "\n=== TC-PRD-003: Tìm kiếm sản phẩm ==="
curl -s -X GET "$BASE_URL/products/search?searchQuery=canon" | python3 -m json.tool

echo -e "\n=== TC-PRD-006: Sản phẩm không tồn tại ==="
curl -s -X GET "$BASE_URL/products/999999" | python3 -m json.tool

# ===== CART =====
echo -e "\n=== TC-CRT-001: Lấy giỏ hàng (cần TOKEN) ==="
curl -s -X GET "$BASE_URL/cart" \
  -H "Authorization: Bearer $TOKEN" | python3 -m json.tool

echo -e "\n=== TC-CRT-003: Thêm vào giỏ hàng ==="
curl -s -X POST "$BASE_URL/cart/add" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"itemId": 1, "type": "PRODUCT", "quantity": 1}' | python3 -m json.tool

# ===== ORDERS =====
echo -e "\n=== TC-ORD-001: Tạo đơn hàng ==="
curl -s -X POST "$BASE_URL/orders" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "shippingAddress": {
      "fullName": "Nguyen Van A",
      "phone": "0901234567",
      "province": "Hà Nội",
      "district": "Cầu Giấy",
      "ward": "Dịch Vọng",
      "addressDetail": "123 Đường ABC"
    },
    "paymentMethod": "COD",
    "shippingFee": 30000,
    "items": [{"productId": 1, "quantity": 1, "price": 1500000}]
  }' | python3 -m json.tool

# ===== SHIPPING =====
echo -e "\n=== TC-SHP-002: Danh sách tỉnh/thành ==="
curl -s -X GET "$BASE_URL/shipping/provinces" | python3 -m json.tool

echo -e "\n=== TC-SHP-001: Tính phí ship ==="
curl -s -X POST "$BASE_URL/shipping/calculate" \
  -H "Content-Type: application/json" \
  -d '{"toDistrict": 1442, "toWard": "21211", "weight": 500, "insuranceValue": 1500000}' | python3 -m json.tool

# ===== CHATBOT =====
echo -e "\n=== TC-CHB-001: Chat sync ==="
curl -s -X POST "$BASE_URL/chatbot/chat-sync" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"message": "Tôi muốn tìm máy ảnh Canon EOS", "conversationId": null, "userId": null}' | python3 -m json.tool
