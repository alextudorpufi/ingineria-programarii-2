/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import React from 'react';
import { AppProvider, useApp } from './context/AppContext';
import { WorkspaceToolbar } from './components/common/WorkspaceToolbar';
import { DesktopApp } from './components/desktop/DesktopApp';
import { MobileApp } from './components/mobile/MobileApp';

const AppContent: React.FC = () => {
  const { appMode, darkMode } = useApp();

  return (
    <div className={`w-full min-h-screen ${darkMode ? 'dark' : ''}`}>
      {/* Fixed upper Debug / Switcher Toolbar - fixed at top so only the app moves */}
      <WorkspaceToolbar />

      {/* Main App Container with top offset matching fixed bar */}
      <div className="pt-11">
        {appMode === 'desktop' ? <DesktopApp /> : <MobileApp />}
      </div>
    </div>
  );
};

export default function App() {
  return (
    <AppProvider>
      <AppContent />
    </AppProvider>
  );
}
