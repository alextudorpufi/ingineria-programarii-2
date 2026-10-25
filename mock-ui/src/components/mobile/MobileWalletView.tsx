import React from 'react';
import { useApp } from '../../context/AppContext';
import { Coins, History, ArrowDownLeft, ArrowUpRight } from 'lucide-react';
import { Badge } from '../ui/Badge';

export const MobileWalletView: React.FC = () => {
  const { proProfile, transactions, rechargeCredits } = useApp();

  const packages = [
    { name: 'Starter', credits: 50, bonus: 0, price: 50 },
    { name: 'Pro', credits: 150, bonus: 20, price: 150, popular: true },
    { name: 'Studio', credits: 400, bonus: 80, price: 350 }
  ];

  return (
    <div className="h-full overflow-y-auto p-4 space-y-4 bg-slate-50/50 dark:bg-slate-950">
      {/* Visual Balance Card with Vibrant Gradient */}
      <div className="p-5 rounded-3xl bg-gradient-to-br from-indigo-600 via-violet-600 to-indigo-800 text-white shadow-lg space-y-4 relative overflow-hidden">
        <div className="absolute top-0 right-0 -mr-6 -mt-6 w-28 h-28 bg-white/10 rounded-full blur-xl pointer-events-none" />

        <div className="flex items-center justify-between text-xs">
          <span className="font-bold tracking-wider uppercase text-indigo-100 text-[10px]">
            Portofel Credite Servicii
          </span>
          <span className="px-2.5 py-0.5 rounded-full bg-white/20 text-[10px] font-mono font-medium backdrop-blur-xs">
            {proProfile.companyName}
          </span>
        </div>

        <div>
          <span className="text-xs text-indigo-100 block">Sold Activ Oportunități</span>
          <div className="text-3xl font-extrabold font-mono tabular-nums tracking-tight">
            {proProfile.creditsBalance} <span className="text-sm font-sans font-medium text-indigo-200">credite</span>
          </div>
        </div>

        <div className="pt-2 border-t border-white/20 flex items-center justify-between text-[11px] text-indigo-100">
          <span>1 Credit = 1 RON</span>
          <span>Debitare atomică per lead</span>
        </div>
      </div>

      {/* Top-up Options */}
      <div>
        <div className="flex items-center justify-between mb-2.5">
          <h4 className="text-xs font-bold text-slate-800 dark:text-slate-200 uppercase tracking-wider">
            Reîncărcare Rapidă
          </h4>
          <Badge variant="amber">Securizat</Badge>
        </div>

        <div className="grid grid-cols-3 gap-2">
          {packages.map((pkg, i) => (
            <button
              key={i}
              onClick={() => rechargeCredits(pkg.credits + pkg.bonus, `Pachet ${pkg.name}`)}
              className={`p-3.5 rounded-2xl border text-left flex flex-col justify-between active:scale-95 transition-all cursor-pointer ${
                pkg.popular
                  ? 'border-indigo-500 bg-gradient-to-b from-indigo-50/90 to-violet-50/50 dark:from-indigo-950/60 dark:to-violet-950/40 ring-2 ring-indigo-500/20 shadow-xs'
                  : 'border-slate-200/90 dark:border-slate-800 bg-white dark:bg-slate-900 shadow-2xs'
              }`}
            >
              <div>
                <span className="text-[10px] font-bold text-indigo-600 dark:text-indigo-400 block uppercase">
                  {pkg.name}
                </span>
                <span className="text-base font-extrabold font-mono text-slate-900 dark:text-white tabular-nums">
                  +{pkg.credits + pkg.bonus}
                </span>
              </div>
              <span className="text-xs font-bold text-indigo-700 dark:text-indigo-300 mt-2 block">
                {pkg.price} RON
              </span>
            </button>
          ))}
        </div>
      </div>

      {/* Transaction History */}
      <div>
        <div className="flex items-center justify-between mb-2">
          <h4 className="text-xs font-bold text-slate-800 dark:text-slate-200 uppercase tracking-wider flex items-center gap-1.5">
            <History className="w-3.5 h-3.5 text-indigo-500" />
            Istoric Tranzacții
          </h4>
          <Badge variant="indigo">Audit</Badge>
        </div>

        <div className="rounded-2xl border border-slate-200/90 dark:border-slate-800 bg-white dark:bg-slate-900 divide-y divide-slate-100 dark:divide-slate-800 overflow-hidden text-xs shadow-2xs">
          {transactions.map(tx => (
            <div key={tx.id} className="p-3.5 flex items-center justify-between">
              <div className="flex items-center gap-2.5">
                <div
                  className={`w-7 h-7 rounded-xl flex items-center justify-center shrink-0 ${
                    tx.amount > 0
                      ? 'bg-emerald-100 dark:bg-emerald-950 text-emerald-600'
                      : 'bg-indigo-50 dark:bg-slate-800 text-indigo-600'
                  }`}
                >
                  {tx.amount > 0 ? (
                    <ArrowDownLeft className="w-3.5 h-3.5" />
                  ) : (
                    <ArrowUpRight className="w-3.5 h-3.5" />
                  )}
                </div>
                <div>
                  <p className="font-bold text-slate-900 dark:text-white line-clamp-1">{tx.description}</p>
                  <span className="text-[10px] text-slate-400 font-mono">{tx.timestamp}</span>
                </div>
              </div>

              <div className="text-right">
                <span
                  className={`font-mono font-extrabold block tabular-nums ${
                    tx.amount > 0 ? 'text-emerald-600' : 'text-slate-900 dark:text-white'
                  }`}
                >
                  {tx.amount > 0 ? `+${tx.amount}` : tx.amount}
                </span>
                <span className="text-[9px] text-slate-400 font-mono">Sold: {tx.balanceAfter}</span>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};
