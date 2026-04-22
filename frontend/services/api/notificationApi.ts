import { BASE_URL, getHeaders, handleResponse } from './config';

const API_TIMEOUT = 15000;

const fetchWithTimeout = (url: string, options: RequestInit, timeout = API_TIMEOUT): Promise<Response> => {
  return Promise.race([
    fetch(url, options),
    new Promise<never>((_, reject) =>
      setTimeout(() => reject(new Error('Request timed out')), timeout)
    ),
  ]);
};

export interface Notification {
  notificationId: string;
  userId: string;
  title: string;
  message: string;
  type: 'ORDER_UPDATE' | 'RENTAL_REMINDER' | 'RENTAL_OVERDUE' | 'SYSTEM' | 'PROMOTION' | 'PAYMENT_SUCCESS' | 'PAYMENT_FAILED' | 'SHIPPING_UPDATE';
  referenceId?: string;
  referenceType?: 'ORDER' | 'RENTAL';
  isRead: boolean;
  isActionRequired: boolean;
  actionUrl?: string;
  deepLinkUrl?: string;
  createdAt: string;
  readAt?: string;
}

export interface NotificationsResponse {
  content: Notification[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export interface UnreadCountResponse {
  count: number;
}

const EMPTY_NOTIFICATIONS_RESPONSE: NotificationsResponse = {
  content: [],
  page: 0,
  size: 20,
  totalElements: 0,
  totalPages: 0,
  first: true,
  last: true,
};

export const notificationApi = {
  getNotifications: async (token: string, page: number = 0, size: number = 20): Promise<NotificationsResponse> => {
    const response = await fetchWithTimeout(`${BASE_URL}/notifications?page=${page}&size=${size}`, {
      method: 'GET',
      headers: getHeaders(token),
    });
    const data = await handleResponse(response);
    const result = data?.data;
    if (!result || typeof result !== 'object') {
      console.warn('[NotificationApi] getNotifications: invalid data, using empty response. data?.data =', result);
      return { ...EMPTY_NOTIFICATIONS_RESPONSE, page, size };
    }
    const normalized = {
      content: Array.isArray(result.content) ? result.content : [],
      page: typeof result.page === 'number' ? result.page : 0,
      size: typeof result.size === 'number' ? result.size : 20,
      totalElements: typeof result.totalElements === 'number' ? result.totalElements : 0,
      totalPages: typeof result.totalPages === 'number' ? result.totalPages : 0,
      first: result.first ?? true,
      last: result.last ?? true,
    };
    console.log('[NotificationApi] getNotifications: returning', normalized.content.length, 'items');
    return normalized;
  },

  getUnreadNotifications: async (token: string): Promise<Notification[]> => {
    const response = await fetchWithTimeout(`${BASE_URL}/notifications/unread`, {
      method: 'GET',
      headers: getHeaders(token),
    });
    const data = await handleResponse(response);
    return Array.isArray(data?.data) ? data.data : [];
  },

  getUnreadCount: async (token: string): Promise<number> => {
    const response = await fetchWithTimeout(`${BASE_URL}/notifications/unread/count`, {
      method: 'GET',
      headers: getHeaders(token),
    });
    const data = await handleResponse(response);
    const count = data?.data?.count;
    console.log('[NotificationApi] getUnreadCount:', count);
    return typeof count === 'number' ? count : 0;
  },

  markAsRead: async (token: string, notificationId: string): Promise<Notification | null> => {
    const response = await fetchWithTimeout(`${BASE_URL}/notifications/${notificationId}/read`, {
      method: 'POST',
      headers: getHeaders(token),
    });
    const data = await handleResponse(response);
    return data?.data ?? null;
  },

  markAllAsRead: async (token: string): Promise<number> => {
    const response = await fetchWithTimeout(`${BASE_URL}/notifications/read-all`, {
      method: 'POST',
      headers: getHeaders(token),
    });
    const data = await handleResponse(response);
    const markedCount = data?.data?.markedCount;
    return typeof markedCount === 'number' ? markedCount : 0;
  },

  deleteNotification: async (token: string, notificationId: string): Promise<void> => {
    const response = await fetchWithTimeout(`${BASE_URL}/notifications/${notificationId}`, {
      method: 'DELETE',
      headers: getHeaders(token),
    });
    await handleResponse(response);
  },

  createTestNotification: async (
    token: string,
    type: string,
    title: string,
    message: string
  ): Promise<Notification | null> => {
    const response = await fetchWithTimeout(`${BASE_URL}/notifications/test`, {
      method: 'POST',
      headers: getHeaders(token),
      body: JSON.stringify({ type, title, message }),
    });
    const data = await handleResponse(response);
    return data?.data ?? null;
  },

  getSystemNotifications: async (): Promise<Notification[]> => {
    try {
      const response = await fetchWithTimeout(`${BASE_URL}/notifications/system`, {
        method: 'GET',
        headers: { 'Content-Type': 'application/json' },
      });
      const data = await handleResponse(response);
      return Array.isArray(data?.data) ? data.data : [];
    } catch {
      return [];
    }
  },
};