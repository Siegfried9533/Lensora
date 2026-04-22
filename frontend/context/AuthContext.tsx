import React, { createContext, useContext, useState, useEffect } from 'react';
import { authApi, AuthResponse, User as AuthUser } from '@/services/api/authApi';
import { cartApi, CartItem as ApiCartItem } from '@/services/api/cartApi';
import { favoriteApi, Favorite as ApiFavorite } from '@/services/api/favoriteApi';
import { orderApi, Order as ApiOrder } from '@/services/api/orderApi';
import { rentalApi, Rental as ApiRental } from '@/services/api/orderApi';

export interface User {
  userId: string;
  userName: string;
  email: string;
  avatarUrl?: string;
  role: 'user' | 'admin';
  trustScore: number;
}

export interface CartContextItem {
  cartItemId: string;
  id: string;
  quantity: number;
  type: 'PRODUCT' | 'ASSET';
  productName?: string;
  assetName?: string;
  price?: number;
  primaryImageUrl?: string;
}

export interface FavoriteItem {
  favoriteId: string;
  id: string;
  type: 'PRODUCT' | 'ASSET';
  productName?: string;
  assetName?: string;
  price?: number;
  primaryImageUrl?: string;
}

interface AuthContextType {
  user: User | null;
  token: string | null;
  login: (email: string, pass: string) => Promise<boolean>;
  signup: (email: string, pass: string, userName: string) => Promise<boolean>;
  logout: () => void;
  isLoading: boolean;
  error: string | null;

  // Favorites
  favorites: string[];
  favoriteItems: FavoriteItem[];
  toggleFavorite: (id: string, type: 'PRODUCT' | 'ASSET') => Promise<void>;
  loadFavorites: () => Promise<void>;

  // Cart
  cartItems: CartContextItem[];
  addToCart: (id: string, type: 'PRODUCT' | 'ASSET', quantity?: number) => Promise<void>;
  removeFromCart: (cartItemId: string) => Promise<void>;
  updateQuantity: (cartItemId: string, amount: number) => Promise<void>;
  clearCart: () => Promise<void>;
  loadCart: () => Promise<void>;

  // Orders
  orders: ApiOrder[];
  loadOrders: () => Promise<void>;

  // Rentals
  rentals: ApiRental[];
  loadRentals: () => Promise<void>;

  // Profile
  updateAvatar: (uri: string) => Promise<void>;
  changePassword: (oldPass: string, newPass: string) => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

const TOKEN_KEY = 'camera_shop_token';
const USER_KEY = 'camera_shop_user';

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // State for cart, favorites, orders, rentals
  const [cartItems, setCartItems] = useState<CartContextItem[]>([]);
  const [favoriteItems, setFavoriteItems] = useState<FavoriteItem[]>([]);
  const [favorites, setFavorites] = useState<string[]>([]);
  const [orders, setOrders] = useState<ApiOrder[]>([]);
  const [rentals, setRentals] = useState<ApiRental[]>([]);

  // Load token and user from storage on mount
  useEffect(() => {
    const loadStoredAuth = async () => {
      try {
        const storedToken = await AsyncStorage.getItem(TOKEN_KEY);
        const storedUser = await AsyncStorage.getItem(USER_KEY);

        console.log('[AuthContext] Loading stored auth:', {
          hasToken: !!storedToken,
          hasUser: !!storedUser,
          tokenPrefix: storedToken?.substring(0, 20)
        });

        if (storedToken && storedUser) {
          const userData = JSON.parse(storedUser);
          // Verify token is still valid
          try {
            const currentUser = await authApi.getCurrentUser(storedToken);
            console.log('[AuthContext] Token verified, user:', currentUser.email);
            setToken(storedToken);
            setUser({
              userId: currentUser.userId,
              userName: currentUser.userName,
              email: currentUser.email,
              avatarUrl: currentUser.avatarUrl,
              role: currentUser.role as 'user' | 'admin',
              trustScore: currentUser.trustScore,
            });
            // Load user data
            await loadCart();
            await loadFavorites();
            await loadOrders();
            await loadRentals();
          } catch (e: any) {
            console.log('[AuthContext] Token invalid, clearing storage:', e.message);
            // Token invalid, clear storage
            await AsyncStorage.multiRemove([TOKEN_KEY, USER_KEY]);
          }
        } else {
          console.log('[AuthContext] No stored auth found');
        }
      } catch (e) {
        console.error('[AuthContext] Error loading auth:', e);
      } finally {
        console.log('[AuthContext] Loading complete, setting isLoading=false');
        setIsLoading(false);
      }
    };

    loadStoredAuth();
  }, []);

