"use client";

import React, { useId, useState } from 'react';
import { Check, Eye, EyeOff, X } from 'lucide-react';
import { DESIGN_TOKENS } from '@/theme/designSystem';

interface PasswordInputProps extends React.InputHTMLAttributes<HTMLInputElement> {
  label?: string;
  showStrengthMeter?: boolean;
  error?: string;
  helperText?: string;
}

interface StrengthMeta {
  score: number;
  label: string;
  barClass: string;
}

const STRENGTH_BARS = [
  'bg-rose-600',
  'bg-rose-500',
  'bg-amber-500',
  'bg-emerald-500',
  'bg-gradient-to-r from-indigo-500 to-violet-600',
];

export const PasswordInput: React.FC<PasswordInputProps> = ({
  label = 'Parolă',
  showStrengthMeter = false,
  error,
  helperText,
  value = '',
  onChange,
  className = '',
  id,
  autoComplete = 'current-password',
  ...props
}) => {
  const generatedId = useId();
  const inputId = id || generatedId;
  const [showPassword, setShowPassword] = useState(false);

  const password = String(value);

  const criteria = [
    { label: 'Minim 8 caractere', met: password.length >= 8 },
    { label: 'O literă mare (A-Z)', met: /[A-Z]/.test(password) },
    { label: 'O literă mică (a-z)', met: /[a-z]/.test(password) },
    { label: 'O cifră (0-9)', met: /[0-9]/.test(password) },
    { label: 'Un caracter special (!@#$...)', met: /[^A-Za-z0-9]/.test(password) },
  ];

  const metCount = criteria.filter(criterion => criterion.met).length;

  const strength: StrengthMeta = (() => {
    if (password.length === 0) return { score: 0, label: '', barClass: 'bg-slate-200 dark:bg-slate-700' };
    if (metCount <= 1) return { score: 1, label: 'Foarte slabă', barClass: STRENGTH_BARS[0] };
    if (metCount === 2) return { score: 2, label: 'Slabă', barClass: STRENGTH_BARS[1] };
    if (metCount === 3) return { score: 3, label: 'Medie', barClass: STRENGTH_BARS[2] };
    if (metCount === 4) return { score: 4, label: 'Bună', barClass: STRENGTH_BARS[3] };
    return { score: 5, label: 'Excelentă', barClass: STRENGTH_BARS[4] };
  })();

  return (
    <div className="w-full space-y-1.5">
      <div className="flex items-center justify-between">
        <label htmlFor={inputId} className="block text-xs font-semibold text-slate-700 dark:text-slate-200">
          {label}
        </label>
        {showStrengthMeter && password.length > 0 && (
          <span className="text-[11px] font-bold text-slate-500 dark:text-slate-400">
            Securitate:{' '}
            <span className="font-semibold text-indigo-600 dark:text-indigo-400">{strength.label}</span>
          </span>
        )}
      </div>

      <div className="relative flex items-center">
        <input
          id={inputId}
          type={showPassword ? 'text' : 'password'}
          value={value}
          onChange={onChange}
          autoComplete={autoComplete}
          aria-invalid={Boolean(error)}
          aria-describedby={error ? `${inputId}-error` : undefined}
          className={`${DESIGN_TOKENS.inputs.base} pr-10 ${
            error ? 'border-rose-500 focus:ring-rose-500/30 focus:border-rose-500' : ''
          } ${className}`}
          {...props}
        />

        <button
          type="button"
          tabIndex={-1}
          onClick={() => setShowPassword(previous => !previous)}
          aria-label={showPassword ? 'Ascunde parola' : 'Afișează parola'}
          aria-pressed={showPassword}
          className="absolute right-3 text-slate-400 hover:text-slate-600 dark:hover:text-slate-200 transition-colors cursor-pointer p-1"
        >
          {showPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
        </button>
      </div>

      {showStrengthMeter && password.length > 0 && (
        <div className="space-y-2 pt-1">
          <div className="grid grid-cols-5 gap-1.5 h-1.5" aria-hidden="true">
            {[1, 2, 3, 4, 5].map(step => (
              <div
                key={step}
                className={`h-full rounded-full transition-all duration-300 ${
                  strength.score >= step ? strength.barClass : 'bg-slate-200 dark:bg-slate-800'
                }`}
              />
            ))}
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-1 text-[11px] text-slate-500 dark:text-slate-400 pt-0.5">
            {criteria.map(criterion => (
              <div key={criterion.label} className="flex items-center gap-1.5">
                {criterion.met ? (
                  <Check className="w-3.5 h-3.5 text-emerald-500 shrink-0" />
                ) : (
                  <X className="w-3.5 h-3.5 text-slate-300 dark:text-slate-600 shrink-0" />
                )}
                <span className={criterion.met ? 'text-slate-700 dark:text-slate-200 font-medium' : ''}>
                  {criterion.label}
                </span>
              </div>
            ))}
          </div>
        </div>
      )}

      {error ? (
        <p id={`${inputId}-error`} role="alert" className="text-[11px] text-rose-500 font-medium">
          {error}
        </p>
      ) : helperText ? (
        <p className="text-[11px] text-slate-400">{helperText}</p>
      ) : null}
    </div>
  );
};
