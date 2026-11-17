"use client";

import React, { useRef, useState } from 'react';
import { useApp } from '@/context/AppContext';
import { useAuth } from '@/context/AuthContext';
import { canUseHeaderAction, getNavigation, isKnownRole, resolveWorkspaceTab, type WorkspaceTab } from '@/config/navigation';
import { Coins, Sparkles, Plus, Moon, Sun, LogOut, UserRound, Menu, X } from 'lucide-react';
import { Button } from '@/components/ui/Button';

interface HeaderProps {
  currentTab: WorkspaceTab | null;
  setCurrentTab: (tab: WorkspaceTab) => void;
}

export const Header: React.FC<HeaderProps> = ({ currentTab, setCurrentTab }) => {
  const { setShowWalletModal, darkMode, toggleDarkMode } = useApp();
  const { user, isLoading, logout } = useAuth();
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const menuTrigger = useRef<HTMLButtonElement>(null);
  const navigation = getNavigation(isLoading ? null : user?.role);

  if (!user || isLoading || !isKnownRole(user.role)) return null;

  const closeMenu = () => {
    setIsMobileMenuOpen(false);
    menuTrigger.current?.focus();
  };
  const navigate = (tab: WorkspaceTab) => {
    const destination = resolveWorkspaceTab(user.role, tab);
    if (destination) setCurrentTab(destination);
    if (isMobileMenuOpen) closeMenu();
  };
  const openWallet = () => {
    setShowWalletModal(true);
    if (isMobileMenuOpen) closeMenu();
  };
  const roleLabel = user.role === 'admin' ? 'Administrator platformă' : user.role === 'client' ? 'Client' : 'Profesionist';
  const accountActions = (
    <>
      {canUseHeaderAction(user.role, 'wallet') && (
        <button type="button" onClick={openWallet} aria-label={`Deschide portofelul: ${user.creditsBalance} credite`}
          className="flex min-h-11 items-center justify-center gap-1.5 rounded-xl border border-amber-200/90 bg-amber-50 px-3 text-xs font-semibold text-amber-800 hover:bg-amber-100 dark:border-amber-800 dark:bg-amber-950/40 dark:text-amber-300 dark:hover:bg-amber-950/60">
          <Coins aria-hidden="true" className="size-4 shrink-0" />
          <span className="font-mono font-bold tabular-nums">{user.creditsBalance} cr.</span>
        </button>
      )}
      {canUseHeaderAction(user.role, 'createRequest') && (
        <Button variant="primary" size="sm" className="min-h-11" icon={<Plus aria-hidden="true" className="size-4" />} onClick={() => navigate('ai')}>Cerere Nouă</Button>
      )}
    </>
  );

  return (
    <header className="workspace-header sticky top-0 z-40 w-full border-b border-indigo-100 bg-white/95 shadow-2xs backdrop-blur-md dark:border-slate-800 dark:bg-slate-950/95"
      onKeyDown={event => { if (event.key === 'Escape' && isMobileMenuOpen) { event.preventDefault(); closeMenu(); } }}>
      <div className="mx-auto flex h-16 max-w-7xl items-center gap-3 px-4 sm:gap-5 sm:px-6">
        <button type="button" onClick={() => navigate('leads')} aria-label="Skillink — pagina principală"
          className="group flex min-h-11 shrink-0 items-center gap-2 text-lg font-extrabold tracking-tight text-slate-900 dark:text-white">
          <span className="flex size-8 items-center justify-center rounded-xl bg-gradient-to-tr from-indigo-600 via-indigo-500 to-violet-500 text-white shadow-xs">
            <Sparkles aria-hidden="true" className="size-4" />
          </span>
          <span>Skill<span className="bg-gradient-to-r from-indigo-600 to-violet-600 bg-clip-text text-transparent">ink</span></span>
        </button>

        <nav aria-label="Navigație workspace" className="hidden items-center gap-4 text-xs font-semibold text-slate-600 xl:flex dark:text-slate-300">
          {navigation.map(({ tab, label }) => (
            <button type="button" key={tab} data-workspace-tab={tab} onClick={() => navigate(tab)} aria-current={currentTab === tab ? 'true' : undefined}
              className={`min-h-11 whitespace-nowrap border-b-2 transition-colors ${currentTab === tab
                ? 'border-indigo-600 font-bold text-indigo-600 dark:border-indigo-400 dark:text-indigo-400'
                : 'border-transparent hover:text-indigo-600 dark:hover:text-white'}`}>
              {label}
            </button>
          ))}
        </nav>

        <div className="ml-auto flex shrink-0 items-center gap-1 sm:gap-2">
          <button type="button" onClick={toggleDarkMode} aria-label="Comută mod noapte/zi" aria-pressed={darkMode}
            className="flex size-11 items-center justify-center rounded-xl border border-slate-200 text-slate-700 hover:bg-indigo-50 dark:border-slate-800 dark:text-slate-300 dark:hover:bg-slate-800">
            {darkMode ? <Sun aria-hidden="true" className="size-4 text-amber-500" /> : <Moon aria-hidden="true" className="size-4 text-indigo-600" />}
          </button>
          <div className="hidden items-center gap-2 sm:flex">{accountActions}</div>
          <div className="hidden items-center gap-2 pl-2 lg:flex">
            <span className="flex size-8 items-center justify-center overflow-hidden rounded-xl bg-gradient-to-tr from-indigo-600 to-violet-600 text-white">
              {user.avatarUrl ? (
                // eslint-disable-next-line @next/next/no-img-element
                <img src={user.avatarUrl} alt="" className="size-full object-cover" />
              ) : <UserRound aria-hidden="true" className="size-4" />}
            </span>
            <div className="hidden max-w-32 leading-tight 2xl:block">
              <p className="truncate text-xs font-bold">{user.name}</p>
              <p className="text-[10px] text-slate-500 dark:text-slate-400">{roleLabel}</p>
            </div>
          </div>
          <button type="button" onClick={() => void logout().catch(() => undefined)} aria-label="Deconectare"
            className="flex size-11 items-center justify-center rounded-xl text-slate-500 hover:bg-rose-50 hover:text-rose-600 dark:text-slate-400 dark:hover:bg-rose-950/40 dark:hover:text-rose-400">
            <LogOut aria-hidden="true" className="size-4" />
          </button>
          <button ref={menuTrigger} type="button" onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
            aria-label={isMobileMenuOpen ? 'Închide meniul' : 'Deschide meniul'} aria-expanded={isMobileMenuOpen} aria-controls="workspace-mobile-menu"
            className="flex size-11 items-center justify-center rounded-xl border border-slate-200 text-slate-700 hover:bg-indigo-50 xl:hidden dark:border-slate-800 dark:text-slate-300 dark:hover:bg-slate-800">
            {isMobileMenuOpen ? <X aria-hidden="true" className="size-4" /> : <Menu aria-hidden="true" className="size-4" />}
          </button>
        </div>
      </div>

      <nav id="workspace-mobile-menu" aria-label="Navigație workspace mobilă" hidden={!isMobileMenuOpen}
        className="max-h-[calc(100dvh-4rem)] overflow-y-auto border-t border-slate-200/90 bg-white/95 px-4 py-3 xl:hidden dark:border-slate-800 dark:bg-slate-950/95">
        <p className="mb-2 truncate px-3 text-xs text-slate-500 dark:text-slate-400">{user.name} · {roleLabel}</p>
        <div className="flex flex-col gap-1">
          {navigation.map(({ tab, label }) => (
            <button type="button" key={tab} data-workspace-tab={tab} onClick={() => navigate(tab)} aria-current={currentTab === tab ? 'true' : undefined}
              className={`min-h-11 rounded-xl px-3.5 py-2.5 text-left text-sm font-semibold ${currentTab === tab
                ? 'bg-gradient-to-r from-indigo-600 to-violet-600 text-white shadow-xs'
                : 'text-slate-700 hover:bg-slate-100 dark:text-slate-200 dark:hover:bg-slate-800/60'}`}>
              {label}
            </button>
          ))}
        </div>
        <div className="mt-3 flex flex-col gap-2 sm:hidden">{accountActions}</div>
      </nav>
    </header>
  );
};
