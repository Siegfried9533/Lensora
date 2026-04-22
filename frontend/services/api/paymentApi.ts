import { BASE_URL, getHeaders, handleResponse } from './config';

export interface MoMoPaymentRequest {
  orderId: string;
  amount: number;
  orderInfo?: string;
  requestType?: 'captureWallet' | 'payWithMethod' | 'linkAndPay' | 'linkAndPayWithToken';
}

export interface MoMoPaymentResponse {
  payUrl: string;
  orderId: string;
}

export interface PaymentResult {
  success: boolean;
  message: string;
  orderCode: string;
  amount: number;
  transactionRef: string;
  paymentMethod?: string;
}

export interface ShippingFeeRequest {
  toDistrict: string;
  toWard: string;
  weight: number;
  insuranceValue: number;
}

export interface ShippingFeeResponse {
  shippingFee: number;
  message: string;
}

export interface Province {
  provinceId: string;
  provinceName: string;
}

export interface District {
  districtId: string;
  districtName: string;
  provinceId: string;
}

export interface Ward {
  wardCode: string;
  wardName: string;
  districtId: string;
}

export const paymentApi = {
  /**
   * Create MoMo payment URL
   */
  createMoMoPayment: async (request: MoMoPaymentRequest, token: string): Promise<MoMoPaymentResponse> => {
    const response = await fetch(`${BASE_URL}/payment/momo/create`, {
      method: 'POST',
      headers: getHeaders(token),
      body: JSON.stringify(request),
    });
    const data = await handleResponse(response);
    return data.data as MoMoPaymentResponse;
  },

  /**
   * Create MoMo payment URL for rental
   */
  createMoMoPaymentRental: async (
    rentalId: string,
    orderInfo?: string,
    token?: string
  ): Promise<MoMoPaymentResponse> => {
    const response = await fetch(`${BASE_URL}/payment/momo/create-rental`, {
      method: 'POST',
      headers: token ? getHeaders(token) : getHeaders(),
      body: JSON.stringify({ rentalId, orderInfo }),
    });
    const data = await handleResponse(response);
    return data.data as MoMoPaymentResponse;
  },

  /**
   * Get payment status for an order
   */
  getPaymentStatus: async (orderCode: string): Promise<PaymentResult> => {
    const response = await fetch(`${BASE_URL}/payment/status/${orderCode}`, {
      headers: getHeaders(),
    });
    const data = await handleResponse(response);
    return data.data as PaymentResult;
  },

  /**
   * Calculate shipping fee
   */
  calculateShippingFee: async (request: ShippingFeeRequest): Promise<ShippingFeeResponse> => {
    const response = await fetch(`${BASE_URL}/shipping/calculate`, {
      method: 'POST',
      headers: getHeaders(),
      body: JSON.stringify(request),
    });
    const data = await handleResponse(response);
    return data.data as ShippingFeeResponse;
  },

  /**
   * Get all provinces
   */
  getProvinces: async (): Promise<Province[]> => {
    const response = await fetch(`${BASE_URL}/shipping/provinces`, {
      headers: getHeaders(),
    });
    const data = await handleResponse(response);
    return data.data as Province[];
  },

  /**
   * Get districts by province
   */
  getDistricts: async (provinceId: string): Promise<District[]> => {
    const response = await fetch(`${BASE_URL}/shipping/districts/${provinceId}`, {
      headers: getHeaders(),
    });
    const data = await handleResponse(response);
    return data.data as District[];
  },

  /**
   * Get wards by district
   */
  getWards: async (districtId: string): Promise<Ward[]> => {
    const response = await fetch(`${BASE_URL}/shipping/wards/${districtId}`, {
      headers: getHeaders(),
    });
    const data = await handleResponse(response);
    return data.data as Ward[];
  },

  /**
   * Get order tracking status
   */
  trackOrder: async (orderCode: string): Promise<{ orderCode: string; status: string }> => {
    const response = await fetch(`${BASE_URL}/shipping/track/${orderCode}`, {
      headers: getHeaders(),
    });
    const data = await handleResponse(response);
    return data.data as { orderCode: string; status: string };
  },

  /**
   * Query MoMo transaction status
   */
  queryMoMoTransaction: async (orderId: string, requestId: string, token: string): Promise<any> => {
    const response = await fetch(`${BASE_URL}/payment/momo/query`, {
      method: 'POST',
      headers: getHeaders(token),
      body: JSON.stringify({ orderId, requestId }),
    });
    const data = await handleResponse(response);
    return data.data;
  },
};
