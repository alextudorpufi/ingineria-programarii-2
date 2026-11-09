"use client";

import { useState } from 'react';
import { ArrowDown, ArrowRight, BriefcaseBusiness, Check, ClipboardList, Coins, FileText, MapPin, MoveUpRight, Sparkles, UserRound } from 'lucide-react';
import { AuthModal } from '@/components/auth/AuthModal';
import { Badge } from '@/components/ui/Badge';
import { Button } from '@/components/ui/Button';
import { DESIGN_TOKENS } from '@/theme/designSystem';
import { PublicHeader } from './PublicHeader';

const steps = [
  { icon: FileText, title: 'Pune proiectul în cuvinte', description: 'Spune ce serviciu cauți, unde ai nevoie de el și ce buget ai în vedere.' },
  { icon: ClipboardList, title: 'Publică cererea gratuit', description: 'Adaugă detaliile importante, astfel încât profesioniștii să înțeleagă ce ai de făcut.' },
  { icon: UserRound, title: 'Fă următorul pas', description: 'Profesioniștii interesați pot debloca datele de contact pentru a discuta cu tine despre proiect.' },
];

// A process illustration, without invented leads, profiles or marketplace metrics.
function RequestJourney() {
  return (
    <div className="relative mx-auto w-full max-w-lg lg:ml-auto lg:mr-0">
      <div aria-hidden="true" className="absolute -inset-4 rotate-3 rounded-3xl border border-indigo-200/60 bg-indigo-100/40 dark:border-indigo-800 dark:bg-indigo-950/40" />
      <div className={`relative overflow-hidden p-6 sm:p-8 ${DESIGN_TOKENS.surfaces.card}`}>
        <div className="mb-8 flex items-center justify-between gap-3 border-b border-slate-200 pb-5 dark:border-slate-800">
          <span className="text-xs font-bold uppercase tracking-widest text-slate-500 dark:text-slate-400">De la idee la colaborare</span>
          <Sparkles aria-hidden="true" className="size-5 shrink-0 text-indigo-600 dark:text-indigo-400" />
        </div>
        <div className="flex items-start gap-4">
          <span className="flex size-12 shrink-0 items-center justify-center rounded-2xl bg-indigo-50 text-indigo-600 dark:bg-indigo-950 dark:text-indigo-300"><FileText aria-hidden="true" className="size-6" /></span>
          <div>
            <p className="text-xs font-semibold text-indigo-600 dark:text-indigo-300">PUNCTUL DE PLECARE</p>
            <h2 className="mt-1 text-xl font-bold tracking-tight">Proiectul tău, bine definit.</h2>
            <p className="mt-2 text-sm leading-relaxed text-slate-600 dark:text-slate-300">O cerere clară îi ajută pe specialiști să înțeleagă de ce ai nevoie.</p>
          </div>
        </div>
        <dl className={`mt-6 grid grid-cols-2 gap-4 p-4 text-sm ${DESIGN_TOKENS.surfaces.subtleWell}`}>
          <div><dt className="text-xs text-slate-500 dark:text-slate-400">Ce ai nevoie?</dt><dd className="mt-1 font-semibold">Serviciu & descriere</dd></div>
          <div><dt className="text-xs text-slate-500 dark:text-slate-400">Unde și când?</dt><dd className="mt-1 font-semibold">Locație & termen</dd></div>
        </dl>
        <div aria-hidden="true" className="my-4 flex justify-center text-indigo-400"><ArrowDown className="size-6" /></div>
        <div className="flex items-center gap-4 rounded-2xl border border-emerald-200 bg-emerald-50 p-4 dark:border-emerald-800 dark:bg-emerald-950/50">
          <span className="flex size-11 shrink-0 items-center justify-center rounded-xl bg-white text-emerald-700 dark:bg-emerald-900 dark:text-emerald-200"><BriefcaseBusiness aria-hidden="true" className="size-5" /></span>
          <div><p className="text-sm font-bold text-emerald-900 dark:text-emerald-200">Oportunitate pentru profesioniști</p><p className="mt-1 text-xs leading-relaxed text-emerald-800 dark:text-emerald-300">Detalii înainte de deblocarea contactului.</p></div>
        </div>
        <p className="mt-6 flex items-center gap-2 text-xs text-slate-500 dark:text-slate-400"><MapPin aria-hidden="true" className="size-4 shrink-0" />Serviciul, locația și bugetul, într-un singur loc.</p>
      </div>
    </div>
  );
}

