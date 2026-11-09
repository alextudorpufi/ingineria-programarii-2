"use client";

import { useEffect, useState } from 'react';
import { Menu, Sparkles, X } from 'lucide-react';
import { Button } from '@/components/ui/Button';
import { DESIGN_TOKENS } from '@/theme/designSystem';

interface PublicHeaderProps {
  onLogin: () => void;
  onRegister: () => void;
}

const sections = [
  { href: '#cum-functioneaza', label: 'Cum funcționează' },
  { href: '#pentru-profesionisti', label: 'Pentru profesioniști' },
  { href: '#intrebari-frecvente', label: 'Întrebări frecvente' },
];

export function PublicHeader({ onLogin, onRegister }: PublicHeaderProps) {
  const [menuOpen, setMenuOpen] = useState(false);
  const [activeSection, setActiveSection] = useState<string | null>(null);

  useEffect(() => {
    const updateActiveSection = () => {
      if (window.scrollY > 0 && window.innerHeight + window.scrollY >= document.documentElement.scrollHeight - 2) {
        setActiveSection(sections[sections.length - 1].href);
        return;
      }
      const current = [...sections].reverse().find(section => {
        const element = document.getElementById(section.href.slice(1));
        return element && element.getBoundingClientRect().top <= 140;
      });
      setActiveSection(current?.href ?? null);
    };

    updateActiveSection();
    window.addEventListener('scroll', updateActiveSection, { passive: true });
    window.addEventListener('resize', updateActiveSection);
    return () => {
      window.removeEventListener('scroll', updateActiveSection);
      window.removeEventListener('resize', updateActiveSection);
    };
  }, []);

  const linkClassName = (href: string) => `border-b-2 py-3 transition-colors ${activeSection === href
    ? 'border-indigo-600 font-bold text-indigo-600 dark:border-indigo-400 dark:text-indigo-300'
    : 'border-transparent hover:text-indigo-600 dark:hover:text-indigo-300'}`;

  return (
    <header className={`sticky top-0 z-40 ${DESIGN_TOKENS.surfaces.glass}`}>
      <div className="mx-auto flex h-20 max-w-7xl items-center justify-between gap-3 px-4 sm:px-6 lg:px-8">
        <a href="#inceput" aria-label="Skillink — începutul paginii" className="flex shrink-0 items-center gap-2.5 text-xl font-extrabold tracking-tight">
          <span className={`flex size-9 items-center justify-center rounded-xl text-white ${DESIGN_TOKENS.colors.primaryGradient}`}>
            <Sparkles aria-hidden="true" className="size-5" />
          </span>
          <span>Skill<span className="text-indigo-600 dark:text-indigo-400">ink</span></span>
        </a>
        <nav aria-label="Navigație principală" className="hidden items-center gap-8 text-sm font-semibold text-slate-600 lg:flex dark:text-slate-300">
          {sections.map(section => <a key={section.href} href={section.href} aria-current={activeSection === section.href ? 'location' : undefined} className={linkClassName(section.href)}>{section.label}</a>)}
        </nav>
        <div className="flex items-center gap-2 sm:gap-3">
          <Button variant="ghost" className="hidden min-h-11 sm:inline-flex" onClick={onLogin}>Autentificare</Button>
          <Button className="min-h-11" onClick={onRegister}>Creează cont</Button>
          <Button variant="outline" className="min-h-11 min-w-11 lg:hidden" aria-label={menuOpen ? 'Închide meniul' : 'Deschide meniul'} aria-expanded={menuOpen} aria-controls="public-mobile-menu" onClick={() => setMenuOpen(!menuOpen)}>
            {menuOpen ? <X aria-hidden="true" className="size-5" /> : <Menu aria-hidden="true" className="size-5" />}
          </Button>
        </div>
      </div>
      {menuOpen && (
        <nav id="public-mobile-menu" aria-label="Navigație mobilă" className="border-t border-slate-200 px-4 py-3 lg:hidden dark:border-slate-800" onKeyDown={event => { if (event.key === 'Escape') setMenuOpen(false); }}>
          {sections.map(section => <a key={section.href} href={section.href} aria-current={activeSection === section.href ? 'location' : undefined} onClick={() => setMenuOpen(false)} className="block rounded-xl px-3 py-3 text-sm font-semibold hover:bg-slate-100 dark:hover:bg-slate-800"><span className={linkClassName(section.href)}>{section.label}</span></a>)}
          <Button variant="secondary" className="mt-2 min-h-11 w-full" onClick={() => { setMenuOpen(false); onLogin(); }}>Autentificare</Button>
        </nav>
      )}
    </header>
  );
}
