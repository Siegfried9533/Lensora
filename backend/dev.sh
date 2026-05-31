#!/usr/bin/env bash
# Khởi động backend local (không dùng Docker).
# Yêu cầu: PostgreSQL đang chạy + file .env đã được tạo từ .env.example
#
# Sử dụng:
#   chmod +x dev.sh
#   ./dev.sh

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

if [ ! -f .env ]; then
  echo "Lỗi: File .env không tồn tại."
  echo "Chạy: cp .env.example .env  rồi điền giá trị thật."
  exit 1
fi

echo "[Lensora] Khởi động backend (spring-dotenv sẽ tự load .env)..."
./mvnw spring-boot:run
