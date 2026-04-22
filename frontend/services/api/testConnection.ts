/**
 * Test API connection utility
 * Use this to debug network issues
 */

import { BASE_URL } from './config';

export const testConnection = async (): Promise<{
  success: boolean;
  message: string;
  baseUrl: string;
  response?: any;
}> => {
  console.log('[Test Connection] BASE_URL:', BASE_URL);
  console.log('[Test Connection] process.env.EXPO_PUBLIC_API_URL:', process.env.EXPO_PUBLIC_API_URL);

  try {
    // Test health check endpoint
    const response = await fetch(`${BASE_URL}/auth/login`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        email: 'test@example.com',
        password: 'password123',
      }),
    });

    const text = await response.text();
    console.log('[Test Connection] Raw response:', text);

    if (!text) {
      return {
        success: false,
        message: 'Empty response from server',
        baseUrl: BASE_URL,
      };
    }

    const data = JSON.parse(text);

    if (response.ok && data.success) {
      return {
        success: true,
        message: 'Connection successful!',
        baseUrl: BASE_URL,
        response: data,
      };
    }

    return {
      success: false,
      message: data.message || 'API returned error',
      baseUrl: BASE_URL,
      response: data,
    };
  } catch (error: any) {
    console.error('[Test Connection] Error:', error);
    return {
      success: false,
      message: error.message || 'Network error',
      baseUrl: BASE_URL,
    };
  }
};

export const getDebugInfo = () => ({
  BASE_URL,
  EXPO_PUBLIC_API_URL: process.env.EXPO_PUBLIC_API_URL,
  platform: typeof navigator !== 'undefined' ? navigator.userAgent : 'unknown',
  isDev: __DEV__,
});
