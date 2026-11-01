"use client";

import React, { useState } from 'react';
import { useAuth } from '@/context/AuthContext';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { PasswordInput } from '@/components/auth/PasswordInput';
import { Briefcase, KeyRound, Mail, ShieldAlert, Sparkles, User, X } from 'lucide-react';

interface AuthModalProps {
  isOpen: boolean;
  onClose: () => void;
  initialMode?: 'login' | 'register';
}

export const AuthModal: React.FC<AuthModalProps> = ({ isOpen, onClose, initialMode = 'login' }) => {
  const { login, register } = useAuth();
  const [mode, setMode] = useState<'login' | 'register'>(initialMode);
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [name, setName] = useState('');
  const [role, setRole] = useState<'client' | 'pro'>('pro');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  if (!isOpen) return null;

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    setErrorMessage(null);
    setIsSubmitting(true);

    try {
      if (mode === 'login') {
        await login(email, password);
      } else {
        await register({ email, password, name, role });
      }
      onClose();
    } catch (error: unknown) {
      setErrorMessage(error instanceof Error ? error.message : 'Eroare la autentificare.');
    } finally {
      setIsSubmitting(false);
    }
  };

  const fillDemoAdmin = () => {
    setEmail('skilling@contact.ro');
    setPassword('admin');
    setErrorMessage(null);
  };

  const switchMode = (nextMode: 'login' | 'register') => {
    setMode(nextMode);
    setErrorMessage(null);
  };

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/60 backdrop-blur-sm"
      role="dialog"
      aria-modal="true"
      aria-labelledby="auth-modal-title"
    >
      <div className="relative w-full max-w-md bg-white dark:bg-slate-900 border border-slate-200/90 dark:border-slate-800 rounded-3xl shadow-2xl p-6 sm:p-8 space-y-6">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2.5">
            <div className="w-9 h-9 rounded-xl bg-gradient-to-tr from-indigo-600 to-violet-600 flex items-center justify-center text-white shadow-xs">
              <Sparkles className="w-5 h-5" />
            </div>
            <div>
              <h3 id="auth-modal-title" className="text-lg font-bold text-slate-900 dark:text-white">
                {mode === 'login' ? 'Conectare în Skillink' : 'Creare Cont Nou'}
              </h3>
              <p className="text-xs text-slate-500 dark:text-slate-400">
                {mode === 'login' ? 'Acces la baza de date marketplace' : 'Alege-ți rolul și începe lucrul'}
              </p>
            </div>
          </div>
          <button
            type="button"
            onClick={onClose}
            aria-label="Închide dialogul de autentificare"
            className="p-1.5 rounded-xl text-slate-400 hover:text-slate-600 dark:hover:text-slate-200 hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors cursor-pointer"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        <div className="grid grid-cols-2 p-1 bg-slate-100 dark:bg-slate-800/80 rounded-xl border border-slate-200 dark:border-slate-700">
          <button
            type="button"
            onClick={() => switchMode('login')}
            className={`py-1.5 text-xs font-semibold rounded-lg transition-all cursor-pointer ${
              mode === 'login' ? 'bg-white dark:bg-slate-900 text-indigo-600 dark:text-white shadow-xs' : 'text-slate-500'
            }`}
          >
            Autentificare
          </button>
          <button
            type="button"
            onClick={() => switchMode('register')}
            className={`py-1.5 text-xs font-semibold rounded-lg transition-all cursor-pointer ${
              mode === 'register' ? 'bg-white dark:bg-slate-900 text-indigo-600 dark:text-white shadow-xs' : 'text-slate-500'
            }`}
          >
            Înregistrare
          </button>
        </div>

        {errorMessage && (
          <div
            role="alert"
            className="p-3 rounded-xl bg-rose-50 dark:bg-rose-950/50 border border-rose-200 dark:border-rose-900 flex items-center gap-2.5 text-xs text-rose-600 dark:text-rose-300"
          >
            <ShieldAlert className="w-4 h-4 shrink-0" />
            <span>{errorMessage}</span>
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
          {mode === 'register' && (
            <>
              <Input
                label="Nume Complet"
                placeholder="ex. Radu Teodorescu"
                autoComplete="name"
                leftIcon={<User className="w-4 h-4" />}
                value={name}
                onChange={event => setName(event.target.value)}
                required
              />

              <div className="space-y-1.5">
                <span className="block text-xs font-semibold text-slate-700 dark:text-slate-200">Tip Cont</span>
                <div className="grid grid-cols-2 gap-2">
                  <button
                    type="button"
                    onClick={() => setRole('pro')}
                    aria-pressed={role === 'pro'}
                    className={`p-2.5 rounded-xl border text-xs font-semibold flex items-center justify-center gap-1.5 cursor-pointer transition-all ${
                      role === 'pro'
                        ? 'border-indigo-600 bg-indigo-50 dark:bg-indigo-950/60 text-indigo-700 dark:text-indigo-300'
                        : 'border-slate-200 dark:border-slate-700 text-slate-600 dark:text-slate-400'
                    }`}
                  >
                    <Briefcase className="w-3.5 h-3.5" />
                    Profesionist
                  </button>
                  <button
                    type="button"
                    onClick={() => setRole('client')}
                    aria-pressed={role === 'client'}
                    className={`p-2.5 rounded-xl border text-xs font-semibold flex items-center justify-center gap-1.5 cursor-pointer transition-all ${
                      role === 'client'
                        ? 'border-emerald-600 bg-emerald-50 dark:bg-emerald-950/60 text-emerald-700 dark:text-emerald-300'
                        : 'border-slate-200 dark:border-slate-700 text-slate-600 dark:text-slate-400'
                    }`}
                  >
                    <User className="w-3.5 h-3.5" />
                    Client
                  </button>
                </div>
              </div>
            </>
          )}

          <Input
            label="Adresă Email"
            type="email"
            placeholder="nume@exemplu.ro"
            autoComplete="email"
            leftIcon={<Mail className="w-4 h-4" />}
            value={email}
            onChange={event => setEmail(event.target.value)}
            required
          />

          <PasswordInput
            label="Parolă"
            placeholder="••••••••"
            value={password}
            onChange={event => setPassword(event.target.value)}
            showStrengthMeter={mode === 'register'}
            autoComplete={mode === 'register' ? 'new-password' : 'current-password'}
            required
          />

          <Button type="submit" variant="primary" size="lg" className="w-full justify-center" isLoading={isSubmitting}>
            {mode === 'login' ? 'Autentificare' : 'Creează Contul'}
          </Button>
        </form>

        {mode === 'login' && (
          <div className="pt-2 border-t border-slate-100 dark:border-slate-800 text-center">
            <button
              type="button"
              onClick={fillDemoAdmin}
              className="inline-flex items-center gap-1.5 text-xs text-indigo-600 dark:text-indigo-400 hover:underline font-medium cursor-pointer"
            >
              <KeyRound className="w-3.5 h-3.5" />
              Cont demo: skilling@contact.ro / admin
            </button>
          </div>
        )}
      </div>
    </div>
  );
};
