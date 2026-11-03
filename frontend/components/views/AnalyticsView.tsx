"use client";

import React from 'react';
import { useApp } from '@/context/AppContext';
import { BarChart3, ArrowUpRight } from 'lucide-react';
import { Badge } from '@/components/ui/Badge';

export const AnalyticsView: React.FC = () => {
  const { proProfile, userRole, leads, transactions, disputes } = useApp();
  const ownedLeads = leads.filter(lead => lead.ownedByCurrentUser);
  const completedProjects = ownedLeads.filter(lead => lead.status === 'finalizat').length;
  const receivedOffers = ownedLeads.reduce((total, lead) => total + lead.claimedPros.length, 0);
  const purchasedCredits = transactions
    .filter(transaction => transaction.type === 'lead_purchase')
    .reduce((total, transaction) => total + Math.abs(transaction.amount), 0);

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
          {ownedLeads.length > 0 ? `${ownedLeads.length} solicitări urmărite` : 'Fără date disponibile'}
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
                0 RON
              </div>
              <span className="text-[10px] text-slate-500 block">Comparativ cu devizele clasice de piață</span>
            </div>

            <div className="p-4 rounded-2xl bg-gradient-to-br from-emerald-50/70 to-white dark:from-slate-900 dark:to-slate-900 border border-emerald-200/80 dark:border-slate-800 space-y-1 shadow-xs">
              <span className="text-[11px] text-emerald-600 dark:text-emerald-400 font-semibold uppercase tracking-wider">Oferte per Solicitare</span>
              <div className="text-2xl font-extrabold font-mono text-emerald-600 dark:text-emerald-400 tabular-nums">
                {receivedOffers} Specialiști
              </div>
              <span className="text-[10px] text-slate-500 block">Medie de răspuns în sub 15 minute</span>
            </div>

            <div className="p-4 rounded-2xl bg-gradient-to-br from-amber-50/70 to-white dark:from-slate-900 dark:to-slate-900 border border-amber-200/80 dark:border-slate-800 space-y-1 shadow-xs">
              <span className="text-[11px] text-amber-600 dark:text-amber-400 font-semibold uppercase tracking-wider">Proiecte Încheiate</span>
              <div className="text-2xl font-extrabold font-mono text-amber-600 dark:text-amber-400 tabular-nums">
                {completedProjects} Lucrări
              </div>
              <span className="text-[10px] text-slate-500 block">100% cu recepție calitativă confirmată</span>
            </div>
          </>
        ) : userRole === 'pro' ? (
          <>
            <div className="p-4 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200/90 dark:border-slate-800 space-y-1 shadow-xs">
              <span className="text-[11px] text-slate-400 font-semibold uppercase tracking-wider">Venit Brut Contractat</span>
              <div className="text-2xl font-extrabold font-mono text-slate-900 dark:text-white tabular-nums flex items-baseline gap-2">
                0 RON
              </div>
              <span className="text-[10px] text-slate-500 block">Din 18 proiecte câștigate în 2026</span>
            </div>

            <div className="p-4 rounded-2xl bg-gradient-to-br from-indigo-50/70 to-white dark:from-slate-900 dark:to-slate-900 border border-indigo-200/80 dark:border-slate-800 space-y-1 shadow-xs">
              <span className="text-[11px] text-indigo-600 dark:text-indigo-400 font-semibold uppercase tracking-wider">Credite Consumate</span>
              <div className="text-2xl font-extrabold font-mono text-indigo-600 dark:text-indigo-400 tabular-nums">
                {purchasedCredits} credite
              </div>
              <span className="text-[10px] text-slate-500 block">Cost mediu achiziție client: 35 RON</span>
            </div>

            <div className="p-4 rounded-2xl bg-gradient-to-br from-emerald-50/70 to-white dark:from-slate-900 dark:to-slate-900 border border-emerald-200/80 dark:border-slate-800 space-y-1 shadow-xs">
              <span className="text-[11px] text-emerald-600 dark:text-emerald-400 font-semibold uppercase tracking-wider">Rată Succes Ofertare</span>
              <div className="text-2xl font-extrabold font-mono text-emerald-600 dark:text-emerald-400 tabular-nums">
                0%
              </div>
              <span className="text-[10px] text-slate-500 block">Top 5% profesioniști în categoria ta</span>
            </div>

            <div className="p-4 rounded-2xl bg-gradient-to-br from-amber-50/70 to-white dark:from-slate-900 dark:to-slate-900 border border-amber-200/80 dark:border-slate-800 space-y-1 shadow-xs">
              <span className="text-[11px] text-amber-600 dark:text-amber-400 font-semibold uppercase tracking-wider">Scor Rating Protocol</span>
              <div className="text-2xl font-extrabold font-mono text-amber-600 dark:text-amber-400 tabular-nums">
                {proProfile.averageRating}★
              </div>
              <span className="text-[10px] text-slate-500 block">              {proProfile.reviewCount} recenzii înregistrate</span>
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
                {transactions.reduce((total, transaction) => total + Math.abs(transaction.amount), 0).toLocaleString()} RON
              </div>
              <span className="text-[10px] text-slate-500 block">195 lead-uri vândute (medie 30 RON/lead)</span>
            </div>

            <div className="p-4 rounded-2xl bg-gradient-to-br from-emerald-50/70 to-white dark:from-slate-900 dark:to-slate-900 border border-emerald-200/80 dark:border-slate-800 space-y-1 shadow-xs hover:border-emerald-400 transition-colors">
              <span className="text-[11px] text-emerald-600 dark:text-emerald-400 font-semibold uppercase tracking-wider">Rată Conversie Chat</span>
              <div className="text-2xl font-extrabold font-mono text-emerald-600 dark:text-emerald-400 tabular-nums">
                {transactions.length > 0 ? 'Calcul în curs' : '0%'}
              </div>
              <span className="text-[10px] text-slate-500 block">              {transactions.length} tranzacții înregistrate</span>
            </div>

            <div className="p-4 rounded-2xl bg-gradient-to-br from-amber-50/70 to-white dark:from-slate-900 dark:to-slate-900 border border-amber-200/80 dark:border-slate-800 space-y-1 shadow-xs hover:border-amber-400 transition-colors">
              <span className="text-[11px] text-amber-600 dark:text-amber-400 font-semibold uppercase tracking-wider">Rată Dispute</span>
              <div className="text-2xl font-extrabold font-mono text-amber-600 dark:text-amber-400 tabular-nums">
                {disputes.length > 0 ? 'Calcul în curs' : '0%'}
              </div>
              <span className="text-[10px] text-slate-500 block">{disputes.length} sesizări înregistrate</span>
            </div>
          </>
        )}
      </div>

    </div>
  );
};
