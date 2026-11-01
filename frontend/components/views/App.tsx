"use client";

import React, { useState, Suspense } from 'react';
import { useApp } from '@/context/AppContext';
import { Header } from '@/components/views/Header';
import { WalletModal } from '@/components/views/WalletModal';
import { LoadingSpinner } from '@/components/ui/Skeleton';
import { LeadFeed } from '@/components/views/LeadFeed';
import { LeadDetail } from '@/components/views/LeadDetail';
import { AIArchitect } from '@/components/views/AIArchitect';
import { ChatView } from '@/components/views/ChatView';
import { DisputesView } from '@/components/views/DisputesView';
import { AnalyticsView } from '@/components/views/AnalyticsView';
import { ProfileView } from '@/components/common/ProfileView';
import { LeadDetailSheet } from '@/components/common/LeadDetailSheet';

export const App: React.FC = () => {
  const { leads, activeLeadId, activeLead, activeTab, setActiveTab, setActiveLeadId } = useApp();
  const [isLeadSheetOpen, setIsLeadSheetOpen] = useState(false);

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

  return (
    <div className="min-h-screen flex flex-col bg-slate-50 dark:bg-slate-950 bg-vibrant-mesh text-slate-900 dark:text-slate-100 transition-colors">
      {/* Top Bar Contract (1 row, 3 zones) with Skillink Wordmark */}
      <Header currentTab={activeTab} setCurrentTab={setActiveTab} />

      {/* Main Content Area with Asynchronous Suspense boundary */}
      <main className="flex-1 max-w-7xl w-full mx-auto p-4 sm:p-6 flex flex-col overflow-hidden">
        <div className="flex-1 bg-white/95 dark:bg-slate-900/90 rounded-3xl border border-indigo-100/80 dark:border-slate-800 shadow-md backdrop-blur-xs overflow-hidden flex flex-col min-h-[660px]">
          <Suspense
            fallback={
              <div className="flex-1 flex flex-col items-center justify-center p-12 min-h-[500px]">
                <LoadingSpinner text="Se încarcă modulul Skillink..." size="lg" />
              </div>
            }
          >
            {activeTab === 'leads' && (
              <div className="flex-1 grid grid-cols-1 md:grid-cols-12 overflow-hidden h-full">
                {/* Left Feed */}
                <div className="md:col-span-5 h-[620px] md:h-auto overflow-hidden">
                  <LeadFeed onOpenLeadSheet={handleOpenLeadSheet} />
                </div>

                {/* Right Detail Pane */}
                <div className="md:col-span-7 h-[620px] md:h-auto overflow-hidden border-t md:border-t-0 md:border-l border-slate-200/90 dark:border-slate-800 bg-slate-50/50 dark:bg-slate-900/50">
                  <LeadDetail
                    onOpenChat={handleOpenChat}
                    onOpenDispute={handleOpenDispute}
                  />
                </div>
              </div>
            )}

            {activeTab === 'ai' && (
              <div className="flex-1 h-[720px] overflow-hidden">
                <AIArchitect
                  onPublished={() => {
                    setActiveTab('leads');
                  }}
                />
              </div>
            )}

            {activeTab === 'chat' && (
              <div className="flex-1 h-[720px] overflow-hidden">
                <ChatView onOpenDispute={handleOpenDispute} />
              </div>
            )}

            {activeTab === 'disputes' && (
              <div className="flex-1 h-[720px] overflow-hidden">
                <DisputesView />
              </div>
            )}

            {activeTab === 'analytics' && (
              <div className="flex-1 h-[720px] overflow-hidden">
                <AnalyticsView />
              </div>
            )}

            {activeTab === 'profile' && (
              <div className="flex-1 h-[720px] overflow-hidden">
                <ProfileView />
              </div>
            )}
          </Suspense>
        </div>
      </main>

      {/* Footer */}
      <footer className="w-full border-t border-slate-200/80 dark:border-slate-800 py-4 px-6 text-center text-xs text-slate-500 bg-white/80 dark:bg-slate-950/80 backdrop-blur-xs mt-auto">
        <div className="max-w-7xl mx-auto flex flex-wrap items-center justify-between gap-4">
          <p>© 2026 Skillink · Marketplace Inteligent de Servicii (Proiect Ingineria Programării 2)</p>
          <div className="flex items-center gap-4 text-[11px] text-slate-500">
            <span className="font-medium text-indigo-600 dark:text-indigo-400">Model Economic: Necesit.ro</span>
            <span>·</span>
            <span>Protocol Rating Verificat</span>
            <span>·</span>
            <span>GDPR Compliant</span>
          </div>
        </div>
      </footer>

      {/* Virtual Credit Wallet Modal */}
      <WalletModal />

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
