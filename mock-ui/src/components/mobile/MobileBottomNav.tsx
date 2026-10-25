import React from 'react';
import { useApp } from '../../context/AppContext';
import { Briefcase, Sparkles, MessageSquare, Wallet, User } from 'lucide-react';

export const MobileBottomNav: React.FC = () => {
  const { mobileActiveTab, setMobileActiveTab, chatMessages, proProfile } = useApp();

  // Count active chats
  const activeChatsCount = Object.keys(chatMessages).length;

  return (
    <nav className="w-full bg-white/95 dark:bg-slate-900/95 backdrop-blur-md border-t border-indigo-100 dark:border-slate-800 shrink-0 z-30 shadow-xs">
      <div className="grid grid-cols-5 items-center h-16 max-w-md mx-auto px-1">
        {/* Tab 1: Leads */}
        <button
          onClick={() => setMobileActiveTab('leads')}
          className="min-h-[44px] flex flex-col items-center justify-center transition-all cursor-pointer relative py-1"
        >
          <Briefcase
            className={`w-5 h-5 transition-transform duration-150 ${
              mobileActiveTab === 'leads'
                ? 'text-indigo-600 dark:text-indigo-400 scale-110'
                : 'text-slate-400 dark:text-slate-500'
            }`}
          />
          <span
            className={`text-[10px] tracking-tight mt-1 font-semibold ${
              mobileActiveTab === 'leads'
                ? 'text-indigo-600 dark:text-indigo-400'
                : 'text-slate-500 dark:text-slate-400'
            }`}
          >
            Lead-uri
          </span>
          {mobileActiveTab === 'leads' && (
            <span className="w-1.5 h-1.5 rounded-full bg-indigo-600 dark:bg-indigo-400 absolute bottom-0.5" />
          )}
        </button>

        {/* Tab 2: AI Architect */}
        <button
          onClick={() => setMobileActiveTab('ai')}
          className="min-h-[44px] flex flex-col items-center justify-center transition-all cursor-pointer relative py-1"
        >
          <Sparkles
            className={`w-5 h-5 transition-transform duration-150 ${
              mobileActiveTab === 'ai'
                ? 'text-indigo-600 dark:text-indigo-400 scale-110'
                : 'text-slate-400 dark:text-slate-500'
            }`}
          />
          <span
            className={`text-[10px] tracking-tight mt-1 font-semibold ${
              mobileActiveTab === 'ai'
                ? 'text-indigo-600 dark:text-indigo-400'
                : 'text-slate-500 dark:text-slate-400'
            }`}
          >
            AI Studio
          </span>
          {mobileActiveTab === 'ai' && (
            <span className="w-1.5 h-1.5 rounded-full bg-indigo-600 dark:bg-indigo-400 absolute bottom-0.5" />
          )}
        </button>

        {/* Tab 3: Messages */}
        <button
          onClick={() => setMobileActiveTab('messages')}
          className="min-h-[44px] flex flex-col items-center justify-center transition-all cursor-pointer relative py-1"
        >
          <div className="relative">
            <MessageSquare
              className={`w-5 h-5 transition-transform duration-150 ${
                mobileActiveTab === 'messages'
                  ? 'text-indigo-600 dark:text-indigo-400 scale-110'
                  : 'text-slate-400 dark:text-slate-500'
              }`}
            />
            {activeChatsCount > 0 && (
              <span className="absolute -top-1 -right-1 w-2.5 h-2.5 rounded-full bg-indigo-600 ring-2 ring-white dark:ring-slate-900" />
            )}
          </div>
          <span
            className={`text-[10px] tracking-tight mt-1 font-semibold ${
              mobileActiveTab === 'messages'
                ? 'text-indigo-600 dark:text-indigo-400'
                : 'text-slate-500 dark:text-slate-400'
            }`}
          >
            Chat
          </span>
          {mobileActiveTab === 'messages' && (
            <span className="w-1.5 h-1.5 rounded-full bg-indigo-600 dark:bg-indigo-400 absolute bottom-0.5" />
          )}
        </button>

        {/* Tab 4: Wallet */}
        <button
          onClick={() => setMobileActiveTab('wallet')}
          className="min-h-[44px] flex flex-col items-center justify-center transition-all cursor-pointer relative py-1"
        >
          <Wallet
            className={`w-5 h-5 transition-transform duration-150 ${
              mobileActiveTab === 'wallet'
                ? 'text-amber-500 dark:text-amber-400 scale-110'
                : 'text-slate-400 dark:text-slate-500'
            }`}
          />
          <span
            className={`text-[10px] tracking-tight mt-1 font-semibold ${
              mobileActiveTab === 'wallet'
                ? 'text-amber-600 dark:text-amber-400'
                : 'text-slate-500 dark:text-slate-400'
            }`}
          >
            {proProfile.role === 'client'
              ? 'Escrow'
              : proProfile.role === 'admin'
              ? 'Vault'
              : `${proProfile.creditsBalance} cr.`}
          </span>
          {mobileActiveTab === 'wallet' && (
            <span className="w-1.5 h-1.5 rounded-full bg-amber-500 absolute bottom-0.5" />
          )}
        </button>

        {/* Tab 5: User Profile & Rating Protocol */}
        <button
          onClick={() => setMobileActiveTab('profile')}
          className="min-h-[44px] flex flex-col items-center justify-center transition-all cursor-pointer relative py-1"
        >
          <User
            className={`w-5 h-5 transition-transform duration-150 ${
              mobileActiveTab === 'profile'
                ? 'text-indigo-600 dark:text-indigo-400 scale-110'
                : 'text-slate-400 dark:text-slate-500'
            }`}
          />
          <span
            className={`text-[10px] tracking-tight mt-1 font-semibold ${
              mobileActiveTab === 'profile'
                ? 'text-indigo-600 dark:text-indigo-400'
                : 'text-slate-500 dark:text-slate-400'
            }`}
          >
            Profil
          </span>
          {mobileActiveTab === 'profile' && (
            <span className="w-1.5 h-1.5 rounded-full bg-indigo-600 dark:bg-indigo-400 absolute bottom-0.5" />
          )}
        </button>
      </div>
    </nav>
  );
};
