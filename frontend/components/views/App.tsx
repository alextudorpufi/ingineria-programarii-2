"use client";

import React, { useState, Suspense } from 'react';
import { useApp } from '@/context/AppContext';
import { useAuth } from '@/context/AuthContext';
import { Header } from '@/components/views/Header';
import { WalletModal } from '@/components/views/WalletModal';
import { AuthModal } from '@/components/auth/AuthModal';
import { PasswordInput } from '@/components/auth/PasswordInput';
import { Input } from '@/components/ui/Input';
import { LoadingSpinner } from '@/components/ui/Skeleton';
import { Button } from '@/components/ui/Button';
import { Sparkles, ShieldAlert, Mail, User, Briefcase } from 'lucide-react';
import { LeadFeed } from '@/components/views/LeadFeed';
import { LeadDetail } from '@/components/views/LeadDetail';
import { AIArchitect } from '@/components/views/AIArchitect';
import { ChatView } from '@/components/views/ChatView';
import { DisputesView } from '@/components/views/DisputesView';
import { AnalyticsView } from '@/components/views/AnalyticsView';
import { ProfileView } from '@/components/common/ProfileView';
import { LeadDetailSheet } from '@/components/common/LeadDetailSheet';

export const App: React.FC = () => {
  const {
    leads,
    activeLeadId,
    activeLead,
    activeTab,
    setActiveTab,
    setActiveLeadId,
    isAuthModalOpen,
    closeAuthModal,
  } = useApp();
  const { user, isLoading: isAuthLoading, login, register } = useAuth();
  const [isLeadSheetOpen, setIsLeadSheetOpen] = useState(false);
  const [mobileLeadView, setMobileLeadView] = useState<'feed' | 'detail'>('feed');
  const [authMode, setAuthMode] = useState<'login' | 'register'>('login');
  const [authEmail, setAuthEmail] = useState('');
  const [authPassword, setAuthPassword] = useState('');
  const [authName, setAuthName] = useState('');
  const [authRole, setAuthRole] = useState<'client' | 'pro'>('pro');
  const [isAuthSubmitting, setIsAuthSubmitting] = useState(false);
  const [authError, setAuthError] = useState<string | null>(null);

  const sheetLead = leads.find(lead => lead.id === activeLeadId) || activeLead;

  const handleOpenChat = (leadId: string) => {
    setActiveLeadId(leadId);
    setActiveTab('chat');
  };

  const handleOpenDispute = (leadId: string) => {
    setActiveLeadId(leadId);
    setActiveTab('disputes');
  };

  const handleOpenLeadSheet = (leadId: string) => {
    setActiveLeadId(leadId);
    setIsLeadSheetOpen(true);
  };

  const handleDirectAuthSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    setAuthError(null);
    setIsAuthSubmitting(true);
    try {
      const cleanEmail = authEmail.trim().toLowerCase();
      if (authMode === 'login') {
        await login(cleanEmail, authPassword);
      } else {
        await register({ email: cleanEmail, password: authPassword, name: authName.trim(), role: authRole });
      }
    } catch (error: unknown) {
      setAuthError(error instanceof Error ? error.message : 'Eroare la autentificare.');
    } finally {
      setIsAuthSubmitting(false);
    }
  };

  return (
    <div className="h-screen overflow-hidden flex flex-col bg-slate-50 dark:bg-slate-950 bg-vibrant-mesh text-slate-900 dark:text-slate-100 transition-colors">
      {/* Top Bar Contract (1 row, 3 zones) with Skillink Wordmark */}
      <Header currentTab={activeTab} setCurrentTab={setActiveTab} />

      {/* Main Content Area with Asynchronous Suspense boundary */}
      <main className="flex-1 max-w-7xl w-full mx-auto p-2 sm:p-4 lg:p-6 flex flex-col h-[calc(100dvh-4.25rem)] overflow-hidden">
        <div className="flex-1 bg-white/95 dark:bg-slate-900/90 rounded-2xl sm:rounded-3xl border border-indigo-100/80 dark:border-slate-800 shadow-md backdrop-blur-xs overflow-hidden flex flex-col min-h-0">
          {isAuthLoading ? (
            <div className="flex-1 flex flex-col items-center justify-center p-12 min-h-[500px]">
              <LoadingSpinner text="Se verifică sesiunea Skillink..." size="lg" />
            </div>
          ) : !user ? (
            <div className="flex-1 flex items-center justify-center p-4 sm:p-8 bg-gradient-to-b from-indigo-50/40 via-white to-white dark:from-slate-950 dark:via-slate-900 dark:to-slate-950 min-h-0 overflow-y-auto">
              <div className="w-full max-w-md bg-white dark:bg-slate-900 border border-slate-200/90 dark:border-slate-800 rounded-3xl shadow-xl p-6 sm:p-8 space-y-6">
                {/* Brand Header */}
                <div className="flex items-center gap-3">
                  <div className="w-10 h-10 rounded-2xl bg-gradient-to-tr from-indigo-600 via-indigo-500 to-violet-600 text-white flex items-center justify-center shadow-xs">
                    <Sparkles className="w-5 h-5" />
                  </div>
                  <div>
                    <h2 className="text-lg font-bold text-slate-900 dark:text-white">
                      {authMode === 'login' ? 'Conectare în Skillink' : 'Creare Cont Nou'}
                    </h2>
                    <p className="text-xs text-slate-500 dark:text-slate-400">
                      {authMode === 'login'
                        ? 'Introdu datele pentru accesul la cont'
                        : 'Alege-ți rolul și începe lucrul pe marketplace'}
                    </p>
                  </div>
                </div>

                {/* Mode Selector Toggle: Login vs Register */}
                <div className="grid grid-cols-2 p-1 bg-slate-100 dark:bg-slate-800/80 rounded-xl border border-slate-200 dark:border-slate-700">
                  <button
                    type="button"
                    onClick={() => { setAuthMode('login'); setAuthError(null); }}
                    className={`py-2 text-xs font-semibold rounded-lg transition-all cursor-pointer ${
                      authMode === 'login'
                        ? 'bg-white dark:bg-slate-900 text-indigo-600 dark:text-white shadow-xs'
                        : 'text-slate-500 hover:text-slate-900 dark:hover:text-slate-200'
                    }`}
                  >
                    Autentificare
                  </button>
                  <button
                    type="button"
                    onClick={() => { setAuthMode('register'); setAuthError(null); }}
                    className={`py-2 text-xs font-semibold rounded-lg transition-all cursor-pointer ${
                      authMode === 'register'
                        ? 'bg-white dark:bg-slate-900 text-indigo-600 dark:text-white shadow-xs'
                        : 'text-slate-500 hover:text-slate-900 dark:hover:text-slate-200'
                    }`}
                  >
                    Înregistrare
                  </button>
                </div>

                {/* Error alert banner */}
                {authError && (
                  <div
                    role="alert"
                    className="p-3 rounded-xl bg-rose-50 dark:bg-rose-950/50 border border-rose-200 dark:border-rose-900 flex items-center gap-2.5 text-xs text-rose-600 dark:text-rose-300"
                  >
                    <ShieldAlert className="w-4 h-4 shrink-0" />
                    <span>{authError}</span>
                  </div>
                )}

                {/* Direct Form */}
                <form onSubmit={handleDirectAuthSubmit} className="space-y-4">
                  {authMode === 'register' && (
                    <>
                      <Input
                        label="Nume Complet"
                        placeholder="ex. Radu Teodorescu"
                        autoComplete="name"
                        leftIcon={<User className="w-4 h-4" />}
                        value={authName}
                        onChange={e => setAuthName(e.target.value)}
                        required
                      />

                      <div className="space-y-1.5">
                        <span className="block text-xs font-semibold text-slate-700 dark:text-slate-200">Tip Cont</span>
                        <div className="grid grid-cols-2 gap-2">
                          <button
                            type="button"
                            onClick={() => setAuthRole('pro')}
                            className={`p-2.5 rounded-xl border text-xs font-semibold flex items-center justify-center gap-1.5 cursor-pointer transition-all ${
                              authRole === 'pro'
                                ? 'border-indigo-600 bg-indigo-50 dark:bg-indigo-950/60 text-indigo-700 dark:text-indigo-300'
                                : 'border-slate-200 dark:border-slate-700 text-slate-600 dark:text-slate-400'
                            }`}
                          >
                            <Briefcase className="w-3.5 h-3.5" />
                            Profesionist
                          </button>
                          <button
                            type="button"
                            onClick={() => setAuthRole('client')}
                            className={`p-2.5 rounded-xl border text-xs font-semibold flex items-center justify-center gap-1.5 cursor-pointer transition-all ${
                              authRole === 'client'
                                ? 'border-emerald-600 bg-emerald-50 dark:bg-emerald-950/60 text-emerald-700 dark:text-emerald-300'
                                : 'border-slate-200 dark:border-slate-700 text-slate-600 dark:text-slate-400'
                            }`}
                          >
                            <User className="w-3.5 h-3.5" />
                            Client
                          </button>
                        </div>
                      </div>
                    </>
                  )}

                  <Input
                    label="Adresă Email"
                    type="email"
                    inputMode="email"
                    autoCapitalize="none"
                    autoCorrect="off"
                    spellCheck={false}
                    placeholder="nume@exemplu.ro"
                    autoComplete="email"
                    leftIcon={<Mail className="w-4 h-4" />}
                    value={authEmail}
                    onChange={e => setAuthEmail(e.target.value)}
                    required
                  />

                  <PasswordInput
                    label="Parolă"
                    placeholder="••••••••"
                    value={authPassword}
                    onChange={e => setAuthPassword(e.target.value)}
                    showStrengthMeter={authMode === 'register'}
                    autoComplete={authMode === 'register' ? 'new-password' : 'current-password'}
                    required
                  />

                  <Button
                    type="submit"
                    variant="primary"
                    size="lg"
                    className="w-full justify-center mt-2"
                    isLoading={isAuthSubmitting}
                  >
                    {authMode === 'login' ? 'Autentificare' : 'Creează Contul'}
                  </Button>
                </form>
              </div>
            </div>
          ) : (
          <Suspense
            fallback={
              <div className="flex-1 flex flex-col items-center justify-center p-12 min-h-[500px]">
                <LoadingSpinner text="Se încarcă modulul Skillink..." size="lg" />
              </div>
            }
          >
            {activeTab === 'leads' && (
              <div className="flex-1 grid grid-cols-1 md:grid-cols-12 overflow-hidden h-full min-h-0">
                {/* Zone 1: Left Leads Feed Pane */}
                <div
                  className={`md:col-span-5 h-full min-h-0 flex flex-col overflow-hidden ${
                    mobileLeadView === 'detail' ? 'hidden md:flex' : 'flex'
                  }`}
                >
                  <LeadFeed
                    onOpenLeadSheet={handleOpenLeadSheet}
                    onSelectLeadOnMobile={() => setMobileLeadView('detail')}
                  />
                </div>

                {/* Zone 2: Right Lead Detail Pane */}
                <div
                  className={`md:col-span-7 h-full min-h-0 flex flex-col overflow-hidden border-t md:border-t-0 md:border-l border-slate-200/90 dark:border-slate-800 bg-slate-50/50 dark:bg-slate-900/50 ${
                    mobileLeadView === 'feed' ? 'hidden md:flex' : 'flex'
                  }`}
                >
                  <LeadDetail
                    onBackToFeed={() => setMobileLeadView('feed')}
                    onOpenChat={handleOpenChat}
                    onOpenDispute={handleOpenDispute}
                  />
                </div>
              </div>
            )}

            {activeTab === 'ai' && (
              <div className="flex-1 min-h-0 h-full overflow-hidden">
                <AIArchitect
                  onPublished={() => {
                    setActiveTab('leads');
                  }}
                />
              </div>
            )}

            {activeTab === 'chat' && (
              <div className="flex-1 min-h-0 h-full overflow-hidden">
                <ChatView onOpenDispute={handleOpenDispute} />
              </div>
            )}

            {activeTab === 'disputes' && (
              <div className="flex-1 min-h-0 h-full overflow-hidden">
                <DisputesView />
              </div>
            )}

            {activeTab === 'analytics' && (
              <div className="flex-1 min-h-0 h-full overflow-hidden">
                <AnalyticsView />
              </div>
            )}

            {activeTab === 'profile' && (
              <div className="flex-1 min-h-0 h-full overflow-hidden">
                <ProfileView />
              </div>
            )}
          </Suspense>
          )}
        </div>
      </main>

      {/* Virtual Credit Wallet Modal */}
      <WalletModal />

      {/* Authentication dialog (login / register) */}
      <AuthModal isOpen={isAuthModalOpen} onClose={closeAuthModal} />

      {/* Lead detail sheet (bottom sheet on phones, dialog on larger viewports) */}
      {isLeadSheetOpen && (
        <LeadDetailSheet
          lead={sheetLead}
          onClose={() => setIsLeadSheetOpen(false)}
          onOpenChat={handleOpenChat}
        />
      )}
    </div>
  );
};
