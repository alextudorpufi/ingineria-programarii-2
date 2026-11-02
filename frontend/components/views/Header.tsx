"use client";

import React from 'react';
import { useApp, WorkspaceTab } from '@/context/AppContext';
import { useAuth } from '@/context/AuthContext';
import { Coins, Sparkles, Plus, Moon, Sun, Wallet, LogIn, LogOut, UserRound } from 'lucide-react';
import { Button } from '@/components/ui/Button';


interface HeaderProps {
  currentTab: WorkspaceTab;
  setCurrentTab: (tab: WorkspaceTab) => void;
}

export const Header: React.FC<HeaderProps> = ({ currentTab, setCurrentTab }) => {
  const { userRole, proProfile, setShowWalletModal, darkMode, toggleDarkMode, openAuthModal } = useApp();
  const { user, logout } = useAuth();

  const getTabLabel = (tab: WorkspaceTab) => {
    switch (tab) {
      case 'leads':
        return userRole === 'client'
          ? 'Cererile Mele'
          : userRole === 'admin'
          ? 'Audit & Moderare Lead-uri'
          : 'Oportunități';
      case 'ai':
        return userRole === 'client'
          ? 'AI Architect'
          : userRole === 'admin'
          ? 'Reguli AI & Calitate'
          : 'AI Copilot Oferte';
      case 'chat':
        return userRole === 'client'
          ? 'Oferte & Chat'
          : userRole === 'admin'
          ? 'Mediere & Audit Chat'
          : 'Conversații';
      case 'disputes':
        return userRole === 'client'
          ? 'Garanții & Sesizări'
          : userRole === 'admin'
          ? 'Centru Arbitraj'
          : 'Sesizări';
      case 'analytics':
        return userRole === 'client'
          ? 'Buget & Proiecte'
          : userRole === 'admin'
          ? 'Metrici Platformă'
          : 'Analitice';
      case 'profile':
        return userRole === 'client'
          ? 'Profil Client'
          : userRole === 'admin'
          ? 'Profil Admin'
          : 'Profil & Rating';
    }
  };

  const navTabs: WorkspaceTab[] = ['leads', 'ai', 'chat', 'disputes', 'analytics', 'profile'];

  return (
    <header className="w-full bg-white/95 dark:bg-slate-950/95 backdrop-blur-md border-b border-indigo-100 dark:border-slate-800 sticky top-0 z-40 transition-colors shadow-2xs">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 h-16 flex items-center gap-6 xl:gap-8">
        {/* Left Group: Brand Wordmark + Desktop Nav Tabs Anchored to the LEFT */}
        <div className="flex items-center gap-6 xl:gap-8 shrink-0">
          <button
            onClick={() => setCurrentTab('leads')}
            className="group text-lg font-extrabold tracking-tight text-slate-900 dark:text-white whitespace-nowrap shrink-0 flex items-center gap-2.5 cursor-pointer"
          >
            <div className="w-8 h-8 rounded-xl bg-gradient-to-tr from-indigo-600 via-indigo-500 to-violet-500 flex items-center justify-center text-white shadow-xs group-hover:scale-105 transition-transform">
              <Sparkles className="w-4 h-4" />
            </div>
            <span>
              Skill<span className="bg-gradient-to-r from-indigo-600 to-violet-600 bg-clip-text text-transparent">ink</span>
            </span>
            {/* ROLE BADGE COMPLETELY REMOVED FROM LOGO */}
          </button>

          {/* Navigation Links moved immediately next to Logo */}
          <nav className="hidden lg:flex items-center gap-5 xl:gap-6 text-sm font-semibold text-slate-600 dark:text-slate-300">
            {navTabs.map(tab => (
              <button
                key={tab}
                onClick={() => setCurrentTab(tab)}
                className={`whitespace-nowrap shrink-0 transition-all py-1.5 cursor-pointer ${
                  currentTab === tab
                    ? 'text-indigo-600 dark:text-indigo-400 border-b-2 border-indigo-600 dark:border-indigo-400 font-bold'
                    : 'hover:text-indigo-600 dark:hover:text-white'
                }`}
              >
                {getTabLabel(tab)}
              </button>
            ))}
          </nav>
        </div>

        {/* Right Group: Pushed to far right via ml-auto */}
        <div className="ml-auto flex items-center gap-2 sm:gap-3 shrink-0">
          <button
            onClick={toggleDarkMode}
            aria-label="Comută mod noapte/zi"
            className="p-2 rounded-xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 text-slate-700 dark:text-slate-300 hover:bg-indigo-50 dark:hover:bg-slate-800 transition-colors cursor-pointer shadow-2xs"
            title={darkMode ? 'Schimbă pe Mod Luminos' : 'Schimbă pe Mod Întunecat'}
          >
            {darkMode ? <Sun className="w-4 h-4 text-amber-500" /> : <Moon className="w-4 h-4 text-indigo-600" />}
          </button>

          {user ? (
            <>
              {(userRole === 'pro' || userRole === 'admin') && (
                <button
                  onClick={() => setShowWalletModal(true)}
                  className="px-3 py-2 text-xs font-semibold text-slate-800 dark:text-white bg-amber-50 hover:bg-amber-100/80 dark:bg-amber-950/40 dark:hover:bg-amber-950/60 rounded-xl transition-all flex items-center gap-1.5 whitespace-nowrap border border-amber-200/90 dark:border-amber-800 shadow-2xs active:scale-[0.98] cursor-pointer"
                >
                  <Coins className="w-4 h-4 text-amber-500 shrink-0" />
                  <span className="hidden sm:inline">Sold:</span>
                  <span className="font-mono font-bold tabular-nums text-amber-700 dark:text-amber-300">
                    {proProfile.creditsBalance} cr.
                  </span>
                </button>
              )}

              {userRole === 'client' && (
                <Button variant="primary" size="sm" icon={<Plus className="w-4 h-4" />} onClick={() => setCurrentTab('ai')}>
                  Cerere Nouă
                </Button>
              )}

              <div className="flex items-center gap-2 pl-1 sm:pl-2 border-l border-slate-200 dark:border-slate-800">
                <div className="w-8 h-8 rounded-xl bg-gradient-to-tr from-indigo-600 to-violet-600 text-white flex items-center justify-center text-xs font-bold shadow-xs overflow-hidden shrink-0">
                  {user.avatarUrl ? (
                    // eslint-disable-next-line @next/next/no-img-element
                    <img src={user.avatarUrl} alt={user.name} className="w-full h-full object-cover" />
                  ) : (
                    <UserRound className="w-4 h-4" />
                  )}
                </div>
                <div className="hidden md:block leading-tight max-w-[140px]">
                  <p className="text-xs font-bold text-slate-900 dark:text-white truncate">{user.name}</p>
                  <p className="text-[10px] text-slate-500 dark:text-slate-400 truncate">
                    {user.role === 'admin' ? 'Administrator platformă' : user.role === 'client' ? 'Client verificat' : 'Profesionist'}
                  </p>
                </div>
                <button
                  onClick={() => void logout()}
                  aria-label="Deconectare"
                  title="Deconectare"
                  className="p-2 rounded-xl text-slate-500 hover:text-rose-600 dark:text-slate-400 dark:hover:text-rose-400 hover:bg-rose-50 dark:hover:bg-rose-950/40 transition-colors cursor-pointer"
                >
                  <LogOut className="w-4 h-4" />
                </button>
              </div>
            </>
          ) : (
            <>
              {userRole === 'client' && (
                <div className="hidden md:flex items-center gap-2 px-3 py-1.5 rounded-xl bg-emerald-50 dark:bg-emerald-950/50 border border-emerald-200 dark:border-emerald-800 text-xs text-emerald-800 dark:text-emerald-300 font-medium">
                  <Wallet className="w-3.5 h-3.5 text-emerald-600" />
                  <span>Buget Alocat:</span>
                  <span className="font-mono font-bold tabular-nums">28,500 RON</span>
                </div>
              )}
              <Button variant="primary" size="sm" icon={<LogIn className="w-4 h-4" />} onClick={openAuthModal}>
                Autentificare
              </Button>
            </>
          )}
        </div>
      </div>

      {/* Compact tab strip for narrow viewports */}
      <nav className="lg:hidden border-t border-slate-100 dark:border-slate-800/80 bg-white/80 dark:bg-slate-950/80">
        <div className="max-w-7xl mx-auto px-2 flex items-center gap-1 overflow-x-auto py-1.5 text-xs font-semibold text-slate-600 dark:text-slate-300 [scrollbar-width:none] [&::-webkit-scrollbar]:hidden">
          {navTabs.map(tab => (
            <button
              key={tab}
              onClick={() => setCurrentTab(tab)}
              className={`whitespace-nowrap shrink-0 px-3 py-1.5 rounded-lg transition-all cursor-pointer ${
                currentTab === tab
                  ? 'bg-gradient-to-r from-indigo-600 to-violet-600 text-white shadow-xs'
                  : 'hover:bg-indigo-50 dark:hover:bg-slate-800'
              }`}
            >
              {getTabLabel(tab)}
            </button>
          ))}
        </div>
      </nav>
    </header>
  );
};
