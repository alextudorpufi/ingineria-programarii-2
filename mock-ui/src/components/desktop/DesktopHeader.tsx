import React from 'react';
import { useApp, DesktopNavTab } from '../../context/AppContext';
import { Coins, Sparkles, Plus, Moon, Sun, Shield, Lock, Wallet } from 'lucide-react';
import { Button } from '../ui/Button';

interface DesktopHeaderProps {
  currentTab: DesktopNavTab;
  setCurrentTab: (tab: DesktopNavTab) => void;
}

export const DesktopHeader: React.FC<DesktopHeaderProps> = ({
  currentTab,
  setCurrentTab
}) => {
  const { userRole, proProfile, setShowWalletModal, setShowAIArchitect, darkMode, toggleDarkMode } = useApp();

  // Dynamic tab labels based on role
  const getTabLabel = (tab: DesktopNavTab) => {
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

  return (
    <header className="w-full bg-white/95 dark:bg-slate-950/95 backdrop-blur-md border-b border-indigo-100 dark:border-slate-800 sticky top-0 z-40 transition-colors shadow-2xs">
      <div className="max-w-7xl mx-auto px-6 h-16 flex items-center justify-between gap-6">
        {/* Zone 1: Brand Wordmark (Skillink) */}
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
          <span className="text-[10px] uppercase font-bold tracking-wider px-2 py-0.5 rounded-md bg-indigo-50 dark:bg-indigo-950/80 text-indigo-700 dark:text-indigo-300 border border-indigo-200/80 dark:border-indigo-800">
            {userRole === 'client' ? 'Client' : userRole === 'admin' ? 'Admin' : 'Pro'}
          </span>
        </button>

        {/* Zone 2: Navigation Links adapting to Role */}
        <nav className="hidden md:flex items-center gap-6 text-sm font-semibold text-slate-600 dark:text-slate-300">
          {(['leads', 'ai', 'chat', 'disputes', 'analytics', 'profile'] as DesktopNavTab[]).map(tab => (
            <button
              key={tab}
              onClick={() => {
                setCurrentTab(tab);
                if (tab === 'ai') setShowAIArchitect(true);
              }}
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

        {/* Zone 3: Primary Action + Theme Toggle */}
        <div className="flex items-center gap-3 shrink-0">
          <button
            onClick={toggleDarkMode}
            aria-label="Comută mod noapte/zi"
            className="p-2 rounded-xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 text-slate-700 dark:text-slate-300 hover:bg-indigo-50 dark:hover:bg-slate-800 transition-colors cursor-pointer shadow-2xs"
            title={darkMode ? 'Schimbă pe Mod Luminos' : 'Schimbă pe Mod Întunecat'}
          >
            {darkMode ? <Sun className="w-4 h-4 text-amber-500" /> : <Moon className="w-4 h-4 text-indigo-600" />}
          </button>

          {userRole === 'pro' && (
            <button
              onClick={() => setShowWalletModal(true)}
              className="px-3.5 py-2 text-xs font-semibold text-slate-800 dark:text-white bg-amber-50 hover:bg-amber-100/80 dark:bg-amber-950/40 dark:hover:bg-amber-950/60 rounded-xl transition-all flex items-center gap-2 whitespace-nowrap shrink-0 border border-amber-200/90 dark:border-amber-800 shadow-2xs hover:shadow-xs active:scale-[0.98] cursor-pointer"
            >
              <Coins className="w-4 h-4 text-amber-500 shrink-0" />
              <span>Sold:</span>
              <span className="font-mono font-bold tabular-nums text-amber-700 dark:text-amber-300">
                {proProfile.creditsBalance} credite
              </span>
            </button>
          )}

          {userRole === 'client' && (
            <div className="flex items-center gap-2">
              <div className="px-3 py-1.5 rounded-xl bg-emerald-50 dark:bg-emerald-950/50 border border-emerald-200 dark:border-emerald-800 text-xs text-emerald-800 dark:text-emerald-300 flex items-center gap-1.5 font-medium">
                <Wallet className="w-3.5 h-3.5 text-emerald-600" />
                <span>Buget Alocat:</span>
                <span className="font-mono font-bold">28,500 RON</span>
              </div>
              <Button
                variant="primary"
                size="sm"
                icon={<Plus className="w-4 h-4" />}
                onClick={() => {
                  setCurrentTab('ai');
                  setShowAIArchitect(true);
                }}
              >
                Cerere Nouă
              </Button>
            </div>
          )}

          {userRole === 'admin' && (
            <div className="flex items-center gap-2">
              <div className="px-3 py-1.5 rounded-xl bg-purple-50 dark:bg-purple-950/60 border border-purple-200 dark:border-purple-800 text-xs text-purple-800 dark:text-purple-300 flex items-center gap-1.5 font-medium">
                <Shield className="w-3.5 h-3.5 text-purple-600" />
                <span className="hidden sm:inline">Platform Vault:</span>
                <span className="font-mono font-bold">{proProfile.creditsBalance} credite</span>
              </div>
              <span className="text-[11px] font-bold px-2.5 py-1 rounded-xl bg-rose-50 text-rose-700 border border-rose-200 dark:bg-rose-950/60 dark:text-rose-300 dark:border-rose-900 flex items-center gap-1">
                <Lock className="w-3 h-3" />
                Audit Live
              </span>
            </div>
          )}
        </div>
      </div>
    </header>
  );
};
