import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import {
  UserRole,
  AppDeviceMode,
  DisputeRecord,
  WalletTransaction,
  ChatMessage,
  AIChatMessage
} from '../types';
import { LeadModel } from '../models/LeadModel';
import { ProfileModel } from '../models/ProfileModel';
import { RatingProtocolModel, RawRatingPayload } from '../models/RatingProtocolModel';
import { skillinkDataService } from '../services/data/MockDataService';
import { INITIAL_CHAT_MESSAGES, INITIAL_PRO_PROFILE } from '../data/mockData';

export type DesktopNavTab = 'leads' | 'ai' | 'chat' | 'disputes' | 'analytics' | 'profile';
export type MobileNavTab = 'leads' | 'ai' | 'messages' | 'wallet' | 'profile';

interface AppContextType {
  appMode: AppDeviceMode;
  setAppMode: (mode: AppDeviceMode) => void;
  userRole: UserRole;
  setUserRole: (role: UserRole) => void;
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
  unlockLead: (leadId: string) => { success: boolean; message: string };
  isLeadClaimedByMe: (leadId: string) => boolean;
  createLead: (newLead: Partial<LeadModel>) => LeadModel;
  fileDispute: (data: { leadId: string; reason: DisputeRecord['reason']; details: string }) => boolean;
  resolveDispute: (disputeId: string, decision: 'aprobat' | 'respins', note: string) => void;
  rechargeCredits: (amount: number, packageName: string) => void;
  sendChatMessage: (leadId: string, content: string) => void;
  aiMessages: AIChatMessage[];
  sendAIMessage: (text: string) => void;
  resetAIMessages: () => void;
  showWalletModal: boolean;
  setShowWalletModal: (show: boolean) => void;
  showAIArchitect: boolean;
  setShowAIArchitect: (show: boolean) => void;
  desktopActiveTab: DesktopNavTab;
  setDesktopActiveTab: (tab: DesktopNavTab) => void;
  mobileActiveTab: MobileNavTab;
  setMobileActiveTab: (tab: MobileNavTab) => void;
  refreshData: () => Promise<void>;
}

const AppContext = createContext<AppContextType | undefined>(undefined);

