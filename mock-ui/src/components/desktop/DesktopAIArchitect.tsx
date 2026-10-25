import React, { useState } from 'react';
import { useApp } from '../../context/AppContext';
import { Sparkles, Send, RefreshCw, CheckCircle2, ArrowRight, ShieldCheck, FileCheck } from 'lucide-react';
import { Lead } from '../../types';
import { Button } from '../ui/Button';
import { Badge } from '../ui/Badge';
import { DESIGN_TOKENS } from '../../theme/designSystem';

interface DesktopAIArchitectProps {
  onPublished?: (lead: Lead) => void;
}

export const DesktopAIArchitect: React.FC<DesktopAIArchitectProps> = ({ onPublished }) => {
  const { aiMessages, sendAIMessage, resetAIMessages, createLead, setActiveLeadId, userRole } = useApp();
  const [inputText, setInputText] = useState('');
  const [isPublishing, setIsPublishing] = useState(false);
  const [publishedSuccess, setPublishedSuccess] = useState<Lead | null>(null);

  // Latest extracted spec from AI messages
  const latestSpecMessage = [...aiMessages].reverse().find(m => m.extractedSpec);
  const currentSpec = latestSpecMessage?.extractedSpec;

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!inputText.trim()) return;
    sendAIMessage(inputText);
    setInputText('');
  };

  const handlePublish = () => {
    if (!currentSpec) return;
    setIsPublishing(true);
    setTimeout(() => {
      const created = createLead({
        title: currentSpec.title,
        category: currentSpec.category,
        subcategory: currentSpec.subcategory,
        description: `Proiect structurat prin AI Project Architect: ${currentSpec.title}. Solicitare verificată pentru profesioniști calificați.`,
        location: currentSpec.location || 'București & Ilfov',
        budget: currentSpec.budget,
        timeline: currentSpec.timeline,
        leadPrice: currentSpec.leadPrice || 30,
        leadPassport: currentSpec.leadPassport
      });
      setIsPublishing(false);
      setPublishedSuccess(created);
      setActiveLeadId(created.id);
      if (onPublished) onPublished(created);
    }, 600);
  };

  return (
    <div className="h-full flex flex-col bg-slate-50/50 dark:bg-slate-950 overflow-hidden">
      {/* Top Banner / Explanation adapting to Role */}
      <div className="px-6 py-4 bg-white/90 dark:bg-slate-900/90 backdrop-blur-xs border-b border-indigo-100 dark:border-slate-800 flex items-center justify-between">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-2xl bg-gradient-to-tr from-indigo-600 via-indigo-500 to-violet-600 text-white flex items-center justify-center shadow-xs">
            <Sparkles className="w-5 h-5" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <h2 className="text-base font-bold text-slate-900 dark:text-white">
                {userRole === 'client'
                  ? 'AI Project Architect (Client Mode)'
                  : userRole === 'admin'
                  ? 'AI Rule Engine & Gatekeeper (Admin Mode)'
                  : 'AI Proposal Copilot & Estimator (Pro Mode)'}
              </h2>
              <Badge variant={userRole === 'client' ? 'emerald' : userRole === 'admin' ? 'violet' : 'indigo'}>
                {userRole === 'client'
                  ? 'Elicitare Conversațională'
                  : userRole === 'admin'
                  ? 'Configurare Gate'
                  : 'Generator Oferte Tehnice'}
              </Badge>
            </div>
            <p className="text-xs text-slate-500 dark:text-slate-400">
              {userRole === 'client'
                ? 'Transformă nevoile exprimate liber în specificații structurate și compatibile cu algoritmul de matching.'
                : userRole === 'admin'
                ? 'Ajustează pragurile automate de completitudine Lead Passport (minim 75%) și filtrele anti-fraudă.'
                : 'Generează oferte tehnice competitive, devize pe etape și mesaje introductive profesionale către clienți.'}
            </p>
          </div>
        </div>

        <Button
          variant="outline"
          size="sm"
          icon={<RefreshCw className="w-3.5 h-3.5" />}
          onClick={resetAIMessages}
        >
          Resetează Dialogul
        </Button>
      </div>

      {/* Main Split Body: Conversation on Left, Live Structured Spec on Right */}
      <div className="flex-1 grid grid-cols-1 lg:grid-cols-12 overflow-hidden">
        {/* Left: Chat Flow */}
        <div className="lg:col-span-7 flex flex-col h-full border-r border-slate-200/90 dark:border-slate-800 bg-white dark:bg-slate-900">
          {/* Message List */}
          <div className="flex-1 overflow-y-auto p-6 space-y-4">
            {aiMessages.map(msg => (
              <div
                key={msg.id}
                className={`flex flex-col ${msg.sender === 'user' ? 'items-end' : 'items-start'}`}
              >
                <div
                  className={`max-w-[85%] rounded-2xl p-4 text-xs sm:text-sm leading-relaxed shadow-2xs ${
                    msg.sender === 'user'
                      ? 'bg-gradient-to-r from-indigo-600 to-violet-600 text-white rounded-br-xs'
                      : 'bg-slate-50 dark:bg-slate-800/80 text-slate-900 dark:text-slate-100 border border-slate-200/80 dark:border-slate-700 rounded-bl-xs'
                  }`}
                >
                  <p className="whitespace-pre-line">{msg.text}</p>
                </div>

                {/* Suggested Quick Options */}
                {msg.suggestedPicks && msg.suggestedPicks.length > 0 && (
                  <div className="mt-3 flex flex-wrap gap-2 max-w-[90%]">
                    {msg.suggestedPicks.map((pick, i) => (
                      <button
                        key={i}
                        onClick={() => sendAIMessage(pick)}
                        className="text-xs px-3 py-1.5 rounded-xl border border-indigo-200/90 dark:border-indigo-900/60 bg-gradient-to-r from-indigo-50/90 to-violet-50/50 dark:from-indigo-950/40 dark:to-violet-950/30 hover:border-indigo-400 dark:hover:border-indigo-600 text-indigo-900 dark:text-indigo-200 text-left transition-all cursor-pointer active:scale-[0.98] shadow-2xs"
                      >
                        {pick}
                      </button>
                    ))}
                  </div>
                )}
              </div>
            ))}
          </div>

          {/* Input Box */}
          <form
            onSubmit={handleSubmit}
            className="p-4 border-t border-slate-200/90 dark:border-slate-800 bg-white dark:bg-slate-900 flex items-center gap-2"
          >
            <input
              type="text"
              placeholder="Descrie proiectul tău în limbaj natural (ex: florărie online cu livrări)..."
              value={inputText}
              onChange={e => setInputText(e.target.value)}
              className={DESIGN_TOKENS.inputs.base}
            />
            <Button
              type="submit"
              variant="primary"
              size="md"
              disabled={!inputText.trim()}
              icon={<Send className="w-4 h-4" />}
            />
          </form>
        </div>

        {/* Right: Live Structured Lead & Passport Preview */}
        <div className="lg:col-span-5 p-6 overflow-y-auto bg-slate-50/70 dark:bg-slate-950 flex flex-col justify-between">
          <div className="space-y-5">
            <div className="flex items-center justify-between">
              <h3 className="text-sm font-bold text-slate-900 dark:text-white flex items-center gap-2">
                <FileCheck className="w-4 h-4 text-emerald-500" />
                Specificație Proiect Structurat
              </h3>
              <Badge variant="indigo">JSON-Schema V1</Badge>
            </div>

            {currentSpec ? (
              <div className="space-y-4">
                {/* Title and Category */}
                <div className="p-4 rounded-2xl bg-white dark:bg-slate-900 border border-indigo-100 dark:border-slate-800 space-y-2 shadow-2xs">
                  <div className="flex items-center gap-1.5">
                    <Badge variant="indigo">{currentSpec.category}</Badge>
                    <span className="text-[11px] text-slate-500">{currentSpec.subcategory}</span>
                  </div>

                  <h4 className="text-sm font-bold text-slate-900 dark:text-white">
                    {currentSpec.title}
                  </h4>

                  <div className="grid grid-cols-2 gap-2 text-xs pt-2 border-t border-slate-100 dark:border-slate-800">
                    <div>
                      <span className="text-slate-400 block text-[10px] uppercase font-semibold">Buget estimat</span>
                      <span className="font-mono font-bold text-slate-900 dark:text-slate-200 tabular-nums">
                        {currentSpec.budget?.min} - {currentSpec.budget?.max} {currentSpec.budget?.currency}
                      </span>
                    </div>
                    <div>
                      <span className="text-slate-400 block text-[10px] uppercase font-semibold">Termen dorit</span>
                      <span className="font-semibold text-slate-800 dark:text-slate-200">
                        {currentSpec.timeline}
                      </span>
                    </div>
                  </div>
                </div>

                {/* Lead Passport Quality Preview */}
                {currentSpec.leadPassport && (
                  <div className="p-4 rounded-2xl bg-gradient-to-br from-emerald-50/60 to-white dark:from-slate-900 dark:to-slate-900 border border-emerald-200/80 dark:border-emerald-800/60 space-y-3 shadow-2xs">
                    <div className="flex items-center justify-between pb-2 border-b border-emerald-100 dark:border-emerald-900/40">
                      <span className="text-xs font-bold text-slate-800 dark:text-slate-200 flex items-center gap-1.5">
                        <ShieldCheck className="w-4 h-4 text-emerald-500" />
                        Scor Calificare Lead Passport
                      </span>
                      <span className="text-xs font-mono font-extrabold text-emerald-600 dark:text-emerald-400">
                        {currentSpec.leadPassport.completenessScore}%
                      </span>
                    </div>

                    <div className="text-xs text-slate-600 dark:text-slate-300">
                      <span className="font-bold text-slate-700 dark:text-slate-300 block mb-1">
                        Livrabile identificate:
                      </span>
                      <ul className="space-y-1">
                        {currentSpec.leadPassport.requiredDeliverables.map((d, i) => (
                          <li key={i} className="flex items-start gap-1.5 text-[11px]">
                            <span className="text-emerald-500 font-bold">✓</span>
                            <span>{d}</span>
                          </li>
                        ))}
                      </ul>
                    </div>
                  </div>
                )}
              </div>
            ) : (
              <div className="p-8 text-center text-xs text-slate-400 border-2 border-dashed border-indigo-200 dark:border-slate-800 rounded-2xl">
                Începe conversația din stânga. AI Project Architect va extrage automat cerințele într-o structură completă.
              </div>
            )}

            {publishedSuccess && (
              <div className="p-4 rounded-2xl bg-emerald-50 dark:bg-emerald-950/50 border border-emerald-200 dark:border-emerald-800 text-xs text-emerald-900 dark:text-emerald-200 flex items-center gap-2.5">
                <CheckCircle2 className="w-5 h-5 text-emerald-500 shrink-0" />
                <div>
                  <strong>Solicitare publicată cu succes!</strong> Profesioniștii compatibili din categoria aleasă au fost notificați prin matching explicabil.
                </div>
              </div>
            )}
          </div>

          {/* Action bottom */}
          <div className="pt-6">
            <Button
              variant="primary"
              size="lg"
              className="w-full"
              disabled={!currentSpec}
              isLoading={isPublishing}
              iconRight={<ArrowRight className="w-4 h-4" />}
              onClick={handlePublish}
            >
              Publică Solicitarea în Marketplace (Gratuit)
            </Button>
            <p className="text-[11px] text-center text-slate-400 mt-2 font-medium">
              Clienții publică gratuit · Profesioniștii acceptă lead-ul pe bază de credite
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};
