"use client";

import React, { createContext, useCallback, useContext, useEffect, useState } from 'react';
import {
  UserRole,
  DisputeRecord,
  WalletTransaction,
  CreditPurchaseStatus,
  ChatMessage,
  AIChatMessage
} from '@/types';
import { LeadModel } from '@/models/LeadModel';
import { ProfileModel } from '@/models/ProfileModel';
import { RatingProtocolModel, RawRatingPayload } from '@/models/RatingProtocolModel';
import { useAuth } from '@/context/AuthContext';
import type { UserProfile } from '@/services/api/authService';
import { marketplaceService, CreateLeadPayload } from '@/services/api/marketplaceService';
import { resolveCategoryId } from '@/services/api/categoryCatalog';

export type WorkspaceTab = 'leads' | 'ai' | 'chat' | 'disputes' | 'analytics' | 'profile';

interface AppContextType {
  userRole: UserRole;
  darkMode: boolean;
  toggleDarkMode: () => void;
  leads: LeadModel[];
  isLoadingLeads: boolean;
  activeLeadId: string | null;
  setActiveLeadId: (id: string | null) => void;
  activeLead: LeadModel | null;
  selectedCategory: string;
  setSelectedCategory: (category: string) => void;
  searchQuery: string;
  setSearchQuery: (query: string) => void;
  proProfile: ProfileModel;
  isLoadingProfile: boolean;
  submitRating: (payload: RawRatingPayload) => Promise<RatingProtocolModel>;
  transactions: WalletTransaction[];
  disputes: DisputeRecord[];
  chatMessages: Record<string, ChatMessage[]>;
  unlockLead: (leadId: string) => Promise<{ success: boolean; message: string }>;
  isLeadClaimedByMe: (leadId: string) => boolean;
  createLead: (newLead: Partial<LeadModel>) => Promise<LeadModel>;
  fileDispute: (data: { leadId: string; reason: DisputeRecord['reason']; details: string }) => Promise<boolean>;
  resolveDispute: (disputeId: string, decision: 'aprobat' | 'respins', note: string) => Promise<void>;
  createCreditCheckout: (bundleId: string, idempotencyKey: string) => Promise<string>;
  getCreditPurchaseStatus: (purchaseId: string) => Promise<CreditPurchaseStatus>;
  refreshWalletData: () => Promise<void>;
  sendChatMessage: (leadId: string, content: string) => void;
  aiMessages: AIChatMessage[];
  sendAIMessage: (text: string) => void;
  resetAIMessages: () => void;
  showWalletModal: boolean;
  setShowWalletModal: (show: boolean) => void;
  isAuthModalOpen: boolean;
  openAuthModal: () => void;
  closeAuthModal: () => void;
  activeTab: WorkspaceTab;
  setActiveTab: (tab: WorkspaceTab) => void;
}

const AppContext = createContext<AppContextType | undefined>(undefined);

const EMPTY_PROFILE = new ProfileModel({
  id: 'guest',
  role: 'pro',
  name: 'Vizitator Skillink',
  companyName: 'Skillink Marketplace',
  title: 'Autentificare necesară',
  email: '',
  phone: '',
  creditsBalance: 0,
  hourlyRate: 0,
  clientBudget: 0,
  completedJobs: 0,
  trustScore: 100,
  responseTime: '-',
  memberSince: '-',
  categories: [],
  servicesOffered: [],
  coverageArea: [],
  verifiedBadge: false,
});

function toProfileModel(user: UserProfile): ProfileModel {
  const numeric = (value: number | null | undefined): number => {
    const parsed = Number(value ?? 0);
    return Number.isFinite(parsed) ? parsed : 0;
  };

  return new ProfileModel({
    id: user.id,
    role: user.role,
    name: user.name,
    companyName: user.companyName ?? '',
    title: user.title ?? '',
    email: user.email,
    phone: user.phone ?? '',
    avatarUrl: user.avatarUrl ?? '',
    categories: user.categories ?? [],
    servicesOffered: user.servicesOffered ?? [],
    coverageArea: user.coverageAreas ?? [],
    creditsBalance: user.creditsBalance,
    hourlyRate: numeric(user.hourlyRate),
    clientBudget: numeric(user.clientBudget),
    bio: user.bio ?? '',
    verifiedBadge: user.verifiedBadge,
    responseTime: user.responseTime ?? '-',
    completedJobs: user.completedJobs,
    trustScore: numeric(user.trustScore),
    memberSince: user.memberSince ?? '-',
  });
}

