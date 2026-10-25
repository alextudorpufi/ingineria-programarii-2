import React from 'react';
import { MatchExplanation } from '../../types';
import { Sparkles, Clock, Zap } from 'lucide-react';
import { Badge } from '../ui/Badge';

interface ExplainableMatchCardProps {
  match: MatchExplanation;
  compact?: boolean;
}

export const ExplainableMatchCard: React.FC<ExplainableMatchCardProps> = ({
  match,
  compact = false
}) => {
  return (
    <div
      className={`rounded-2xl border border-indigo-200 dark:border-indigo-900/60 bg-gradient-to-br from-indigo-50/80 via-violet-50/40 to-white dark:from-slate-900 dark:via-indigo-950/30 dark:to-slate-900 shadow-xs ${
        compact ? 'p-3.5 text-xs' : 'p-5 text-sm'
      }`}
    >
      <div className="flex items-center justify-between pb-3 border-b border-indigo-100 dark:border-indigo-900/40">
        <div className="flex items-center gap-2.5">
          <div className="w-8 h-8 rounded-xl bg-gradient-to-tr from-indigo-600 to-violet-600 text-white flex items-center justify-center shrink-0 shadow-xs">
            <Zap className="w-4 h-4" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <span className="font-bold text-slate-900 dark:text-white">
                Explainable Matching
              </span>
              <Badge variant="violet" icon={<Sparkles className="w-3 h-3 text-violet-500" />}>
                Hibrid AI + Reguli
              </Badge>
            </div>
            <p className="text-[11px] text-slate-500 dark:text-slate-400">
              Asociere transparentă pe baza profilului tău profesional
            </p>
          </div>
        </div>

        <div className="flex items-center gap-1.5 bg-white/90 dark:bg-slate-800/80 px-3 py-1.5 rounded-xl border border-indigo-200/80 dark:border-indigo-800 shadow-2xs">
          <span className="text-xs font-medium text-slate-500 dark:text-slate-400">Potrivire:</span>
          <span className="font-mono font-extrabold text-indigo-600 dark:text-indigo-400 text-sm tabular-nums">
            {match.overallScore}%
          </span>
        </div>
      </div>

      <div className="mt-3">
        <p className="text-xs text-slate-700 dark:text-slate-300 mb-2 font-bold uppercase tracking-wider text-[11px]">
          Motive verificabile ale asocierii:
        </p>

        <ul className="space-y-1.5 text-xs text-slate-700 dark:text-slate-300">
          {match.reasons.map((reason, idx) => (
            <li
              key={idx}
              className="flex items-start gap-2.5 p-2 rounded-lg bg-white/70 dark:bg-slate-800/40 border border-indigo-100/60 dark:border-indigo-950"
            >
              <span className="w-4 h-4 rounded-full bg-emerald-100 dark:bg-emerald-950 text-emerald-600 dark:text-emerald-400 flex items-center justify-center shrink-0 mt-0.5 text-[10px] font-bold">
                ✓
              </span>
              <span className="leading-tight font-medium">{reason}</span>
            </li>
          ))}
        </ul>

        {/* Semantic Keywords Overlap */}
        {match.semanticKeywords.length > 0 && (
          <div className="mt-3.5 pt-3 border-t border-indigo-100 dark:border-indigo-900/40 flex flex-wrap items-center gap-1.5 text-xs">
            <span className="text-slate-500 dark:text-slate-400 flex items-center gap-1 text-[11px] font-medium mr-1">
              <Sparkles className="w-3 h-3 text-indigo-500" />
              Similaritate semantică:
            </span>
            {match.semanticKeywords.map((kw, i) => (
              <span
                key={i}
                className="px-2.5 py-0.5 rounded-full bg-white dark:bg-slate-800 border border-indigo-200/80 dark:border-indigo-800 text-[11px] font-semibold text-indigo-800 dark:text-indigo-200 shadow-2xs"
              >
                {kw}
              </span>
            ))}
          </div>
        )}

        <div className="mt-2.5 flex items-center gap-2 text-[11px] text-slate-500 dark:text-slate-400">
          <Clock className="w-3.5 h-3.5 text-indigo-500" />
          <span>
            Timp optim de prim răspuns recomandat:{' '}
            <strong className="text-slate-800 dark:text-slate-200 font-bold">{match.expectedResponseTime}</strong>
          </span>
        </div>
      </div>
    </div>
  );
};