  const login = async (email: string, pass: string): Promise<boolean> => {
    try {
      setError(null);
      const response = await authApi.login({ email, password: pass });
      await saveAuth(response);
      return true;
    } catch (e: any) {
      const message = e.message || 'Login failed';
      setError(message);
      throw new Error(message);
    }
  };

  const signup = async (email: string, pass: string, userName: string): Promise<boolean> => {
    try {
      setError(null);
      const response = await authApi.register({ email, password: pass, userName });
      await saveAuth(response);
      return true;
    } catch (e: any) {
      setError(e.message || 'Registration failed');
      return false;
    }
  };

  const saveAuth = async (response: AuthResponse) => {
    setToken(response.token);
    setUser({
      userId: response.userId,
      userName: response.userName,
      email: response.email,
      role: response.role as 'user' | 'admin',
      trustScore: 100,
    });

    await AsyncStorage.multiSet([
      [TOKEN_KEY, response.token],
      [USER_KEY, JSON.stringify({
        userId: response.userId,
        userName: response.userName,
        email: response.email,
        role: response.role,
        trustScore: 100,
      })],
    ]);

    // Load user data after login
    await loadCart();
    await loadFavorites();
    await loadOrders();
    await loadRentals();
  };

  const logout = async () => {
    setToken(null);
    setUser(null);
    setCartItems([]);
    setFavoriteItems([]);
    setFavorites([]);
    setOrders([]);
    setRentals([]);
    await AsyncStorage.multiRemove([TOKEN_KEY, USER_KEY]);
  };

  const loadCart = async () => {
    if (!token) return;
    try {
      const items = await cartApi.getCartItems(token);
      setCartItems(items.map(item => ({
        cartItemId: item.cartItemId,
        id: item.productId || item.assetId || '',
        quantity: item.quantity,
        type: item.type,
        productName: item.productName,
        assetName: item.assetName,
        price: item.price,
        primaryImageUrl: item.primaryImageUrl,
      })));
    } catch (e) {
      console.error('Error loading cart:', e);
    }
  };

  const addToCart = async (id: string, type: 'PRODUCT' | 'ASSET', quantity: number = 1) => {
    if (!token) return;
    try {
      const newItem = await cartApi.addToCart(token, id, type, quantity);
      setCartItems(prev => [...prev, {
        cartItemId: newItem.cartItemId,
        id: newItem.productId || newItem.assetId || '',
        quantity: newItem.quantity,
        type: newItem.type,
        productName: newItem.productName,
        assetName: newItem.assetName,
        price: newItem.price,
        primaryImageUrl: newItem.primaryImageUrl,
      }]);
    } catch (e: any) {
      setError(e.message || 'Failed to add to cart');
    }
  };

  const removeFromCart = async (cartItemId: string) => {
    if (!token) return;
    try {
      await cartApi.removeFromCart(token, cartItemId);
      setCartItems(prev => prev.filter(item => item.cartItemId !== cartItemId));
    } catch (e: any) {
      setError(e.message || 'Failed to remove from cart');
    }
  };

  const updateQuantity = async (cartItemId: string, amount: number) => {
    if (!token) return;
    try {
      const item = cartItems.find(i => i.cartItemId === cartItemId);
      if (!item) return;

      const newQuantity = Math.max(1, item.quantity + amount);
      const updated = await cartApi.updateQuantity(token, cartItemId, newQuantity);
      setCartItems(prev => prev.map(i =>
        i.cartItemId === cartItemId
          ? { ...i, quantity: updated.quantity }
          : i
      ));
    } catch (e: any) {
      setError(e.message || 'Failed to update quantity');
    }
  };

  const clearCart = async () => {
    if (!token) return;
    try {
      await cartApi.clearCart(token);
      setCartItems([]);
    } catch (e: any) {
      setError(e.message || 'Failed to clear cart');
    }
  };

