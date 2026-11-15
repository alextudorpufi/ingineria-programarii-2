import { apiFetch, clearStoredSession } from './apiClient';

export type AuthRole = 'client' | 'pro' | 'admin';

export interface UserProfile {
  id: string;
  email: string;
  role: AuthRole;
  name: string;
  companyName?: string | null;
  title?: string | null;
  phone?: string | null;
  avatarUrl?: string | null;
  bio?: string | null;
  verifiedBadge: boolean;
  proVerified?: boolean;
  rating: number;
  reviewCount: number;
  creditsBalance: number;
  hourlyRate?: number | null;
  clientBudget?: number | null;
  responseTime?: string | null;
  completedJobs: number;
  trustScore: number;
  memberSince?: string | null;
  coverageAreas: string[];
  servicesOffered: string[];
  categories: string[];
}

export interface AuthResponse {
  token: string;
  tokenType: string;
  expiresInSeconds: number;
  user: UserProfile;
}

export interface RegisterPayload {
  email: string;
  password: string;
  name: string;
  role: 'client' | 'pro';
  phone?: string;
  companyName?: string;
}

export const authService = {
  login: async (email: string, password: string): Promise<AuthResponse> => {
    return apiFetch<AuthResponse>('/api/auth/login', {
      method: 'POST',
      body: JSON.stringify({ email, password }),
    });
  },

  register: async (payload: RegisterPayload): Promise<AuthResponse> => {
    return apiFetch<AuthResponse>('/api/auth/register', {
      method: 'POST',
      body: JSON.stringify(payload),
    });
  },

  getMe: async (): Promise<UserProfile> => {
    return apiFetch<UserProfile>('/api/auth/me', { method: 'GET' });
  },

  logout: async (): Promise<void> => {
    try {
      await apiFetch<void>('/api/auth/logout', { method: 'POST' });
    } finally {
      clearStoredSession();
    }
  },
};
