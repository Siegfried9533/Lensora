import { BASE_URL, getHeaders, handleResponse } from './config';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  userName: string;
  email: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  email: string;
  userName: string;
  role: string;
  userId: string;
  emailVerified?: boolean;
  message?: string;
}

export interface User {
  userId: string;
  userName: string;
  email: string;
  avatarUrl?: string;
  role: string;
  trustScore: number;
  emailVerified?: boolean;
  provider?: string;
}

export const authApi = {
  login: async (credentials: LoginRequest): Promise<AuthResponse> => {
    const response = await fetch(`${BASE_URL}/auth/login`, {
      method: 'POST',
      headers: getHeaders(),
      body: JSON.stringify(credentials),
    });
    const data = await handleResponse(response);
    return data.data as AuthResponse;
  },

  register: async (data: RegisterRequest): Promise<AuthResponse> => {
    const response = await fetch(`${BASE_URL}/auth/register`, {
      method: 'POST',
      headers: getHeaders(),
      body: JSON.stringify(data),
    });
    const res = await handleResponse(response);
    return res.data as AuthResponse;
  },

  getCurrentUser: async (token: string): Promise<User> => {
    const response = await fetch(`${BASE_URL}/auth/me`, {
      headers: getHeaders(token),
    });
    const data = await handleResponse(response);
    return data.data as User;
  },

  updateAvatar: async (token: string, avatarUrl: string): Promise<User> => {
    const response = await fetch(`${BASE_URL}/auth/avatar`, {
      method: 'PUT',
      headers: getHeaders(token),
      body: JSON.stringify({ avatarUrl }),
    });
    const data = await handleResponse(response);
    return data.data as User;
  },

  changePassword: async (
    token: string,
    oldPassword: string,
    newPassword: string
  ): Promise<void> => {
    const response = await fetch(`${BASE_URL}/auth/change-password`, {
      method: 'POST',
      headers: getHeaders(token),
      body: JSON.stringify({ oldPassword, newPassword }),
    });
    await handleResponse(response);
  },

  verifyEmail: async (token: string): Promise<void> => {
    const response = await fetch(`${BASE_URL}/auth/verify-email?token=${token}`, {
      method: 'POST',
      headers: getHeaders(),
    });
    await handleResponse(response);
  },

  resendVerificationEmail: async (email: string): Promise<void> => {
    const response = await fetch(`${BASE_URL}/auth/resend-verification`, {
      method: 'POST',
      headers: getHeaders(),
      body: JSON.stringify({ email }),
    });
    await handleResponse(response);
  },
};
