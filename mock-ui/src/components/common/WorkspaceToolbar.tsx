import React from 'react';
import { useApp } from '../../context/AppContext';
import { Monitor, Smartphone, Moon, Sun, UserCheck, Shield, Briefcase } from 'lucide-react';

export const WorkspaceToolbar: React.FC = () => {
  const { appMode, setAppMode, userRole, setUserRole, darkMode, toggleDarkMode } = useApp();

  return (
    <div className="fixed top-0 left-0 right-0 z-50 h-11 bg-slate-900/95 dark:bg-slate-950/95 backdrop-blur-md border-b border-indigo-900/40 text-xs px-4 sm:px-6 flex items-center shadow-md">
      <div className="max-w-7xl mx-auto w-full flex items-center justify-between gap-3">
        {/* Device Switcher */}
        <div className="flex items-center gap-2">
          <span className="text-slate-400 font-semibold text-[10px] uppercase tracking-wider hidden sm:inline">
            Mod:
          </span>
          <div className="inline-flex rounded-lg bg-slate-800 p-0.5 border border-slate-700 shadow-2xs">
            <button
              onClick={() => setAppMode('desktop')}
              className={`px-2.5 py-1 rounded-md font-semibold flex items-center gap-1.5 transition-all cursor-pointer whitespace-nowrap text-xs ${
                appMode === 'desktop'
                  ? 'bg-indigo-600 text-white shadow-xs'
                  : 'text-slate-400 hover:text-white'
              }`}
            >
              <Monitor className="w-3.5 h-3.5" />
              <span>Desktop</span>
            </button>

            <button
              onClick={() => setAppMode('mobile')}
              className={`px-2.5 py-1 rounded-md font-semibold flex items-center gap-1.5 transition-all cursor-pointer whitespace-nowrap text-xs ${
                appMode === 'mobile'
                  ? 'bg-indigo-600 text-white shadow-xs'
                  : 'text-slate-400 hover:text-white'
              }`}
            >
              <Smartphone className="w-3.5 h-3.5" />
              <span>Mobile Touch</span>
            </button>
          </div>
        </div>

        {/* User Role Switcher */}
        <div className="flex items-center gap-2">
          <span className="text-slate-400 font-semibold text-[10px] uppercase tracking-wider hidden md:inline">
            Rol Activ:
          </span>
          <div className="inline-flex rounded-lg bg-slate-800 p-0.5 border border-slate-700 shadow-2xs">
            <button
              onClick={() => setUserRole('pro')}
              className={`px-2.5 py-1 rounded-md font-semibold flex items-center gap-1.5 transition-all cursor-pointer whitespace-nowrap text-xs ${
                userRole === 'pro'
                  ? 'bg-gradient-to-r from-indigo-500 to-violet-600 text-white shadow-xs'
                  : 'text-slate-400 hover:text-white'
              }`}
            >
              <Briefcase className="w-3.5 h-3.5" />
              <span>Profesionist</span>
            </button>

            <button
              onClick={() => setUserRole('client')}
              className={`px-2.5 py-1 rounded-md font-semibold flex items-center gap-1.5 transition-all cursor-pointer whitespace-nowrap text-xs ${
                userRole === 'client'
                  ? 'bg-gradient-to-r from-emerald-500 to-teal-600 text-white shadow-xs'
                  : 'text-slate-400 hover:text-white'
              }`}
            >
              <UserCheck className="w-3.5 h-3.5" />
              <span>Client</span>
            </button>

            <button
              onClick={() => setUserRole('admin')}
              className={`px-2.5 py-1 rounded-md font-semibold flex items-center gap-1.5 transition-all cursor-pointer whitespace-nowrap text-xs ${
                userRole === 'admin'
                  ? 'bg-gradient-to-r from-amber-500 to-rose-600 text-white shadow-xs'
                  : 'text-slate-400 hover:text-white'
              }`}
            >
              <Shield className="w-3.5 h-3.5" />
              <span>Administrator</span>
            </button>
          </div>
        </div>

        {/* Dark Mode Toggle */}
        <div className="flex items-center gap-2">
          <button
            onClick={toggleDarkMode}
            aria-label="Comută mod noapte/zi"
            className="flex items-center gap-1.5 px-2.5 py-1 rounded-lg border border-slate-700 bg-slate-800 text-slate-200 hover:bg-slate-700 hover:text-white transition-colors cursor-pointer shadow-2xs font-semibold text-xs"
          >
            {darkMode ? (
              <>
                <Sun className="w-3.5 h-3.5 text-amber-400" />
                <span className="hidden sm:inline">Luminos</span>
              </>
            ) : (
              <>
                <Moon className="w-3.5 h-3.5 text-indigo-400" />
                <span className="hidden sm:inline">Întunecat</span>
              </>
            )}
          </button>
        </div>
      </div>
    </div>
  );
};
