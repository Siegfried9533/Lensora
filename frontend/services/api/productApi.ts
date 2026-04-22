import { BASE_URL, getHeaders, handleResponse } from './config';

export interface Product {
  productId: string;
  categoryId: string;
  categoryName: string;
  userId: string;
  productName: string;
  brand: string;
  description: string;
  price: number;
  stockQuantity: number;
  imageUrls: string[];
  primaryImageUrl: string;
}

export interface Category {
  categoryId: string;
  categoryName: string;
  type: 'PRODUCT' | 'ASSET';
}

export interface Asset {
  assetId: string;
  categoryId: string;
  categoryName: string;
  userId: string;
  modelName: string;
  brand: string;
  dailyRate: number;
  status: 'AVAILABLE' | 'RENTED' | 'MAINTENANCE';
  serialNumber: string;
  imageUrls: string[];
  primaryImageUrl: string;
}

export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export const productApi = {
  getAllProducts: async (page = 0, size = 100): Promise<PaginatedResponse<Product>> => {
    const response = await fetch(`${BASE_URL}/products?page=${page}&size=${size}`);
    const data = await handleResponse(response);
    return data.data as PaginatedResponse<Product>;
  },

  searchProducts: async (
    searchQuery?: string,
    categoryId?: string,
    page = 0,
    size = 100
  ): Promise<PaginatedResponse<Product>> => {
    const params = new URLSearchParams();
    if (searchQuery) params.append('searchQuery', searchQuery);
    if (categoryId) params.append('categoryId', categoryId);
    params.append('page', page.toString());
    params.append('size', size.toString());

    const response = await fetch(`${BASE_URL}/products/search?${params.toString()}`);
    const data = await handleResponse(response);
    return data.data as PaginatedResponse<Product>;
  },

  getProductById: async (id: string): Promise<Product> => {
    const response = await fetch(`${BASE_URL}/products/${id}`);
    const data = await handleResponse(response);
    return data.data as Product;
  },

  getCategories: async (): Promise<Category[]> => {
    const response = await fetch(`${BASE_URL}/categories`);
    const data = await handleResponse(response);
    return data.data as Category[];
  },

  getCategoriesByType: async (type: 'PRODUCT' | 'ASSET'): Promise<Category[]> => {
    const response = await fetch(`${BASE_URL}/categories/by-type/${type}`);
    const data = await handleResponse(response);
    return data.data as Category[];
  },
};

export const assetApi = {
  getAllAssets: async (page = 0, size = 100): Promise<PaginatedResponse<Asset>> => {
    const response = await fetch(`${BASE_URL}/assets?page=${page}&size=${size}`);
    const data = await handleResponse(response);
    return data.data as PaginatedResponse<Asset>;
  },

  searchAssets: async (
    searchQuery?: string,
    categoryId?: string,
    status?: string,
    page = 0,
    size = 100
  ): Promise<PaginatedResponse<Asset>> => {
    const params = new URLSearchParams();
    if (searchQuery) params.append('searchQuery', searchQuery);
    if (categoryId) params.append('categoryId', categoryId);
    if (status) params.append('status', status);
    params.append('page', page.toString());
    params.append('size', size.toString());

    const response = await fetch(`${BASE_URL}/assets/search?${params.toString()}`);
    const data = await handleResponse(response);
    return data.data as PaginatedResponse<Asset>;
  },

  getAssetById: async (id: string): Promise<Asset> => {
    const response = await fetch(`${BASE_URL}/assets/${id}`);
    const data = await handleResponse(response);
    return data.data as Asset;
  },
};
