import React, { useState } from 'react';
import { useApp } from '../../context/AppContext';
import { RawRatingPayload } from '../../models/RatingProtocolModel';
import { Button } from '../ui/Button';
import { Badge } from '../ui/Badge';
import { DESIGN_TOKENS } from '../../theme/designSystem';
import {
  ShieldCheck,
  Star,
  CheckCircle2,
  Clock,
  Wallet,
  MapPin,
  Briefcase,
  Award,
  Sparkles,
  MessageSquare,
  Plus,
  X,
  FileCheck,
  ChevronRight,
  TrendingUp,
  UserCheck,
  Shield,
  Layers,
  Lock
} from 'lucide-react';

interface ProfileViewProps {
  onBackToFeed?: () => void;
}

export const ProfileView: React.FC<ProfileViewProps> = () => {
  const { proProfile, userRole, submitRating, leads } = useApp();
  const [showRatingModal, setShowRatingModal] = useState(false);

  // Modal form state
  const [reviewLeadId, setReviewLeadId] = useState(leads[0]?.id || 'lead-101');
  const [reviewerName, setReviewerName] = useState(
    userRole === 'client' ? 'Ing. Radu Teodorescu (Specialist)' : 'Elena Popescu (Beneficiar)'
  );
  const [reviewCity, setReviewCity] = useState('București');
  const [reviewDim1, setReviewDim1] = useState(5);
  const [reviewDim2, setReviewDim2] = useState(5);
  const [reviewDim3, setReviewDim3] = useState(5);
  const [reviewDim4, setReviewDim4] = useState(5);
  const [reviewComment, setReviewComment] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Dimension breakdown calculated directly by ProfileModel
  const breakdown = proProfile.dimensionBreakdown;

  // Dynamic dimension titles based on role
  const getDimensionNames = () => {
    if (userRole === 'client') {
      return {
        dim1: '1. Promptitudine Plată',
        desc1: 'Plată efectuată conform termenelor din contractul agreat.',
        dim2: '2. Claritate Cerințe',
        desc2: 'Cerințe bine formulate și documentate prin AI Architect.',
        dim3: '3. Respect Timp Specialiști',
        desc3: 'Acces garantat la șantier și decizii operative de execuție.',
        dim4: '4. Comunicare & Feedback',
        desc4: 'Dialog profesionist și deschis cu echipa tehnică.'
      };
    }
    if (userRole === 'admin') {
      return {
        dim1: '1. Neutralitate Arbitraj',
        desc1: 'Decizii obiective bazate exclusiv pe probe și chat.',
        dim2: '2. Timp de Soluționare',
        desc2: 'Sesizări rezolvate în medie sub 2 ore conform SLA.',
        dim3: '3. Integritate Proces',
        desc3: 'Verificare meticuloasă a numerelor și prevenirea fraudelor.',
        dim4: '4. Claritate Decizii',
        desc4: 'Motivare transparentă a soluționării fiecărui litigiu.'
      };
    }
    return {
      dim1: '1. Punctualitate',
      desc1: 'Respectarea termenelor-limită agreate în Lead Passport.',
      dim2: '2. Calitate Tehnică',
      desc2: 'Livrabile conform standardelor de siguranță și execuție ANRE.',
      dim3: '3. Comunicare',
      desc3: 'Transparență, devize clare și disponibilitate în chat.',
      dim4: '4. Respectare Buget',
      desc4: 'Zero costuri ascunse față de devizul contractual inițial.'
    };
  };

  const dimNames = getDimensionNames();

  const handleRatingSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!reviewComment.trim()) return;

    setIsSubmitting(true);
    const selectedLead = leads.find(l => l.id === reviewLeadId);

    const payload: RawRatingPayload = {
      proId: proProfile.id,
      leadId: reviewLeadId,
      leadTitle: selectedLead ? selectedLead.title : 'Contract Execuție Servicii',
      clientName: reviewerName,
      clientCity: reviewCity,
      scores: {
        timeliness: reviewDim1,
        quality: reviewDim2,
        communication: reviewDim3,
        budgetRespect: reviewDim4
      },
      comment: reviewComment,
      verifiedStatus: true
    };

    await submitRating(payload);
    setIsSubmitting(false);
    setShowRatingModal(false);
    setReviewComment('');
  };

  // Avatar initials
  const initials = proProfile.name
    .split(' ')
    .filter(Boolean)
    .map(w => w[0])
    .join('')
    .slice(0, 2);

  return (
    <div className="h-full overflow-y-auto p-4 sm:p-8 space-y-8 bg-slate-50/50 dark:bg-slate-950">
      {/* Profile Header Hero */}
      <div className="p-6 sm:p-8 rounded-3xl bg-white dark:bg-slate-900 border border-indigo-100 dark:border-slate-800 shadow-sm relative overflow-hidden">
        <div className="absolute top-0 right-0 w-80 h-80 bg-gradient-to-bl from-indigo-500/10 via-violet-500/10 to-transparent rounded-full pointer-events-none" />

        <div className="flex flex-col md:flex-row items-start md:items-center justify-between gap-6 relative">
          <div className="flex flex-col sm:flex-row items-start sm:items-center gap-5">
            {/* Avatar with Verified Ring */}
            <div className="relative">
              <div
                className={`w-20 h-20 sm:w-24 sm:h-24 rounded-3xl flex items-center justify-center text-white text-2xl font-extrabold shadow-md ${
                  userRole === 'client'
                    ? 'bg-gradient-to-tr from-emerald-600 to-teal-600'
                    : userRole === 'admin'
                    ? 'bg-gradient-to-tr from-purple-600 to-rose-600'
                    : 'bg-gradient-to-tr from-indigo-600 to-violet-600'
                }`}
              >
                {initials}
              </div>
              <div
                className={`absolute -bottom-1 -right-1 w-7 h-7 rounded-full text-white flex items-center justify-center ring-3 ring-white dark:ring-slate-900 shadow-xs ${
                  userRole === 'admin'
                    ? 'bg-purple-600'
                    : 'bg-emerald-500'
                }`}
              >
                {userRole === 'admin' ? (
                  <Shield className="w-4 h-4" />
                ) : (
                  <CheckCircle2 className="w-4 h-4" />
                )}
              </div>
            </div>

            {/* Profile Core Info */}
            <div className="space-y-1.5">
              <div className="flex flex-wrap items-center gap-2">
                <h1 className="text-xl sm:text-2xl font-extrabold text-slate-900 dark:text-white">
                  {proProfile.name}
                </h1>
                <Badge
                  variant={
                    userRole === 'client'
                      ? 'emerald'
                      : userRole === 'admin'
                      ? 'violet'
                      : 'indigo'
                  }
                >
                  {userRole === 'client'
                    ? 'Client Verificat Skillink'
                    : userRole === 'admin'
                    ? 'Super-Admin & Arbitraj'
                    : 'Profesionist Autorizat ANRE'}
                </Badge>
              </div>

              <p className="text-sm font-semibold text-indigo-600 dark:text-indigo-400">
                {proProfile.companyName} · <span className="text-slate-500 text-xs font-normal">{proProfile.title}</span>
              </p>

              <p className="text-xs text-slate-600 dark:text-slate-300 max-w-xl leading-relaxed pt-1">
                {proProfile.bio}
              </p>

              <div className="flex flex-wrap items-center gap-3 pt-2 text-xs text-slate-500 dark:text-slate-400">
                <span className="flex items-center gap-1 font-medium">
                  <MapPin className="w-3.5 h-3.5 text-indigo-500" />
                  {proProfile.coverageArea.join(' · ')}
                </span>
                <span>·</span>
                <span className="flex items-center gap-1 font-medium">
                  <Clock className="w-3.5 h-3.5 text-emerald-500" />
                  Timp răspuns: {proProfile.responseTime}
                </span>
                <span>·</span>
                <span>Membru din {proProfile.memberSince}</span>
              </div>
            </div>
          </div>

          {/* Quick Metrics & Actions */}
          <div className="flex flex-row md:flex-col items-center md:items-end justify-between w-full md:w-auto gap-4 pt-4 md:pt-0 border-t md:border-t-0 border-slate-100 dark:border-slate-800">
            <div className="text-left md:text-right">
              {userRole === 'pro' && (
                <>
                  <span className="text-[10px] text-slate-400 uppercase font-bold tracking-wider block">
                    Tarif Orientativ
                  </span>
                  <span className="text-xl font-mono font-extrabold text-slate-900 dark:text-white tabular-nums">
                    {proProfile.hourlyRate} RON<span className="text-xs font-normal text-slate-400">/oră</span>
                  </span>
                </>
              )}

              {userRole === 'client' && (
                <>
                  <span className="text-[10px] text-slate-400 uppercase font-bold tracking-wider block">
                    Buget Total Proiecte
                  </span>
                  <span className="text-xl font-mono font-extrabold text-emerald-600 dark:text-emerald-400 tabular-nums">
                    {proProfile.clientBudget.toLocaleString()} RON
                  </span>
                </>
              )}

              {userRole === 'admin' && (
                <>
                  <span className="text-[10px] text-slate-400 uppercase font-bold tracking-wider block">
                    Audituri & Arbitraje
                  </span>
                  <span className="text-xl font-mono font-extrabold text-purple-600 dark:text-purple-400 tabular-nums">
                    {proProfile.completedJobs} Finalizate
                  </span>
                </>
              )}
            </div>

            <Button
              variant="primary"
              size="sm"
              icon={<Star className="w-4 h-4 text-amber-300 fill-amber-300" />}
              onClick={() => setShowRatingModal(true)}
            >
              {userRole === 'client'
                ? 'Evaluează Clientul'
                : userRole === 'admin'
                ? 'Înregistrează Notă Audit'
                : 'Adaugă Evaluare Verificată'}
            </Button>
          </div>
        </div>

        {/* Dynamic Key Stats Strip */}
        {proProfile.stats && Object.keys(proProfile.stats).length > 0 && (
          <div className="mt-6 pt-5 border-t border-slate-100 dark:border-slate-800 grid grid-cols-2 sm:grid-cols-4 gap-3">
            {Object.entries(proProfile.stats).map(([k, v]) => (
              <div key={k} className="p-3 rounded-2xl bg-slate-50 dark:bg-slate-800/60 border border-slate-200/80 dark:border-slate-800 text-center">
                <span className="text-[10px] text-slate-400 uppercase font-bold block">{k}</span>
                <span className="text-base font-extrabold font-mono text-slate-900 dark:text-white mt-0.5 block">{v}</span>
              </div>
            ))}
          </div>
        )}

        {/* Verification Seals & Trust Markers */}
        <div className="mt-4 pt-4 border-t border-slate-100 dark:border-slate-800 grid grid-cols-1 sm:grid-cols-3 gap-3">
          {proProfile.certifications.map((cert, idx) => (
            <div
              key={idx}
              className="flex items-center gap-2.5 p-2.5 rounded-xl bg-slate-50 dark:bg-slate-800/60 border border-slate-200/80 dark:border-slate-800 text-xs"
            >
              <div className="w-6 h-6 rounded-lg bg-emerald-500/10 text-emerald-600 flex items-center justify-center shrink-0">
                <Award className="w-3.5 h-3.5" />
              </div>
              <span className="font-semibold text-slate-800 dark:text-slate-200">{cert}</span>
            </div>
          ))}
        </div>
      </div>

      {/* RATING PROTOCOL SECTION (Key Proposal & User Requirement) */}
      <div className="space-y-6">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <div>
            <div className="flex items-center gap-2">
              <h2 className="text-lg font-extrabold text-slate-900 dark:text-white">
                Protocol de Evaluare & Rating Verificat
              </h2>
              <Badge variant="emerald">Audit Anti-Fraudă</Badge>
            </div>
            <p className="text-xs text-slate-500 dark:text-slate-400 mt-0.5">
              Toate evaluările sunt ancorate strict în contracte atribuite prin platformă (zero recenzii plătite sau neverificate).
            </p>
          </div>

          <div className="flex items-center gap-2">
            <span className="text-xs font-semibold text-slate-600 dark:text-slate-400">
              Scor Încredere (Trust Index):
            </span>
            <span className="font-mono font-extrabold text-emerald-600 dark:text-emerald-400 text-base tabular-nums bg-emerald-50 dark:bg-emerald-950/60 px-2.5 py-0.5 rounded-xl border border-emerald-200/80 dark:border-emerald-800">
              {proProfile.trustScore}%
            </span>
          </div>
        </div>

        {/* 4-Dimension Breakdown Matrix Adapting to Role */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          <div className="p-4 rounded-2xl bg-white dark:bg-slate-900 border border-indigo-100 dark:border-slate-800 shadow-2xs space-y-2">
            <div className="flex items-center justify-between">
              <span className="text-[11px] font-bold text-slate-400 uppercase tracking-wider">
                {dimNames.dim1}
              </span>
              <div className="flex items-center gap-1 text-amber-500 font-mono font-bold text-sm">
                <Star className="w-3.5 h-3.5 fill-current" />
                <span>{breakdown.timeliness}</span>
              </div>
            </div>
            <p className="text-[11px] text-slate-600 dark:text-slate-400">
              {dimNames.desc1}
            </p>
            <div className="w-full h-1.5 bg-slate-100 dark:bg-slate-800 rounded-full overflow-hidden">
              <div
                className="h-full bg-gradient-to-r from-indigo-500 to-violet-500 rounded-full"
                style={{ width: `${(breakdown.timeliness / 5) * 100}%` }}
              />
            </div>
          </div>

          <div className="p-4 rounded-2xl bg-white dark:bg-slate-900 border border-indigo-100 dark:border-slate-800 shadow-2xs space-y-2">
            <div className="flex items-center justify-between">
              <span className="text-[11px] font-bold text-slate-400 uppercase tracking-wider">
                {dimNames.dim2}
              </span>
              <div className="flex items-center gap-1 text-amber-500 font-mono font-bold text-sm">
                <Star className="w-3.5 h-3.5 fill-current" />
                <span>{breakdown.quality}</span>
              </div>
            </div>
            <p className="text-[11px] text-slate-600 dark:text-slate-400">
              {dimNames.desc2}
            </p>
            <div className="w-full h-1.5 bg-slate-100 dark:bg-slate-800 rounded-full overflow-hidden">
              <div
                className="h-full bg-gradient-to-r from-emerald-500 to-teal-500 rounded-full"
                style={{ width: `${(breakdown.quality / 5) * 100}%` }}
              />
            </div>
          </div>

          <div className="p-4 rounded-2xl bg-white dark:bg-slate-900 border border-indigo-100 dark:border-slate-800 shadow-2xs space-y-2">
            <div className="flex items-center justify-between">
              <span className="text-[11px] font-bold text-slate-400 uppercase tracking-wider">
                {dimNames.dim3}
              </span>
              <div className="flex items-center gap-1 text-amber-500 font-mono font-bold text-sm">
                <Star className="w-3.5 h-3.5 fill-current" />
                <span>{breakdown.communication}</span>
              </div>
            </div>
            <p className="text-[11px] text-slate-600 dark:text-slate-400">
              {dimNames.desc3}
            </p>
            <div className="w-full h-1.5 bg-slate-100 dark:bg-slate-800 rounded-full overflow-hidden">
              <div
                className="h-full bg-gradient-to-r from-amber-500 to-orange-500 rounded-full"
                style={{ width: `${(breakdown.communication / 5) * 100}%` }}
              />
            </div>
          </div>

          <div className="p-4 rounded-2xl bg-white dark:bg-slate-900 border border-indigo-100 dark:border-slate-800 shadow-2xs space-y-2">
            <div className="flex items-center justify-between">
              <span className="text-[11px] font-bold text-slate-400 uppercase tracking-wider">
                {dimNames.dim4}
              </span>
              <div className="flex items-center gap-1 text-amber-500 font-mono font-bold text-sm">
                <Star className="w-3.5 h-3.5 fill-current" />
                <span>{breakdown.budgetRespect}</span>
              </div>
            </div>
            <p className="text-[11px] text-slate-600 dark:text-slate-400">
              {dimNames.desc4}
            </p>
            <div className="w-full h-1.5 bg-slate-100 dark:bg-slate-800 rounded-full overflow-hidden">
              <div
                className="h-full bg-gradient-to-r from-violet-500 to-fuchsia-500 rounded-full"
                style={{ width: `${(breakdown.budgetRespect / 5) * 100}%` }}
              />
            </div>
          </div>
        </div>

        {/* Verified Reviews Feed */}
        <div className="space-y-4">
          <div className="flex items-center justify-between">
            <h3 className="text-sm font-bold text-slate-900 dark:text-white flex items-center gap-2">
              <FileCheck className="w-4 h-4 text-emerald-500" />
              Recenzii & Confirmări Verificate ({proProfile.ratings.length})
            </h3>
            <span className="text-xs text-slate-400">
              Medie Generală: <strong className="text-slate-900 dark:text-white font-mono">{proProfile.averageRating} / 5.0</strong>
            </span>
          </div>

          <div className="space-y-3">
            {proProfile.ratings.map(review => (
              <div
                key={review.id}
                className="p-5 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200/90 dark:border-slate-800 space-y-3 shadow-2xs"
              >
                <div className="flex flex-wrap items-center justify-between gap-2 pb-2.5 border-b border-slate-100 dark:border-slate-800">
                  <div>
                    <div className="flex items-center gap-2">
                      <span className="font-bold text-slate-900 dark:text-white text-sm">
                        {review.clientName}
                      </span>
                      <span className="text-xs text-slate-400">({review.clientCity})</span>
                      <Badge variant="emerald" icon={<ShieldCheck className="w-3 h-3 text-emerald-600" />}>
                        Lead Verificat #{review.leadId.slice(0, 8)}
                      </Badge>
                    </div>
                    <span className="text-xs text-indigo-600 dark:text-indigo-400 font-medium block mt-0.5">
                      Proiect: {review.leadTitle}
                    </span>
                  </div>

                  <div className="flex items-center gap-2">
                    <div className="flex items-center gap-1 text-amber-500 font-mono font-bold text-sm bg-amber-50 dark:bg-amber-950/60 px-2.5 py-1 rounded-xl border border-amber-200/80">
                      <Star className="w-3.5 h-3.5 fill-current" />
                      <span>{review.overallScore}</span>
                    </div>
                    <span className="text-[11px] text-slate-400 font-mono">{review.createdAt}</span>
                  </div>
                </div>

                {/* Individual Dimension Chips */}
                <div className="grid grid-cols-2 sm:grid-cols-4 gap-2 text-[11px]">
                  <div className="p-2 rounded-xl bg-slate-50 dark:bg-slate-800/50 flex items-center justify-between">
                    <span className="text-slate-500">P1:</span>
                    <span className="font-mono font-bold text-amber-500">{review.scores.timeliness}★</span>
                  </div>
                  <div className="p-2 rounded-xl bg-slate-50 dark:bg-slate-800/50 flex items-center justify-between">
                    <span className="text-slate-500">P2:</span>
                    <span className="font-mono font-bold text-amber-500">{review.scores.quality}★</span>
                  </div>
                  <div className="p-2 rounded-xl bg-slate-50 dark:bg-slate-800/50 flex items-center justify-between">
                    <span className="text-slate-500">P3:</span>
                    <span className="font-mono font-bold text-amber-500">{review.scores.communication}★</span>
                  </div>
                  <div className="p-2 rounded-xl bg-slate-50 dark:bg-slate-800/50 flex items-center justify-between">
                    <span className="text-slate-500">P4:</span>
                    <span className="font-mono font-bold text-amber-500">{review.scores.budgetRespect}★</span>
                  </div>
                </div>

                {/* Written Testimonial */}
                <p className="text-xs sm:text-sm text-slate-700 dark:text-slate-300 leading-relaxed italic bg-slate-50/60 dark:bg-slate-800/30 p-3 rounded-xl">
                  "{review.comment}"
                </p>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Interactive Review Modal */}
      {showRatingModal && (
        <div className="fixed inset-0 z-50 bg-slate-900/60 backdrop-blur-xs flex items-center justify-center p-4">
          <div className="bg-white dark:bg-slate-900 rounded-3xl border border-indigo-100 dark:border-slate-800 shadow-2xl max-w-lg w-full overflow-hidden flex flex-col max-h-[90vh]">
            <div className="p-5 border-b border-slate-100 dark:border-slate-800 flex items-center justify-between">
              <div className="flex items-center gap-2">
                <div className="w-8 h-8 rounded-xl bg-gradient-to-tr from-indigo-600 to-violet-600 text-white flex items-center justify-center">
                  <Star className="w-4 h-4 fill-white" />
                </div>
                <div>
                  <h3 className="text-sm font-bold text-slate-900 dark:text-white">
                    {userRole === 'client'
                      ? 'Adaugă Evaluare Client (Pro către Client)'
                      : userRole === 'admin'
                      ? 'Adaugă Notă Audit Platformă'
                      : 'Adaugă Evaluare Verificată (Client către Pro)'}
                  </h3>
                  <span className="text-[11px] text-slate-400">Protocol de Rating în 4 Dimensiuni</span>
                </div>
              </div>

              <button
                onClick={() => setShowRatingModal(false)}
                className="p-1 text-slate-400 hover:text-slate-600 dark:hover:text-white cursor-pointer"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            <form onSubmit={handleRatingSubmit} className="p-6 overflow-y-auto space-y-4 text-xs">
              <div>
                <label className="block text-slate-700 dark:text-slate-300 font-semibold mb-1">
                  Proiect asociat din platformă:
                </label>
                <select
                  value={reviewLeadId}
                  onChange={e => setReviewLeadId(e.target.value)}
                  className={DESIGN_TOKENS.inputs.select}
                >
                  {leads.map(l => (
                    <option key={l.id} value={l.id}>
                      {l.title.slice(0, 48)}... ({l.category})
                    </option>
                  ))}
                </select>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-slate-700 dark:text-slate-300 font-semibold mb-1">
                    Nume Evaluator:
                  </label>
                  <input
                    type="text"
                    required
                    value={reviewerName}
                    onChange={e => setReviewerName(e.target.value)}
                    className={DESIGN_TOKENS.inputs.base}
                  />
                </div>
                <div>
                  <label className="block text-slate-700 dark:text-slate-300 font-semibold mb-1">
                    Oraș:
                  </label>
                  <input
                    type="text"
                    required
                    value={reviewCity}
                    onChange={e => setReviewCity(e.target.value)}
                    className={DESIGN_TOKENS.inputs.base}
                  />
                </div>
              </div>

              {/* 4 Dimension Range Sliders */}
              <div className="space-y-3 pt-2 border-t border-slate-100 dark:border-slate-800">
                <span className="text-[10px] uppercase font-bold text-indigo-600 dark:text-indigo-400 tracking-wider block">
                  Scoruri per dimensiune (1 la 5 stele):
                </span>

                <div className="grid grid-cols-2 gap-3">
                  <div className="p-3 rounded-xl bg-slate-50 dark:bg-slate-800/60 border border-slate-200/80">
                    <div className="flex items-center justify-between mb-1">
                      <span className="font-semibold text-slate-800 dark:text-slate-200">{dimNames.dim1.split('. ')[1]}:</span>
                      <span className="font-mono font-bold text-amber-500">{reviewDim1}★</span>
                    </div>
                    <input
                      type="range"
                      min={1}
                      max={5}
                      step={1}
                      value={reviewDim1}
                      onChange={e => setReviewDim1(Number(e.target.value))}
                      className="w-full cursor-pointer accent-indigo-600"
                    />
                  </div>

                  <div className="p-3 rounded-xl bg-slate-50 dark:bg-slate-800/60 border border-slate-200/80">
                    <div className="flex items-center justify-between mb-1">
                      <span className="font-semibold text-slate-800 dark:text-slate-200">{dimNames.dim2.split('. ')[1]}:</span>
                      <span className="font-mono font-bold text-amber-500">{reviewDim2}★</span>
                    </div>
                    <input
                      type="range"
                      min={1}
                      max={5}
                      step={1}
                      value={reviewDim2}
                      onChange={e => setReviewDim2(Number(e.target.value))}
                      className="w-full cursor-pointer accent-indigo-600"
                    />
                  </div>

                  <div className="p-3 rounded-xl bg-slate-50 dark:bg-slate-800/60 border border-slate-200/80">
                    <div className="flex items-center justify-between mb-1">
                      <span className="font-semibold text-slate-800 dark:text-slate-200">{dimNames.dim3.split('. ')[1]}:</span>
                      <span className="font-mono font-bold text-amber-500">{reviewDim3}★</span>
                    </div>
                    <input
                      type="range"
                      min={1}
                      max={5}
                      step={1}
                      value={reviewDim3}
                      onChange={e => setReviewDim3(Number(e.target.value))}
                      className="w-full cursor-pointer accent-indigo-600"
                    />
                  </div>

                  <div className="p-3 rounded-xl bg-slate-50 dark:bg-slate-800/60 border border-slate-200/80">
                    <div className="flex items-center justify-between mb-1">
                      <span className="font-semibold text-slate-800 dark:text-slate-200">{dimNames.dim4.split('. ')[1]}:</span>
                      <span className="font-mono font-bold text-amber-500">{reviewDim4}★</span>
                    </div>
                    <input
                      type="range"
                      min={1}
                      max={5}
                      step={1}
                      value={reviewDim4}
                      onChange={e => setReviewDim4(Number(e.target.value))}
                      className="w-full cursor-pointer accent-indigo-600"
                    />
                  </div>
                </div>
              </div>

              <div>
                <label className="block text-slate-700 dark:text-slate-300 font-semibold mb-1">
                  Comentariu justificativ & detalii colaborare:
                </label>
                <textarea
                  required
                  rows={3}
                  value={reviewComment}
                  onChange={e => setReviewComment(e.target.value)}
                  placeholder="Descrie colaborarea, calitatea livrabilelor sau respectarea condițiilor..."
                  className={DESIGN_TOKENS.inputs.textarea}
                />
              </div>

              <div className="flex items-center justify-end gap-2 pt-2 border-t border-slate-100 dark:border-slate-800">
                <Button
                  variant="outline"
                  size="sm"
                  type="button"
                  onClick={() => setShowRatingModal(false)}
                >
                  Anulează
                </Button>
                <Button
                  variant="primary"
                  size="sm"
                  type="submit"
                  isLoading={isSubmitting}
                >
                  Confirmă & Publică
                </Button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