export function LandingPage() {
  const [auth, setAuth] = useState<{ mode: 'login' | 'register'; role: 'client' | 'pro' } | null>(null);
  const registerClient = () => setAuth({ mode: 'register', role: 'client' });

  return (
    <div id="inceput" className={`landing-page min-h-dvh text-slate-900 dark:text-slate-100 ${DESIGN_TOKENS.surfaces.appBackground}`}>
      <a href="#continut" className="sr-only z-50 rounded-xl bg-white px-4 py-3 text-indigo-700 focus:not-sr-only focus:fixed focus:left-4 focus:top-4">Sari la conținut</a>
      <PublicHeader onLogin={() => setAuth({ mode: 'login', role: 'client' })} onRegister={registerClient} />
      <main id="continut" tabIndex={-1}>
        <section aria-labelledby="hero-title" className="bg-vibrant-mesh border-b border-slate-200/80 dark:border-slate-800">
          <div className="mx-auto grid max-w-7xl items-center gap-14 px-5 py-14 sm:px-6 sm:py-20 lg:min-h-[660px] lg:grid-cols-2 lg:gap-16 lg:px-8 lg:py-24">
            <div>
              <Badge variant="indigo" icon={<Sparkles aria-hidden="true" className="size-3" />}>Oameni pricepuți. Proiecte cu sens.</Badge>
              <h1 id="hero-title" className="mt-6 max-w-xl text-4xl font-extrabold leading-[1.12] tracking-tight sm:text-5xl xl:text-6xl">Ideea e a ta.<br />Următorul pas,<br /><span className="text-indigo-600 dark:text-indigo-400">îl găsim împreună.</span></h1>
              <p className="mt-6 max-w-lg text-base leading-relaxed text-slate-600 sm:text-lg dark:text-slate-300">Skillink conectează clienții cu profesioniștii prin cereri de servicii. Descrie ce ai de făcut și oferă specialiștilor un punct de plecare clar.</p>
              <div className="mt-8 flex flex-col gap-3 sm:flex-row sm:items-center">
                <Button size="lg" className="min-h-12" onClick={registerClient} iconRight={<ArrowRight aria-hidden="true" className="size-4" />}>Începe ca client</Button>
                <a href="#cum-functioneaza" className="inline-flex min-h-12 items-center justify-center gap-2 rounded-xl px-4 text-sm font-semibold text-slate-700 hover:text-indigo-600 dark:text-slate-200 dark:hover:text-indigo-300">Vezi cum funcționează<ArrowDown aria-hidden="true" className="size-4" /></a>
              </div>
              <p className="mt-4 flex items-center gap-2 text-xs text-slate-600 dark:text-slate-300"><Check aria-hidden="true" className="size-4 text-emerald-700 dark:text-emerald-400" />Publicarea cererilor este gratuită pentru clienți.</p>
            </div>
            <RequestJourney />
          </div>
        </section>

        <section id="cum-functioneaza" aria-labelledby="steps-title" className="mx-auto max-w-7xl scroll-mt-24 px-5 py-16 sm:px-6 sm:py-24 lg:px-8">
          <div className="max-w-2xl">
            <p className="text-xs font-bold uppercase tracking-widest text-indigo-600 dark:text-indigo-400">Pentru clienți</p>
            <h2 id="steps-title" className="mt-3 text-2xl font-extrabold tracking-tight sm:text-3xl">Mai puține necunoscute.<br />Un început mai simplu.</h2>
            <p className="mt-4 text-sm leading-relaxed text-slate-600 dark:text-slate-300">Nu trebuie să ai toate răspunsurile. Începe cu nevoia ta și adaugă detaliile care contează.</p>
          </div>
          <ol className="mt-10 grid gap-8 md:grid-cols-3 md:gap-10">
            {steps.map(({ icon: Icon, title, description }, index) => (
              <li key={title} className="border-t border-slate-200 pt-6 dark:border-slate-800">
                <div className="flex items-center justify-between"><Icon aria-hidden="true" className="size-6 text-indigo-600 dark:text-indigo-400" /><span aria-hidden="true" className="font-mono text-sm tabular-nums text-slate-500 dark:text-slate-400">0{index + 1}</span></div>
                <h3 className="mt-5 text-base font-bold">{title}</h3>
                <p className="mt-3 text-sm leading-relaxed text-slate-600 dark:text-slate-300">{description}</p>
              </li>
            ))}
          </ol>
        </section>

        <section id="pentru-profesionisti" aria-labelledby="pro-title" className="mx-auto max-w-7xl scroll-mt-24 px-5 pb-16 sm:px-6 sm:pb-24 lg:px-8">
          <div className={`grid gap-10 p-6 sm:p-10 lg:grid-cols-2 lg:items-center lg:gap-16 lg:p-12 ${DESIGN_TOKENS.surfaces.card}`}>
            <div>
              <Badge variant="emerald" icon={<BriefcaseBusiness aria-hidden="true" className="size-3" />}>Pentru profesioniști</Badge>
              <h2 id="pro-title" className="mt-5 text-2xl font-extrabold tracking-tight sm:text-3xl">Tu ai priceperea.<br />Alege următoarea oportunitate.</h2>
              <p className="mt-4 text-sm leading-relaxed text-slate-600 dark:text-slate-300">Prezintă serviciile pe care le oferi și zonele în care lucrezi. Consultă cererile și decide pentru ce oportunități vrei să deblochezi contactul.</p>
              <Button variant="success" size="lg" className="mt-6 min-h-12" onClick={() => setAuth({ mode: 'register', role: 'pro' })} iconRight={<MoveUpRight aria-hidden="true" className="size-4" />}>Înscrie-te ca profesionist</Button>
            </div>
            <div className={`space-y-6 p-6 ${DESIGN_TOKENS.surfaces.subtleWell}`}>
              <div className="flex gap-4"><UserRound aria-hidden="true" className="mt-1 size-5 shrink-0 text-indigo-600 dark:text-indigo-400" /><div><h3 className="text-sm font-bold">Un profil care te reprezintă</h3><p className="mt-2 text-sm leading-relaxed text-slate-600 dark:text-slate-300">Adaugă specializările, experiența și aria ta de acoperire.</p></div></div>
              <div className="flex gap-4"><FileText aria-hidden="true" className="mt-1 size-5 shrink-0 text-indigo-600 dark:text-indigo-400" /><div><h3 className="text-sm font-bold">Context înainte de contact</h3><p className="mt-2 text-sm leading-relaxed text-slate-600 dark:text-slate-300">Vezi descrierea, locația și bugetul cererii înainte să iei o decizie.</p></div></div>
              <div className="flex gap-4"><Coins aria-hidden="true" className="mt-1 size-5 shrink-0 text-emerald-700 dark:text-emerald-400" /><div><h3 className="text-sm font-bold">Tu alegi unde folosești creditele</h3><p className="mt-2 text-sm leading-relaxed text-slate-600 dark:text-slate-300">Deblocarea datelor de contact costă credite virtuale. Achiziția unui lead nu garantează atribuirea lucrării.</p></div></div>
            </div>
          </div>
        </section>

        <section aria-labelledby="start-title" className="border-y border-indigo-100 bg-indigo-50/70 dark:border-indigo-900 dark:bg-indigo-950/30">
          <div className="mx-auto flex max-w-7xl flex-col items-start justify-between gap-6 px-5 py-12 sm:px-6 md:flex-row md:items-center lg:px-8">
            <div><h2 id="start-title" className="text-2xl font-extrabold tracking-tight">Orice proiect începe cu un prim pas.</h2><p className="mt-3 text-sm text-slate-600 dark:text-slate-300">Creează un cont de client sau profesionist și intră în Skillink.</p></div>
            <Button size="lg" className="min-h-12 w-full sm:w-auto" onClick={registerClient} iconRight={<ArrowRight aria-hidden="true" className="size-4" />}>Creează cont</Button>
          </div>
        </section>
      </main>
      <footer className="mx-auto flex max-w-7xl flex-col gap-5 px-5 py-8 text-xs text-slate-600 sm:flex-row sm:items-center sm:justify-between sm:px-6 lg:px-8 dark:text-slate-300">
        <p><span className="font-bold text-slate-900 dark:text-white">Skillink</span><span className="mx-2" aria-hidden="true">/</span>Clienți și profesioniști. Un punct de întâlnire.</p>
        <a href="#inceput" className="inline-flex min-h-11 items-center gap-2 self-start font-semibold hover:text-indigo-600 dark:hover:text-indigo-300">Înapoi sus<ArrowRight aria-hidden="true" className="size-3 -rotate-90" /></a>
      </footer>
      {auth && <AuthModal isOpen initialMode={auth.mode} initialRole={auth.role} onClose={() => setAuth(null)} />}
    </div>
  );
}
