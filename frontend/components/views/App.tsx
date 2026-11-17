"use client";

import React, { useState, Suspense } from 'react';
import { useApp } from '@/context/AppContext';
import { Header } from '@/components/views/Header';
import { WalletModal } from '@/components/views/WalletModal';
import { AuthModal } from '@/components/auth/AuthModal';
import { LoadingSpinner } from '@/components/ui/Skeleton';
import { LeadFeed } from '@/components/views/LeadFeed';
import { LeadDetail } from '@/components/views/LeadDetail';
import { AIArchitect } from '@/components/views/AIArchitect';
import { ChatView } from '@/components/views/ChatView';
import { DisputesView } from '@/components/views/DisputesView';
import { AnalyticsView } from '@/components/views/AnalyticsView';
import { ProfileView } from '@/components/common/ProfileView';
import { LeadDetailSheet } from '@/components/common/LeadDetailSheet';
import { useAuth } from '@/context/AuthContext';
import { canAccessTab, canUseHeaderAction, resolveWorkspaceTab } from '@/config/navigation';

export const App: React.FC = () => {
  const {
    leads,
    activeLeadId,
    activeLead,
    activeTab: requestedTab,
    setActiveTab,
    setActiveLeadId,
    isAuthModalOpen,
    closeAuthModal,
  } = useApp();
  const [isLeadSheetOpen, setIsLeadSheetOpen] = useState(false);
  const [mobileLeadView, setMobileLeadView] = useState<'feed' | 'detail'>('feed');
  const { user, isLoading } = useAuth();
  const role = isLoading ? null : user?.role;
  const activeTab = resolveWorkspaceTab(role, requestedTab);

  const sheetLead = leads.find(lead => lead.id === activeLeadId) || activeLead;

  const handleOpenChat = (leadId: string) => {
    if (!canAccessTab(role, 'chat')) return;
    setActiveLeadId(leadId);
    setActiveTab('chat');
  };

  const handleOpenDispute = (leadId: string) => {
    if (!canAccessTab(role, 'disputes')) return;
    setActiveLeadId(leadId);
    setActiveTab('disputes');
  };

  const handleOpenLeadSheet = (leadId: string) => {
    setActiveLeadId(leadId);
    setIsLeadSheetOpen(true);
  };

  if (!activeTab) return null;

  return (
    <div className="h-screen overflow-hidden flex flex-col bg-slate-50 dark:bg-slate-950 bg-vibrant-mesh text-slate-900 dark:text-slate-100 transition-colors">
      {/* Top Bar Contract (1 row, 3 zones) with Skillink Wordmark */}
      <Header currentTab={activeTab} setCurrentTab={setActiveTab} />

      {/* Main Content Area with Asynchronous Suspense boundary */}
      <main className="flex-1 max-w-7xl w-full mx-auto p-2 sm:p-4 lg:p-6 flex flex-col h-[calc(100dvh-4.25rem)] overflow-hidden">
        <div className="flex-1 bg-white/95 dark:bg-slate-900/90 rounded-2xl sm:rounded-3xl border border-indigo-100/80 dark:border-slate-800 shadow-md backdrop-blur-xs overflow-hidden flex flex-col min-h-0">
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
                    onOpenDispute={canAccessTab(role, 'disputes') ? handleOpenDispute : undefined}
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
                <ChatView onOpenDispute={canAccessTab(role, 'disputes') ? handleOpenDispute : undefined} />
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
        </div>
      </main>

      {/* Virtual Credit Wallet Modal */}
      {canUseHeaderAction(role, 'wallet') && <WalletModal />}

      {/* Authentication dialog (login / register) */}
      <AuthModal isOpen={isAuthModalOpen} onClose={closeAuthModal} />

      {/* Lead detail sheet (bottom sheet on phones, dialog on larger viewports) */}
      {isLeadSheetOpen && (
        <LeadDetailSheet
          lead={sheetLead}
          onClose={() => setIsLeadSheetOpen(false)}
          onOpenChat={canAccessTab(role, 'chat') ? handleOpenChat : undefined}
        />
      )}
    </div>
  );
};