  const loadFavorites = async () => {
    if (!token) return;
    try {
      const items = await favoriteApi.getFavorites(token);
      const favItems = items.map(item => ({
        favoriteId: item.favoriteId,
        id: item.productId || item.assetId || '',
        type: item.type,
        productName: item.productName,
        assetName: item.assetName,
        price: item.price,
        primaryImageUrl: item.primaryImageUrl,
      }));
      setFavoriteItems(favItems);
      setFavorites(favItems.map(f => f.id));
    } catch (e) {
      console.error('Error loading favorites:', e);
    }
  };

  const toggleFavorite = async (id: string, type: 'PRODUCT' | 'ASSET') => {
    if (!token) return;
    try {
      const result = await favoriteApi.toggleFavorite(token, id, type);
      if (result.action === 'removed') {
        setFavoriteItems(prev => prev.filter(f => f.id !== id));
        setFavorites(prev => prev.filter(f => f !== id));
      } else if (result.favorite) {
        const favItem: FavoriteItem = {
          favoriteId: result.favorite.favoriteId,
          id: result.favorite.productId || result.favorite.assetId || '',
          type: result.favorite.type,
          productName: result.favorite.productName,
          assetName: result.favorite.assetName,
          price: result.favorite.price,
          primaryImageUrl: result.favorite.primaryImageUrl,
        };
        setFavoriteItems(prev => [...prev, favItem]);
        setFavorites(prev => [...prev, id]);
      }
    } catch (e: any) {
      setError(e.message || 'Failed to toggle favorite');
    }
  };

  const loadOrders = async () => {
    if (!token) return;
    try {
      const userOrders = await orderApi.getOrders(token);
      setOrders(userOrders);
    } catch (e) {
      console.error('Error loading orders:', e);
    }
  };

  const loadRentals = async () => {
    if (!token) return;
    try {
      const userRentals = await rentalApi.getRentals(token);
      setRentals(userRentals);
    } catch (e) {
      console.error('Error loading rentals:', e);
    }
  };

  const updateAvatar = async (uri: string) => {
    if (!token || !user) return;
    try {
      const updatedUser = await authApi.updateAvatar(token, uri);
      setUser({
        ...user,
        avatarUrl: updatedUser.avatarUrl,
      });
      await AsyncStorage.setItem(USER_KEY, JSON.stringify({
        userId: user.userId,
        userName: user.userName,
        email: user.email,
        role: user.role,
        trustScore: user.trustScore,
        avatarUrl: updatedUser.avatarUrl,
      }));
    } catch (e: any) {
      setError(e.message || 'Failed to update avatar');
    }
  };

  const changePassword = async (oldPass: string, newPass: string) => {
    if (!token) return;
    try {
      await authApi.changePassword(token, oldPass, newPass);
    } catch (e: any) {
      throw new Error(e.message || 'Failed to change password');
    }
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        login,
        signup,
        logout,
        isLoading,
        error,
        favorites,
        favoriteItems,
        toggleFavorite,
        loadFavorites,
        cartItems,
        addToCart,
        removeFromCart,
        updateQuantity,
        clearCart,
        loadCart,
        orders,
        loadOrders,
        rentals,
        loadRentals,
        updateAvatar,
        changePassword,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}

// AsyncStorage polyfill for React Native
const AsyncStorage = {
  getItem: async (key: string): Promise<string | null> => {
    try {
      const item = await import('@react-native-async-storage/async-storage');
      return item.default.getItem(key);
    } catch {
      // Fallback to localStorage for web
      return typeof window !== 'undefined' ? window.localStorage.getItem(key) : null;
    }
  },
  setItem: async (key: string, value: string): Promise<void> => {
    try {
      const item = await import('@react-native-async-storage/async-storage');
      await item.default.setItem(key, value);
    } catch {
      if (typeof window !== 'undefined') {
        window.localStorage.setItem(key, value);
      }
    }
  },
  multiSet: async (keyValuePairs: [string, string][]): Promise<void> => {
    try {
      const item = await import('@react-native-async-storage/async-storage');
      await item.default.multiSet(keyValuePairs);
    } catch {
      if (typeof window !== 'undefined') {
        keyValuePairs.forEach(([key, value]) => window.localStorage.setItem(key, value));
      }
    }
  },
  multiRemove: async (keys: string[]): Promise<void> => {
    try {
      const item = await import('@react-native-async-storage/async-storage');
      await item.default.multiRemove(keys);
    } catch {
      if (typeof window !== 'undefined') {
        keys.forEach(key => window.localStorage.removeItem(key));
      }
    }
  },
};
