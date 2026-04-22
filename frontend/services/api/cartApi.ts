import { BASE_URL, getHeaders, handleResponse } from './config';

export interface CartItem {
  cartItemId: string;
  productId?: string;
  assetId?: string;
  quantity: number;
  type: 'PRODUCT' | 'ASSET';
  productName?: string;
  assetName?: string;
  price?: number;
  primaryImageUrl?: string;
}

export const cartApi = {
  getCartItems: async (token: string): Promise<CartItem[]> => {
    const response = await fetch(`${BASE_URL}/cart`, {
      headers: getHeaders(token),
    });
    const data = await handleResponse(response);
    return data.data as CartItem[];
  },

  addToCart: async (
    token: string,
    itemId: string,
    type: 'PRODUCT' | 'ASSET',
    quantity: number = 1
  ): Promise<CartItem> => {
    const response = await fetch(`${BASE_URL}/cart/add`, {
      method: 'POST',
      headers: getHeaders(token),
      body: JSON.stringify({ itemId, type, quantity }),
    });
    const data = await handleResponse(response);
    return data.data as CartItem;
  },

  removeFromCart: async (token: string, cartItemId: string): Promise<void> => {
    const response = await fetch(`${BASE_URL}/cart/${cartItemId}`, {
      method: 'DELETE',
      headers: getHeaders(token),
    });
    await handleResponse(response);
  },

  updateQuantity: async (
    token: string,
    cartItemId: string,
    quantity: number
  ): Promise<CartItem> => {
    const response = await fetch(`${BASE_URL}/cart/${cartItemId}/quantity`, {
      method: 'PUT',
      headers: getHeaders(token),
      body: JSON.stringify({ quantity }),
    });
    const data = await handleResponse(response);
    return data.data as CartItem;
  },

  clearCart: async (token: string): Promise<void> => {
    const response = await fetch(`${BASE_URL}/cart`, {
      method: 'DELETE',
      headers: getHeaders(token),
    });
    await handleResponse(response);
  },
};
