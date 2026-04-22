import { BASE_URL, getHeaders, handleResponse } from './config';

export interface RentalPriceRequest {
  assetId: string;
  startDate: string;
  endDate: string;
}

export interface RentalPriceResponse {
  dailyRate: number;
  days: number;
  totalRentFee: number;
  depositFee: number;
  total: number;
}

export interface AvailabilityCheck {
  available: boolean;
  assetId: string;
  startDate: string;
  endDate: string;
}

export interface CreateRentalRequest {
  assetId: string;
  startDate: string;
  endDate: string;
  shippingAddress: string;
  paymentMethod: 'COD' | 'MoMo';
  shippingFee: number;
}

export const rentalApi = {
  checkAvailability: async (
    assetId: string,
    startDate: string,
    endDate: string
  ): Promise<AvailabilityCheck> => {
    const params = new URLSearchParams({
      assetId,
      startDate,
      endDate,
    });
    const response = await fetch(`${BASE_URL}/rentals/check-availability?${params.toString()}`, {
      headers: getHeaders(),
    });
    const data = await handleResponse(response);
    return data.data as AvailabilityCheck;
  },

  calculatePrice: async (request: RentalPriceRequest): Promise<RentalPriceResponse> => {
    const response = await fetch(`${BASE_URL}/rentals/calculate-price`, {
      method: 'POST',
      headers: getHeaders(),
      body: JSON.stringify(request),
    });
    const data = await handleResponse(response);
    return data.data as RentalPriceResponse;
  },

  extendRental: async (rentalId: string, newEndDate: string, token: string): Promise<any> => {
    const response = await fetch(`${BASE_URL}/rentals/${rentalId}/extend`, {
      method: 'POST',
      headers: getHeaders(token),
      body: JSON.stringify({ newEndDate }),
    });
    const data = await handleResponse(response);
    return data.data;
  },

  returnRental: async (rentalId: string, returnDate: string, token: string): Promise<any> => {
    const response = await fetch(`${BASE_URL}/rentals/${rentalId}/return`, {
      method: 'POST',
      headers: getHeaders(token),
      body: JSON.stringify({ returnDate }),
    });
    const data = await handleResponse(response);
    return data.data;
  },

  createRental: async (token: string, request: CreateRentalRequest): Promise<any> => {
    const response = await fetch(`${BASE_URL}/rentals`, {
      method: 'POST',
      headers: getHeaders(token),
      body: JSON.stringify(request),
    });
    const data = await handleResponse(response);
    return data.data;
  },
};
