"use client";

import React, { createContext, useCallback, useContext, useEffect, useState } from 'react';
import { authService, type RegisterPayload, type UserProfile } from '@/services/api/authService';
import { clearStoredSession, getStoredToken, storeSession } from '@/services/api/apiClient';

interface AuthContextType {
  user: UserProfile | null;
  token: string | null;
  isLoading: boolean;
  login: (email: string, pass: string) => Promise<void>;
  register: (payload: RegisterPayload) => Promise<void>;
  logout: () => Promise<void>;
  refreshUser: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<UserProfile | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);

  const clearSession = useCallback(() => {
    clearStoredSession();
    setToken(null);
    setUser(null);
  }, []);

  useEffect(() => {
    const handleUnauthorized = () => {
      setUser(null);
      setToken(null);
    };
    window.addEventListener('auth:unauthorized', handleUnauthorized);

    const storedToken = getStoredToken();
    if (!storedToken) {
      Promise.resolve().then(() => setIsLoading(false));
      return () => window.removeEventListener('auth:unauthorized', handleUnauthorized);
    }

    Promise.resolve()
      .then(() => {
        setToken(storedToken);
        return authService.getMe();
      })
      .then(profile => setUser(profile))
      .catch(() => clearSession())
      .finally(() => setIsLoading(false));

    return () => window.removeEventListener('auth:unauthorized', handleUnauthorized);
  }, [clearSession]);

  const login = async (email: string, pass: string) => {
    const response = await authService.login(email, pass);
    storeSession(response.token);
    setToken(response.token);
    setUser(response.user);
  };

  const register = async (payload: RegisterPayload) => {
    const response = await authService.register(payload);
    storeSession(response.token);
    setToken(response.token);
    setUser(response.user);
  };

  const logout = async () => {
    try {
      await authService.logout();
    } finally {
      clearSession();
    }
  };

  const refreshUser = async () => {
    if (!getStoredToken()) return;
    const profile = await authService.getMe();
    setUser(profile);
  };

  return (
    <AuthContext.Provider value={{ user, token, isLoading, login, register, logout, refreshUser }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
