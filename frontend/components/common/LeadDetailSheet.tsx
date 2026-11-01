"use client";

import React, { useEffect, useState } from 'react';
import { LeadModel } from '@/models/LeadModel';
import { useApp } from '@/context/AppContext';
import { LeadPassportCard } from '@/components/common/LeadPassportCard';
import { ExplainableMatchCard } from '@/components/common/ExplainableMatchCard';
import { Button } from '@/components/ui/Button';
import { Badge } from '@/components/ui/Badge';
import { X, Unlock, Phone, Mail, MessageSquare, CheckCircle2 } from 'lucide-react';

interface LeadDetailSheetProps {
  lead: LeadModel | null;
  onClose: () => void;
  onOpenChat: (leadId: string) => void;
}

export const LeadDetailSheet: React.FC<LeadDetailSheetProps> = ({
  lead,
  onClose,
  onOpenChat
}) => {
  const { unlockLead, isLeadClaimedByMe } = useApp();
  const [feedback, setFeedback] = useState<{ success: boolean; message: string } | null>(null);
  const [isProcessing, setIsProcessing] = useState(false);

  useEffect(() => {
    if (!lead) return;
    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape') onClose();
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [lead, onClose]);

  if (!lead) return null;

  const isClaimed = isLeadClaimedByMe(lead.id);
  const isFull = lead.claimedPros.length >= lead.maxPros;

  const handleUnlock = async () => {
    setIsProcessing(true);
    try {
      const res = await unlockLead(lead.id);
      setFeedback(res);
    } finally {
      setIsProcessing(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 bg-black/60 backdrop-blur-xs flex flex-col justify-end sm:justify-center sm:items-center sm:p-6">
      {/* Dimmed backdrop dismiss */}
      <div className="absolute inset-0" onClick={onClose} />

      {/* Sheet Modal (bottom sheet on phones, centered dialog from sm up) */}
      <div className="relative w-full sm:max-w-2xl max-h-[88vh] sm:max-h-[85vh] bg-white dark:bg-slate-900 rounded-t-[32px] sm:rounded-3xl border-t sm:border border-indigo-100 dark:border-slate-800 shadow-2xl flex flex-col overflow-hidden">
        {/* Grab Handle (phones only) */}
        <div className="w-12 h-1.5 bg-slate-300 dark:bg-slate-700 rounded-full mx-auto my-3 shrink-0 sm:hidden" />

        {/* Header */}
        <div className="px-5 py-3 sm:pt-5 border-b border-slate-100 dark:border-slate-800 flex items-center justify-between shrink-0 gap-3">
          <div className="min-w-0">
            <div className="flex items-center gap-1.5 mb-1">
              <Badge variant="indigo">{lead.category}</Badge>
            </div>
            <h3 className="text-sm font-bold text-slate-900 dark:text-white line-clamp-1">
              {lead.title}
            </h3>
          </div>

          <button
            onClick={onClose}
            aria-label="Închide detaliile solicitării"
            className="w-8 h-8 rounded-full bg-slate-100 dark:bg-slate-800 text-slate-500 hover:text-slate-800 dark:hover:text-white flex items-center justify-center shrink-0 cursor-pointer"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        {/* Scrollable Body */}
        <div className="p-5 overflow-y-auto space-y-4 flex-1 text-xs">
          {/* Project Description */}
          <div className="p-4 rounded-2xl bg-slate-50 dark:bg-slate-800/60 border border-slate-200/90 dark:border-slate-800">
            <span className="text-[10px] text-slate-400 block mb-1 uppercase font-bold tracking-wider">
              Descriere Solicitare
            </span>
            <p className="text-slate-800 dark:text-slate-200 leading-relaxed font-normal">
              {lead.description}
            </p>
          </div>

          {/* Lead Passport Component */}
          <LeadPassportCard
            passport={lead.leadPassport}
            isUnlocked={isClaimed}
            leadPrice={lead.leadPrice}
          />

          {/* Explainable Match */}
          {lead.matchExplanation && (
            <ExplainableMatchCard match={lead.matchExplanation} compact />
          )}

          {/* Unlocked Contact Details */}
          {isClaimed && (
            <div className="p-4 rounded-2xl bg-emerald-50 dark:bg-emerald-950/40 border border-emerald-200 dark:border-emerald-800 space-y-2.5">
              <span className="text-xs font-bold text-emerald-800 dark:text-emerald-300 flex items-center gap-1.5">
                <CheckCircle2 className="w-4 h-4 text-emerald-600" />
                Contact Client Deblocat
              </span>
              <div className="space-y-1.5 text-xs">
                <div className="flex items-center gap-2">
                  <Phone className="w-3.5 h-3.5 text-emerald-600" />
                  <span className="font-mono font-bold text-slate-900 dark:text-white">{lead.client.phone}</span>
                </div>
                <div className="flex items-center gap-2">
                  <Mail className="w-3.5 h-3.5 text-emerald-600" />
                  <span className="font-mono font-bold text-slate-900 dark:text-white">{lead.client.email}</span>
                </div>
              </div>
            </div>
          )}

          {feedback && (
            <div
              className={`p-3.5 rounded-xl text-xs flex items-center gap-2 ${
                feedback.success
                  ? 'bg-emerald-50 text-emerald-800 dark:bg-emerald-950 dark:text-emerald-300 border border-emerald-200'
                  : 'bg-rose-50 text-rose-800 dark:bg-rose-950 dark:text-rose-300 border border-rose-200'
              }`}
            >
              <span>{feedback.message}</span>
            </div>
          )}
        </div>

        {/* Sticky Action Footer */}
        <div className="p-4 border-t border-slate-200/90 dark:border-slate-800 bg-white dark:bg-slate-900 shrink-0">
          {isClaimed ? (
            <Button
              variant="primary"
              size="lg"
              className="w-full h-12 shadow-md"
              icon={<MessageSquare className="w-4 h-4" />}
              onClick={() => {
                onClose();
                onOpenChat(lead.id);
              }}
            >
              Deschide Chat în Timp Real
            </Button>
          ) : (
            <Button
              variant="success"
              size="lg"
              className="w-full h-12 shadow-md"
              disabled={isFull}
              isLoading={isProcessing}
              icon={<Unlock className="w-4 h-4" />}
              onClick={handleUnlock}
            >
              {isFull ? 'Limită maximă profesioniști atinsă' : `Cumpără Lead (${lead.leadPrice} Credite)`}
            </Button>
          )}
        </div>
      </div>
    </div>
  );
};
