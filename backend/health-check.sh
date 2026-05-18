#!/bin/bash

# Script kiểm tra sức khỏe cho ứng dụng Spring Boot
# Mã thoát 0 = khỏe mạnh, 1 = không khỏe mạnh

HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/health 2>/dev/null)

if [ "$HTTP_CODE" -eq 200 ]; then
    echo "Health check passed: HTTP $HTTP_CODE"
    exit 0
else
    echo "Health check failed: HTTP $HTTP_CODE"
    exit 1
fi
