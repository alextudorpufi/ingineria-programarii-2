/**
 * Centralized fetch wrapper for the Skillink REST API.
 *
 * - Injects the bearer token stored by AuthContext.
 * - Normalizes RFC 7807 ProblemDetail errors into Error instances.
 * - Broadcasts `auth:unauthorized` when an authenticated session expires.
 */

const BASE_URL = process.env.NEXT_PUBLIC_API_URL || '';

const TOKEN_STORAGE_KEY = 'skillink_token';
const USER_STORAGE_KEY = 'skillink_user';

export interface ApiError {
  status: number;
  title: string;
  detail: string;
  invalidParams?: Record<string, string>;
}

function isAuthRequest(endpoint: string): boolean {
  return endpoint.includes('/api/auth/login') || endpoint.includes('/api/auth/register');
}

async function readErrorMessage(response: Response): Promise<string> {
  try {
    const payload = (await response.json()) as Partial<ApiError> & { message?: string };
    if (payload.invalidParams && Object.keys(payload.invalidParams).length > 0) {
      return Object.values(payload.invalidParams).join(' ');
    }
    return payload.detail || payload.title || payload.message || response.statusText;
  } catch {
    return response.statusText || 'A apărut o eroare neașteptată.';
  }
}

export function clearStoredSession(): void {
  if (typeof window === 'undefined') return;
  localStorage.removeItem(TOKEN_STORAGE_KEY);
  localStorage.removeItem(USER_STORAGE_KEY);
}

export function getStoredToken(): string | null {
  if (typeof window === 'undefined') return null;
  return localStorage.getItem(TOKEN_STORAGE_KEY);
}

export function storeSession(token: string): void {
  if (typeof window === 'undefined') return;
  localStorage.setItem(TOKEN_STORAGE_KEY, token);
}

export async function apiFetch<T>(endpoint: string, options: RequestInit = {}): Promise<T> {
  const token = getStoredToken();

  const headers = new Headers(options.headers || {});
  headers.set('Content-Type', 'application/json');
  if (token) {
    headers.set('Authorization', `Bearer ${token}`);
  }

  const url = endpoint.startsWith('http') ? endpoint : `${BASE_URL}${endpoint}`;
  const response = await fetch(url, { ...options, headers });

  if (response.status === 401 && !isAuthRequest(endpoint)) {
    clearStoredSession();
    if (typeof window !== 'undefined') {
      window.dispatchEvent(new Event('auth:unauthorized'));
    }
    throw new Error('Sesiunea a expirat. Vă rugăm să vă autentificați din nou.');
  }

  if (!response.ok) {
    throw new Error(await readErrorMessage(response));
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return (await response.json()) as T;
}
