import { BASE_URL, getHeaders, handleResponse } from './config';

export interface OrderItem {
  productName: string;
  quantity: number;
  priceAtPurchase: number;
  imageUrl?: string;
}

export interface Order {
  orderId: string;
  userId: string;
  orderDate: string;
  totalAmount: number;
  shippingAddress: string;
  status: 'PENDING' | 'SHIPPED' | 'DELIVERED' | 'CANCELLED';
  paymentMethod?: 'COD' | 'VNPay';
  paymentStatus?: 'PENDING' | 'SUCCESS' | 'FAILED';
  shippingFee?: number;
  ghnOrderId?: string;
  orderItems: OrderItem[];
}

export interface CreateOrderRequest {
  shippingAddress: string;
  paymentMethod?: string;
  shippingFee?: number;
  items: { productId: string; quantity: number }[];
}

export const orderApi = {
  createOrder: async (token: string, data: CreateOrderRequest): Promise<Order> => {
    const response = await fetch(`${BASE_URL}/orders`, {
      method: 'POST',
      headers: getHeaders(token),
      body: JSON.stringify(data),
    });
    const res = await handleResponse(response);
    return res.data as Order;
  },

  getOrders: async (token: string): Promise<Order[]> => {
    const response = await fetch(`${BASE_URL}/orders`, {
      headers: getHeaders(token),
    });
    const data = await handleResponse(response);
    return data.data as Order[];
  },

  getOrderById: async (token: string, orderId: string): Promise<Order> => {
    const response = await fetch(`${BASE_URL}/orders/${orderId}`, {
      headers: getHeaders(token),
    });
    const data = await handleResponse(response);
    return data.data as Order;
  },
};

export interface Rental {
  rentalId: string;
  userId: string;
  assetId: string;
  assetName: string;
  assetBrand: string;
  primaryImageUrl?: string;
  startDate: string;
  endDate: string;
  returnDate?: string;
  depositFee: number;
  totalRentFee: number;
  penaltyFee: number;
  status: 'PENDING' | 'ACTIVE' | 'COMPLETED' | 'CANCELLED';
}

export const rentalApi = {
  createRental: async (
    token: string,
    assetId: string,
    startDate: string,
    endDate: string
  ): Promise<Rental> => {
    const response = await fetch(`${BASE_URL}/rentals`, {
      method: 'POST',
      headers: getHeaders(token),
      body: JSON.stringify({ assetId, startDate, endDate }),
    });
    const data = await handleResponse(response);
    return data.data as Rental;
  },

  getRentals: async (token: string): Promise<Rental[]> => {
    const response = await fetch(`${BASE_URL}/rentals`, {
      headers: getHeaders(token),
    });
    const data = await handleResponse(response);
    return data.data as Rental[];
  },

  getRentalById: async (token: string, rentalId: string): Promise<Rental> => {
    const response = await fetch(`${BASE_URL}/rentals/${rentalId}`, {
      headers: getHeaders(token),
    });
    const data = await handleResponse(response);
    return data.data as Rental;
  },
};
