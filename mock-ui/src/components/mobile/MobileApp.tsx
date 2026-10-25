import React, { useState, Suspense, lazy } from 'react';
import { useApp } from '../../context/AppContext';
import { MobileLeadCard } from './MobileLeadCard';
import { MobileLeadSheet } from './MobileLeadSheet';
import { MobileBottomNav } from './MobileBottomNav';
import { SERVICE_CATEGORIES } from '../../data/mockData';
import { Lead } from '../../types';
import { Search, Sparkles, Wifi, Battery, Signal, Smartphone, Maximize2, Minimize2, Moon, Sun } from 'lucide-react';
import { Input } from '../ui/Input';
import { Badge } from '../ui/Badge';
import { SkeletonFeed, LoadingSpinner } from '../ui/Skeleton';

// Asynchronously load secondary views
const MobileAIChat = lazy(() =>
  import('./MobileAIChat').then(m => ({ default: m.MobileAIChat }))
);
const MobileChatView = lazy(() =>
  import('./MobileChatView').then(m => ({ default: m.MobileChatView }))
);
const MobileWalletView = lazy(() =>
  import('./MobileWalletView').then(m => ({ default: m.MobileWalletView }))
);
const ProfileView = lazy(() =>
  import('../common/ProfileView').then(m => ({ default: m.ProfileView }))
);

