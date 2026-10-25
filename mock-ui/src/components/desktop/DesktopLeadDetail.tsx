import React, { useState } from 'react';
import { useApp } from '../../context/AppContext';
import { LeadPassportCard } from '../common/LeadPassportCard';
import { ExplainableMatchCard } from '../common/ExplainableMatchCard';
import { Button } from '../ui/Button';
import { Badge } from '../ui/Badge';
import {
  MapPin,
  Calendar,
  Wallet,
  Phone,
  Mail,
  Lock,
  Unlock,
  MessageSquare,
  AlertTriangle,
  Users,
  CheckCircle2,
  ShieldAlert,
  ShieldCheck,
  Check,
  UserCheck
} from 'lucide-react';

interface DesktopLeadDetailProps {
  onOpenChat: (leadId: string) => void;
  onOpenDispute: (leadId: string) => void;
}

export const DesktopLeadDetail: React.FC<DesktopLeadDetailProps> = ({
  onOpenChat,
  onOpenDispute
}) => {
  const { activeLead, unlockLead, isLeadClaimedByMe, proProfile, userRole } = useApp();
  const [unlockFeedback, setUnlockFeedback] = useState<{ success: boolean; message: string } | null>(null);
  const [isUnlocking, setIsUnlocking] = useState(false);
  const [adminStatusMessage, setAdminStatusMessage] = useState<string | null>(null);

  if (!activeLead) {
    return (
      <div className="h-full flex flex-col items-center justify-center p-8 text-center text-slate-400">
        <p className="text-sm">Selectează o oportunitate din listă pentru a vizualiza detaliile și Lead Passport-ul.</p>
      </div>
    );
  }

  const isClaimed = isLeadClaimedByMe(activeLead.id);
  const isFull = activeLead.claimedPros.length >= activeLead.maxPros;

  const handleUnlock = () => {
    setIsUnlocking(true);
    setUnlockFeedback(null);
    setTimeout(() => {
      const res = unlockLead(activeLead.id);
      setUnlockFeedback(res);
      setIsUnlocking(false);
    }, 450);
  };

  const handleAdminApprove = () => {
    setAdminStatusMessage('Lead auditat și marcat ca 100% Validat în baza de date.');
    setTimeout(() => setAdminStatusMessage(null), 3000);
  };

  const handleAdminRefundAll = () => {
    setAdminStatusMessage(`Rambursare forțată: creditele au fost returnate celor ${activeLead.claimedPros.length} profesioniști.`);
    setTimeout(() => setAdminStatusMessage(null), 4000);
  };

  return (
    <div className="h-full overflow-y-auto p-6 space-y-6">
      {/* Top Header */}
      <div className="border-b border-slate-200/90 dark:border-slate-800 pb-5">
        <div className="flex flex-wrap items-center justify-between gap-2 mb-2">
          <div className="flex items-center gap-2 text-xs">
            <Badge variant="indigo">{activeLead.category}</Badge>
            <span className="text-slate-400">·</span>
            <span className="text-slate-600 dark:text-slate-400 font-medium">{activeLead.subcategory}</span>
            <span className="text-slate-400">·</span>
            <span className="text-slate-400 font-mono text-[11px]">Publicat {activeLead.createdAt}</span>
          </div>

          <div className="flex items-center gap-2">
            <span className="text-xs text-slate-600 dark:text-slate-400 flex items-center gap-1 font-mono font-medium">
              <Users className="w-3.5 h-3.5 text-indigo-500" />
              {activeLead.claimedPros.length} / {activeLead.maxPros} profesioniști acceptați
            </span>
          </div>
        </div>

        <h2 className="text-xl font-extrabold text-slate-900 dark:text-white leading-snug">
          {activeLead.title}
        </h2>

        {/* Primary Meta Strip */}
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-3 mt-4 text-xs">
          <div className="p-3.5 rounded-xl bg-gradient-to-br from-indigo-50/80 to-white dark:from-slate-900 dark:to-slate-900 border border-indigo-100 dark:border-slate-800 flex items-center gap-3 shadow-2xs">
            <div className="w-8 h-8 rounded-lg bg-indigo-500/10 text-indigo-600 dark:text-indigo-400 flex items-center justify-center shrink-0">
              <MapPin className="w-4 h-4" />
            </div>
            <div>
              <span className="text-slate-400 block text-[10px] uppercase font-semibold">Locație solicitată</span>
              <span className="font-bold text-slate-900 dark:text-slate-100">{activeLead.location}</span>
            </div>
          </div>

          <div className="p-3.5 rounded-xl bg-gradient-to-br from-emerald-50/80 to-white dark:from-slate-900 dark:to-slate-900 border border-emerald-100 dark:border-slate-800 flex items-center gap-3 shadow-2xs">
            <div className="w-8 h-8 rounded-lg bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 flex items-center justify-center shrink-0">
              <Wallet className="w-4 h-4" />
            </div>
            <div>
              <span className="text-slate-400 block text-[10px] uppercase font-semibold">Buget orientativ</span>
              <span className="font-bold text-slate-900 dark:text-slate-100 font-mono tabular-nums">
                {activeLead.budget.min.toLocaleString()} - {activeLead.budget.max.toLocaleString()} {activeLead.budget.currency}
              </span>
            </div>
          </div>

          <div className="p-3.5 rounded-xl bg-gradient-to-br from-amber-50/80 to-white dark:from-slate-900 dark:to-slate-900 border border-amber-100 dark:border-slate-800 flex items-center gap-3 shadow-2xs">
            <div className="w-8 h-8 rounded-lg bg-amber-500/10 text-amber-600 dark:text-amber-400 flex items-center justify-center shrink-0">
              <Calendar className="w-4 h-4" />
            </div>
            <div>
              <span className="text-slate-400 block text-[10px] uppercase font-semibold">Termen de execuție</span>
              <span className="font-bold text-slate-900 dark:text-slate-100">{activeLead.timeline}</span>
            </div>
          </div>
        </div>
      </div>

      {/* Description */}
      <div>
        <h3 className="text-xs font-bold text-slate-800 dark:text-slate-200 uppercase tracking-wider mb-2">
          Descrierea Proiectului
        </h3>
        <p className="text-xs sm:text-sm text-slate-600 dark:text-slate-300 leading-relaxed bg-white dark:bg-slate-900/60 p-4 rounded-2xl border border-slate-200/80 dark:border-slate-800">
          {activeLead.description}
        </p>
      </div>

      {/* AI Lead Passport Specification */}
      <LeadPassportCard
        passport={activeLead.leadPassport}
        isUnlocked={isClaimed}
        leadPrice={activeLead.leadPrice}
      />

      {/* Explainable Semantic Matching Card (for Pro view) */}
      {userRole === 'pro' && activeLead.matchExplanation && (
        <ExplainableMatchCard match={activeLead.matchExplanation} />
      )}

      {/* CLIENT ROLE VIEW: Offers Received & Direct Pro Contact */}
      {userRole === 'client' && (
        <div className="p-5 rounded-2xl bg-white dark:bg-slate-900 border border-emerald-200/90 dark:border-emerald-900/60 shadow-xs space-y-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <div className="w-8 h-8 rounded-xl bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 flex items-center justify-center">
                <UserCheck className="w-4 h-4" />
              </div>
              <div>
                <h4 className="text-xs font-bold text-slate-900 dark:text-white uppercase tracking-wider">
                  Profesioniști Înscriși pe Solicitarea Ta
                </h4>
                <p className="text-xs text-slate-500">
                  {activeLead.claimedPros.length} specialiști autorizați au acces la datele tale de contact
                </p>
              </div>
            </div>

            <Badge variant="emerald">{activeLead.claimedPros.length} / {activeLead.maxPros} Locuri Ocupate</Badge>
          </div>

          <div className="space-y-2.5 pt-2">
            {activeLead.claimedPros.map((pro, index) => (
              <div
                key={pro.proId || index}
                className="p-3.5 rounded-xl bg-slate-50 dark:bg-slate-950/60 border border-slate-200/90 dark:border-slate-800 flex items-center justify-between gap-3"
              >
                <div className="flex items-center gap-3">
                  <div className="w-9 h-9 rounded-full bg-indigo-600 text-white font-bold text-xs flex items-center justify-center">
                    {pro.proName.charAt(0)}
                  </div>
                  <div>
                    <h5 className="text-xs font-bold text-slate-900 dark:text-white flex items-center gap-1.5">
                      {pro.proName}
                      <span className="text-[10px] font-bold text-indigo-600 dark:text-indigo-400 bg-indigo-50 dark:bg-indigo-950 px-1.5 py-0.5 rounded">
                        Autorizat ANRE
                      </span>
                    </h5>
                    <p className="text-[11px] text-slate-500">
                      Înscris la {pro.claimedAt} · Scor Calitate: 99.4%
                    </p>
                  </div>
                </div>

                <div className="flex items-center gap-2">
                  <Button
                    variant="primary"
                    size="sm"
                    icon={<MessageSquare className="w-3.5 h-3.5" />}
                    onClick={() => onOpenChat(activeLead.id)}
                  >
                    Conversație Directă
                  </Button>
                </div>
              </div>
            ))}
          </div>

          <div className="flex items-center justify-between pt-2 border-t border-slate-100 dark:border-slate-800 text-xs">
            <span className="text-slate-500">
              Bugetul tău este garantat prin sistemul de escrow Skillink.
            </span>
            <Button
              variant="outline"
              size="sm"
              icon={<AlertTriangle className="w-3.5 h-3.5 text-amber-500" />}
              onClick={() => onOpenDispute(activeLead.id)}
            >
              Semnalează Problemă
            </Button>
          </div>
        </div>
      )}

      {/* ADMIN ROLE VIEW: Security Gate & Audit Override */}
      {userRole === 'admin' && (
        <div className="p-5 rounded-2xl bg-purple-50/50 dark:bg-purple-950/30 border border-purple-200 dark:border-purple-800 space-y-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <ShieldCheck className="w-5 h-5 text-purple-600 dark:text-purple-400" />
              <div>
                <h4 className="text-xs font-bold text-purple-900 dark:text-purple-200 uppercase tracking-wider">
                  Panou Audit & Securitate Lead (Admin Mode)
                </h4>
                <p className="text-xs text-purple-700/80 dark:text-purple-300/80">
                  ID Tehnic: <span className="font-mono font-bold">{activeLead.id}</span> · Risc Fraudă Contact: <span className="text-emerald-600 font-bold">0.1% (Minim)</span>
                </p>
              </div>
            </div>
            <span className="text-[11px] font-mono font-bold px-2.5 py-1 rounded bg-purple-200/70 dark:bg-purple-900 text-purple-900 dark:text-purple-100">
              Cost: {activeLead.leadPrice} Credite
            </span>
          </div>

          {/* Contact Details (Always unmasked for Admin) */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 text-xs bg-white dark:bg-slate-900 p-3.5 rounded-xl border border-purple-200 dark:border-purple-800">
            <div className="flex items-center gap-2">
              <Phone className="w-4 h-4 text-purple-600" />
              <div>
                <span className="text-slate-400 block text-[10px]">Telefon Client (Verificat SMS)</span>
                <span className="font-mono font-bold text-slate-900 dark:text-white">{activeLead.client.phone}</span>
              </div>
            </div>
            <div className="flex items-center gap-2">
              <Mail className="w-4 h-4 text-purple-600" />
              <div>
                <span className="text-slate-400 block text-[10px]">Email Client</span>
                <span className="font-mono font-bold text-slate-900 dark:text-white truncate block max-w-[200px]">{activeLead.client.email}</span>
              </div>
            </div>
          </div>

          {adminStatusMessage && (
            <div className="p-3 rounded-xl bg-purple-100 dark:bg-purple-900/80 text-purple-900 dark:text-purple-100 text-xs font-semibold flex items-center gap-2">
              <Check className="w-4 h-4 text-purple-700 dark:text-purple-300" />
              <span>{adminStatusMessage}</span>
            </div>
          )}

          {/* Admin Override Action Buttons */}
          <div className="flex flex-wrap items-center gap-2.5 pt-1">
            <Button
              variant="primary"
              size="sm"
              icon={<ShieldCheck className="w-3.5 h-3.5" />}
              onClick={handleAdminApprove}
            >
              Confirmă Validare & Calitate
            </Button>
            <Button
              variant="outline"
              size="sm"
              icon={<ShieldAlert className="w-3.5 h-3.5 text-rose-500" />}
              onClick={handleAdminRefundAll}
            >
              Forțează Rambursare Credite ({activeLead.claimedPros.length} Pros)
            </Button>
          </div>
        </div>
      )}

      {/* PRO ROLE VIEW: Monetization & Unlock Gate */}
      {userRole === 'pro' && (
        <div className="p-5 rounded-2xl bg-white dark:bg-slate-900 border border-indigo-100/80 dark:border-slate-800 shadow-xs space-y-4">
          <div className="flex items-center justify-between">
            <h3 className="text-xs font-bold text-slate-800 dark:text-slate-200 uppercase tracking-wider flex items-center gap-2">
              {isClaimed ? (
                <>
                  <Unlock className="w-4 h-4 text-emerald-500" />
                  <span>Date Directe de Contact (Deblocat)</span>
                </>
              ) : (
                <>
                  <Lock className="w-4 h-4 text-indigo-500" />
                  <span>Date Directe de Contact (Protejat prin Credite)</span>
                </>
              )}
            </h3>

            {!isClaimed && (
              <span className="text-[11px] font-mono font-bold text-indigo-600 dark:text-indigo-400 bg-indigo-50 dark:bg-indigo-950/80 px-2.5 py-1 rounded-lg border border-indigo-200/80 dark:border-indigo-900">
                Cost: {activeLead.leadPrice} Credite
              </span>
            )}
          </div>

          {/* Contact Details Pane */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <div className="p-3.5 rounded-xl bg-slate-50 dark:bg-slate-950/60 border border-slate-200/80 dark:border-slate-800 flex items-center gap-3">
              <div className="w-9 h-9 rounded-xl bg-indigo-100 dark:bg-indigo-950/80 text-indigo-600 dark:text-indigo-400 flex items-center justify-center shrink-0">
                <Phone className="w-4 h-4" />
              </div>
              <div>
                <span className="text-slate-400 block text-[10px] uppercase font-semibold">Telefon client</span>
                <span className="font-mono font-bold text-slate-900 dark:text-white text-xs sm:text-sm">
                  {isClaimed ? activeLead.client.phone : '+40 7** *** ***'}
                </span>
              </div>
            </div>

            <div className="p-3.5 rounded-xl bg-slate-50 dark:bg-slate-950/60 border border-slate-200/80 dark:border-slate-800 flex items-center gap-3">
              <div className="w-9 h-9 rounded-xl bg-indigo-100 dark:bg-indigo-950/80 text-indigo-600 dark:text-indigo-400 flex items-center justify-center shrink-0">
                <Mail className="w-4 h-4" />
              </div>
              <div>
                <span className="text-slate-400 block text-[10px] uppercase font-semibold">Email client</span>
                <span className="font-mono font-bold text-slate-900 dark:text-white text-xs sm:text-sm truncate block max-w-[220px]">
                  {isClaimed ? activeLead.client.email : 'c******@*******.ro'}
                </span>
              </div>
            </div>
          </div>

          {/* Feedback message */}
          {unlockFeedback && (
            <div
              className={`p-3.5 rounded-xl text-xs mb-4 flex items-center gap-2 ${
                unlockFeedback.success
                  ? 'bg-emerald-50 dark:bg-emerald-950/50 border border-emerald-200 dark:border-emerald-800 text-emerald-800 dark:text-emerald-300'
                  : 'bg-rose-50 dark:bg-rose-950/50 border border-rose-200 dark:border-rose-800 text-rose-800 dark:text-rose-300'
              }`}
            >
              {unlockFeedback.success ? (
                <CheckCircle2 className="w-4 h-4 text-emerald-500 shrink-0" />
              ) : (
                <AlertTriangle className="w-4 h-4 text-rose-500 shrink-0" />
              )}
              <span>{unlockFeedback.message}</span>
            </div>
          )}

          {/* Action Buttons using UI primitives */}
          <div className="flex flex-wrap items-center justify-between gap-3 pt-2">
            {isClaimed ? (
              <div className="flex flex-wrap items-center gap-3 w-full sm:w-auto">
                <Button
                  variant="primary"
                  icon={<MessageSquare className="w-4 h-4" />}
                  onClick={() => onOpenChat(activeLead.id)}
                >
                  Deschide Chat în Timp Real
                </Button>

                <Button
                  variant="outline"
                  icon={<AlertTriangle className="w-4 h-4 text-amber-500" />}
                  onClick={() => onOpenDispute(activeLead.id)}
                >
                  Contestă Lead (Sesizare)
                </Button>
              </div>
            ) : (
              <div className="flex flex-wrap items-center justify-between gap-3 w-full">
                <div className="text-xs text-slate-600 dark:text-slate-400">
                  Soldul tău curent:{' '}
                  <span className="font-mono font-extrabold text-slate-900 dark:text-white tabular-nums">
                    {proProfile.creditsBalance} credite
                  </span>
                </div>

                <Button
                  variant="success"
                  size="md"
                  disabled={isFull}
                  isLoading={isUnlocking}
                  icon={<Unlock className="w-4 h-4" />}
                  onClick={handleUnlock}
                >
                  {isFull ? 'Limită profesioniști atinsă' : `Cumpără Lead-ul (${activeLead.leadPrice} Credite)`}
                </Button>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
};
