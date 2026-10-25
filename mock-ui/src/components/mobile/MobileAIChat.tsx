import React, { useState } from 'react';
import { useApp } from '../../context/AppContext';
import { Sparkles, Send, RefreshCw, CheckCircle2, ArrowRight } from 'lucide-react';
import { Button } from '../ui/Button';
import { Badge } from '../ui/Badge';
import { DESIGN_TOKENS } from '../../theme/designSystem';

export const MobileAIChat: React.FC = () => {
  const { aiMessages, sendAIMessage, resetAIMessages, createLead, setMobileActiveTab } = useApp();
  const [inputVal, setInputVal] = useState('');
  const [isPublishing, setIsPublishing] = useState(false);
  const [success, setSuccess] = useState(false);

  const latestSpecMessage = [...aiMessages].reverse().find(m => m.extractedSpec);
  const currentSpec = latestSpecMessage?.extractedSpec;

  const handleSend = (e: React.FormEvent) => {
    e.preventDefault();
    if (!inputVal.trim()) return;
    sendAIMessage(inputVal);
    setInputVal('');
  };

  const handlePublish = () => {
    if (!currentSpec) return;
    setIsPublishing(true);
    setTimeout(() => {
      createLead({
        title: currentSpec.title,
        category: currentSpec.category,
        subcategory: currentSpec.subcategory,
        description: `Proiect structurat pe mobil prin AI Project Architect: ${currentSpec.title}.`,
        location: currentSpec.location || 'București & Ilfov',
        budget: currentSpec.budget,
        timeline: currentSpec.timeline,
        leadPrice: currentSpec.leadPrice || 30,
        leadPassport: currentSpec.leadPassport
      });
      setIsPublishing(false);
      setSuccess(true);
      setTimeout(() => {
        setMobileActiveTab('leads');
      }, 1200);
    }, 500);
  };

  return (
    <div className="h-full flex flex-col bg-slate-50/50 dark:bg-slate-950 overflow-hidden">
      {/* Mobile Assistant Header */}
      <div className="px-4 py-3 bg-white dark:bg-slate-900 border-b border-indigo-100 dark:border-slate-800 flex items-center justify-between shrink-0">
        <div className="flex items-center gap-2.5">
          <div className="w-8 h-8 rounded-xl bg-gradient-to-tr from-indigo-600 to-violet-600 text-white flex items-center justify-center shadow-xs">
            <Sparkles className="w-4 h-4" />
          </div>
          <div>
            <div className="flex items-center gap-1.5">
              <h3 className="text-xs font-bold text-slate-900 dark:text-white">AI Project Architect</h3>
              <Badge variant="indigo">Elicitare</Badge>
            </div>
            <span className="text-[10px] text-slate-400">Ghidare conversațională</span>
          </div>
        </div>

        <button
          onClick={resetAIMessages}
          className="p-1.5 text-slate-400 hover:text-slate-600 dark:hover:text-white cursor-pointer"
        >
          <RefreshCw className="w-3.5 h-3.5" />
        </button>
      </div>

      {/* Message Feed */}
      <div className="flex-1 overflow-y-auto p-4 space-y-3">
        {aiMessages.map(msg => (
          <div
            key={msg.id}
            className={`flex flex-col ${msg.sender === 'user' ? 'items-end' : 'items-start'}`}
          >
            <div
              className={`max-w-[85%] rounded-2xl p-3.5 text-xs leading-relaxed shadow-2xs ${
                msg.sender === 'user'
                  ? 'bg-gradient-to-r from-indigo-600 to-violet-600 text-white rounded-br-xs'
                  : 'bg-white dark:bg-slate-800 text-slate-800 dark:text-slate-100 border border-slate-200/90 dark:border-slate-700 rounded-bl-xs'
              }`}
            >
              <p className="whitespace-pre-line">{msg.text}</p>
            </div>

            {msg.suggestedPicks && (
              <div className="mt-2 flex flex-wrap gap-1.5 max-w-[95%]">
                {msg.suggestedPicks.map((pick, i) => (
                  <button
                    key={i}
                    onClick={() => sendAIMessage(pick)}
                    className="text-[11px] px-3 py-1.5 rounded-xl bg-indigo-50 dark:bg-indigo-950/60 border border-indigo-200/90 dark:border-indigo-900 text-indigo-700 dark:text-indigo-300 text-left active:scale-[0.98] transition-transform font-medium cursor-pointer"
                  >
                    {pick}
                  </button>
                ))}
              </div>
            )}
          </div>
        ))}

        {/* Spec Preview Card if available */}
        {currentSpec && (
          <div className="p-4 rounded-2xl bg-white dark:bg-slate-900 border border-indigo-200/90 dark:border-indigo-900 shadow-xs space-y-2.5 text-xs">
            <div className="flex items-center justify-between">
              <Badge variant="indigo">Proiect Identificat</Badge>
              <span className="font-mono text-emerald-600 font-extrabold">
                {currentSpec.leadPassport?.completenessScore}% Passport
              </span>
            </div>
            <h4 className="font-bold text-slate-900 dark:text-white text-sm">{currentSpec.title}</h4>
            <div className="text-[11px] text-slate-500 font-medium">
              Buget estimat: {currentSpec.budget?.min} - {currentSpec.budget?.max} {currentSpec.budget?.currency}
            </div>

            <Button
              variant="primary"
              size="md"
              className="w-full mt-2"
              disabled={isPublishing}
              isLoading={isPublishing}
              iconRight={<ArrowRight className="w-4 h-4" />}
              onClick={handlePublish}
            >
              Publică Gratuit Solicitarea
            </Button>
          </div>
        )}

        {success && (
          <div className="p-3.5 rounded-2xl bg-emerald-50 dark:bg-emerald-950 text-emerald-800 dark:text-emerald-200 text-xs flex items-center gap-2 border border-emerald-200">
            <CheckCircle2 className="w-4 h-4 text-emerald-600 shrink-0" />
            <span className="font-medium">Solicitarea a fost publicată! Te redirecționăm...</span>
          </div>
        )}
      </div>

      {/* Input Form */}
      <form
        onSubmit={handleSend}
        className="p-3 bg-white dark:bg-slate-900 border-t border-slate-200/90 dark:border-slate-800 flex items-center gap-2 shrink-0"
      >
        <input
          type="text"
          placeholder="Ex: Vreau magazin online flori..."
          value={inputVal}
          onChange={e => setInputVal(e.target.value)}
          className={DESIGN_TOKENS.inputs.base}
        />
        <Button
          type="submit"
          variant="primary"
          size="md"
          disabled={!inputVal.trim()}
          icon={<Send className="w-4 h-4" />}
        />
      </form>
    </div>
  );
};