export const MobileApp: React.FC = () => {
  const {
    leads,
    selectedCategory,
    setSelectedCategory,
    searchQuery,
    setSearchQuery,
    mobileActiveTab,
    setMobileActiveTab,
    proProfile,
    setActiveLeadId,
    darkMode,
    toggleDarkMode,
    isLoadingLeads
  } = useApp();

  const [selectedLeadForSheet, setSelectedLeadForSheet] = useState<Lead | null>(null);
  const [deviceFrameMode, setDeviceFrameMode] = useState<boolean>(true);

  // Filtered leads
  const filteredLeads = leads.filter(lead => {
    if (selectedCategory !== 'all') {
      const catObj = SERVICE_CATEGORIES.find(c => c.id === selectedCategory);
      if (catObj && lead.category !== catObj.name) return false;
    }
    if (searchQuery.trim()) {
      const q = searchQuery.toLowerCase();
      return (
        lead.title.toLowerCase().includes(q) ||
        lead.description.toLowerCase().includes(q) ||
        lead.location.toLowerCase().includes(q)
      );
    }
    return true;
  });

  const handleOpenSheet = (lead: Lead) => {
    setSelectedLeadForSheet(lead);
    setActiveLeadId(lead.id);
  };

  const handleOpenChatFromSheet = (leadId: string) => {
    setActiveLeadId(leadId);
    setMobileActiveTab('messages');
  };

  const renderActiveScreen = () => {
    switch (mobileActiveTab) {
      case 'leads':
        return (
          <div className="flex-1 overflow-y-auto p-4 space-y-3 pb-20">
            {/* Search bar using standardized Input primitive */}
            <Input
              placeholder="Caută oportunități..."
              value={searchQuery}
              onChange={e => setSearchQuery(e.target.value)}
              leftIcon={<Search className="w-4 h-4 text-indigo-500" />}
            />

            {/* Horizontal Category Pill Carousel with Vibrant Gradients */}
            <div className="flex items-center gap-1.5 overflow-x-auto pb-1 text-xs no-scrollbar">
              <button
                onClick={() => setSelectedCategory('all')}
                className={`px-3.5 py-1.5 rounded-full font-bold whitespace-nowrap text-xs transition-all cursor-pointer ${
                  selectedCategory === 'all'
                    ? 'bg-gradient-to-r from-indigo-600 to-violet-600 text-white shadow-xs'
                    : 'bg-slate-100 hover:bg-indigo-50 text-slate-600 dark:bg-slate-800 dark:text-slate-400'
                }`}
              >
                Toate
              </button>
              {SERVICE_CATEGORIES.map(cat => (
                <button
                  key={cat.id}
                  onClick={() => setSelectedCategory(cat.id)}
                  className={`px-3.5 py-1.5 rounded-full font-bold whitespace-nowrap text-xs transition-all cursor-pointer ${
                    selectedCategory === cat.id
                      ? 'bg-gradient-to-r from-indigo-600 to-violet-600 text-white shadow-xs'
                      : 'bg-slate-100 hover:bg-indigo-50 text-slate-600 dark:bg-slate-800 dark:text-slate-400'
                  }`}
                >
                  {cat.name.split(' ')[0]}
                </button>
              ))}
            </div>

            {/* AI Callout Banner with Radiant Gradient */}
            <div
              onClick={() => setMobileActiveTab('ai')}
              className="p-4 rounded-2xl bg-gradient-to-r from-indigo-600 via-indigo-500 to-violet-600 text-white shadow-md flex items-center justify-between cursor-pointer active:scale-[0.99] transition-transform relative overflow-hidden"
            >
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-xl bg-white/20 backdrop-blur-xs flex items-center justify-center shrink-0">
                  <Sparkles className="w-5 h-5 text-white" />
                </div>
                <div>
                  <h4 className="text-xs font-extrabold leading-tight">AI Project Architect</h4>
                  <p className="text-[11px] text-indigo-100 font-medium">Structurează solicitarea prin conversație</p>
                </div>
              </div>
              <span className="text-[11px] px-2.5 py-1 rounded-xl bg-white/20 font-bold backdrop-blur-xs">
                Start
              </span>
            </div>

            {/* Leads list */}
            <div className="space-y-3 pt-1">
              <div className="flex items-center justify-between text-xs text-slate-500 px-1">
                <span className="font-bold text-slate-800 dark:text-slate-200">
                  Oportunități Recomandate
                </span>
                <span className="font-mono text-[11px] font-bold text-indigo-600 dark:text-indigo-400">
                  {filteredLeads.length} lead-uri
                </span>
              </div>

              {isLoadingLeads ? (
                <SkeletonFeed count={3} />
              ) : (
                filteredLeads.map(lead => (
                  <MobileLeadCard
                    key={lead.id}
                    lead={lead}
                    onOpenDetails={handleOpenSheet}
                  />
                ))
              )}
            </div>
          </div>
        );

      case 'ai':
        return (
          <Suspense fallback={<div className="p-8"><LoadingSpinner text="Se încarcă AI Architect..." /></div>}>
            <MobileAIChat />
          </Suspense>
        );

      case 'messages':
        return (
          <Suspense fallback={<div className="p-8"><LoadingSpinner text="Se încarcă conversația..." /></div>}>
            <MobileChatView />
          </Suspense>
        );

      case 'wallet':
        return (
          <Suspense fallback={<div className="p-8"><LoadingSpinner text="Se încarcă portofelul..." /></div>}>
            <MobileWalletView />
          </Suspense>
        );

      case 'profile':
        return (
          <Suspense fallback={<div className="p-8"><LoadingSpinner text="Se încarcă profilul & ratingul..." /></div>}>
            <div className="flex-1 overflow-y-auto">
              <ProfileView />
            </div>
          </Suspense>
        );

      default:
        return null;
    }
  };

  return (
    <div className="min-h-screen flex flex-col bg-slate-50 dark:bg-slate-950 bg-vibrant-mesh text-slate-900 dark:text-slate-100 transition-colors">
      {/* Simulator Device Frame or Fullscreen mode selector */}
      <div className="max-w-7xl mx-auto w-full px-4 pt-3 flex items-center justify-between text-xs text-slate-500">
        <div className="flex items-center gap-2">
          <div className="w-6 h-6 rounded-lg bg-indigo-500/10 text-indigo-600 flex items-center justify-center">
            <Smartphone className="w-3.5 h-3.5" />
          </div>
          <span className="font-bold text-slate-800 dark:text-slate-200">
            Skillink Mobile Touch App (iOS / Android)
          </span>
        </div>

        <button
          onClick={() => setDeviceFrameMode(!deviceFrameMode)}
          className="flex items-center gap-1.5 px-3 py-1.5 rounded-xl border border-indigo-200/90 dark:border-slate-700 bg-white dark:bg-slate-900 text-slate-700 dark:text-slate-300 hover:bg-indigo-50 dark:hover:bg-slate-800 transition-colors cursor-pointer shadow-2xs font-medium"
        >
          {deviceFrameMode ? (
            <>
              <Maximize2 className="w-3.5 h-3.5 text-indigo-500" />
              <span>Mod Ecran Complet</span>
            </>
          ) : (
            <>
              <Minimize2 className="w-3.5 h-3.5 text-indigo-500" />
              <span>Mod Cadru Smartphone</span>
            </>
          )}
        </button>
      </div>

      {/* Main Container */}
      <div className="flex-1 flex items-center justify-center p-2 sm:p-6">
        {deviceFrameMode ? (
          /* Smartphone Bezel Container */
          <div className="w-full max-w-[420px] h-[820px] bg-slate-900 rounded-[50px] p-3 shadow-2xl ring-1 ring-slate-800 flex flex-col relative overflow-hidden border-4 border-slate-700">
            {/* Phone Screen Glass */}
            <div className="w-full h-full bg-slate-50 dark:bg-slate-950 rounded-[40px] flex flex-col overflow-hidden relative border border-slate-800 shadow-inner">
              {/* Phone Status Bar & Dynamic Island */}
              <div className="h-10 w-full bg-white dark:bg-slate-900 flex items-center justify-between px-6 pt-1 text-slate-900 dark:text-white shrink-0 z-40">
                <span className="text-xs font-extrabold font-mono">09:41</span>
                {/* Dynamic Island Pill */}
                <div className="w-24 h-5 bg-black rounded-full mx-auto" />
                <div className="flex items-center gap-1.5">
                  <Signal className="w-3 h-3" />
                  <Wifi className="w-3 h-3" />
                  <Battery className="w-3.5 h-3.5" />
                </div>
              </div>

              {/* Mobile Top App Header */}
              <div className="px-4 py-2.5 bg-white/95 dark:bg-slate-900/95 backdrop-blur-xs border-b border-indigo-100 dark:border-slate-800 flex items-center justify-between shrink-0">
                <div className="flex items-center gap-2">
                  <div className="w-6 h-6 rounded-lg bg-gradient-to-tr from-indigo-600 to-violet-600 flex items-center justify-center text-white text-[10px] font-bold">
                    <Sparkles className="w-3.5 h-3.5" />
                  </div>
                  <span className="text-sm font-extrabold tracking-tight text-slate-900 dark:text-white">
                    Skill<span className="bg-gradient-to-r from-indigo-600 to-violet-600 bg-clip-text text-transparent">ink</span>
                  </span>
                  <Badge variant="indigo">Mobile</Badge>
                </div>

                <div className="flex items-center gap-2">
                  <button
                    onClick={toggleDarkMode}
                    aria-label="Comută temă"
                    className="p-1.5 rounded-xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 text-slate-700 dark:text-slate-300 hover:bg-indigo-50 dark:hover:bg-slate-800 cursor-pointer shadow-2xs"
                  >
                    {darkMode ? <Sun className="w-3.5 h-3.5 text-amber-500" /> : <Moon className="w-3.5 h-3.5 text-indigo-600" />}
                  </button>
                  <button
                    onClick={() => setMobileActiveTab('wallet')}
                    className="px-2.5 py-1 rounded-xl bg-amber-50 dark:bg-amber-950/60 border border-amber-200 dark:border-amber-900 text-[11px] font-mono font-bold text-amber-700 dark:text-amber-300 flex items-center gap-1 cursor-pointer active:scale-95"
                  >
                    <span>{proProfile.creditsBalance} cr.</span>
                  </button>
                </div>
              </div>

              {/* Active Screen Content */}
              <div className="flex-1 overflow-hidden flex flex-col">
                {renderActiveScreen()}
              </div>

              {/* Fixed Bottom Navigation */}
              <MobileBottomNav />

              {/* iOS Home Indicator Bar */}
              <div className="h-4 w-full bg-white dark:bg-slate-900 flex items-center justify-center pb-1 shrink-0">
                <div className="w-28 h-1 bg-slate-300 dark:bg-slate-700 rounded-full" />
              </div>
            </div>
          </div>
        ) : (
          /* Fullscreen Mobile View (e.g. for native devices) */
          <div className="w-full max-w-md h-[800px] bg-white dark:bg-slate-950 rounded-3xl border border-indigo-100 dark:border-slate-800 shadow-xl flex flex-col overflow-hidden relative">
            {/* Top Bar */}
            <div className="px-4 py-3 bg-white/95 dark:bg-slate-900/95 backdrop-blur-xs border-b border-indigo-100 dark:border-slate-800 flex items-center justify-between shrink-0">
              <span className="text-base font-extrabold tracking-tight text-slate-900 dark:text-white">
                Skill<span className="bg-gradient-to-r from-indigo-600 to-violet-600 bg-clip-text text-transparent">ink</span>
              </span>
              <div className="flex items-center gap-2">
                <button
                  onClick={toggleDarkMode}
                  aria-label="Comută temă"
                  className="p-1.5 rounded-xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 text-slate-700 dark:text-slate-300 hover:bg-indigo-50 dark:hover:bg-slate-800 cursor-pointer shadow-2xs"
                >
                  {darkMode ? <Sun className="w-3.5 h-3.5 text-amber-500" /> : <Moon className="w-3.5 h-3.5 text-indigo-600" />}
                </button>
                <button
                  onClick={() => setMobileActiveTab('wallet')}
                  className="px-3 py-1 rounded-xl bg-amber-50 dark:bg-slate-800 border border-amber-200 dark:border-amber-900 text-xs font-mono font-bold text-amber-700 dark:text-amber-400 cursor-pointer"
                >
                  {proProfile.creditsBalance} credite
                </button>
              </div>
            </div>

            {/* Content */}
            <div className="flex-1 overflow-hidden flex flex-col">
              {renderActiveScreen()}
            </div>

            {/* Bottom Nav */}
            <MobileBottomNav />
          </div>
        )}
      </div>

      {/* Mobile Lead Passport & Unlock Sheet */}
      <MobileLeadSheet
        lead={selectedLeadForSheet}
        onClose={() => setSelectedLeadForSheet(null)}
        onOpenChat={handleOpenChatFromSheet}
      />
    </div>
  );
};
