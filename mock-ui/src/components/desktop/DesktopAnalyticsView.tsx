import React from 'react';
import { useApp } from '../../context/AppContext';
import { BarChart3, TrendingUp, Users, CheckCircle2, Sparkles, Layers, ArrowUpRight, Wallet, ShieldCheck, Award } from 'lucide-react';
import { Badge } from '../ui/Badge';

export const DesktopAnalyticsView: React.FC = () => {
  const { leads, disputes, proProfile, userRole } = useApp();

  return (
    <div className="h-full overflow-y-auto p-6 space-y-6 bg-slate-50/40 dark:bg-slate-950">
      {/* Top Header */}
      <div className="border-b border-indigo-100 dark:border-slate-800 pb-4 flex flex-wrap items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-2">
            <h2 className="text-xl font-extrabold text-slate-900 dark:text-white flex items-center gap-2">
              <BarChart3 className="w-5 h-5 text-indigo-600 dark:text-indigo-400" />
              {userRole === 'client'
                ? 'Analitice Buget & Proiecte Rezidențiale'
                : userRole === 'admin'
                ? 'Panou Monitorizare & Product Analytics (Admin)'
                : 'Analitice Performanță & ROI Profesionist'}
            </h2>
            <Badge variant={userRole === 'client' ? 'emerald' : userRole === 'admin' ? 'violet' : 'indigo'}>
              {userRole === 'client' ? 'Client Insights' : userRole === 'admin' ? 'PostHog Instrumented' : 'Pro ROI Engine'}
            </Badge>
          </div>
          <p className="text-xs text-slate-500 dark:text-slate-400 mt-1">
            {userRole === 'client'
              ? 'Urmărește eficiența cheltuielilor, ofertele primite și economia realizată prin selecția transparentă de pe Skillink.'
              : userRole === 'admin'
              ? 'Monitorizarea fluxului end-to-end conform cerințelor de inginerie software și evaluarea experimentală AI vs Baseline (Capitolele 1.3 G & 5.6).'
              : 'Evaluează randamentul investiției în credite, rata de conversie a solicitărilor și satisfacția clienților.'}
          </p>
        </div>

        <span className="text-xs text-emerald-600 font-semibold flex items-center gap-1 bg-emerald-50 dark:bg-emerald-950/60 px-3 py-1 rounded-full border border-emerald-200 dark:border-emerald-800">
          <ArrowUpRight className="w-3.5 h-3.5" />
          {userRole === 'client'
            ? 'Economie medie de 14.2% per proiect'
            : userRole === 'admin'
            ? '+28.4% retenție profesioniști'
            : 'ROI mediu 18.2x per credit investit'}
        </span>
      </div>

      {/* KPI Cards Strip */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {userRole === 'client' ? (
          <>
            <div className="p-4 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200/90 dark:border-slate-800 space-y-1 shadow-xs">
              <span className="text-[11px] text-slate-400 font-semibold uppercase tracking-wider">Total Buget Investit</span>
              <div className="text-2xl font-extrabold font-mono text-emerald-600 dark:text-emerald-400 tabular-nums">
                {proProfile.clientBudget.toLocaleString()} RON
              </div>
              <span className="text-[10px] text-slate-500 block">Alocat în siguranță prin Escrow</span>
            </div>

            <div className="p-4 rounded-2xl bg-gradient-to-br from-indigo-50/70 to-white dark:from-slate-900 dark:to-slate-900 border border-indigo-200/80 dark:border-slate-800 space-y-1 shadow-xs">
              <span className="text-[11px] text-indigo-600 dark:text-indigo-400 font-semibold uppercase tracking-wider">Economie Realizată</span>
              <div className="text-2xl font-extrabold font-mono text-indigo-600 dark:text-indigo-400 tabular-nums">
                4.200 RON
              </div>
              <span className="text-[10px] text-slate-500 block">Comparativ cu devizele clasice de piață</span>
            </div>

            <div className="p-4 rounded-2xl bg-gradient-to-br from-emerald-50/70 to-white dark:from-slate-900 dark:to-slate-900 border border-emerald-200/80 dark:border-slate-800 space-y-1 shadow-xs">
              <span className="text-[11px] text-emerald-600 dark:text-emerald-400 font-semibold uppercase tracking-wider">Oferte per Solicitare</span>
              <div className="text-2xl font-extrabold font-mono text-emerald-600 dark:text-emerald-400 tabular-nums">
                3.4 Specialiști
              </div>
              <span className="text-[10px] text-slate-500 block">Medie de răspuns în sub 15 minute</span>
            </div>

            <div className="p-4 rounded-2xl bg-gradient-to-br from-amber-50/70 to-white dark:from-slate-900 dark:to-slate-900 border border-amber-200/80 dark:border-slate-800 space-y-1 shadow-xs">
              <span className="text-[11px] text-amber-600 dark:text-amber-400 font-semibold uppercase tracking-wider">Proiecte Încheiate</span>
              <div className="text-2xl font-extrabold font-mono text-amber-600 dark:text-amber-400 tabular-nums">
                {proProfile.completedJobs} Lucrări
              </div>
              <span className="text-[10px] text-slate-500 block">100% cu recepție calitativă confirmată</span>
            </div>
          </>
        ) : userRole === 'pro' ? (
          <>
            <div className="p-4 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200/90 dark:border-slate-800 space-y-1 shadow-xs">
              <span className="text-[11px] text-slate-400 font-semibold uppercase tracking-wider">Venit Brut Contractat</span>
              <div className="text-2xl font-extrabold font-mono text-slate-900 dark:text-white tabular-nums flex items-baseline gap-2">
                38.400 RON <span className="text-xs font-sans text-emerald-600 font-bold">+24%</span>
              </div>
              <span className="text-[10px] text-slate-500 block">Din 18 proiecte câștigate în 2026</span>
            </div>

            <div className="p-4 rounded-2xl bg-gradient-to-br from-indigo-50/70 to-white dark:from-slate-900 dark:to-slate-900 border border-indigo-200/80 dark:border-slate-800 space-y-1 shadow-xs">
              <span className="text-[11px] text-indigo-600 dark:text-indigo-400 font-semibold uppercase tracking-wider">Credite Consumate</span>
              <div className="text-2xl font-extrabold font-mono text-indigo-600 dark:text-indigo-400 tabular-nums">
                210 credite
              </div>
              <span className="text-[10px] text-slate-500 block">Cost mediu achiziție client: 35 RON</span>
            </div>

            <div className="p-4 rounded-2xl bg-gradient-to-br from-emerald-50/70 to-white dark:from-slate-900 dark:to-slate-900 border border-emerald-200/80 dark:border-slate-800 space-y-1 shadow-xs">
              <span className="text-[11px] text-emerald-600 dark:text-emerald-400 font-semibold uppercase tracking-wider">Rată Succes Ofertare</span>
              <div className="text-2xl font-extrabold font-mono text-emerald-600 dark:text-emerald-400 tabular-nums">
                41.8%
              </div>
              <span className="text-[10px] text-slate-500 block">Top 5% profesioniști în categoria ta</span>
            </div>

            <div className="p-4 rounded-2xl bg-gradient-to-br from-amber-50/70 to-white dark:from-slate-900 dark:to-slate-900 border border-amber-200/80 dark:border-slate-800 space-y-1 shadow-xs">
              <span className="text-[11px] text-amber-600 dark:text-amber-400 font-semibold uppercase tracking-wider">Scor Rating Protocol</span>
              <div className="text-2xl font-extrabold font-mono text-amber-600 dark:text-amber-400 tabular-nums">
                {proProfile.averageRating}★
              </div>
              <span className="text-[10px] text-slate-500 block">99.4% Trust Index pe baza a 54 recenzii</span>
            </div>
          </>
        ) : (
          <>
            <div className="p-4 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200/90 dark:border-slate-800 space-y-1 shadow-xs hover:border-indigo-300 transition-colors">
              <span className="text-[11px] text-slate-400 font-semibold uppercase tracking-wider">Volum Solicitări</span>
              <div className="text-2xl font-extrabold font-mono text-slate-900 dark:text-white tabular-nums flex items-baseline gap-2">
                320 <span className="text-xs font-sans text-emerald-600 font-bold">+18% lună</span>
              </div>
              <span className="text-[10px] text-slate-500 block">Necesit.ro benchmark: ~10.000 lunar</span>
            </div>

            <div className="p-4 rounded-2xl bg-gradient-to-br from-indigo-50/70 to-white dark:from-slate-900 dark:to-slate-900 border border-indigo-200/80 dark:border-slate-800 space-y-1 shadow-xs hover:border-indigo-400 transition-colors">
              <span className="text-[11px] text-indigo-600 dark:text-indigo-400 font-semibold uppercase tracking-wider">Venit Brut Simulat</span>
              <div className="text-2xl font-extrabold font-mono text-indigo-600 dark:text-indigo-400 tabular-nums">
                5.850 RON
              </div>
              <span className="text-[10px] text-slate-500 block">195 lead-uri vândute (medie 30 RON/lead)</span>
            </div>

            <div className="p-4 rounded-2xl bg-gradient-to-br from-emerald-50/70 to-white dark:from-slate-900 dark:to-slate-900 border border-emerald-200/80 dark:border-slate-800 space-y-1 shadow-xs hover:border-emerald-400 transition-colors">
              <span className="text-[11px] text-emerald-600 dark:text-emerald-400 font-semibold uppercase tracking-wider">Rată Conversie Chat</span>
              <div className="text-2xl font-extrabold font-mono text-emerald-600 dark:text-emerald-400 tabular-nums">
                91.3%
              </div>
              <span className="text-[10px] text-slate-500 block">178 conversații inițiate în &lt; 30 min</span>
            </div>

            <div className="p-4 rounded-2xl bg-gradient-to-br from-amber-50/70 to-white dark:from-slate-900 dark:to-slate-900 border border-amber-200/80 dark:border-slate-800 space-y-1 shadow-xs hover:border-amber-400 transition-colors">
              <span className="text-[11px] text-amber-600 dark:text-amber-400 font-semibold uppercase tracking-wider">Rată Dispute</span>
              <div className="text-2xl font-extrabold font-mono text-amber-600 dark:text-amber-400 tabular-nums">
                1.2%
              </div>
              <span className="text-[10px] text-slate-500 block">Sub pragul critic de 3.0% (Cap. 4.3)</span>
            </div>
          </>
        )}
      </div>

      {/* Experimental Evaluation Matrix */}
      <div className="p-5 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200/90 dark:border-slate-800 shadow-2xs space-y-4">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <Sparkles className="w-4 h-4 text-indigo-600 dark:text-indigo-400" />
            <h3 className="text-xs font-bold text-slate-900 dark:text-white uppercase tracking-wider">
              Evaluare Experimentală: AI Qualification Pipeline vs Baseline (Formular Tradițional)
            </h3>
          </div>
          <Badge variant="indigo">Cap. 5.6 Raport</Badge>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs border-collapse">
            <thead>
              <tr className="border-b border-slate-200 dark:border-slate-800 text-slate-400 font-semibold">
                <th className="py-2.5 px-3">Metrica Evaluată</th>
                <th className="py-2.5 px-3">Baseline (Formular Necesit.ro)</th>
                <th className="py-2.5 px-3 text-indigo-600 dark:text-indigo-400">Skillink AI Pipeline</th>
                <th className="py-2.5 px-3 text-emerald-600 font-bold">Îmbunătățire</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 dark:divide-slate-800 font-mono text-[11px]">
              <tr>
                <td className="py-3 px-3 font-sans font-medium text-slate-800 dark:text-slate-200">
                  Completitudine Lead Passport (informații critice)
                </td>
                <td className="py-3 px-3 text-slate-500">42.5%</td>
                <td className="py-3 px-3 font-bold text-indigo-600 dark:text-indigo-400">88.4%</td>
                <td className="py-3 px-3 font-bold text-emerald-600">+108%</td>
              </tr>
              <tr>
                <td className="py-3 px-3 font-sans font-medium text-slate-800 dark:text-slate-200">
                  Rată Răspuns Profesioniști (în primele 2 ore)
                </td>
                <td className="py-3 px-3 text-slate-500">31.0%</td>
                <td className="py-3 px-3 font-bold text-indigo-600 dark:text-indigo-400">74.2%</td>
                <td className="py-3 px-3 font-bold text-emerald-600">+139%</td>
              </tr>
              <tr>
                <td className="py-3 px-3 font-sans font-medium text-slate-800 dark:text-slate-200">
                  Rată de Dispute pe Contacte Invalide
                </td>
                <td className="py-3 px-3 text-slate-500">8.4%</td>
                <td className="py-3 px-3 font-bold text-indigo-600 dark:text-indigo-400">1.2%</td>
                <td className="py-3 px-3 font-bold text-emerald-600">-85.7%</td>
              </tr>
              <tr>
                <td className="py-3 px-3 font-sans font-medium text-slate-800 dark:text-slate-200">
                  Timp Mediu Închidere Tranzacție
                </td>
                <td className="py-3 px-3 text-slate-500">4.8 zile</td>
                <td className="py-3 px-3 font-bold text-indigo-600 dark:text-indigo-400">1.9 zile</td>
                <td className="py-3 px-3 font-bold text-emerald-600">-60.4%</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};
