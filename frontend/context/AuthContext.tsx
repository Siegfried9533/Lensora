import React, { createContext, useContext, useState } from 'react';
import { USERS } from '@/constants/mockData';
import { User } from '@/constants/types';

interface AuthContextType {
  user: User | null;
  login: (email: string, pass: string) => boolean;
  signup: (email: string, pass: string, userName: string) => boolean;
  logout: () => void;
  // Giả lập Favorites
  favorites: string[];
  toggleFavorite: (id: string) => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  //const [allUsers, setAllUsers] = useState<User[]>(USERS);
  const [favorites, setFavorites] = useState<string[]>([]);

  // Giả lập Login
  const login = (email: string, pass: string) => {
    // Tìm user trong mảng mock
    const foundUser = USERS.find(u => u.email === email && u.password === pass);
    if (foundUser) {
      setUser(foundUser);
      return true;
    }
    return false;
  };

  const signup = (email: string, pass: string, userName: string) => {
    // Check trùng
    if (USERS.find(u => u.email === email)) return false;
    
    const newUser: User = {
      userId: `u${USERS.length + 1}`,
      email,
      password: pass,
      userName,
      role: 'user',
      trustScore: 100,
    };
    USERS.push(newUser); // Push mock
    setUser(newUser);
    return true;
  };

  const logout = () => {
    setUser(null);
    setFavorites([]);
  };

  const toggleFavorite = (id: string) => {
    setFavorites(prev => 
      prev.includes(id) ? prev.filter(f => f !== id) : [...prev, id]
    );
  };

  return (
    <AuthContext.Provider value={{ user, login, signup, logout, favorites, toggleFavorite }}>
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
