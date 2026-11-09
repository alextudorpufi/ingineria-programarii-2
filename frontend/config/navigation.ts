import type { AuthRole } from '../services/api/authService';

export type WorkspaceTab = 'leads' | 'ai' | 'chat' | 'disputes' | 'analytics' | 'profile';

interface NavigationItem {
  readonly tab: WorkspaceTab;
  readonly label: string;
}

interface RoleNavigation {
  readonly items: readonly NavigationItem[];
  readonly defaultTab: WorkspaceTab;
  readonly actions: { readonly createRequest: boolean; readonly wallet: boolean };
}

export const ROLE_NAVIGATION = {
  client: {
    items: [
      { tab: 'leads', label: 'Cererile Mele' },
      { tab: 'ai', label: 'AI Architect' },
      { tab: 'chat', label: 'Oferte & Chat' },
      { tab: 'profile', label: 'Profil Client' },
    ],
    defaultTab: 'leads',
    actions: { createRequest: true, wallet: false },
  },
  pro: {
    items: [
      { tab: 'leads', label: 'Oportunități' },
      { tab: 'chat', label: 'Conversații' },
      { tab: 'disputes', label: 'Sesizări' },
      { tab: 'analytics', label: 'Analitice' },
      { tab: 'profile', label: 'Profil & Rating' },
    ],
    defaultTab: 'leads',
    actions: { createRequest: false, wallet: true },
  },
  admin: {
    items: [
      { tab: 'leads', label: 'Audit & Moderare Lead-uri' },
      { tab: 'disputes', label: 'Centru Arbitraj' },
      { tab: 'analytics', label: 'Metrici Platformă' },
      { tab: 'profile', label: 'Profil Admin' },
    ],
    defaultTab: 'leads',
    actions: { createRequest: false, wallet: false },
  },
} as const satisfies Record<AuthRole, RoleNavigation>;

export function isKnownRole(role: unknown): role is AuthRole {
  return typeof role === 'string' && Object.hasOwn(ROLE_NAVIGATION, role);
}

export function getNavigation(role: unknown): readonly NavigationItem[] {
  return isKnownRole(role) ? ROLE_NAVIGATION[role].items : [];
}

export function canAccessTab(role: unknown, tab: unknown): tab is WorkspaceTab {
  return getNavigation(role).some(item => item.tab === tab);
}

export function resolveWorkspaceTab(role: unknown, tab: unknown): WorkspaceTab | null {
  if (!isKnownRole(role)) return null;
  return canAccessTab(role, tab) ? tab : ROLE_NAVIGATION[role].defaultTab;
}

export function canUseHeaderAction(role: unknown, action: keyof RoleNavigation['actions']): boolean {
  return isKnownRole(role) && ROLE_NAVIGATION[role].actions[action];
}
