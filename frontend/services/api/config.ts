<<<<<<< HEAD
import Constants from 'expo-constants';
import * as Device from 'expo-device';
import { Platform } from 'react-native';

// API Base URL Configuration
// Priority order:
// 1. EXPO_PUBLIC_API_URL override from .env
// 2. Android emulator: 10.0.2.2
// 3. iOS simulator / web: localhost
// 4. Physical device: reuse the Expo host IP when available
// 5. Final fallback: localhost

const API_PORT = 8080;
const API_PATH = '/api';

const normalizeBaseUrl = (host: string) => {
  const trimmed = host.replace(/\/$/, '');
  return `http://${trimmed}:${API_PORT}${API_PATH}`;
};

const getExpoHost = (): string | null => {
  const hostUri =
    Constants.expoConfig?.hostUri ??
    Constants.manifest2?.extra?.expoClient?.hostUri ??
    // @ts-ignore - legacy Expo manifest shape for older runtimes
    Constants.manifest?.debuggerHost ??
    null;

  if (!hostUri) {
    return null;
  }

  try {
    return hostUri.includes('://') ? new URL(`http://${hostUri}`).hostname : new URL(`http://${hostUri}`).hostname;
  } catch {
    return hostUri.split(':')[0] ?? null;
  }
};

const getEnvUrl = (): string => {
  const envUrl = process.env.EXPO_PUBLIC_API_URL;

  if (Platform.OS === 'android') {
    // Android emulators cannot reach the host machine through localhost.
    if (!Device.isDevice) {
      return `http://10.0.2.2:${API_PORT}${API_PATH}`;
    }

    if (envUrl) {
      return envUrl;
    }

    const host = getExpoHost();
    if (host && host !== 'localhost' && host !== '127.0.0.1') {
      return normalizeBaseUrl(host);
    }

    return `http://localhost:${API_PORT}${API_PATH}`;
  }

  if (envUrl) {
    return envUrl;
  }

  if (Platform.OS === 'ios' || Platform.OS === 'web') {
    return `http://localhost:${API_PORT}${API_PATH}`;
  }

  const host = getExpoHost();
  if (host && host !== 'localhost' && host !== '127.0.0.1') {
    return normalizeBaseUrl(host);
  }

  return `http://localhost:${API_PORT}${API_PATH}`;
=======
// API Base URL Configuration
// - iOS Simulator: http://localhost:8080/api works
// - Android Emulator: Needs http://10.0.2.2:8080/api (Android's localhost alias)
// - Physical Device: Needs your computer's IP (e.g., http://192.168.1.100:8080/api)
//
// To configure for your device:
// 1. Find your computer's IP:
//    - Windows: ipconfig (look for IPv4 Address)
//    - Mac/Linux: ifconfig (look for inet under en0/wlan0)
// 2. Update .env file: EXPO_PUBLIC_API_URL=http://YOUR_IP:8080/api
// 3. Restart Expo: npx expo start -c (clear cache)

const getEnvUrl = (): string => {
  // 1. Try Expo environment variable (injected at build time)
  // @ts-ignore
  if (process.env.EXPO_PUBLIC_API_URL) {
    // @ts-ignore
    return process.env.EXPO_PUBLIC_API_URL;
  }
  // 2. Fallback to localhost (works on iOS Simulator)
  return 'http://localhost:8080/api';
>>>>>>> LTH
};

export const BASE_URL = getEnvUrl();

// Debug log
if (__DEV__) {
  console.log('[API Config] BASE_URL:', BASE_URL);
<<<<<<< HEAD
  if (!process.env.EXPO_PUBLIC_API_URL) {
    console.info('[API Config] EXPO_PUBLIC_API_URL not set; using automatic platform-based fallback.');
=======
  // @ts-ignore
  if (!process.env.EXPO_PUBLIC_API_URL) {
    console.warn('⚠️ EXPO_PUBLIC_API_URL not set — using localhost. For physical devices, set it in .env');
>>>>>>> LTH
  }
}

export const getHeaders = (token?: string) => {
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
  };
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }
  return headers;
};

export const handleResponse = async (response: Response) => {
  let text: string;
  try {
    text = await response.text();
  } catch (e) {
    throw new Error('Cannot connect to server - please check if backend is running on port 8080');
  }

  if (!text) {
    throw new Error('Server returned empty response');
  }

  let data: any;
  try {
    data = JSON.parse(text);
  } catch (e) {
    throw new Error('Server returned invalid response');
  }

  if (!response.ok) {
    if (response.status === 401) {
      throw new Error('Unauthorized - please login again');
    }
    if (response.status === 403) {
      throw new Error('Access denied - insufficient permissions');
    }
    if (response.status === 404) {
      throw new Error('Resource not found');
    }
    if (response.status === 500) {
      throw new Error('Server error - please try again later');
    }
    throw new Error(data.message || data.error || `Request failed (${response.status})`);
  }

  // Check application-level errors (backend returned 200 but success=false)
  if (data && typeof data.success === 'boolean' && !data.success) {
    throw new Error(data.message || 'Request failed');
  }

  if (__DEV__) {
    console.log(`[API] ${response.url} -> success:`, data?.success, 'hasData:', data?.data != null);
  }

  return data;
};

// Network error types for better error handling
export const NetworkError = {
  CONNECTION_FAILED: 'Cannot connect to server - please check if backend is running',
  INVALID_RESPONSE: 'Server returned invalid response',
  UNAUTHORIZED: 'Session expired - please login again',
  TIMEOUT: 'Request timed out - please check your connection',
};
