import React, { useState } from 'react';
import { useApp } from '../../context/AppContext';
import { Send, Phone, Mail, CheckCircle2, User, AlertTriangle } from 'lucide-react';
import { Button } from '../ui/Button';
import { Badge } from '../ui/Badge';
import { DESIGN_TOKENS } from '../../theme/designSystem';

interface DesktopChatViewProps {
  onOpenDispute?: (leadId: string) => void;
}

export const DesktopChatView: React.FC<DesktopChatViewProps> = ({ onOpenDispute }) => {
  const {
    leads,
    activeLeadId,
    setActiveLeadId,
    chatMessages,
    sendChatMessage,
    userRole,
    proProfile,
    isLeadClaimedByMe
  } = useApp();

  const [messageInput, setMessageInput] = useState('');

  const activeChatLeads = leads.filter(l => {
    if (userRole === 'admin') return true;
    if (userRole === 'client') return l.claimedPros.length > 0 || (chatMessages[l.id] && chatMessages[l.id].length > 0);
    return isLeadClaimedByMe(l.id) || (chatMessages[l.id] && chatMessages[l.id].length > 0);
  });
  const currentLead = leads.find(l => l.id === activeLeadId) || activeChatLeads[0] || null;
  const currentMessages = currentLead ? chatMessages[currentLead.id] || [] : [];

  const handleSend = (e: React.FormEvent) => {
    e.preventDefault();
    if (!messageInput.trim() || !currentLead) return;
    sendChatMessage(currentLead.id, messageInput);
    setMessageInput('');
  };

  if (activeChatLeads.length === 0) {
    return (
      <div className="h-full flex flex-col items-center justify-center p-8 text-center bg-slate-50/50 dark:bg-slate-950">
        <div className="w-14 h-14 rounded-2xl bg-indigo-500/10 text-indigo-600 flex items-center justify-center mb-3">
          <User className="w-7 h-7" />
        </div>
        <h3 className="text-base font-bold text-slate-900 dark:text-white mb-1">
          Nicio conversație activă încă
        </h3>
        <p className="text-xs text-slate-500 max-w-md mb-4">
          Cumpără un lead din feed sau publică o solicitare nouă pentru a deschide canalul de comunicare directă în timp real.
        </p>
      </div>
    );
  }

  return (
    <div className="h-full grid grid-cols-1 md:grid-cols-12 bg-white dark:bg-slate-950 overflow-hidden">
      {/* Left Chat Contact List */}
      <div className="md:col-span-4 border-r border-slate-200/90 dark:border-slate-800 flex flex-col h-full bg-slate-50/60 dark:bg-slate-900/40">
        <div className="p-4 border-b border-slate-200/90 dark:border-slate-800 flex items-center justify-between">
          <div>
            <h3 className="text-xs font-bold text-slate-800 dark:text-slate-200 uppercase tracking-wider">
              Conversații Active
            </h3>
            <span className="text-[11px] text-slate-400">Canal securizat WebSocket</span>
          </div>
          <Badge variant="emerald">{activeChatLeads.length} active</Badge>
        </div>

        <div className="flex-1 overflow-y-auto divide-y divide-slate-100 dark:divide-slate-800">
          {activeChatLeads.map(lead => {
            const isSelected = currentLead?.id === lead.id;
            const msgs = chatMessages[lead.id] || [];
            const lastMsg = msgs[msgs.length - 1];

            return (
              <div
                key={lead.id}
                onClick={() => setActiveLeadId(lead.id)}
                className={`p-3.5 cursor-pointer transition-all duration-150 ${
                  isSelected
                    ? 'bg-gradient-to-r from-indigo-50/90 via-violet-50/40 to-white dark:from-indigo-950/40 dark:via-slate-900 dark:to-slate-900 border-l-4 border-indigo-600'
                    : 'hover:bg-slate-100/70 dark:hover:bg-slate-900'
                }`}
              >
                <div className="flex items-center justify-between text-xs mb-1">
                  <span className="font-bold text-slate-900 dark:text-white truncate max-w-[170px]">
                    {lead.client.name.split('(')[0]}
                  </span>
                  <span className="text-[10px] text-slate-400 font-mono">
                    {lastMsg?.timestamp || 'Activ'}
                  </span>
                </div>
                <p className="text-[11px] font-semibold text-indigo-600 dark:text-indigo-400 truncate mb-1">
                  {lead.title}
                </p>
                <p className="text-xs text-slate-500 dark:text-slate-400 truncate">
                  {lastMsg?.content || 'Începeți conversația...'}
                </p>
              </div>
            );
          })}
        </div>
      </div>

      {/* Right Direct Messaging Canvas */}
      <div className="md:col-span-8 flex flex-col h-full bg-white dark:bg-slate-950">
        {currentLead ? (
          <>
            {/* Top Chat Bar with Client Info */}
            <div className="p-4 border-b border-slate-200/90 dark:border-slate-800 flex items-center justify-between bg-white dark:bg-slate-900">
              <div>
                <h4 className="text-sm font-bold text-slate-900 dark:text-white flex items-center gap-2">
                  <span>{currentLead.client.name}</span>
                  <Badge variant="emerald" icon={<CheckCircle2 className="w-3 h-3 text-emerald-600" />}>
                    Contact deblocat
                  </Badge>
                </h4>
                <div className="flex items-center gap-3 text-xs text-slate-500 dark:text-slate-400 mt-1">
                  <span className="flex items-center gap-1">
                    <Phone className="w-3.5 h-3.5 text-indigo-500" />
                    <span className="font-mono font-medium text-slate-800 dark:text-slate-200">{currentLead.client.phone}</span>
                  </span>
                  <span>·</span>
                  <span className="flex items-center gap-1">
                    <Mail className="w-3.5 h-3.5 text-indigo-500" />
                    <span className="font-mono font-medium text-slate-800 dark:text-slate-200">{currentLead.client.email}</span>
                  </span>
                </div>
              </div>

              {onOpenDispute && (
                <Button
                  variant="outline"
                  size="xs"
                  icon={<AlertTriangle className="w-3.5 h-3.5 text-amber-500" />}
                  onClick={() => onOpenDispute(currentLead.id)}
                >
                  Sesizare
                </Button>
              )}
            </div>

            {/* Message Area */}
            <div className="flex-1 overflow-y-auto p-6 space-y-4 bg-gradient-to-b from-indigo-50/20 to-white dark:from-slate-950 dark:to-slate-950">
              {currentMessages.map(msg => {
                const isMe =
                  (userRole === 'pro' && msg.senderRole === 'pro') ||
                  (userRole === 'client' && msg.senderRole === 'client');
                const isSystem = msg.senderRole === 'system';

                if (isSystem) {
                  return (
                    <div key={msg.id} className="text-center my-3">
                      <span className="inline-block px-3.5 py-1.5 rounded-full bg-indigo-50 dark:bg-slate-800 border border-indigo-100 dark:border-slate-700 text-[11px] font-medium text-indigo-800 dark:text-indigo-300 shadow-2xs">
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
                    <span className="text-[10px] text-slate-400 mb-1 px-1 font-medium">
                      {msg.senderName} · {msg.timestamp}
                    </span>
                    <div
                      className={`max-w-[75%] rounded-2xl px-4 py-2.5 text-xs sm:text-sm leading-relaxed shadow-2xs ${
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

            {/* Quick Proposal & Input Bar */}
            <div className="p-4 border-t border-slate-200/90 dark:border-slate-800 bg-white dark:bg-slate-900 space-y-2.5">
              <div className="flex items-center gap-2 overflow-x-auto pb-1 text-xs">
                <button
                  type="button"
                  onClick={() =>
                    setMessageInput('Bună ziua! Am analizat specificațiile din Lead Passport și vă pot oferi un termen ferm de livrare.')
                  }
                  className="px-3 py-1.5 rounded-xl bg-indigo-50 dark:bg-indigo-950/60 hover:bg-indigo-100 text-indigo-700 dark:text-indigo-300 text-[11px] font-semibold whitespace-nowrap transition-colors border border-indigo-200/80 dark:border-indigo-900 cursor-pointer"
                >
                  ⚡ Ofertă fermă
                </button>
                <button
                  type="button"
                  onClick={() =>
                    setMessageInput('Când doriți să stabilim un apel telefonic scurt de 10 minute pentru clarificare?')
                  }
                  className="px-3 py-1.5 rounded-xl bg-violet-50 dark:bg-violet-950/60 hover:bg-violet-100 text-violet-700 dark:text-violet-300 text-[11px] font-semibold whitespace-nowrap transition-colors border border-violet-200/80 dark:border-violet-900 cursor-pointer"
                >
                  📞 Programează apel
                </button>
              </div>

              <form onSubmit={handleSend} className="flex items-center gap-2">
                <input
                  type="text"
                  placeholder="Scrie un mesaj clientului..."
                  value={messageInput}
                  onChange={e => setMessageInput(e.target.value)}
                  className={DESIGN_TOKENS.inputs.base}
                />
                <Button
                  type="submit"
                  variant="primary"
                  size="md"
                  disabled={!messageInput.trim()}
                  icon={<Send className="w-4 h-4" />}
                />
              </form>
            </div>
          </>
        ) : (
          <div className="h-full flex items-center justify-center p-8 text-center text-xs text-slate-400">
            Selectează o conversație din panoul din stânga.
          </div>
        )}
      </div>
    </div>
  );
};
