"use client";

import { DESIGN_TOKENS } from '@/theme/designSystem';

export function TextListField({ id, label, hint, value, onChange }: {
  id: string; label: string; hint: string; value: string; onChange: (value: string) => void;
}) {
  return (
    <div className="space-y-1.5">
      <label htmlFor={id} className="block text-xs font-semibold text-slate-700 dark:text-slate-200">{label}</label>
      <textarea id={id} rows={3} maxLength={12800} value={value}
        onChange={event => onChange(event.target.value)} aria-describedby={`${id}-hint`}
        className={DESIGN_TOKENS.inputs.textarea} />
      <p id={`${id}-hint`} className="text-xs text-slate-500 dark:text-slate-400">{hint}</p>
    </div>
  );
}
