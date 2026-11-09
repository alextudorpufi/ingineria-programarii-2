"use client";

import React, { useEffect, useRef, useState } from 'react';
import { Save, RotateCcw, UserRound, BriefcaseBusiness, MapPin } from 'lucide-react';
import { useAuth } from '@/context/AuthContext';
import { Input } from '@/components/ui/Input';
import { TextListField } from '@/components/ui/TextListField';
import { Button } from '@/components/ui/Button';
import { Badge } from '@/components/ui/Badge';
import { LoadingSpinner } from '@/components/ui/Skeleton';
import { CountyCoverageSelector } from '@/components/common/CountyCoverageSelector';
import { DESIGN_TOKENS } from '@/theme/designSystem';
import { SERVICE_CATEGORIES, resolveCategoryLabel } from '@/services/api/categoryCatalog';
import { professionalProfileService } from '@/services/api/professionalProfileService';
import type { EditableProfessionalProfile } from '@/types';

type Draft = Omit<EditableProfessionalProfile,
  'servicesOffered' | 'capabilities' | 'yearsExperience'> & {
  servicesOffered: string;
  capabilities: string;
  yearsExperience: string;
};

function toDraft(profile: EditableProfessionalProfile): Draft {
  return {
    ...profile,
    servicesOffered: profile.servicesOffered.join('\n'),
    capabilities: profile.capabilities.join('\n'),
    yearsExperience: profile.yearsExperience?.toString() ?? '',
  };
}

function lines(value: string): string[] {
  return [...new Set(value.split(/\r?\n/).map(line => line.trim()).filter(Boolean))];
}

function WorkModeField({ id, label, value, onChange }: {
  id: string; label: string; value: boolean | null; onChange: (value: boolean | null) => void;
}) {
  return (
    <div className="space-y-1.5">
      <label htmlFor={id} className="block text-xs font-semibold text-slate-700 dark:text-slate-200">{label}</label>
      <select id={id} value={value === null ? '' : String(value)}
        onChange={event => onChange(event.target.value === '' ? null : event.target.value === 'true')}
        className={DESIGN_TOKENS.inputs.select}>
        <option value="">Nespecificat</option>
        <option value="true">Da</option>
        <option value="false">Nu</option>
      </select>
    </div>
  );
}

