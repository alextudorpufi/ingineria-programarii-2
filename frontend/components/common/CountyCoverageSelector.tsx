"use client";

import { useState } from 'react';
import { Input } from '@/components/ui/Input';
import { Button } from '@/components/ui/Button';
import { useRomanianCounties } from '@/hooks/useRomanianCounties';

function searchable(value: string): string {
  return value.normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLocaleLowerCase('ro-RO').trim();
}

export function CountyCoverageSelector({ value, onChange }: {
  value: string[]; onChange: (value: string[]) => void;
}) {
  const { counties, loading, error, retry } = useRomanianCounties();
  const [query, setQuery] = useState('');
  const visible = counties.filter(county => searchable(county.name).includes(searchable(query)));
  const legacy = value.filter(id => !counties.some(county => county.id === id));

  function toggle(id: string, checked: boolean) {
    onChange(checked
      ? [...value.filter(current => counties.some(county => county.id === current)), id]
      : value.filter(current => current !== id));
  }

  return (
    <fieldset className="space-y-3" aria-describedby="pro-counties-hint">
      <legend className="text-xs font-semibold text-slate-700 dark:text-slate-200">Județe de acoperire</legend>
      <p id="pro-counties-hint" className="text-xs text-slate-500 dark:text-slate-400">
        Selectează județele în care te deplasezi. Fiecare selecție înseamnă acoperirea întregului județ.
        București se selectează separat de Ilfov. Necesar dacă lucrezi la locația clientului.
      </p>
      {loading && <p role="status" className="text-xs text-slate-500 dark:text-slate-400">Se încarcă județele...</p>}
      {error && <div role="alert" className="text-xs text-rose-700 dark:text-rose-300">
        <p>{error}</p>
        <Button type="button" variant="outline" size="sm" onClick={retry}>Reîncearcă</Button>
      </div>}
      {!loading && !error && <>
        <Input id="pro-county-search" label="Caută un județ" type="search" value={query}
          placeholder="De exemplu: Iași" onChange={event => setQuery(event.target.value)} />
        <div role="group" aria-label="Județe disponibile" tabIndex={0}
          className="max-h-60 overflow-y-auto overscroll-contain rounded-xl border border-slate-200 dark:border-slate-700 bg-slate-50/90 dark:bg-slate-900/50 p-2 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-indigo-500">
          {visible.map(county => <label key={county.id}
            className="flex items-center gap-3 rounded-lg p-2.5 text-sm text-slate-700 dark:text-slate-200 hover:bg-indigo-50 dark:hover:bg-indigo-950/50 cursor-pointer">
            <input type="checkbox" checked={value.includes(county.id)}
              onChange={event => toggle(county.id, event.target.checked)} className="h-4 w-4 accent-indigo-600" />
            {county.name}
          </label>)}
          {visible.length === 0 && <p role="status" className="p-3 text-xs text-slate-500 dark:text-slate-400">Niciun județ găsit.</p>}
        </div>
        <p role="status" className="text-xs tabular-nums text-slate-500 dark:text-slate-400">
          {value.length - legacy.length} din {counties.length} selectate
        </p>
        {value.length > legacy.length && <div className="flex flex-wrap gap-2" aria-label="Județe selectate">
          {counties.filter(county => value.includes(county.id)).map(county =>
            <Button key={county.id} type="button" variant="outline" size="xs"
              aria-label={`Elimină ${county.name}`} onClick={() => toggle(county.id, false)}>{county.name} ×</Button>)}
        </div>}
        {legacy.length > 0 && <div className="space-y-2 rounded-xl border border-amber-200 dark:border-amber-800 bg-amber-50 dark:bg-amber-950/40 p-3">
          <p className="text-xs text-amber-800 dark:text-amber-200">Zone salvate anterior: selectarea unui județ înlocuiește automat aceste valori. Dacă lucrezi doar la distanță, le poți elimina pe toate. Ciorna le poate păstra până alegi județele potrivite.</p>
          <Button type="button" variant="outline" size="sm"
            onClick={() => onChange(value.filter(id => counties.some(county => county.id === id)))}>
            Elimină toate zonele vechi
          </Button>
          {legacy.map(area => <div key={area} className="flex items-center justify-between gap-2 text-xs text-slate-700 dark:text-slate-200">
            <span>{area}</span>
            <Button type="button" variant="ghost" size="xs" aria-label={`Elimină zona veche ${area}`}
              onClick={() => toggle(area, false)}>Elimină</Button>
          </div>)}
        </div>}
      </>}
    </fieldset>
  );
}

export function CountyCoverageSummary({ areas }: { areas: string[] }) {
  const { counties, loading, error } = useRomanianCounties();
  if (areas.length === 0) return <>Acoperire nespecificată</>;
  if (loading) return <>Se încarcă acoperirea...</>;
  if (error) return <>Acoperire indisponibilă temporar</>;
  return <>{areas.map(id => counties.find(county => county.id === id)?.name ?? id).join(' · ')}</>;
}
