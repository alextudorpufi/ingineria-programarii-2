import React from 'react';
import { LeadPassport } from '../../types';
import { ShieldCheck, CheckCircle2, AlertCircle, Sparkles, FileText, Lock } from 'lucide-react';
import { Badge } from '../ui/Badge';

interface LeadPassportCardProps {
  passport: LeadPassport;
  isUnlocked?: boolean;
  leadPrice: number;
}

export const LeadPassportCard: React.FC<LeadPassportCardProps> = ({
  passport,
  isUnlocked = false,
  leadPrice
}) => {
  return (
    <div className="bg-gradient-to-br from-emerald-50/70 via-teal-50/30 to-indigo-50/40 dark:from-slate-900 dark:via-emerald-950/20 dark:to-slate-900 border border-emerald-200/80 dark:border-emerald-800/60 rounded-2xl p-5 shadow-xs transition-all">
      {/* Header with Title and Completeness Score */}
      <div className="flex items-center justify-between pb-4 border-b border-emerald-100 dark:border-emerald-900/40">
        <div className="flex items-center gap-3">
          <div className="w-9 h-9 rounded-xl bg-gradient-to-tr from-emerald-500 to-teal-600 text-white flex items-center justify-center shadow-xs">
            <ShieldCheck className="w-5 h-5" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <h4 className="text-sm font-bold text-slate-900 dark:text-white">Lead Passport™</h4>
              <Badge variant="emerald" icon={<Sparkles className="w-3 h-3 text-emerald-600 dark:text-emerald-400" />}>
                Verificat AI & Reguli
              </Badge>
            </div>
            <p className="text-xs text-slate-500 dark:text-slate-400">
              Fișă de transparență și calificare a oportunității înainte de achiziție
            </p>
          </div>
        </div>

        {/* Completeness Gauge */}
        <div className="text-right">
          <div className="flex items-center justify-end gap-1.5">
            <span className="text-xs font-medium text-slate-500 dark:text-slate-400">Completitudine:</span>
            <span className="text-base font-extrabold font-mono text-emerald-600 dark:text-emerald-400 tabular-nums">
              {passport.completenessScore}%
            </span>
          </div>
          <div className="w-28 h-2 bg-slate-200/80 dark:bg-slate-800 rounded-full overflow-hidden mt-1 ml-auto">
            <div
              className="h-full bg-gradient-to-r from-emerald-500 to-teal-400 rounded-full transition-all duration-500"
              style={{ width: `${passport.completenessScore}%` }}
            />
          </div>
        </div>
      </div>

      {/* Grid of Qualification Signals */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 py-4 border-b border-emerald-100 dark:border-emerald-900/40 text-xs">
        <div className="p-2.5 rounded-xl bg-white/80 dark:bg-slate-800/60 border border-emerald-100 dark:border-emerald-900/40">
          <span className="text-slate-400 dark:text-slate-500 block mb-0.5 text-[10px] uppercase font-semibold">
            Coerență Proiect
          </span>
          <span className="font-bold text-slate-800 dark:text-slate-200 flex items-center gap-1.5">
            <CheckCircle2 className="w-3.5 h-3.5 text-emerald-500" />
            {passport.coherenceStatus}
          </span>
        </div>

        <div className="p-2.5 rounded-xl bg-white/80 dark:bg-slate-800/60 border border-emerald-100 dark:border-emerald-900/40">
          <span className="text-slate-400 dark:text-slate-500 block mb-0.5 text-[10px] uppercase font-semibold">
            Date Contact
          </span>
          <span className="font-bold text-slate-800 dark:text-slate-200 flex items-center gap-1.5">
            <CheckCircle2 className="w-3.5 h-3.5 text-emerald-500" />
            {passport.contactVerified ? 'SMS & Email Confirmat' : 'Neverificat'}
          </span>
        </div>

        <div className="p-2.5 rounded-xl bg-white/80 dark:bg-slate-800/60 border border-emerald-100 dark:border-emerald-900/40">
          <span className="text-slate-400 dark:text-slate-500 block mb-0.5 text-[10px] uppercase font-semibold">
            Intenție Cumpărare
          </span>
          <span className="font-bold text-slate-800 dark:text-slate-200 flex items-center gap-1.5">
            <CheckCircle2 className="w-3.5 h-3.5 text-emerald-500" />
            {passport.intentVerified ? 'Contractare Iminentă' : 'Informare'}
          </span>
        </div>

        <div className="p-2.5 rounded-xl bg-white/80 dark:bg-slate-800/60 border border-emerald-100 dark:border-emerald-900/40">
          <span className="text-slate-400 dark:text-slate-500 block mb-0.5 text-[10px] uppercase font-semibold">
            Risc Duplicat
          </span>
          <span className="font-bold text-slate-800 dark:text-slate-200 flex items-center gap-1.5">
            <CheckCircle2 className="w-3.5 h-3.5 text-emerald-500" />
            {passport.duplicateRisk}
          </span>
        </div>
      </div>

      {/* Required Deliverables Extracted */}
      <div className="pt-3">
        <div className="flex items-center justify-between mb-2">
          <span className="text-xs font-bold text-slate-800 dark:text-slate-200 flex items-center gap-1.5">
            <FileText className="w-3.5 h-3.5 text-emerald-600" />
            Livrabile și cerințe tehnice extrase ({passport.requiredDeliverables.length}):
          </span>
          {passport.extractedByAI && (
            <Badge variant="indigo" icon={<Sparkles className="w-3 h-3 text-indigo-500" />}>
              AI Architect Certified
            </Badge>
          )}
        </div>

        <ul className="grid grid-cols-1 sm:grid-cols-2 gap-2 text-xs text-slate-700 dark:text-slate-300">
          {passport.requiredDeliverables.map((item, index) => (
            <li
              key={index}
              className="flex items-start gap-2 p-2 rounded-lg bg-white/90 dark:bg-slate-800/50 border border-slate-200/60 dark:border-slate-800"
            >
              <span className="w-4 h-4 rounded-full bg-emerald-100 dark:bg-emerald-950 text-emerald-600 dark:text-emerald-400 flex items-center justify-center text-[10px] font-bold shrink-0 mt-0.5">
                ✓
              </span>
              <span className="leading-tight">{item}</span>
            </li>
          ))}
        </ul>

        {passport.clarificationsRemaining.length > 0 && (
          <div className="mt-3.5 p-3 rounded-xl bg-amber-500/10 border border-amber-500/30 text-xs text-amber-900 dark:text-amber-200">
            <div className="font-bold flex items-center gap-1.5 mb-1 text-amber-800 dark:text-amber-300">
              <AlertCircle className="w-4 h-4 text-amber-500" />
              Clarificări restante identificate de sistem:
            </div>
            <ul className="list-disc pl-5 space-y-0.5">
              {passport.clarificationsRemaining.map((cl, i) => (
                <li key={i}>{cl}</li>
              ))}
            </ul>
          </div>
        )}
      </div>

      {/* Security Transparency Notice */}
      {!isUnlocked && (
        <div className="mt-4 pt-3 border-t border-emerald-100 dark:border-emerald-900/40 flex items-center justify-between text-xs text-slate-600 dark:text-slate-400">
          <span className="flex items-center gap-1.5">
            <Lock className="w-3.5 h-3.5 text-slate-400" />
            Datele de contact (telefon & email) se deblochează instant la acceptare
          </span>
          <span className="font-mono font-bold text-indigo-700 dark:text-indigo-300 tabular-nums bg-indigo-50 dark:bg-indigo-950/60 px-2 py-0.5 rounded-md border border-indigo-200/60 dark:border-indigo-900">
            Tarif: {leadPrice} credite
          </span>
        </div>
      )}
    </div>
  );
};
