import React from 'react';
import { Lead } from '../../types';
import { useApp } from '../../context/AppContext';
import { MapPin, Wallet, Users, Check, ChevronRight } from 'lucide-react';
import { Badge } from '../ui/Badge';

interface MobileLeadCardProps {
  lead: Lead;
  onOpenDetails: (lead: Lead) => void;
}

export const MobileLeadCard: React.FC<MobileLeadCardProps> = ({ lead, onOpenDetails }) => {
  const { isLeadClaimedByMe } = useApp();
  const isClaimed = isLeadClaimedByMe(lead.id);

  return (
    <div
      onClick={() => onOpenDetails(lead)}
      className="p-4 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200/90 dark:border-slate-800 shadow-xs hover:border-indigo-300 dark:hover:border-indigo-700 active:scale-[0.98] transition-all cursor-pointer relative"
    >
      {/* Top category & timing */}
      <div className="flex items-center justify-between text-xs text-slate-500 dark:text-slate-400 mb-2">
        <Badge variant="indigo">
          {lead.category.split('&')[0]}
        </Badge>
        <span className="font-mono text-[11px] text-slate-400">{lead.createdAt}</span>
      </div>

      {/* Title */}
      <h3 className="text-sm font-bold text-slate-900 dark:text-white leading-snug line-clamp-2 mb-2">
        {lead.title}
      </h3>

      {/* Meta Strip */}
      <div className="flex flex-wrap items-center gap-2 text-xs text-slate-600 dark:text-slate-300 mb-3">
        <span className="flex items-center gap-1 font-medium">
          <MapPin className="w-3.5 h-3.5 text-indigo-500 shrink-0" />
          <span className="truncate max-w-[120px]">{lead.location}</span>
        </span>
        <span className="text-slate-300 dark:text-slate-700">·</span>
        <span className="flex items-center gap-1 font-mono font-bold text-slate-900 dark:text-slate-100 tabular-nums">
          <Wallet className="w-3.5 h-3.5 text-emerald-500 shrink-0" />
          <span>{lead.budget.max.toLocaleString()} {lead.budget.currency}</span>
        </span>
      </div>

      {/* Bottom Passport & Price Bar */}
      <div className="pt-2.5 border-t border-slate-100 dark:border-slate-800 flex items-center justify-between">
        <div className="flex items-center gap-2 text-xs">
          <Badge variant="emerald">
            {lead.leadPassport.completenessScore}% Passport
          </Badge>

          <span className="text-[11px] text-slate-400 flex items-center gap-1 font-mono font-medium">
            <Users className="w-3 h-3 text-indigo-500" />
            {lead.claimedPros.length}/{lead.maxPros}
          </span>
        </div>

        <div className="flex items-center gap-1.5">
          {isClaimed ? (
            <Badge variant="emerald" icon={<Check className="w-3 h-3" />}>
              Deblocat
            </Badge>
          ) : (
            <span className="px-3 py-1 rounded-xl bg-gradient-to-r from-indigo-600 to-violet-600 text-white font-bold text-xs flex items-center gap-1 shadow-xs">
              <span>{lead.leadPrice} Credite</span>
              <ChevronRight className="w-3.5 h-3.5" />
            </span>
          )}
        </div>
      </div>
    </div>
  );
};
