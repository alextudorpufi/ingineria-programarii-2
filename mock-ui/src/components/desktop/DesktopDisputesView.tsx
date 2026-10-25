import React, { useState } from 'react';
import { useApp } from '../../context/AppContext';
import { DisputeRecord } from '../../types';
import { AlertTriangle, CheckCircle2, XCircle, Plus, X } from 'lucide-react';
import { Button } from '../ui/Button';
import { Badge } from '../ui/Badge';
import { DESIGN_TOKENS } from '../../theme/designSystem';

export const DesktopDisputesView: React.FC = () => {
  const { disputes, resolveDispute, fileDispute, leads, isLeadClaimedByMe, userRole } = useApp();

  const [selectedDisputeId, setSelectedDisputeId] = useState<string | null>(disputes[0]?.id || null);
  const [adminNote, setAdminNote] = useState('');
  const [showFileModal, setShowFileModal] = useState(false);

  // Form state for filing dispute
  const [newDisputeLeadId, setNewDisputeLeadId] = useState<string>('');
  const [newDisputeReason, setNewDisputeReason] = useState<DisputeRecord['reason']>('unreachable_contact');
  const [newDisputeDetails, setNewDisputeDetails] = useState('');

  const claimedLeads = leads.filter(l => isLeadClaimedByMe(l.id));
  const selectedDispute = disputes.find(d => d.id === selectedDisputeId) || disputes[0] || null;

  const handleCreateDispute = (e: React.FormEvent) => {
    e.preventDefault();
    if (!newDisputeLeadId || !newDisputeDetails.trim()) return;

    fileDispute({
      leadId: newDisputeLeadId,
      reason: newDisputeReason,
      details: newDisputeDetails
    });

    setShowFileModal(false);
    setNewDisputeDetails('');
  };

  const handleAdminApprove = () => {
    if (!selectedDispute) return;
    resolveDispute(
      selectedDispute.id,
      'aprobat',
      adminNote || 'Rambursare aprobată conform politicii de transparență și calificare a lead-urilor.'
    );
    setAdminNote('');
  };

  const handleAdminReject = () => {
    if (!selectedDispute) return;
    resolveDispute(
      selectedDispute.id,
      'respins',
      adminNote || 'Sesizare respinsă. Datele de contact și specificațiile au fost verificate ca fiind valide.'
    );
    setAdminNote('');
  };

  return (
    <div className="h-full flex flex-col bg-slate-50/50 dark:bg-slate-950 overflow-hidden">
      {/* Top Bar */}
      <div className="px-6 py-4 bg-white/90 dark:bg-slate-900/90 backdrop-blur-xs border-b border-amber-100 dark:border-slate-800 flex items-center justify-between">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-2xl bg-gradient-to-tr from-amber-500 to-orange-500 text-white flex items-center justify-center shadow-xs">
            <AlertTriangle className="w-5 h-5" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <h2 className="text-base font-bold text-slate-900 dark:text-white">
                Sesizări și Soluționare Dispute
              </h2>
              <Badge variant="amber">Audit & Rambursare Credite</Badge>
            </div>
            <p className="text-xs text-slate-500 dark:text-slate-400">
              Profesioniștii pot contesta lead-urile neconforme pentru restituirea transparentă a creditelor.
            </p>
          </div>
        </div>

        {(userRole === 'pro' || userRole === 'client') && (
          <Button
            variant="primary"
            size="sm"
            icon={<Plus className="w-4 h-4" />}
            onClick={() => {
              const available = userRole === 'client' ? leads : claimedLeads;
              if (available.length > 0) {
                setNewDisputeLeadId(available[0].id);
                setShowFileModal(true);
              } else {
                setNewDisputeLeadId(leads[0]?.id || 'lead-101');
                setShowFileModal(true);
              }
            }}
          >
            {userRole === 'client' ? 'Sesizare Calitate / Execuție' : 'Deschide Sesizare Nouă'}
          </Button>
        )}
      </div>

      {/* Main Split Grid */}
      <div className="flex-1 grid grid-cols-1 md:grid-cols-12 overflow-hidden">
        {/* Left: Dispute Records List */}
        <div className="md:col-span-5 border-r border-slate-200/90 dark:border-slate-800 bg-white dark:bg-slate-900 flex flex-col h-full overflow-y-auto divide-y divide-slate-100 dark:divide-slate-800">
          <div className="p-3.5 bg-gradient-to-r from-amber-50/60 to-white dark:from-slate-900 dark:to-slate-900 text-xs text-slate-700 dark:text-slate-300 font-bold border-b border-slate-200/90 dark:border-slate-800 flex items-center justify-between">
            <span>Coada Sesizărilor ({disputes.length})</span>
            <span className="text-[11px] text-amber-600 font-medium">Timp mediu soluționare: &lt; 2h</span>
          </div>

          {disputes.map(disp => {
            const isSelected = selectedDispute?.id === disp.id;
            return (
              <div
                key={disp.id}
                onClick={() => setSelectedDisputeId(disp.id)}
                className={`p-4 cursor-pointer transition-all duration-150 ${
                  isSelected
                    ? 'bg-gradient-to-r from-amber-50/80 via-orange-50/30 to-white dark:from-amber-950/40 dark:via-slate-900 dark:to-slate-900 border-l-4 border-amber-500'
                    : 'hover:bg-amber-50/30 dark:hover:bg-slate-900'
                }`}
              >
                <div className="flex items-center justify-between text-xs mb-1">
                  <span className="font-bold text-slate-900 dark:text-white">
                    {disp.proName}
                  </span>
                  <Badge
                    variant={disp.status === 'aprobat' ? 'emerald' : disp.status === 'respins' ? 'rose' : 'amber'}
                  >
                    {disp.status === 'aprobat' ? 'Rambursat' : disp.status === 'respins' ? 'Respins' : 'În Analiză'}
                  </Badge>
                </div>

                <p className="text-xs text-slate-700 dark:text-slate-300 font-medium line-clamp-1 mb-1">
                  {disp.leadTitle}
                </p>

                <div className="flex items-center justify-between text-[11px] text-slate-400">
                  <span className="font-mono font-bold text-amber-600 dark:text-amber-400">
                    Cerere: +{disp.creditsRequested} credite
                  </span>
                  <span>{disp.createdAt}</span>
                </div>
              </div>
            );
          })}
        </div>

        {/* Right: Dispute Inspection & Admin Adjudication */}
        <div className="md:col-span-7 p-6 overflow-y-auto bg-slate-50/60 dark:bg-slate-950 flex flex-col justify-between">
          {selectedDispute ? (
            <div className="space-y-5">
              <div className="p-5 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200/90 dark:border-slate-800 space-y-3 shadow-xs">
                <div className="flex items-center justify-between pb-3 border-b border-slate-100 dark:border-slate-800">
                  <div>
                    <span className="text-[10px] text-slate-400 font-mono block">ID Sesizare: {selectedDispute.id}</span>
                    <h3 className="text-sm font-bold text-slate-900 dark:text-white">
                      {selectedDispute.leadTitle}
                    </h3>
                  </div>

                  <span className="font-mono font-extrabold text-amber-600 dark:text-amber-400 text-sm bg-amber-50 dark:bg-amber-950/60 px-2.5 py-1 rounded-xl border border-amber-200/80 dark:border-amber-900">
                    +{selectedDispute.creditsRequested} Credite disputate
                  </span>
                </div>

                <div className="grid grid-cols-2 gap-3 text-xs">
                  <div>
                    <span className="text-slate-400 block text-[10px] uppercase font-semibold">Profesionist contestatar</span>
                    <span className="font-bold text-slate-900 dark:text-slate-100">{selectedDispute.proName}</span>
                  </div>
                  <div>
                    <span className="text-slate-400 block text-[10px] uppercase font-semibold">Motiv sesizare</span>
                    <span className="font-bold text-slate-900 dark:text-slate-100">
                      {selectedDispute.reason === 'unreachable_contact' && 'Date contact invalide / nu răspunde'}
                      {selectedDispute.reason === 'wrong_category' && 'Categorie servicii greșită'}
                      {selectedDispute.reason === 'incompatible_budget' && 'Buget incompatibil cu complexitatea'}
                      {selectedDispute.reason === 'duplicate_or_fake' && 'Solicitare duplicată sau falsă'}
                    </span>
                  </div>
                </div>

                <div className="pt-2">
                  <span className="text-[10px] text-slate-400 block mb-1 uppercase font-semibold">
                    Explicație & Dovezi profesionist:
                  </span>
                  <p className="text-xs text-slate-700 dark:text-slate-300 bg-slate-50 dark:bg-slate-800/60 p-3.5 rounded-xl border border-slate-200/80 dark:border-slate-800 leading-relaxed">
                    "{selectedDispute.details}"
                  </p>
                </div>

                {selectedDispute.adminDecisionNote && (
                  <div className="p-3.5 rounded-xl bg-emerald-50 dark:bg-emerald-950/40 border border-emerald-200 dark:border-emerald-800 text-xs">
                    <span className="font-bold text-emerald-800 dark:text-emerald-300 block mb-0.5">
                      Decizie Administrator ({selectedDispute.resolvedAt}):
                    </span>
                    <p className="text-emerald-700 dark:text-emerald-200">{selectedDispute.adminDecisionNote}</p>
                  </div>
                )}
              </div>

              {/* Admin Decision Actions */}
              {userRole === 'admin' && selectedDispute.status === 'in_analiza' && (
                <div className="p-5 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200/90 dark:border-slate-800 space-y-3 shadow-xs">
                  <h4 className="text-xs font-bold text-slate-800 dark:text-slate-200 uppercase tracking-wider">
                    Panou Soluționare Administrator
                  </h4>

                  <textarea
                    rows={2}
                    placeholder="Introdu nota de justificare pentru profesionist..."
                    value={adminNote}
                    onChange={e => setAdminNote(e.target.value)}
                    className={DESIGN_TOKENS.inputs.textarea}
                  />

                  <div className="flex items-center gap-3">
                    <Button
                      variant="success"
                      icon={<CheckCircle2 className="w-4 h-4" />}
                      onClick={handleAdminApprove}
                    >
                      Aprobă Rambursarea (+{selectedDispute.creditsRequested} credite)
                    </Button>

                    <Button
                      variant="outline"
                      icon={<XCircle className="w-4 h-4 text-rose-500" />}
                      onClick={handleAdminReject}
                    >
                      Respinge Sesizarea
                    </Button>
                  </div>
                </div>
              )}
            </div>
          ) : (
            <div className="text-center text-xs text-slate-400 p-8">
              Selectează o sesizare pentru a vizualiza detaliile.
            </div>
          )}
        </div>
      </div>

      {/* File Dispute Modal */}
      {showFileModal && (
        <div className="fixed inset-0 z-50 bg-black/60 backdrop-blur-xs flex items-center justify-center p-4">
          <div className="w-full max-w-md bg-white dark:bg-slate-900 rounded-2xl border border-slate-200 dark:border-slate-800 p-6 space-y-4 shadow-xl">
            <div className="flex items-center justify-between pb-2 border-b border-slate-100 dark:border-slate-800">
              <h3 className="text-sm font-bold text-slate-900 dark:text-white flex items-center gap-2">
                <AlertTriangle className="w-4 h-4 text-amber-500" />
                Deschidere Sesizare Lead
              </h3>
              <button
                onClick={() => setShowFileModal(false)}
                className="text-slate-400 hover:text-slate-600 dark:hover:text-white text-sm cursor-pointer p-1"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            <form onSubmit={handleCreateDispute} className="space-y-3.5 text-xs">
              <div>
                <label className="block text-slate-700 dark:text-slate-300 font-semibold mb-1">
                  Selectează Lead-ul contestat:
                </label>
                <select
                  value={newDisputeLeadId}
                  onChange={e => setNewDisputeLeadId(e.target.value)}
                  className={DESIGN_TOKENS.inputs.select}
                >
                  {claimedLeads.map(l => (
                    <option key={l.id} value={l.id}>
                      {l.title.slice(0, 45)}... ({l.leadPrice} credite)
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-slate-700 dark:text-slate-300 font-semibold mb-1">
                  Motivul sesizării (conform Section 1.3 F):
                </label>
                <select
                  value={newDisputeReason}
                  onChange={e => setNewDisputeReason(e.target.value as DisputeRecord['reason'])}
                  className={DESIGN_TOKENS.inputs.select}
                >
                  <option value="unreachable_contact">Date de contact invalide sau clientul nu răspunde</option>
                  <option value="wrong_category">Solicitarea aparține unei categorii de servicii greșite</option>
                  <option value="incompatible_budget">Bugetul nu este compatibil cu complexitatea cerută</option>
                  <option value="duplicate_or_fake">Solicitarea este duplicată sau falsă</option>
                </select>
              </div>

              <div>
                <label className="block text-slate-700 dark:text-slate-300 font-semibold mb-1">
                  Detalii și dovezi:
                </label>
                <textarea
                  rows={3}
                  required
                  placeholder="Descrie pe scurt încercarea de contactare sau neconcordanța..."
                  value={newDisputeDetails}
                  onChange={e => setNewDisputeDetails(e.target.value)}
                  className={DESIGN_TOKENS.inputs.textarea}
                />
              </div>

              <div className="flex items-center justify-end gap-2.5 pt-2">
                <Button
                  type="button"
                  variant="outline"
                  size="sm"
                  onClick={() => setShowFileModal(false)}
                >
                  Anulează
                </Button>
                <Button
                  type="submit"
                  variant="primary"
                  size="sm"
                >
                  Trimite Sesizarea
                </Button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
