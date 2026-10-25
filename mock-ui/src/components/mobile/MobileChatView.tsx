import React, { useState } from 'react';
import { useApp } from '../../context/AppContext';
import { Send, Phone, ChevronLeft, User } from 'lucide-react';
import { Button } from '../ui/Button';
import { Badge } from '../ui/Badge';
import { DESIGN_TOKENS } from '../../theme/designSystem';

export const MobileChatView: React.FC = () => {
  const { leads, chatMessages, sendChatMessage, userRole, proProfile, isLeadClaimedByMe } = useApp();

  const activeChatLeads = leads.filter(l => isLeadClaimedByMe(l.id) || chatMessages[l.id]?.length > 0);
  const [selectedLeadId, setSelectedLeadId] = useState<string | null>(activeChatLeads[0]?.id || null);
  const [msgInput, setMsgInput] = useState('');

  const currentLead = leads.find(l => l.id === selectedLeadId) || null;
  const currentMessages = currentLead ? chatMessages[currentLead.id] || [] : [];

  const handleSend = (e: React.FormEvent) => {
    e.preventDefault();
    if (!msgInput.trim() || !currentLead) return;
    sendChatMessage(currentLead.id, msgInput);
    setMsgInput('');
  };

  if (activeChatLeads.length === 0) {
    return (
      <div className="h-full flex flex-col items-center justify-center p-6 text-center text-slate-400 bg-slate-50/50 dark:bg-slate-950">
        <div className="w-14 h-14 rounded-2xl bg-indigo-500/10 text-indigo-600 flex items-center justify-center mb-2">
          <User className="w-7 h-7" />
        </div>
        <p className="text-sm font-bold text-slate-800 dark:text-slate-200">Nicio conversație activă</p>
        <p className="text-xs text-slate-500 mt-1">Cumpără un lead sau publică o solicitare pentru a iniția discuția directă.</p>
      </div>
    );
  }

  // If a lead chat is opened on mobile
  if (currentLead) {
    return (
      <div className="h-full flex flex-col bg-white dark:bg-slate-950 overflow-hidden">
        {/* Chat Header */}
        <div className="px-4 py-3 bg-white dark:bg-slate-900 border-b border-indigo-100 dark:border-slate-800 flex items-center justify-between shrink-0">
          <div className="flex items-center gap-2">
            <button
              onClick={() => setSelectedLeadId(null)}
              className="p-1 -ml-1 text-slate-500 hover:text-slate-800 dark:hover:text-white cursor-pointer"
            >
              <ChevronLeft className="w-5 h-5" />
            </button>
            <div>
              <h4 className="text-xs font-bold text-slate-900 dark:text-white line-clamp-1">
                {currentLead.client.name.split('(')[0]}
              </h4>
              <span className="text-[10px] text-emerald-600 dark:text-emerald-400 font-mono font-medium">
                {currentLead.client.phone}
              </span>
            </div>
          </div>

          <a
            href={`tel:${currentLead.client.phone}`}
            className="p-2 rounded-xl bg-emerald-50 dark:bg-emerald-950/60 text-emerald-600 dark:text-emerald-400 border border-emerald-200"
          >
            <Phone className="w-4 h-4" />
          </a>
        </div>

        {/* Message Bubble Feed */}
        <div className="flex-1 overflow-y-auto p-4 space-y-3 bg-slate-50/40 dark:bg-slate-950">
          {currentMessages.map(msg => {
            const isMe =
              (userRole === 'pro' && msg.senderRole === 'pro') ||
              (userRole === 'client' && msg.senderRole === 'client');
            const isSystem = msg.senderRole === 'system';

            if (isSystem) {
              return (
                <div key={msg.id} className="text-center my-2">
                  <span className="inline-block px-3 py-1 rounded-full bg-indigo-50 dark:bg-slate-800 border border-indigo-100 dark:border-slate-700 text-[10px] text-indigo-700 dark:text-indigo-300">
                    {msg.content}
                  </span>
                </div>
              );
            }

            return (
              <div
                key={msg.id}
                className={`flex flex-col ${isMe ? 'items-end' : 'items-start'}`}
              >
                <span className="text-[10px] text-slate-400 mb-0.5 px-1 font-mono">
                  {msg.timestamp}
                </span>
                <div
                  className={`max-w-[80%] rounded-2xl px-3.5 py-2 text-xs leading-relaxed shadow-2xs ${
                    isMe
                      ? 'bg-gradient-to-r from-indigo-600 to-violet-600 text-white rounded-br-xs'
                      : 'bg-white dark:bg-slate-800 text-slate-800 dark:text-slate-100 border border-slate-200/90 dark:border-slate-700 rounded-bl-xs'
                  }`}
                >
                  {msg.content}
                </div>
              </div>
            );
          })}
        </div>

        {/* Input Bar */}
        <form
          onSubmit={handleSend}
          className="p-3 bg-white dark:bg-slate-900 border-t border-slate-200/90 dark:border-slate-800 flex items-center gap-2 shrink-0"
        >
          <input
            type="text"
            placeholder="Scrie mesaj..."
            value={msgInput}
            onChange={e => setMsgInput(e.target.value)}
            className={DESIGN_TOKENS.inputs.base}
          />
          <Button
            type="submit"
            variant="primary"
            size="md"
            disabled={!msgInput.trim()}
            icon={<Send className="w-4 h-4" />}
          />
        </form>
      </div>
    );
  }

  // Conversation list view
  return (
    <div className="h-full flex flex-col bg-white dark:bg-slate-950 divide-y divide-slate-100 dark:divide-slate-800 overflow-y-auto">
      <div className="p-4 bg-gradient-to-r from-indigo-50/60 to-white dark:from-slate-900 dark:to-slate-900 flex items-center justify-between">
        <h3 className="text-xs font-bold text-slate-800 dark:text-slate-200 uppercase tracking-wider">
          Conversații Directe
        </h3>
        <Badge variant="indigo">{activeChatLeads.length} active</Badge>
      </div>

      {activeChatLeads.map(lead => {
        const msgs = chatMessages[lead.id] || [];
        const lastMsg = msgs[msgs.length - 1];

        return (
          <div
            key={lead.id}
            onClick={() => setSelectedLeadId(lead.id)}
            className="p-4 active:bg-indigo-50/50 dark:active:bg-slate-900 flex items-center justify-between cursor-pointer transition-colors"
          >
            <div>
              <h4 className="text-xs font-bold text-slate-900 dark:text-white">
                {lead.client.name}
              </h4>
              <p className="text-[11px] font-semibold text-indigo-600 dark:text-indigo-400 line-clamp-1">
                {lead.title}
              </p>
              <p className="text-xs text-slate-500 dark:text-slate-400 line-clamp-1 mt-0.5">
                {lastMsg?.content || 'Apasă pentru a deschide chatul'}
              </p>
            </div>

            <span className="text-[10px] text-slate-400 font-mono">
              {lastMsg?.timestamp || ''}
            </span>
          </div>
        );
      })}
    </div>
  );
};