export const AppProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [appMode, setAppMode] = useState<AppDeviceMode>('desktop');
  const [userRole, setUserRole] = useState<UserRole>('pro');
  const [darkMode, setDarkMode] = useState<boolean>(false);

  // Asynchronous model data states
  const [leads, setLeads] = useState<LeadModel[]>([]);
  const [isLoadingLeads, setIsLoadingLeads] = useState<boolean>(true);
  const [activeLeadId, setActiveLeadId] = useState<string | null>(null);
  const [selectedCategory, setSelectedCategory] = useState<string>('all');
  const [searchQuery, setSearchQuery] = useState<string>('');

  const [proProfile, setProProfile] = useState<ProfileModel>(new ProfileModel(INITIAL_PRO_PROFILE));
  const [isLoadingProfile, setIsLoadingProfile] = useState<boolean>(true);

  const [transactions, setTransactions] = useState<WalletTransaction[]>([]);
  const [disputes, setDisputes] = useState<DisputeRecord[]>([]);
  const [chatMessages, setChatMessages] = useState<Record<string, ChatMessage[]>>(INITIAL_CHAT_MESSAGES);

  const [showWalletModal, setShowWalletModal] = useState<boolean>(false);
  const [showAIArchitect, setShowAIArchitect] = useState<boolean>(false);

  const [desktopActiveTab, setDesktopActiveTab] = useState<DesktopNavTab>('leads');
  const [mobileActiveTab, setMobileActiveTab] = useState<MobileNavTab>('leads');

  // AI Architect Conversational messages
  const [aiMessages, setAiMessages] = useState<AIChatMessage[]>([
    {
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
    }
  ]);

  // Handle Role change and load that role's profile
  const handleSetUserRole = async (newRole: UserRole) => {
    setUserRole(newRole);
    setIsLoadingProfile(true);
    try {
      const roleProfile = await skillinkDataService.getProfileByRoleAsync(newRole);
      setProProfile(roleProfile);
    } catch (err) {
      console.error('Failed to load role profile:', err);
    } finally {
      setIsLoadingProfile(false);
    }
  };

  // Load asynchronous data strictly once on mount through SkillinkDataService
  const loadData = useCallback(async () => {
    try {
      setIsLoadingLeads(true);
      setIsLoadingProfile(true);

      const [loadedLeads, loadedProfile, loadedTx, loadedDisputes] = await Promise.all([
        skillinkDataService.getLeadsAsync(),
        skillinkDataService.getProfileByRoleAsync('pro'),
        skillinkDataService.getTransactionsAsync(),
        skillinkDataService.getDisputesAsync()
      ]);

      setLeads(loadedLeads);
      setActiveLeadId(prev => prev || (loadedLeads.length > 0 ? loadedLeads[0].id : null));
      setProProfile(loadedProfile);
      setTransactions(loadedTx);
      setDisputes(loadedDisputes);
    } catch (err) {
      console.error('Failed to load Skillink data:', err);
    } finally {
      setIsLoadingLeads(false);
      setIsLoadingProfile(false);
    }
  }, []);

  useEffect(() => {
    loadData();
  }, [loadData]);

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
    setDarkMode(prev => !prev);
  };

  const activeLead = leads.find(l => l.id === activeLeadId) || leads[0] || null;

  const isLeadClaimedByMe = (leadId: string): boolean => {
    const lead = leads.find(l => l.id === leadId);
    if (!lead) return false;
    return lead.isClaimedBy(proProfile.id);
  };

  // Atomic Lead unlock per project proposal rules using SkillinkDataService
  const unlockLead = (leadId: string): { success: boolean; message: string } => {
    const lead = leads.find(l => l.id === leadId);
    if (!lead) {
      return { success: false, message: 'Solicitarea nu a fost găsită.' };
    }

    if (isLeadClaimedByMe(leadId)) {
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

    // Atomic debit via Model & Service
    lead.addClaim(proProfile.id, proProfile.name);
    proProfile.creditsBalance -= lead.leadPrice;

    // Record transaction
    const newTx: WalletTransaction = {
      id: `tx-${Date.now()}`,
      type: 'lead_purchase',
      amount: -lead.leadPrice,
      description: `Deblocare lead: ${lead.title.slice(0, 38)}...`,
      timestamp: new Date().toLocaleTimeString('ro-RO', { hour: '2-digit', minute: '2-digit' }),
      leadId: lead.id,
      balanceAfter: proProfile.creditsBalance
    };
    setTransactions(prev => [newTx, ...prev]);

    // Initialize chat if not exists
    if (!chatMessages[leadId]) {
      setChatMessages(prev => ({
        ...prev,
        [leadId]: [
          {
            id: `msg-${Date.now()}`,
            leadId,
            senderId: 'system',
            senderName: 'Sistem Skillink',
            senderRole: 'system',
            content: `Lead deblocat cu succes. Ai acces la datele directe de contact ale clientului: Tel: ${lead.client.phone}, Email: ${lead.client.email}.`,
            timestamp: new Date().toLocaleTimeString('ro-RO', { hour: '2-digit', minute: '2-digit' })
          }
        ]
      }));
    }

    return { success: true, message: `Lead deblocat cu succes! S-au dedus ${lead.leadPrice} credite.` };
  };

  // Add new lead through Model
  const createLead = (newLeadData: Partial<LeadModel>): LeadModel => {
    const lead = new LeadModel({
      ...newLeadData,
      id: `lead-${Date.now()}`,
      createdAt: 'Chiar acum',
      status: 'validat'
    });

    setLeads(prev => [lead, ...prev]);
    setActiveLeadId(lead.id);
    return lead;
  };

  // Submit Rating Protocol Review
  const submitRating = async (payload: RawRatingPayload): Promise<RatingProtocolModel> => {
    const createdRating = await skillinkDataService.submitVerifiedRatingAsync(userRole, payload);
    // Refresh profile in state
    const updatedProfile = await skillinkDataService.getProfileByRoleAsync(userRole);
    setProProfile(updatedProfile);
    return createdRating;
  };

  // Dispute resolution
  const fileDispute = (data: { leadId: string; reason: DisputeRecord['reason']; details: string }): boolean => {
    const lead = leads.find(l => l.id === data.leadId);
    if (!lead) return false;

    const newDispute: DisputeRecord = {
      id: `disp-${Date.now()}`,
      leadId: lead.id,
      leadTitle: lead.title,
      proId: proProfile.id,
      proName: proProfile.name,
      reason: data.reason,
      details: data.details,
      creditsRequested: lead.leadPrice,
      createdAt: new Date().toLocaleString('ro-RO'),
      status: 'in_analiza'
    };

    setDisputes(prev => [newDispute, ...prev]);
    return true;
  };

  const resolveDispute = (disputeId: string, decision: 'aprobat' | 'respins', note: string) => {
    const dispute = disputes.find(d => d.id === disputeId);
    if (!dispute) return;

    if (decision === 'aprobat') {
      const newBalance = proProfile.creditsBalance + dispute.creditsRequested;
      proProfile.creditsBalance = newBalance;

      const newTx: WalletTransaction = {
        id: `tx-ref-${Date.now()}`,
        type: 'refund',
        amount: dispute.creditsRequested,
        description: `Rambursare aprobată (${dispute.leadTitle.slice(0, 30)}...)`,
        timestamp: new Date().toLocaleTimeString('ro-RO', { hour: '2-digit', minute: '2-digit' }),
        leadId: dispute.leadId,
        balanceAfter: newBalance
      };
      setTransactions(prev => [newTx, ...prev]);
    }

    setDisputes(prev =>
      prev.map(d => {
        if (d.id === disputeId) {
          return {
            ...d,
            status: decision,
            adminDecisionNote: note,
            resolvedAt: new Date().toLocaleString('ro-RO')
          };
        }
        return d;
      })
    );
  };

  // Top up credits
  const rechargeCredits = (amount: number, packageName: string) => {
    const newBalance = proProfile.creditsBalance + amount;
    proProfile.creditsBalance = newBalance;

    const newTx: WalletTransaction = {
      id: `tx-top-${Date.now()}`,
      type: 'top_up',
      amount,
      description: `Reîncărcare: ${packageName}`,
      timestamp: new Date().toLocaleTimeString('ro-RO', { hour: '2-digit', minute: '2-digit' }),
      balanceAfter: newBalance
    };
    setTransactions(prev => [newTx, ...prev]);
  };

  // Direct chat simulation
  const sendChatMessage = (leadId: string, content: string) => {
    if (!content.trim()) return;

    const lead = leads.find(l => l.id === leadId);
    const clientName = lead ? lead.client.name.split(' ')[0] : 'Client';

    const userMsg: ChatMessage = {
      id: `msg-${Date.now()}`,
      leadId,
      senderId: userRole === 'pro' ? proProfile.id : 'client-881',
      senderName: userRole === 'pro' ? proProfile.name : clientName,
      senderRole: userRole === 'pro' ? 'pro' : 'client',
      content,
      timestamp: new Date().toLocaleTimeString('ro-RO', { hour: '2-digit', minute: '2-digit' })
    };

    setChatMessages(prev => ({
      ...prev,
      [leadId]: [...(prev[leadId] || []), userMsg]
    }));

    // Realistic auto-reply simulation
    setTimeout(() => {
      const isClientReplying = userRole === 'pro';
      const replyMsg: ChatMessage = {
        id: `msg-reply-${Date.now()}`,
        leadId,
        senderId: isClientReplying ? 'client-reply' : proProfile.id,
        senderName: isClientReplying ? clientName : proProfile.name,
        senderRole: isClientReplying ? 'client' : 'pro',
        content: isClientReplying
          ? 'Mulțumesc mult pentru mesaj! Sună foarte bine. Când ați avea disponibilitate pentru o discuție detaliată?'
          : 'Am notat cerințele tale! Pot pregăti o propunere tehnică detaliată și un deviz estimativ în cursul zilei de azi.',
        timestamp: new Date().toLocaleTimeString('ro-RO', { hour: '2-digit', minute: '2-digit' })
      };

      setChatMessages(prev => ({
        ...prev,
        [leadId]: [...(prev[leadId] || []), replyMsg]
      }));
    }, 1200);
  };

  // AI Architect Conversational elicitation
  const sendAIMessage = (text: string) => {
    if (!text.trim()) return;

    const userMsg: AIChatMessage = {
      id: `ai-usr-${Date.now()}`,
      sender: 'user',
      text,
      timestamp: 'Acum'
    };

    setAiMessages(prev => [...prev, userMsg]);

    setTimeout(() => {
      let aiReplyText = '';
      let suggestedPicks: string[] | undefined = undefined;
      let extractedSpec: Partial<LeadModel> | undefined = undefined;

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

      setAiMessages(prev => [...prev, aiReplyMsg]);
    }, 800);
  };

  const resetAIMessages = () => {
    setAiMessages([
      {
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
      }
    ]);
  };

  return (
    <AppContext.Provider
      value={{
        appMode,
        setAppMode,
        userRole,
        setUserRole: handleSetUserRole,
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
        rechargeCredits,
        sendChatMessage,
        aiMessages,
        sendAIMessage,
        resetAIMessages,
        showWalletModal,
        setShowWalletModal,
        showAIArchitect,
        setShowAIArchitect,
        desktopActiveTab,
        setDesktopActiveTab,
        mobileActiveTab,
        setMobileActiveTab,
        refreshData: loadData
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
