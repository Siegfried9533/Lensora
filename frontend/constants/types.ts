export interface User {
  userId: string;
  userName: string;
  email: string;
  password?: string; // Bỏ qua mật khẩu thực khi return
  avatarUrl?: string;
  role: 'user' | 'admin';
  trustScore: number;
}

export interface Category {
  categoryId: string;
  categoryName: string;
  type: 'PRODUCT' | 'ASSET';
}

export interface Product {
  productId: string;
  categoryId: string;
  productName: string;
  brand: string;
  description: string;
  price: number;
  stockQuantity: number;
}

export interface Asset {
  assetId: string;
  categoryId: string;
  modelName: string;
  brand: string;
  dailyRate: number;
  status: 'AVAILABLE' | 'RENTED' | 'MAINTENANCE';
  seriNumber: string;
}

export interface Rental {
  rentalId: string;
  userId: string;
  assetId: string;
  startDate: string;
  endDate: string;
  returnDate?: string | null;
  depositFee: number;
  totalRentFee: number;
  penaltyFee: number;
  status: 'PENDING' | 'ACTIVE' | 'COMPLETED' | 'CANCELLED';
}

export interface Order {
  orderId: string;
  userId: string;
  orderDate: string;
  totalAmount: number;
  shippingAddress: string;
  status: 'PENDING' | 'SHIPPED' | 'DELIVERED' | 'CANCELLED';
}

export interface OrderItem {
  orderItemId: string;
  orderId: string;
  productId: string;
  quantity: number;
  priceAtPurchase: number;
}

export interface Cart {
  cartId: string;
  userId: string;
  productId: string;
  quantity: number;
}

export interface CartContextItem {
  id: string; // entity ID (productId or assetId)
  quantity: number;
  type: 'PRODUCT' | 'ASSET';
}

export interface Review {
  reviewId: string;
  userId: string;
  entityId: string; // productId or assetId
  rating: number;
  comment: string;
  type: 'PRODUCT' | 'ASSET';
}

export interface ImageRecord {
  imageId: string;
  entityId: string; // productId or assetId
  url: string;
  isPrimary: boolean;
  type: 'PRODUCT' | 'ASSET';
}
