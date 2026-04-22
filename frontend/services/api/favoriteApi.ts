import { BASE_URL, getHeaders, handleResponse } from './config';

export interface Favorite {
  favoriteId: string;
  productId?: string;
  assetId?: string;
  type: 'PRODUCT' | 'ASSET';
  productName?: string;
  assetName?: string;
  price?: number;
  primaryImageUrl?: string;
}

export const favoriteApi = {
  getFavorites: async (token: string): Promise<Favorite[]> => {
    const response = await fetch(`${BASE_URL}/favorites`, {
      headers: getHeaders(token),
    });
    const data = await handleResponse(response);
    return data.data as Favorite[];
  },

  toggleFavorite: async (
    token: string,
    itemId: string,
    type: 'PRODUCT' | 'ASSET'
  ): Promise<{ action: string; favorite?: Favorite }> => {
    const response = await fetch(`${BASE_URL}/favorites/toggle`, {
      method: 'POST',
      headers: getHeaders(token),
      body: JSON.stringify({ itemId, type }),
    });
    const data = await handleResponse(response);
    return data.data as { action: string; favorite?: Favorite };
  },

  isFavorite: async (
    token: string,
    itemId: string,
    type: 'PRODUCT' | 'ASSET'
  ): Promise<boolean> => {
    const response = await fetch(
      `${BASE_URL}/favorites/check?itemId=${itemId}&type=${type}`,
      {
        headers: getHeaders(token),
      }
    );
    const data = await handleResponse(response);
    return data.data.isFavorite as boolean;
  },
};
