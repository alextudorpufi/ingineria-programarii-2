import React, { useState } from 'react';
import { useApp } from '../../context/AppContext';
import { SERVICE_CATEGORIES } from '../../data/mockData';
import { Search, MapPin, Wallet, Users, Check, Sparkles, Plus, ShieldCheck, AlertCircle, PhoneCall } from 'lucide-react';
import { Input } from '../ui/Input';
import { Badge } from '../ui/Badge';
import { Button } from '../ui/Button';
import { SkeletonFeed } from '../ui/Skeleton';

export const DesktopLeadFeed: React.FC = () => {
  const {
    leads,
    isLoadingLeads,
    activeLeadId,
    setActiveLeadId,
    selectedCategory,
    setSelectedCategory,
    searchQuery,
    setSearchQuery,
    isLeadClaimedByMe,
    userRole,
    setDesktopActiveTab,
    setShowAIArchitect
  } = useApp();

  const [filterType, setFilterType] = useState<'all' | 'available' | 'claimed'>('all');
  const [adminStatusFilter, setAdminStatusFilter] = useState<'all' | 'validat' | 'audit'>('all');

  const filteredLeads = leads.filter(lead => {
    // Role-specific filtering
    if (userRole === 'admin' && adminStatusFilter !== 'all') {
      if (adminStatusFilter === 'validat' && lead.status !== 'validat') return false;
      if (adminStatusFilter === 'audit' && lead.status === 'validat') return false;
    }

    // Category filter
    if (selectedCategory !== 'all') {
      const catObj = SERVICE_CATEGORIES.find(c => c.id === selectedCategory);
      if (catObj && lead.category !== catObj.name) return false;
    }

    // Filter type (available vs claimed) for Pro
    if (userRole === 'pro') {
      const claimed = isLeadClaimedByMe(lead.id);
      if (filterType === 'available' && claimed) return false;
      if (filterType === 'claimed' && !claimed) return false;
    }

    // Search query
    if (searchQuery.trim()) {
      const q = searchQuery.toLowerCase();
      return (
        lead.title.toLowerCase().includes(q) ||
        lead.description.toLowerCase().includes(q) ||
        lead.location.toLowerCase().includes(q) ||
        lead.category.toLowerCase().includes(q)
      );
    }

    return true;
  });

  return (
    <div className="h-full flex flex-col border-r border-slate-200/90 dark:border-slate-800 bg-white dark:bg-slate-950">
      {/* Top Search & Filter Bar */}
      <div className="p-4 border-b border-slate-200/90 dark:border-slate-800 space-y-3 bg-gradient-to-b from-indigo-50/40 to-white dark:from-slate-900/60 dark:to-slate-950">
        {/* Role Banner / Subtitle */}
        {userRole === 'client' && (
          <div className="flex items-center justify-between pb-1">
            <div>
              <h2 className="text-xs font-bold uppercase tracking-wider text-emerald-600 dark:text-emerald-400">
                Panou Client
              </h2>
              <p className="text-xs font-semibold text-slate-800 dark:text-slate-200">
                Cererile Tale Postate & Oferte Primite
              </p>
            </div>
            <Button
              variant="primary"
              size="sm"
              icon={<Plus className="w-3.5 h-3.5" />}
              onClick={() => {
                setDesktopActiveTab('ai');
                setShowAIArchitect(true);
              }}
            >
              Cerere Nouă
            </Button>
          </div>
        )}

        {userRole === 'admin' && (
          <div className="flex items-center justify-between pb-1">
            <div>
              <h2 className="text-xs font-bold uppercase tracking-wider text-purple-600 dark:text-purple-400">
                Gate Securitate & Moderare
              </h2>
              <p className="text-xs font-semibold text-slate-800 dark:text-slate-200">
                Audit Lead-uri & Verificare Contacte
              </p>
            </div>
            <span className="text-[10px] font-mono font-bold px-2 py-0.5 rounded bg-purple-100 dark:bg-purple-950/80 text-purple-700 dark:text-purple-300 border border-purple-200 dark:border-purple-800">
              {leads.length} Total În Sistem
            </span>
          </div>
        )}

        {/* Search using standardized Input component */}
        <Input
          placeholder={
            userRole === 'client'
              ? 'Caută în cererile tale...'
              : userRole === 'admin'
              ? 'Caută după client, telefon, ID lead...'
              : 'Caută servicii, oraș, cerințe...'
          }
          value={searchQuery}
          onChange={e => setSearchQuery(e.target.value)}
          leftIcon={<Search className="w-4 h-4 text-indigo-500" />}
        />

        {/* Categories Scroller */}
        <div className="flex items-center gap-1.5 overflow-x-auto pb-1 text-xs no-scrollbar">
          <button
            onClick={() => setSelectedCategory('all')}
            className={`px-3 py-1.5 rounded-xl font-semibold whitespace-nowrap transition-all duration-150 cursor-pointer ${
              selectedCategory === 'all'
                ? 'bg-gradient-to-r from-indigo-600 to-violet-600 text-white shadow-xs'
                : 'bg-slate-100 hover:bg-indigo-50 text-slate-600 dark:bg-slate-800 dark:hover:bg-slate-700 dark:text-slate-300'
            }`}
          >
            Toate Categoriile
          </button>
          {SERVICE_CATEGORIES.map(cat => (
            <button
              key={cat.id}
              onClick={() => setSelectedCategory(cat.id)}
              className={`px-3 py-1.5 rounded-xl font-semibold whitespace-nowrap transition-all duration-150 cursor-pointer ${
                selectedCategory === cat.id
                  ? 'bg-gradient-to-r from-indigo-600 to-violet-600 text-white shadow-xs'
                  : 'bg-slate-100 hover:bg-indigo-50 text-slate-600 dark:bg-slate-800 dark:hover:bg-slate-700 dark:text-slate-300'
              }`}
            >
              {cat.name.split(' ')[0]}
            </button>
          ))}
        </div>

        {/* Secondary Filter depending on role */}
        <div className="flex items-center justify-between text-xs pt-1 border-t border-slate-100 dark:border-slate-800">
          {userRole === 'pro' && (
            <div className="flex items-center gap-2">
              <button
                onClick={() => setFilterType('all')}
                className={`text-[11px] font-semibold transition-colors cursor-pointer ${
                  filterType === 'all'
                    ? 'text-indigo-600 dark:text-indigo-400 font-bold'
                    : 'text-slate-500 hover:text-indigo-600 dark:hover:text-slate-200'
                }`}
              >
                Toate ({leads.length})
              </button>
              <span className="text-slate-300 dark:text-slate-700">·</span>
              <button
                onClick={() => setFilterType('available')}
                className={`text-[11px] font-semibold transition-colors cursor-pointer ${
                  filterType === 'available'
                    ? 'text-indigo-600 dark:text-indigo-400 font-bold'
                    : 'text-slate-500 hover:text-indigo-600 dark:hover:text-slate-200'
                }`}
              >
                Disponibile
              </button>
              <span className="text-slate-300 dark:text-slate-700">·</span>
              <button
                onClick={() => setFilterType('claimed')}
                className={`text-[11px] font-semibold transition-colors cursor-pointer ${
                  filterType === 'claimed'
                    ? 'text-indigo-600 dark:text-indigo-400 font-bold'
                    : 'text-slate-500 hover:text-indigo-600 dark:hover:text-slate-200'
                }`}
              >
                Deblocate de mine
              </button>
            </div>
          )}

          {userRole === 'client' && (
            <div className="flex items-center gap-2 text-[11px] text-slate-500 font-semibold">
              <span className="text-emerald-600 dark:text-emerald-400 font-bold">
                Cereri Publicate de Tine ({filteredLeads.length})
              </span>
              <span>·</span>
              <span>Garanție Escrow Activă</span>
            </div>
          )}

          {userRole === 'admin' && (
            <div className="flex items-center gap-2">
              <button
                onClick={() => setAdminStatusFilter('all')}
                className={`text-[11px] font-semibold transition-colors cursor-pointer ${
                  adminStatusFilter === 'all'
                    ? 'text-purple-600 dark:text-purple-400 font-bold'
                    : 'text-slate-500 hover:text-purple-600'
                }`}
              >
                Toate Lead-urile
              </button>
              <span className="text-slate-300 dark:text-slate-700">·</span>
              <button
                onClick={() => setAdminStatusFilter('validat')}
                className={`text-[11px] font-semibold transition-colors cursor-pointer ${
                  adminStatusFilter === 'validat'
                    ? 'text-purple-600 dark:text-purple-400 font-bold'
                    : 'text-slate-500 hover:text-purple-600'
                }`}
              >
                Aprobate AI
              </button>
            </div>
          )}

          <span className="text-[11px] text-indigo-600 dark:text-indigo-400 font-mono font-bold tabular-nums">
            {filteredLeads.length} rezultate
          </span>
        </div>
      </div>

      {/* Feed List */}
      <div className="flex-1 overflow-y-auto divide-y divide-slate-100 dark:divide-slate-800/80">
        {isLoadingLeads ? (
          <SkeletonFeed />
        ) : filteredLeads.length === 0 ? (
          <div className="p-8 text-center text-xs text-slate-400">
            Nu a fost găsită nicio oportunitate conform filtrelor alese.
          </div>
        ) : (
          filteredLeads.map(lead => {
            const isSelected = lead.id === activeLeadId;
            const isClaimed = isLeadClaimedByMe(lead.id);

            return (
              <div
                key={lead.id}
                onClick={() => setActiveLeadId(lead.id)}
                className={`p-4 cursor-pointer transition-all duration-150 relative ${
                  isSelected
                    ? 'bg-gradient-to-r from-indigo-50/90 via-violet-50/40 to-white dark:from-indigo-950/40 dark:via-slate-900 dark:to-slate-900 border-l-4 border-indigo-600'
                    : 'hover:bg-indigo-50/40 dark:hover:bg-slate-900/60'
                }`}
              >
                <div className="space-y-2">
                  {/* Category & Status */}
                  <div className="flex items-center justify-between text-[11px] text-slate-500 dark:text-slate-400">
                    <span className="font-bold text-indigo-600 dark:text-indigo-400">
                      {lead.category}
                    </span>
                    <span className="font-mono text-slate-400">
                      {lead.createdAt}
                    </span>
                  </div>

                  {/* Title */}
                  <h3 className="text-xs sm:text-sm font-bold text-slate-900 dark:text-white leading-snug line-clamp-2">
                    {lead.title}
                  </h3>

                  {/* Meta: Location and Budget */}
                  <div className="flex items-center gap-3 text-xs text-slate-600 dark:text-slate-300">
                    <span className="flex items-center gap-1">
                      <MapPin className="w-3.5 h-3.5 text-indigo-500 shrink-0" />
                      <span className="truncate max-w-[130px] font-medium">{lead.location}</span>
                    </span>

                    <span className="text-slate-300 dark:text-slate-700">·</span>

                    <span className="flex items-center gap-1 font-mono font-bold text-slate-900 dark:text-white tabular-nums">
                      <Wallet className="w-3.5 h-3.5 text-emerald-500 shrink-0" />
                      <span>{lead.formattedBudget}</span>
                    </span>
                  </div>

                  {/* Quality Indicators & Price Strip */}
                  <div className="flex items-center justify-between pt-2 border-t border-slate-100 dark:border-slate-800 text-[11px]">
                    <div className="flex items-center gap-2">
                      <Badge variant="emerald">
                        {lead.leadPassport.completenessScore}% Passport
                      </Badge>

                      {userRole === 'admin' ? (
                        <span className="text-[10px] font-mono font-bold text-emerald-700 dark:text-emerald-400 bg-emerald-50 dark:bg-emerald-950/80 px-1.5 py-0.5 rounded flex items-center gap-1">
                          <ShieldCheck className="w-3 h-3 text-emerald-500" />
                          SMS OK
                        </span>
                      ) : lead.matchExplanation ? (
                        <Badge variant="violet" icon={<Sparkles className="w-2.5 h-2.5 text-violet-500" />}>
                          {lead.matchExplanation.overallScore}% Match
                        </Badge>
                      ) : null}
                    </div>

                    <div className="flex items-center gap-2">
                      {userRole === 'client' ? (
                        <span className="text-[11px] font-semibold text-emerald-700 dark:text-emerald-400 bg-emerald-50 dark:bg-emerald-950/60 px-2 py-0.5 rounded-lg border border-emerald-200 dark:border-emerald-800">
                          {lead.claimedPros.length} / {lead.maxPros} oferte
                        </span>
                      ) : userRole === 'admin' ? (
                        <span className="font-mono text-[11px] text-purple-700 dark:text-purple-300 bg-purple-50 dark:bg-purple-950/60 px-2 py-0.5 rounded-lg border border-purple-200 dark:border-purple-800">
                          Preț: {lead.leadPrice} cr.
                        </span>
                      ) : isClaimed ? (
                        <Badge variant="emerald" icon={<Check className="w-3 h-3 text-emerald-600" />}>
                          Deblocat
                        </Badge>
                      ) : (
                        <span className="font-mono font-bold text-indigo-600 dark:text-indigo-400 bg-indigo-50 dark:bg-indigo-950/80 px-2 py-0.5 rounded-lg border border-indigo-200/80 dark:border-indigo-900 tabular-nums">
                          {lead.leadPrice} credite
                        </span>
                      )}
                    </div>
                  </div>
                </div>
              </div>
            );
          })
        )}
      </div>
    </div>
  );
};
