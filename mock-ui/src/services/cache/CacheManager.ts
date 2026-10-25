/**
 * CacheManager Class
 * Follows Single Responsibility Principle (SRP) for in-memory and local storage caching with TTL.
 */

interface CacheEntry<T> {
  data: T;
  expiry: number; // timestamp in ms
  createdAt: number;
}

export class CacheManager {
  private static instance: CacheManager;
  private memoryCache: Map<string, CacheEntry<any>> = new Map();
  private readonly defaultTTL: number; // milliseconds

  private constructor(defaultTTLMs: number = 5 * 60 * 1000) {
    this.defaultTTL = defaultTTLMs;
  }

  public static getInstance(defaultTTLMs?: number): CacheManager {
    if (!CacheManager.instance) {
      CacheManager.instance = new CacheManager(defaultTTLMs);
    }
    return CacheManager.instance;
  }

  public get<T>(key: string): T | null {
    const entry = this.memoryCache.get(key);
    if (!entry) {
      // Fallback to localStorage if available
      try {
        const itemStr = localStorage.getItem(`skillink_cache_${key}`);
        if (!itemStr) return null;
        const parsed: CacheEntry<T> = JSON.parse(itemStr);
        if (Date.now() > parsed.expiry) {
          localStorage.removeItem(`skillink_cache_${key}`);
          return null;
        }
        // Warm memory cache
        this.memoryCache.set(key, parsed);
        return parsed.data;
      } catch {
        return null;
      }
    }

    if (Date.now() > entry.expiry) {
      this.memoryCache.delete(key);
      try {
        localStorage.removeItem(`skillink_cache_${key}`);
      } catch {}
      return null;
    }

    return entry.data as T;
  }

  public set<T>(key: string, data: T, ttlMs?: number): void {
    const duration = ttlMs !== undefined ? ttlMs : this.defaultTTL;
    const entry: CacheEntry<T> = {
      data,
      expiry: Date.now() + duration,
      createdAt: Date.now()
    };

    this.memoryCache.set(key, entry);

    try {
      localStorage.setItem(`skillink_cache_${key}`, JSON.stringify(entry));
    } catch {
      // Storage quota or private browsing fallback
    }
  }

  public invalidate(key: string): void {
    this.memoryCache.delete(key);
    try {
      localStorage.removeItem(`skillink_cache_${key}`);
    } catch {}
  }

  public clear(): void {
    this.memoryCache.clear();
    try {
      const keys = Object.keys(localStorage);
      for (const k of keys) {
        if (k.startsWith('skillink_cache_')) {
          localStorage.removeItem(k);
        }
      }
    } catch {}
  }
}

export const globalCache = CacheManager.getInstance();