const INITIAL_AI_MESSAGE: AIChatMessage = {
  id: 'ai-init',
  sender: 'ai',
  text: 'Salut! Sunt AI Project Architect pe Skillink. Spune-mi cu propriile cuvinte ce proiect dorești să realizezi (de exemplu: "Vreau un magazin online pentru o florărie" sau "Renovare apartament 3 camere"). Voi structura cerințele, bugetul și livrabilele pentru a primi oferte calificate de la cei mai buni profesioniști.',
  timestamp: 'Acum',
  suggestedPicks: [
    'Florărie online cu plăți cu cardul și livrare',
    'Renovare baie & schimbat gresie/faianță',
    'Contabilitate lunară SRL firmă IT',
    'Campanie Google Ads pentru cabinet dentar'
  ]
};

export const AppProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { user: authUser, isLoading: isAuthLoading, refreshUser } = useAuth();

  const [darkMode, setDarkMode] = useState<boolean>(false);

  const [leads, setLeads] = useState<LeadModel[]>([]);
  const [isLoadingLeads, setIsLoadingLeads] = useState<boolean>(true);
  const [activeLeadId, setActiveLeadId] = useState<string | null>(null);
  const [selectedCategory, setSelectedCategory] = useState<string>('all');
  const [searchQuery, setSearchQuery] = useState<string>('');

  const [proProfile, setProProfile] = useState<ProfileModel>(EMPTY_PROFILE);
  const [isLoadingProfile, setIsLoadingProfile] = useState<boolean>(true);

  const [transactions, setTransactions] = useState<WalletTransaction[]>([]);
  const [disputes, setDisputes] = useState<DisputeRecord[]>([]);
  const [chatMessages, setChatMessages] = useState<Record<string, ChatMessage[]>>({});

  const [showWalletModal, setShowWalletModal] = useState<boolean>(false);
  const [isAuthModalOpen, setIsAuthModalOpen] = useState<boolean>(false);

  const [activeTab, setActiveTab] = useState<WorkspaceTab>('leads');

  const [aiMessages, setAiMessages] = useState<AIChatMessage[]>([INITIAL_AI_MESSAGE]);

  const currentUserId = authUser?.id ?? null;
  const currentUserRole = authUser?.role ?? null;
  const userRole: UserRole = currentUserRole ?? 'pro';

  // Sync the authenticated profile. `authUser` changes after claim/top-up refreshes.
  useEffect(() => {
    let isCancelled = false;
    Promise.resolve().then(() => {
      if (isCancelled) return;
      setProProfile(authUser ? toProfileModel(authUser) : EMPTY_PROFILE);
      setIsLoadingProfile(isAuthLoading);
    });
    return () => {
      isCancelled = true;
    };
  }, [authUser, isAuthLoading]);

  // Load real database-backed workspace data whenever the authenticated user changes.
  useEffect(() => {
    let isCancelled = false;

    const loadWorkspace = async () => {
      if (isAuthLoading) return;

      if (!currentUserId || !currentUserRole) {
        setLeads([]);
        setTransactions([]);
        setDisputes([]);
        setActiveLeadId(null);
        setIsLoadingLeads(false);
        return;
      }

      setIsLoadingLeads(true);
      try {
        const [loadedLeads, loadedTransactions, loadedDisputes] = await Promise.all([
          marketplaceService.getLeads(100),
          marketplaceService.getWalletTransactions(currentUserId),
          marketplaceService.getDisputes(currentUserId, currentUserRole),
        ]);
        if (isCancelled) return;

        const models = loadedLeads.map(raw => new LeadModel(raw));
        setLeads(models);
        setTransactions(loadedTransactions);
        setDisputes(loadedDisputes);
        setActiveLeadId(previous => (previous && models.some(lead => lead.id === previous)
          ? previous
          : models[0]?.id ?? null));
      } catch (error) {
        if (!isCancelled) {
          console.error('Failed to load Skillink workspace data:', error);
          setLeads([]);
          setTransactions([]);
          setDisputes([]);
        }
      } finally {
        if (!isCancelled) setIsLoadingLeads(false);
      }
    };

    loadWorkspace();
    return () => {
      isCancelled = true;
    };
  }, [currentUserId, currentUserRole, isAuthLoading]);

  // Sync dark mode class on document element and body
  useEffect(() => {
    const root = document.documentElement;
    const body = document.body;
    if (darkMode) {
      root.classList.add('dark');
      body.classList.add('dark');
      root.style.colorScheme = 'dark';
    } else {
      root.classList.remove('dark');
      body.classList.remove('dark');
      root.style.colorScheme = 'light';
    }
  }, [darkMode]);

  const toggleDarkMode = () => {
    setDarkMode(previous => !previous);
  };

  const activeLead = leads.find(lead => lead.id === activeLeadId) || leads[0] || null;

  const isLeadClaimedByMe = (leadId: string): boolean => {
    if (!currentUserId) return false;
    const lead = leads.find(item => item.id === leadId);
    return lead ? lead.isClaimedBy(currentUserId) : false;
  };

  const refreshWallet = async (userId: string) => {
    try {
      const refreshedTransactions = await marketplaceService.getWalletTransactions(userId);
      setTransactions(refreshedTransactions);
    } catch (error) {
      console.error('Failed to refresh wallet ledger:', error);
    }
  };

  const refreshWalletData = useCallback(async () => {
    if (!authUser) return;
    const [userResult, transactionsResult] = await Promise.allSettled([
      refreshUser(),
      marketplaceService.getWalletTransactions(authUser.id),
    ]);
    if (transactionsResult.status === 'fulfilled') {
      setTransactions(transactionsResult.value);
    } else {
      console.error('Failed to refresh wallet ledger:', transactionsResult.reason);
    }
    if (userResult.status === 'rejected') {
      console.error('Failed to refresh authenticated user after wallet update:', userResult.reason);
    }
  }, [authUser, refreshUser]);

  const unlockLead = async (leadId: string): Promise<{ success: boolean; message: string }> => {
    if (!authUser) {
      return { success: false, message: 'Autentifică-te pentru a debloca lead-uri.' };
    }

    const lead = leads.find(item => item.id === leadId);
    if (!lead) {
      return { success: false, message: 'Solicitarea nu a fost găsită.' };
    }

    if (lead.isClaimedBy(authUser.id)) {
      return { success: true, message: 'Ai deja acces complet la acest lead.' };
    }

    if (lead.isFullyClaimed) {
      return { success: false, message: `Numărul maxim de ${lead.maxPros} profesioniști a fost atins pentru această oportunitate.` };
    }

    if (proProfile.creditsBalance < lead.leadPrice) {
      setShowWalletModal(true);
      return {
        success: false,
        message: `Sold insuficient (${proProfile.creditsBalance} credite). Acest lead costă ${lead.leadPrice} credite.`
      };
    }

    try {
      const claim = await marketplaceService.claimLead(leadId);

      setLeads(previous => previous.map(item => {
        if (item.id !== leadId) return item;
        const updated = new LeadModel(item.toJSON());
        updated.addClaim(claim.proId, claim.proName);
        return updated;
      }));

      await Promise.allSettled([refreshUser(), refreshWallet(authUser.id)]);

      setChatMessages(previous => {
        if (previous[leadId]) return previous;
        return {
          ...previous,
          [leadId]: [
            {
              id: `msg-${Date.now()}`,
              leadId,
              senderId: 'system',
              senderName: 'Sistem Skillink',
              senderRole: 'system',
              content: 'Lead deblocat cu succes. Datele directe de contact ale clientului sunt acum disponibile în panoul de detalii.',
              timestamp: new Date().toLocaleTimeString('ro-RO', { hour: '2-digit', minute: '2-digit' })
            }
          ]
        };
      });

      return { success: true, message: `Lead deblocat cu succes! S-au dedus ${lead.leadPrice} credite.` };
    } catch (error) {
      return {
        success: false,
        message: error instanceof Error ? error.message : 'Lead-ul nu a putut fi deblocat.'
      };
    }
  };

  const createLead = async (newLeadData: Partial<LeadModel>): Promise<LeadModel> => {
    if (!authUser) {
      throw new Error('Autentifică-te pentru a publica o solicitare.');
    }
    if (authUser.role !== 'client' && authUser.role !== 'admin') {
      throw new Error('Doar conturile de client pot publica solicitări în marketplace.');
    }

    const budgetMin = newLeadData.budget?.min ?? 1000;
    const budgetMax = newLeadData.budget?.max ?? Math.max(budgetMin, 5000);
    const payload: CreateLeadPayload = {
      title: newLeadData.title || 'Solicitare de servicii',
      categoryId: resolveCategoryId(newLeadData.categoryId || newLeadData.category || 'it-software'),
      subcategory: newLeadData.subcategory || 'General',
      description: newLeadData.description || newLeadData.title || 'Solicitare de servicii publicată prin AI Architect.',
      location: newLeadData.location || 'București',
      budgetMin,
      budgetMax,
      budgetCurrency: newLeadData.budget?.currency ?? 'RON',
      budgetIsNegotiable: newLeadData.budget?.isNegotiable ?? true,
      budgetIsConfirmed: newLeadData.budget?.isConfirmed ?? true,
      timeline: newLeadData.timeline || 'În următoarele 2 săptămâni',
      leadPrice: newLeadData.leadPrice || 30,
      maxPros: newLeadData.maxPros || 4,
      clientName: authUser.name,
      clientPhone: authUser.phone || '+40 700 000 000',
      clientEmail: authUser.email,
      clientCity: newLeadData.location || 'București',
    };

    const rawLead = await marketplaceService.createLead(payload);
    const created = new LeadModel(rawLead);

    setLeads(previous => [created, ...previous]);
    setActiveLeadId(created.id);
    return created;
  };

  // Local-only rating protocol draft until a dedicated reviews endpoint is exposed.
  const submitRating = async (payload: RawRatingPayload): Promise<RatingProtocolModel> => {
    const createdRating = new RatingProtocolModel(payload);
    setProProfile(previous => {
      const updated = new ProfileModel(previous.toJSON());
      updated.addRating(createdRating);
      return updated;
    });
    return createdRating;
  };

  const fileDispute = async (data: { leadId: string; reason: DisputeRecord['reason']; details: string }): Promise<boolean> => {
    if (!authUser) return false;

    const lead = leads.find(item => item.id === data.leadId);
    if (!lead) return false;

    try {
      const createdDispute = await marketplaceService.raiseDispute({
        leadId: lead.id,
        leadTitle: lead.title,
        reason: data.reason,
        details: data.details,
        creditsRequested: lead.leadPrice,
      });

      setDisputes(previous => [createdDispute, ...previous]);
      return true;
    } catch (error) {
      console.error('Failed to raise dispute:', error);
      return false;
    }
  };

  const resolveDispute = async (disputeId: string, decision: 'aprobat' | 'respins', note: string): Promise<void> => {
    if (!authUser || authUser.role !== 'admin') return;

    try {
      const updated = await marketplaceService.resolveDispute(disputeId, decision === 'aprobat', note);
      setDisputes(previous => previous.map(dispute => (dispute.id === disputeId ? updated : dispute)));
    } catch (error) {
      console.error('Failed to resolve dispute:', error);
    }
  };

  const createCreditCheckout = useCallback(async (bundleId: string, idempotencyKey: string): Promise<string> => {
    if (!authUser || authUser.role !== 'pro') {
      throw new Error('Reîncărcarea creditelor este disponibilă doar profesioniștilor autentificați.');
    }
    const checkout = await marketplaceService.createCreditCheckout(bundleId, idempotencyKey);
    return checkout.checkoutUrl;
  }, [authUser]);

  const getCreditPurchaseStatus = useCallback(async (purchaseId: string): Promise<CreditPurchaseStatus> => {
    return marketplaceService.getCreditPurchaseStatus(purchaseId);
  }, []);

  // Chat transport is not part of the REST surface yet: keep the local thread and auto-reply.
  const sendChatMessage = (leadId: string, content: string) => {
    if (!content.trim() || !authUser) return;

    const lead = leads.find(item => item.id === leadId);
    const clientName = lead ? lead.client.name.split(' ')[0] : 'Client';

    const userMsg: ChatMessage = {
      id: `msg-${Date.now()}`,
      leadId,
      senderId: authUser.id,
      senderName: authUser.role === 'pro' ? authUser.name : clientName,
      senderRole: authUser.role === 'client' ? 'client' : 'pro',
      content,
      timestamp: new Date().toLocaleTimeString('ro-RO', { hour: '2-digit', minute: '2-digit' })
    };

    setChatMessages(previous => ({
      ...previous,
      [leadId]: [...(previous[leadId] || []), userMsg]
    }));

    setTimeout(() => {
      const isClientReplying = authUser.role === 'pro';
      const replyMsg: ChatMessage = {
        id: `msg-reply-${Date.now()}`,
        leadId,
        senderId: isClientReplying ? 'client-reply' : authUser.id,
        senderName: isClientReplying ? clientName : authUser.name,
        senderRole: isClientReplying ? 'client' : 'pro',
        content: isClientReplying
          ? 'Mulțumesc mult pentru mesaj! Sună foarte bine. Când ați avea disponibilitate pentru o discuție detaliată?'
          : 'Am notat cerințele tale! Pot pregăti o propunere tehnică detaliată și un deviz estimativ în cursul zilei de azi.',
        timestamp: new Date().toLocaleTimeString('ro-RO', { hour: '2-digit', minute: '2-digit' })
      };

      setChatMessages(previous => ({
        ...previous,
        [leadId]: [...(previous[leadId] || []), replyMsg]
      }));
    }, 1200);
  };

  const sendAIMessage = (text: string) => {
    if (!text.trim()) return;

    const userMsg: AIChatMessage = {
      id: `ai-usr-${Date.now()}`,
      sender: 'user',
      text,
      timestamp: 'Acum'
    };

    setAiMessages(previous => [...previous, userMsg]);

    setTimeout(() => {
      let aiReplyText = '';
      let suggestedPicks: string[] | undefined;
      let extractedSpec: Partial<LeadModel> | undefined;

      const lower = text.toLowerCase();
      if (lower.includes('flori') || lower.includes('florărie') || lower.includes('magazin') || lower.includes('site')) {
        aiReplyText = 'Am identificat nevoia: Dezvoltare Magazin Online E-Commerce pe Skillink. Pentru ca profesioniștii să vină cu oferte exacte, avem nevoie de încă 2 clarificări esențiale: \n1. Ce volum de produse estimezi la lansare?\n2. Ai deja identitatea vizuală (logo, culori, poze) sau ai nevoie și de design?';
        suggestedPicks = [
          'Sub 50 de produse, avem deja logo și poze',
          'Peste 200 de produse, avem nevoie și de branding',
          'Vrem și integrare curier Sameday / FanCourier'
        ];
        extractedSpec = {
          title: 'Magazin Online E-Commerce cu modul livrări programate',
          category: 'IT & Dezvoltare Software',
          categoryId: 'it-software',
          subcategory: 'E-commerce & Plăți Online',
          budget: { min: 4500, max: 8000, currency: 'RON', isNegotiable: true, isConfirmed: true },
          timeline: '3-4 săptămâni',
          location: 'București & Ilfov',
          leadPrice: 30,
          leadPassport: {
            completenessScore: 92,
            coherenceStatus: 'Excelentă',
            contactVerified: true,
            intentVerified: true,
            requiredDeliverables: [
              'Design e-commerce responsive adaptat domeniului floral',
              'Sistem plăți cu cardul securizat (Stripe/Netopia)',
              'Selector interval orar și dată livrare',
              'Panou administrare comenzi și stocuri'
            ],
            clarificationsRemaining: [],
            extractedByAI: true,
            duplicateRisk: 'Scăzut (< 2%)'
          }
        };
      } else if (lower.includes('renov') || lower.includes('apartament') || lower.includes('baie') || lower.includes('zugrăvit')) {
        aiReplyText = 'Am structurat proiectul în categoria Construcții & Amenajări Interioare. Care este suprafața aproximativă a spațiului și când ați dori începerea lucrărilor?';
        suggestedPicks = [
          'Suprafață 65-75 mp, începere în 2-3 săptămâni',
          'Doar baie și bucătărie (aprox 20 mp), urgent',
          'Renovare integrală cu schimbat instalație sanitară și electrică'
        ];
        extractedSpec = {
          title: 'Renovare completă apartament și finisaje interioare',
          category: 'Construcții & Amenajări',
          categoryId: 'constructii',
          subcategory: 'Renovare Apartament / Casă',
          budget: { min: 18000, max: 32000, currency: 'RON', isNegotiable: true, isConfirmed: true },
          timeline: 'Începere în 2 săptămâni',
          location: 'Cluj-Napoca / București',
          leadPrice: 35,
          leadPassport: {
            completenessScore: 88,
            coherenceStatus: 'Excelentă',
            contactVerified: true,
            intentVerified: true,
            requiredDeliverables: [
              'Pregătire suprafețe și decopertare',
              'Gletuire, finisaj și zugrăveală lavabilă',
              'Montaj gresie, faianță și parchet'
            ],
            clarificationsRemaining: ['Releveu apartament sau măsurători pe teren'],
            extractedByAI: true,
            duplicateRisk: 'Scăzut (< 2%)'
          }
        };
      } else {
        aiReplyText = `Am înțeles cerința pentru: "${text}". Am formulat o structură de proiect inițială. Care este bugetul estimat pe care l-ai alocat pentru această colaborare?`;
        suggestedPicks = [
          'Buget orientativ 1.500 - 3.000 RON',
          'Buget mediu 3.000 - 7.000 RON',
          'Proiect amplu > 10.000 RON',
          'Aștept recomandări de preț de la experți'
        ];
        extractedSpec = {
          title: text.length > 50 ? `${text.slice(0, 48)}...` : text,
          category: 'IT & Dezvoltare Software',
          categoryId: 'it-software',
          subcategory: 'General',
          budget: { min: 2500, max: 5500, currency: 'RON', isNegotiable: true, isConfirmed: true },
          timeline: '1-2 luni',
          location: 'București & Remote',
          leadPrice: 30,
          leadPassport: {
            completenessScore: 85,
            coherenceStatus: 'Bună',
            contactVerified: true,
            intentVerified: true,
            requiredDeliverables: ['Cerințe analizate', 'Deviz și execuție'],
            clarificationsRemaining: ['Detaliere livrabile finale'],
            extractedByAI: true,
            duplicateRisk: 'Scăzut (< 2%)'
          }
        };
      }

      const aiReplyMsg: AIChatMessage = {
        id: `ai-rep-${Date.now()}`,
        sender: 'ai',
        text: aiReplyText,
        timestamp: 'Acum',
        suggestedPicks,
        extractedSpec
      };

      setAiMessages(previous => [...previous, aiReplyMsg]);
    }, 800);
  };

  const resetAIMessages = () => {
    setAiMessages([INITIAL_AI_MESSAGE]);
  };

  const openAuthModal = () => setIsAuthModalOpen(true);
  const closeAuthModal = () => setIsAuthModalOpen(false);

  return (
    <AppContext.Provider
      value={{
        userRole,
        darkMode,
        toggleDarkMode,
        leads,
        isLoadingLeads,
        activeLeadId,
        setActiveLeadId,
        activeLead,
        selectedCategory,
        setSelectedCategory,
        searchQuery,
        setSearchQuery,
        proProfile,
        isLoadingProfile,
        submitRating,
        transactions,
        disputes,
        chatMessages,
        unlockLead,
        isLeadClaimedByMe,
        createLead,
        fileDispute,
        resolveDispute,
        createCreditCheckout,
        getCreditPurchaseStatus,
        refreshWalletData,
        sendChatMessage,
        aiMessages,
        sendAIMessage,
        resetAIMessages,
        showWalletModal,
        setShowWalletModal,
        isAuthModalOpen,
        openAuthModal,
        closeAuthModal,
        activeTab,
        setActiveTab
      }}
    >
      {children}
    </AppContext.Provider>
  );
};

export const useApp = () => {
  const context = useContext(AppContext);
  if (!context) {
    throw new Error('useApp must be used within an AppProvider');
  }
  return context;
};