export function ProfessionalProfileEditor() {
  const { refreshUser } = useAuth();
  const [saved, setSaved] = useState<EditableProfessionalProfile | null>(null);
  const [draft, setDraft] = useState<Draft | null>(null);
  const [loading, setLoading] = useState(true);
  const [reload, setReload] = useState(0);
  const [saving, setSaving] = useState<'DRAFT' | 'COMPLETE' | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [message, setMessage] = useState<string | null>(null);
  const feedbackRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (error || message) feedbackRef.current?.scrollIntoView({ block: 'nearest' });
  }, [error, message]);

  useEffect(() => {
    let active = true;
    professionalProfileService.get().then(profile => {
      if (!active) return;
      setSaved(profile);
      setDraft(toDraft(profile));
      setError(null);
      setMessage(null);
    }).catch(cause => {
      if (active) setError(cause instanceof Error ? cause.message : 'Profilul nu a putut fi încărcat.');
    }).finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, [reload]);

  function change<K extends keyof Draft>(field: K, value: Draft[K]) {
    setDraft(current => current ? { ...current, [field]: value } : current);
    setMessage(null);
  }

  async function save(status: 'DRAFT' | 'COMPLETE') {
    if (!draft || saving) return;
    setSaving(status);
    setError(null);
    setMessage(null);
    try {
      const payload: EditableProfessionalProfile = {
        ...draft,
        servicesOffered: lines(draft.servicesOffered),
        capabilities: lines(draft.capabilities),
        yearsExperience: draft.yearsExperience === '' ? null : Number(draft.yearsExperience),
        onboardingStatus: status, acceptingLeads: status === 'COMPLETE' && draft.acceptingLeads,
      };
      if (payload.yearsExperience !== null && (!Number.isInteger(payload.yearsExperience)
        || payload.yearsExperience < 0 || payload.yearsExperience > 100)) {
        throw new Error('Experiența trebuie să fie un număr întreg între 0 și 100.');
      }
      if (payload.servicesOffered.length > 30 || payload.coverageAreas.length > 42 || payload.capabilities.length > 50
        || [...payload.servicesOffered, ...payload.coverageAreas, ...payload.capabilities].some(value => value.length > 255)) {
        throw new Error('Folosește maximum 30 de servicii, 42 de județe și 50 de competențe, cu cel mult 255 de caractere fiecare.');
      }
      const profile = await professionalProfileService.save(payload);
      setSaved(profile);
      setDraft(toDraft(profile));
      setMessage(status === 'DRAFT' ? 'Ciorna a fost salvată. Poți continua oricând.' : 'Profilul profesional a fost salvat.');
      try { await refreshUser(); } catch { setMessage('Profilul este salvat. Reîncarcă pagina pentru a actualiza și sumarul.'); }
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'Profilul nu a putut fi salvat.');
    } finally {
      setSaving(null);
    }
  }

  const options = [...SERVICE_CATEGORIES, ...(draft?.categories ?? [])
    .filter(id => !SERVICE_CATEGORIES.some(category => category.id === id))
    .map(id => ({ id, name: resolveCategoryLabel(id), shortName: resolveCategoryLabel(id) }))];

  return (
    <section aria-labelledby="professional-editor-title" className="rounded-3xl border border-indigo-100 dark:border-slate-800 bg-white dark:bg-slate-900 shadow-sm">
      <div className="p-5 sm:p-6 border-b border-slate-100 dark:border-slate-800 flex flex-wrap items-center justify-between gap-3">
        <div>
          <h2 id="professional-editor-title" className="text-lg font-bold tracking-tight text-slate-900 dark:text-white">Editează profilul profesional</h2>
          <p className="mt-1 text-xs text-slate-500 dark:text-slate-400">Spune ce servicii oferi și unde lucrezi, pentru cereri mai relevante.</p>
        </div>
        {saved && <Badge variant={saved.onboardingStatus === 'COMPLETE' ? 'emerald' : 'amber'}>
          {saved.onboardingStatus === 'COMPLETE' ? 'Profil completat' : 'Ciornă'}
        </Badge>}
      </div>
      {loading ? <div className="p-8"><LoadingSpinner text="Se încarcă profilul..." /></div> : (
        <form onSubmit={event => { event.preventDefault(); void save('COMPLETE'); }} className="p-5 sm:p-6 space-y-7">
          <div ref={feedbackRef} className="space-y-3">
          {error && <div role="alert" className="rounded-xl border border-rose-200 dark:border-rose-900 bg-rose-50 dark:bg-rose-950/50 p-3 text-sm text-rose-700 dark:text-rose-300">
            <p>{error}</p>
            <Button type="button" variant="ghost" size="sm" className="mt-2" disabled={saving !== null}
              onClick={() => { setLoading(true); setReload(value => value + 1); }}>Reîncarcă profilul</Button>
          </div>}
          {message && <p role="status" className="rounded-xl border border-emerald-200 dark:border-emerald-900 bg-emerald-50 dark:bg-emerald-950/50 p-3 text-sm text-emerald-700 dark:text-emerald-300">{message}</p>}
          </div>
          {draft && <>
            <fieldset disabled={saving !== null} className="space-y-7 disabled:opacity-60">
              <div className="space-y-4">
                <h3 className="flex items-center gap-2 text-sm font-semibold text-slate-800 dark:text-slate-100"><UserRound className="h-4 w-4 text-indigo-500" />Despre tine</h3>
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                  <Input id="pro-name" label="Nume complet" required maxLength={255} value={draft.name} onChange={event => change('name', event.target.value)} autoComplete="name" />
                  <Input id="pro-company" label="Firmă / denumire profesională" maxLength={255} value={draft.companyName ?? ''} onChange={event => change('companyName', event.target.value)} autoComplete="organization" />
                  <Input id="pro-title" label="Titlu profesional" maxLength={255} value={draft.title ?? ''} onChange={event => change('title', event.target.value)} placeholder="De exemplu: dezvoltator web" />
                  <Input id="pro-phone" label="Telefon" type="tel" maxLength={50} value={draft.phone ?? ''} onChange={event => change('phone', event.target.value)} autoComplete="tel" />
                </div>
                <div className="space-y-1.5">
                  <label htmlFor="pro-bio" className="block text-xs font-semibold text-slate-700 dark:text-slate-200">Descriere profesională</label>
                  <textarea id="pro-bio" rows={3} maxLength={5000} value={draft.bio ?? ''} onChange={event => change('bio', event.target.value)} className={DESIGN_TOKENS.inputs.textarea} />
                </div>
              </div>
              <div className="space-y-4 border-t border-slate-100 dark:border-slate-800 pt-6">
                <h3 className="flex items-center gap-2 text-sm font-semibold text-slate-800 dark:text-slate-100"><BriefcaseBusiness className="h-4 w-4 text-indigo-500" />Servicii și experiență</h3>
                <fieldset className="space-y-2">
                  <legend className="text-xs font-semibold text-slate-700 dark:text-slate-200">Categorii de servicii</legend>
                  <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
                    {options.map(category => <label key={category.id} className="flex items-center gap-2 rounded-xl border border-slate-200 dark:border-slate-700 p-3 text-xs text-slate-700 dark:text-slate-200 cursor-pointer">
                      <input type="checkbox" checked={draft.categories.includes(category.id)} className="h-4 w-4 accent-indigo-600"
                        onChange={event => change('categories', event.target.checked ? [...draft.categories, category.id] : draft.categories.filter(id => id !== category.id))} />
                      {category.name}
                    </label>)}
                  </div>
                </fieldset>
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                  <TextListField id="pro-services" label="Servicii oferite" hint="Un serviciu pe linie. De exemplu: creare magazine online." value={draft.servicesOffered} onChange={value => change('servicesOffered', value)} />
                  <TextListField id="pro-capabilities" label="Competențe și specializări" hint="O competență pe linie. De exemplu: integrarea plăților online." value={draft.capabilities} onChange={value => change('capabilities', value)} />
                </div>
                <Input id="pro-experience" label="Ani de experiență" type="number" min={0} max={100} step={1} className="tabular-nums" value={draft.yearsExperience} onChange={event => change('yearsExperience', event.target.value)} helperText="Opțional. Lasă gol dacă nu dorești să specifici." />
              </div>
              <div className="space-y-4 border-t border-slate-100 dark:border-slate-800 pt-6">
                <h3 className="flex items-center gap-2 text-sm font-semibold text-slate-800 dark:text-slate-100"><MapPin className="h-4 w-4 text-indigo-500" />Mod de lucru și acoperire</h3>
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                  <WorkModeField id="pro-onsite" label="Lucrezi la locația clientului?" value={draft.supportsOnsite} onChange={value => change('supportsOnsite', value)} />
                  <WorkModeField id="pro-remote" label="Oferi servicii la distanță?" value={draft.supportsRemote} onChange={value => change('supportsRemote', value)} />
                </div>
                <CountyCoverageSelector value={draft.coverageAreas} onChange={value => change('coverageAreas', value)} />
                <label className="flex items-start gap-3 rounded-xl border border-indigo-100 dark:border-indigo-900 bg-indigo-50/50 dark:bg-indigo-950/30 p-4 cursor-pointer">
                  <input type="checkbox" checked={draft.acceptingLeads} onChange={event => change('acceptingLeads', event.target.checked)} className="mt-0.5 h-4 w-4 accent-indigo-600" />
                  <span className="text-sm font-medium text-slate-800 dark:text-slate-100">Doresc să primesc cereri noi<span className="mt-1 block text-xs font-normal text-slate-500 dark:text-slate-400">Se activează după salvarea profilului completat. Ciornele nu primesc cereri noi.</span></span>
                </label>
              </div>
            </fieldset>
            <div className="flex flex-col sm:flex-row flex-wrap gap-3 border-t border-slate-100 dark:border-slate-800 pt-5">
              <Button type="submit" icon={<Save className="h-4 w-4" />} isLoading={saving === 'COMPLETE'} disabled={saving !== null}>Salvează profilul</Button>
              <Button type="button" variant="outline" isLoading={saving === 'DRAFT'} disabled={saving !== null} onClick={() => void save('DRAFT')}>Salvează ciorna</Button>
              <Button type="button" variant="ghost" icon={<RotateCcw className="h-4 w-4" />} disabled={saving !== null} onClick={() => { if (saved) setDraft(toDraft(saved)); setError(null); setMessage(null); }}>Renunță la modificări</Button>
            </div>
          </>}
        </form>
      )}
    </section>
  );
}
