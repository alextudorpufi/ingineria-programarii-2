import React, { Suspense, lazy } from 'react';
import { useApp } from '../../context/AppContext';
import { DesktopHeader } from './DesktopHeader';
import { DesktopWalletModal } from './DesktopWalletModal';
import { LoadingSpinner } from '../ui/Skeleton';

// Asynchronous Component Loading (Code splitting & non-blocking execution)
const DesktopLeadFeed = lazy(() =>
  import('./DesktopLeadFeed').then(module => ({ default: module.DesktopLeadFeed }))
);
const DesktopLeadDetail = lazy(() =>
  import('./DesktopLeadDetail').then(module => ({ default: module.DesktopLeadDetail }))
);
const DesktopAIArchitect = lazy(() =>
  import('./DesktopAIArchitect').then(module => ({ default: module.DesktopAIArchitect }))
);
const DesktopChatView = lazy(() =>
  import('./DesktopChatView').then(module => ({ default: module.DesktopChatView }))
);
const DesktopDisputesView = lazy(() =>
  import('./DesktopDisputesView').then(module => ({ default: module.DesktopDisputesView }))
);
const DesktopAnalyticsView = lazy(() =>
  import('./DesktopAnalyticsView').then(module => ({ default: module.DesktopAnalyticsView }))
);
const ProfileView = lazy(() =>
  import('../common/ProfileView').then(module => ({ default: module.ProfileView }))
);

export const DesktopApp: React.FC = () => {
  const { desktopActiveTab, setDesktopActiveTab, setActiveLeadId } = useApp();

  const handleOpenChat = (leadId: string) => {
    setActiveLeadId(leadId);
    setDesktopActiveTab('chat');
  };

  const handleOpenDispute = (leadId: string) => {
    setActiveLeadId(leadId);
    setDesktopActiveTab('disputes');
  };

  return (
    <div className="min-h-screen flex flex-col bg-slate-50 dark:bg-slate-950 bg-vibrant-mesh text-slate-900 dark:text-slate-100 transition-colors">
      {/* Top Bar Contract (1 row, 3 zones) with Skillink Wordmark */}
      <DesktopHeader currentTab={desktopActiveTab} setCurrentTab={setDesktopActiveTab} />

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
            {desktopActiveTab === 'leads' && (
              <div className="flex-1 grid grid-cols-1 lg:grid-cols-12 overflow-hidden h-full">
                {/* Left Feed */}
                <div className="lg:col-span-5 h-[700px] lg:h-auto overflow-hidden">
                  <DesktopLeadFeed />
                </div>

                {/* Right Detail Pane */}
                <div className="lg:col-span-7 h-[700px] lg:h-auto overflow-hidden bg-slate-50/50 dark:bg-slate-900/50">
                  <DesktopLeadDetail
                    onOpenChat={handleOpenChat}
                    onOpenDispute={handleOpenDispute}
                  />
                </div>
              </div>
            )}

            {desktopActiveTab === 'ai' && (
              <div className="flex-1 h-[720px] overflow-hidden">
                <DesktopAIArchitect
                  onPublished={() => {
                    setDesktopActiveTab('leads');
                  }}
                />
              </div>
            )}

            {desktopActiveTab === 'chat' && (
              <div className="flex-1 h-[720px] overflow-hidden">
                <DesktopChatView onOpenDispute={handleOpenDispute} />
              </div>
            )}

            {desktopActiveTab === 'disputes' && (
              <div className="flex-1 h-[720px] overflow-hidden">
                <DesktopDisputesView />
              </div>
            )}

            {desktopActiveTab === 'analytics' && (
              <div className="flex-1 h-[720px] overflow-hidden">
                <DesktopAnalyticsView />
              </div>
            )}

            {desktopActiveTab === 'profile' && (
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
      <DesktopWalletModal />
    </div>
  );
};
