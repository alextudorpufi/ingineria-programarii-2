import React from 'react';
import { useApp } from '../../context/AppContext';
import { Coins, CheckCircle2, History, ArrowDownLeft, ArrowUpRight, X, Sparkles } from 'lucide-react';
import { Button } from '../ui/Button';
import { Badge } from '../ui/Badge';

export const DesktopWalletModal: React.FC = () => {
  const { showWalletModal, setShowWalletModal, proProfile, transactions, rechargeCredits } = useApp();

  if (!showWalletModal) return null;

  const packages = [
    { name: 'Pachet Starter', credits: 50, bonus: 0, price: 50, tag: 'Testare Rapidă' },
    { name: 'Pachet Pro Recomandat', credits: 150, bonus: 20, price: 150, tag: 'Cel Mai Popular', popular: true },
    { name: 'Pachet Studio / Agenție', credits: 400, bonus: 80, price: 350, tag: 'Economisești 20%' }
  ];

  const handleSelectPackage = (pkg: typeof packages[0]) => {
    rechargeCredits(pkg.credits + pkg.bonus, pkg.name);
  };

  return (
    <div className="fixed inset-0 z-50 bg-black/60 backdrop-blur-xs flex items-center justify-center p-4">
      <div className="w-full max-w-2xl bg-white dark:bg-slate-900 rounded-3xl border border-indigo-100 dark:border-slate-800 shadow-2xl overflow-hidden flex flex-col max-h-[90vh]">
        {/* Header */}
        <div className="p-6 border-b border-indigo-100 dark:border-slate-800 flex items-center justify-between bg-gradient-to-r from-amber-50/50 via-white to-indigo-50/30 dark:from-slate-900 dark:to-slate-900">
          <div className="flex items-center gap-3">
            <div className="w-12 h-12 rounded-2xl bg-gradient-to-tr from-amber-500 to-orange-500 text-white flex items-center justify-center shadow-xs">
              <Coins className="w-6 h-6" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <h3 className="text-base font-bold text-slate-900 dark:text-white">
                  Portofel Credite Profesionist
                </h3>
                <Badge variant="amber">1 Credit = 1 RON</Badge>
              </div>
              <p className="text-xs text-slate-500 dark:text-slate-400">
                Sistem de debitare atomică per lead conform Section 1.3 D din proiect
              </p>
            </div>
          </div>

          <div className="flex items-center gap-4">
            <div className="text-right bg-white dark:bg-slate-800 px-3.5 py-1.5 rounded-xl border border-amber-200 dark:border-amber-900 shadow-2xs">
              <span className="text-[10px] text-slate-400 uppercase font-semibold block">Sold disponibil:</span>
              <span className="text-lg font-mono font-extrabold text-amber-600 dark:text-amber-400 tabular-nums">
                {proProfile.creditsBalance} credite
              </span>
            </div>

            <button
              onClick={() => setShowWalletModal(false)}
              className="p-2 rounded-xl text-slate-400 hover:text-slate-600 dark:hover:text-white hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors cursor-pointer"
            >
              <X className="w-5 h-5" />
            </button>
          </div>
        </div>

        {/* Body Content */}
        <div className="p-6 overflow-y-auto space-y-6">
          {/* Top-Up Packages */}
          <div>
            <h4 className="text-xs font-bold text-slate-800 dark:text-slate-200 uppercase tracking-wider mb-3">
              Reîncărcare Credite Virtuale
            </h4>
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
              {packages.map((pkg, i) => (
                <div
                  key={i}
                  className={`p-4 rounded-2xl border flex flex-col justify-between transition-all duration-200 ${
                    pkg.popular
                      ? 'border-indigo-500 bg-gradient-to-b from-indigo-50/90 to-violet-50/50 dark:from-indigo-950/40 dark:to-violet-950/20 shadow-md ring-2 ring-indigo-500/30'
                      : 'border-slate-200/90 dark:border-slate-800 bg-white dark:bg-slate-900 hover:border-indigo-300 shadow-2xs'
                  }`}
                >
                  <div className="space-y-1">
                    <span className="text-[10px] uppercase font-bold text-indigo-600 dark:text-indigo-400 tracking-wide">
                      {pkg.tag}
                    </span>
                    <h5 className="text-sm font-bold text-slate-900 dark:text-white">{pkg.name}</h5>
                    <div className="pt-2 text-2xl font-extrabold font-mono text-slate-900 dark:text-white tabular-nums">
                      {pkg.credits + pkg.bonus} <span className="text-xs font-sans font-normal text-slate-400">credite</span>
                    </div>
                    {pkg.bonus > 0 && (
                      <span className="text-[11px] text-emerald-600 font-bold block">
                        +{pkg.bonus} credite bonus cadou
                      </span>
                    )}
                  </div>

                  <div className="mt-4">
                    <Button
                      variant={pkg.popular ? 'primary' : 'secondary'}
                      size="sm"
                      className="w-full"
                      onClick={() => handleSelectPackage(pkg)}
                    >
                      Cumpără · {pkg.price} RON
                    </Button>
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Transaction Ledger */}
          <div>
            <div className="flex items-center justify-between mb-3">
              <h4 className="text-xs font-bold text-slate-800 dark:text-slate-200 uppercase tracking-wider flex items-center gap-1.5">
                <History className="w-3.5 h-3.5 text-indigo-500" />
                Registru Tranzacții & Audit Atomic
              </h4>
              <Badge variant="indigo">Fără Debitări Duplicate</Badge>
            </div>

            <div className="border border-slate-200/90 dark:border-slate-800 rounded-2xl overflow-hidden divide-y divide-slate-100 dark:divide-slate-800 text-xs shadow-2xs">
              {transactions.map(tx => (
                <div key={tx.id} className="p-3.5 flex items-center justify-between bg-white dark:bg-slate-900">
                  <div className="flex items-center gap-3">
                    <div
                      className={`w-8 h-8 rounded-xl flex items-center justify-center shrink-0 ${
                        tx.amount > 0
                          ? 'bg-emerald-100 dark:bg-emerald-950 text-emerald-600 dark:text-emerald-400'
                          : 'bg-indigo-50 dark:bg-slate-800 text-indigo-600 dark:text-indigo-400'
                      }`}
                    >
                      {tx.amount > 0 ? (
                        <ArrowDownLeft className="w-4 h-4" />
                      ) : (
                        <ArrowUpRight className="w-4 h-4" />
                      )}
                    </div>
                    <div>
                      <p className="font-bold text-slate-900 dark:text-white">{tx.description}</p>
                      <span className="text-[10px] text-slate-400 font-mono">{tx.timestamp} · ID: {tx.id}</span>
                    </div>
                  </div>

                  <div className="text-right">
                    <span
                      className={`font-mono font-extrabold block tabular-nums ${
                        tx.amount > 0
                          ? 'text-emerald-600 dark:text-emerald-400'
                          : 'text-slate-900 dark:text-white'
                      }`}
                    >
                      {tx.amount > 0 ? `+${tx.amount}` : tx.amount} credite
                    </span>
                    <span className="text-[10px] text-slate-400 font-mono">
                      Sold după: {tx.balanceAfter}
                    </span>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
