# Giao diện mua bán thiết bị nhiếp ảnh (Mobile App)

Dự án **Giao diện mua bán thiết bị nhiếp ảnh** (Photography Equipment Trading Platform) là ứng dụng di động (Mobile) hiện đại, được xây dựng để cung cấp trải nghiệm mượt mà, tối ưu cho người dùng khi mua bán, trao đổi các thiết bị nhiếp ảnh.

*Lưu ý:* Dự án ban đầu bao gồm cả phiên bản Web, tuy nhiên hiện tại toàn bộ dự án đã được chuyển đổi (migrate) dứt điểm sang ứng dụng di động.

## 🛠 Công nghệ sử dụng

- **Framework**: [React Native](https://reactnative.dev/) và bộ công cụ [Expo](https://expo.dev/).
- **Styling**: [NativeWind](https://www.nativewind.dev/) (sử dụng cú pháp Tailwind CSS cho React Native) giúp việc xây dựng UI dễ dàng và đồng bộ thiết kế nhanh chóng.
- **Routing**: Expo Router (dựa trên cấu trúc file base routing).

---

## 📂 Cấu trúc thư mục

```text
Frontend/
├── app/                # Nơi chứa các màn hình và định tuyến (Expo Router)
├── components/         # Các UI component có thể tái sử dụng
├── constants/          # Các biến hằng số, configuration
├── assets/             # Hình ảnh, font chữ dùng trong app
├── guidelines/         # Tài liệu hoặc định hướng phát triển
├── package.json        # Cấu hình dự án React Native (dependencies)
└── README.md           # Giới thiệu dự án
```

- `/app/equipment/[id].tsx`: Màn hình riêng lẻ xem chi tiết một sản phẩm (Dynamic Route).
- `tailwind.config.js` & `babel.config.js`: Được thiết lập để render NativeWind ngay bên trong React Native.

## 🔐 Tài khoản dùng thử (Test Accounts)
Bạn có thể sử dụng các tài khoản sau để đăng nhập và trải nghiệm đầy đủ tính năng:

| Vai trò | Email | Mật khẩu |
| :--- | :--- | :--- |
| **User** | `test@example.com` | `password123` |
| **Admin** | `john@example.com` | `password123` |

---
*Lưu ý: Dữ liệu hiện tại là Mock Data dùng để minh họa giao diện và luồng hoạt động.*

---

## 🚀 Hướng dẫn cài đặt và khởi chạy

1. Đảm bảo bạn đã cài đặt phiên bản **Node.js** và **npm**.
2. Mở terminal tại thư mục gốc của dự án (`Frontend/`).
3. Cài đặt các gói thư viện (dependencies):
   ```bash
   npm install
   ```
4. Khởi chạy server phát triển Expo:
   ```bash
   npm start
   ```
   *(hoặc `npx expo start`)*
5. Sử dụng ứng dụng **Expo Go** trên điện thoại (iOS/Android) để quét mã QR hiện ra tại terminal. Hoặc bạn cũng có thể mở trực tiếp trên giả lập Android/iOS bằng phím tắt được cung cấp.

---

## 🎨 Thiết kế gốc (Design)
Bản thiết kế giao diện gốc trên Figma có thể được tham khảo tại:
👉 [Giao diện mua bán thiết bị nhiếp ảnh - Figma](https://www.figma.com/design/zWRnV8ly0ogNpLNMXmGH9J/Giao-di%E1%BB%87n-mua-b%C3%A1n-thi%E1%BA%BFt-b%E1%BB%8B-nhi%E1%BA%BFp-%E1%BA%A3nh)
