/**
 * SkillinkDataService Class
 * Encapsulates all mock and remote data fetching into a single testable repository class.
 * Uses CacheManager to ensure fast lookups and avoid redundant round-trips.
 * Supports distinct role profiles: 'pro' | 'client' | 'admin'.
 */

import { LeadModel, RawLeadApiPayload } from '../../models/LeadModel';
import { ProfileModel, RawProfilePayload } from '../../models/ProfileModel';
import { RatingProtocolModel, RawRatingPayload } from '../../models/RatingProtocolModel';
import { DisputeRecord, WalletTransaction, ServiceCategory, UserRole } from '../../types';
import { globalCache } from '../cache/CacheManager';
import {
  INITIAL_LEADS,
  INITIAL_DISPUTES,
  INITIAL_TRANSACTIONS,
  SERVICE_CATEGORIES
} from '../../data/mockData';

export const PRO_PROFILE_DATA: RawProfilePayload = {
  id: 'pro-1',
  role: 'pro',
  name: 'Ing. Radu Teodorescu',
  companyName: 'ElectroSmart Solutions SRL',
  title: 'Inginer Electrician Autorizat ANRE & Smart Home Specialist',
  email: 'radu.teodorescu@electrosmart.ro',
  phone: '+40 722 554 112',
  avatarUrl: '',
  categories: ['Instalații Electrice', 'Smart Home & Automatizări', 'Mentenanță & PRAM'],
  servicesOffered: [
    'Instalații Electrice Rezidențiale & Tablouri KNX',
    'Branșamente Monofazate / Trifazate Autorizate ANRE',
    'Montaj Panouri Solare & Invertor Hibrid',
    'Măsurători Rezistență de Dispersie PRAM cu Buletin'
  ],
  coverageArea: ['București & Ilfov', 'Ploiești', 'Târgoviște'],
  creditsBalance: 145,
  hourlyRate: 180,
  bio: 'Inginer electrician autorizat ANRE gr. IIB cu peste 9 ani experiență în execuție instalații electrice, tablouri inteligente KNX, branșamente și verificări PRAM.',
  verifiedBadge: true,
  responseTime: '< 10 minute',
  completedJobs: 54,
  trustScore: 99.4,
  memberSince: 'Februarie 2023',
  certifications: [
    'Autorizație ANRE Grad II B (valabilă 2024-2029)',
    'Certificare KNX Advanced Partner',
    'CUI ONRC Verificat & Asigurare Răspundere Profesională Generali'
  ],
  stats: {
    'Rată Câștig Lead-uri': '41.8%',
    'Timp Mediu Răspuns': '8 min',
    'Recenzii Verificate': '54',
    'Proiecte Finalizate 2026': '18'
  }
};

export const CLIENT_PROFILE_DATA: RawProfilePayload = {
  id: 'client-1',
  role: 'client',
  name: 'Elena Popescu',
  companyName: 'Popescu Real Estate Investments',
  title: 'Proprietar Rezidențial & Antreprenor',
  email: 'elena.popescu@gmail.com',
  phone: '+40 740 123 987',
  avatarUrl: '',
  categories: ['Renovări Apartamente', 'Design Interior', 'Instalații Sanitare & Climatizare'],
  servicesOffered: [
    'Publicare Proiecte Rezidențiale Calificate AI',
    'Angajare Specialiști Verificați',
    'Plăți Securizate prin Escrow Skillink'
  ],
  coverageArea: ['București - Sector 1', 'București - Nord', 'Brașov'],
  creditsBalance: 0,
  clientBudget: 28500,
  hourlyRate: 0,
  bio: 'Proprietar rezidențial și dezvoltator de spații de închiriat. Public proiecte de renovare, finisaje premium și instalații smart pe Skillink cu bugete transparente și termene clare.',
  verifiedBadge: true,
  responseTime: '< 25 minute',
  completedJobs: 14,
  trustScore: 98.9,
  memberSince: 'Iunie 2024',
  certifications: [
    'Identitate Verificată (eID / Buletin)',
    'Card Bancar Verificat 3D Secure',
    'Plătitor 100% la Termen Garantați'
  ],
  stats: {
    'Cereri Publicate': '9',
    'Specialiști Contractați': '8',
    'Total Investit': '148,500 RON',
    'Evaluare Medie Oferită': '5.0★'
  }
};

export const ADMIN_PROFILE_DATA: RawProfilePayload = {
  id: 'admin-1',
  role: 'admin',
  name: 'Alexandru Mureșan',
  companyName: 'Skillink Core Trust & Safety',
  title: 'Super-Administrator & Platform Compliance Lead',
  email: 'alexandru.muresan@skillink.ro',
  phone: '+40 720 000 100',
  avatarUrl: '',
  categories: ['Lead Passport Audit', 'Dispute Arbitration', 'Fraud Prevention', 'Financial Clearing'],
  servicesOffered: [
    'Arbitrare & Soluționare Litigii Închise',
    'Validare Calitate Lead Passport™ AI',
    'Rambursare Credite & Audit Fraude Numere',
    'Gestiune Politici Economice Platformă'
  ],
  coverageArea: ['Național (România)', 'Platform-Wide Admin'],
  creditsBalance: 4850,
  hourlyRate: 0,
  bio: 'Responsabil principal cu integritatea tranzacțiilor pe Skillink, calitatea Lead Passport™, soluționarea justă a sesizărilor și auditul fraudelor de contact.',
  verifiedBadge: true,
  responseTime: '< 2 minute',
  completedJobs: 428,
  trustScore: 100.0,
  memberSince: 'Ianuarie 2023',
  certifications: [
    'Administrator de Sistem Nivel 1 Skillink',
    'Certificare DPO & Conformitate GDPR',
    'Acreditare Arbitraj Comercial ANPC'
  ],
  stats: {
    'Sesizări Soluționate': '312',
    'Lead-uri Auditate': '1,840',
    'Timp Mediu Arbitraj': '1.4 ore',
    'Scor Neutralitate': '100%'
  }
};

export class SkillinkDataService {
  private static instance: SkillinkDataService;

  private constructor() {
    this.seedInitialData();
  }

  public static getInstance(): SkillinkDataService {
    if (!SkillinkDataService.instance) {
      SkillinkDataService.instance = new SkillinkDataService();
    }
    return SkillinkDataService.instance;
  }

  /**
   * Seed data into cache if not present
   */
  private seedInitialData(): void {
    if (!globalCache.get('leads')) {
      const leadModels = INITIAL_LEADS.map(l => new LeadModel(l));
      globalCache.set('leads', leadModels.map(m => m.toJSON()), 30 * 60 * 1000);
    }

    if (!globalCache.get('profile_pro')) {
      const proProfile = new ProfileModel(PRO_PROFILE_DATA);
      const proRatings = [
        new RatingProtocolModel({
          proId: proProfile.id,
          leadId: 'lead-101',
          leadTitle: 'Refacere tablou electric trifazat vilă Pipera',
          clientName: 'Elena Popescu',
          clientCity: 'București - Sector 1',
          scores: { timeliness: 5, quality: 5, communication: 5, budgetRespect: 5 },
          comment: 'Domnul Radu a lucrat impecabil! A identificat punerea la pământ defectă, a montat siguranțe diferențiale Hager și a lăsat schema unifilară complet etichetată.',
          createdAt: '21 Oct 2026',
          verifiedStatus: true
        }),
        new RatingProtocolModel({
          proId: proProfile.id,
          leadId: 'lead-prev-02',
          leadTitle: 'Instalație Smart Home KNX & Scenarii Iluminat',
          clientName: 'Mihai Dobre (Arhitect)',
          clientCity: 'București - Nord',
          scores: { timeliness: 5, quality: 5, communication: 5, budgetRespect: 4 },
          comment: 'Profesionist de nota 10. A respectat toate specificațiile din Lead Passport și a colaborat direct cu echipa de finisaje.',
          createdAt: '14 Oct 2026',
          verifiedStatus: true
        }),
        new RatingProtocolModel({
          proId: proProfile.id,
          leadId: 'lead-prev-03',
          leadTitle: 'Măsurători PRAM & Buletin Verificare Sediu Birouri',
          clientName: 'Cristian Ionescu',
          clientCity: 'Ploiești',
          scores: { timeliness: 4, quality: 5, communication: 5, budgetRespect: 5 },
          comment: 'Punctual, echipament etalonat metrologic, buletin eliberat în aceeași zi. Foarte mulțumit.',
          createdAt: '29 Sep 2026',
          verifiedStatus: true
        })
      ];
      proProfile.setRatings(proRatings);
      globalCache.set('profile_pro', proProfile.toJSON(), 30 * 60 * 1000);
      globalCache.set('ratings_pro', proRatings.map(r => r.toJSON()), 30 * 60 * 1000);
    }

    if (!globalCache.get('profile_client')) {
      const clientProfile = new ProfileModel(CLIENT_PROFILE_DATA);
      const clientRatings = [
        new RatingProtocolModel({
          proId: clientProfile.id,
          leadId: 'lead-101',
          leadTitle: 'Renovare Instalație & Tablou Electric',
          clientName: 'Ing. Radu Teodorescu (ElectroSmart)',
          clientCity: 'București',
          scores: { timeliness: 5, quality: 5, communication: 5, budgetRespect: 5 },
          comment: 'Client model! Cerințe explicate clar de la început prin AI Architect, accesul în șantier a fost asigurat fără întârzieri, plata a fost confirmată în ziua finalizării.',
          createdAt: '22 Oct 2026',
          verifiedStatus: true
        }),
        new RatingProtocolModel({
          proId: clientProfile.id,
          leadId: 'lead-client-02',
          leadTitle: 'Montaj Gresie Porțelanată Living 60mp',
          clientName: 'Dan Construct SRL',
          clientCity: 'București',
          scores: { timeliness: 5, quality: 5, communication: 4, budgetRespect: 5 },
          comment: 'Colaborare excelentă. Doamna Elena a asigurat materialele la timp și a respectat devizul convenit.',
          createdAt: '05 Oct 2026',
          verifiedStatus: true
        })
      ];
      clientProfile.setRatings(clientRatings);
      globalCache.set('profile_client', clientProfile.toJSON(), 30 * 60 * 1000);
      globalCache.set('ratings_client', clientRatings.map(r => r.toJSON()), 30 * 60 * 1000);
    }

    if (!globalCache.get('profile_admin')) {
      const adminProfile = new ProfileModel(ADMIN_PROFILE_DATA);
      const adminRatings = [
        new RatingProtocolModel({
          proId: adminProfile.id,
          leadId: 'disp-01',
          leadTitle: 'Arbitraj Sesizare Contact Incomplet',
          clientName: 'Audit Independent Skillink',
          clientCity: 'București',
          scores: { timeliness: 5, quality: 5, communication: 5, budgetRespect: 5 },
          comment: 'Sesizare procesată în 18 minute. Creditele au fost returnate profesionistului în conformitate cu Clauza 2.4.',
          createdAt: '23 Oct 2026',
          verifiedStatus: true
        })
      ];
      adminProfile.setRatings(adminRatings);
      globalCache.set('profile_admin', adminProfile.toJSON(), 30 * 60 * 1000);
      globalCache.set('ratings_admin', adminRatings.map(r => r.toJSON()), 30 * 60 * 1000);
    }

    if (!globalCache.get('disputes')) {
      globalCache.set('disputes', INITIAL_DISPUTES, 30 * 60 * 1000);
    }

    if (!globalCache.get('transactions')) {
      globalCache.set('transactions', INITIAL_TRANSACTIONS, 30 * 60 * 1000);
    }

    if (!globalCache.get('categories')) {
      globalCache.set('categories', SERVICE_CATEGORIES, 60 * 60 * 1000);
    }
  }

  // --- Async Leads Fetching ---
  public async getLeadsAsync(): Promise<LeadModel[]> {
    await new Promise(resolve => setTimeout(resolve, 150));

    const cachedRaw = globalCache.get<RawLeadApiPayload[]>('leads');
    if (cachedRaw) {
      return cachedRaw.map(raw => new LeadModel(raw));
    }

    const leadModels = INITIAL_LEADS.map(l => new LeadModel(l));
    globalCache.set('leads', leadModels.map(m => m.toJSON()));
    return leadModels;
  }

  public async getLeadByIdAsync(id: string): Promise<LeadModel | null> {
    const leads = await this.getLeadsAsync();
    return leads.find(l => l.id === id) || null;
  }

  public async createLeadAsync(leadData: Partial<LeadModel>): Promise<LeadModel> {
    await new Promise(resolve => setTimeout(resolve, 200));
    const newLead = new LeadModel(leadData);

    const currentLeads = await this.getLeadsAsync();
    const updated = [newLead, ...currentLeads];

    globalCache.set('leads', updated.map(m => m.toJSON()));
    return newLead;
  }

  // --- Profile by Role & Verified Rating Protocol ---
  public async getProfileByRoleAsync(role: UserRole): Promise<ProfileModel> {
    await new Promise(resolve => setTimeout(resolve, 150));

    const cacheKey = `profile_${role}`;
    const ratingsKey = `ratings_${role}`;

    const fallback =
      role === 'client'
        ? CLIENT_PROFILE_DATA
        : role === 'admin'
        ? ADMIN_PROFILE_DATA
        : PRO_PROFILE_DATA;

    const raw = globalCache.get<RawProfilePayload>(cacheKey) || fallback;
    const profile = new ProfileModel(raw);

    const cachedRatings = globalCache.get<RawRatingPayload[]>(ratingsKey) || [];
    profile.setRatings(cachedRatings.map(r => new RatingProtocolModel(r)));
    return profile;
  }

  public async getProfileAsync(): Promise<ProfileModel> {
    return this.getProfileByRoleAsync('pro');
  }

  public async submitVerifiedRatingAsync(
    role: UserRole,
    payload: RawRatingPayload
  ): Promise<RatingProtocolModel> {
    await new Promise(resolve => setTimeout(resolve, 250));

    const newRating = new RatingProtocolModel(payload);
    const ratingsKey = `ratings_${role}`;
    const cacheKey = `profile_${role}`;

    const cachedRatings = globalCache.get<RawRatingPayload[]>(ratingsKey) || [];
    const updated = [newRating.toJSON(), ...cachedRatings];
    globalCache.set(ratingsKey, updated);

    // Update profile aggregate
    const profile = await this.getProfileByRoleAsync(role);
    profile.addRating(newRating);
    globalCache.set(cacheKey, profile.toJSON());

    return newRating;
  }

  // --- Atomic Claiming & Balance Management ---
  public async claimLeadAtomicAsync(
    leadId: string,
    proId: string,
    proName: string,
    leadPrice: number
  ): Promise<{ success: boolean; message: string; updatedProfile?: ProfileModel }> {
    await new Promise(resolve => setTimeout(resolve, 250));

    const profile = await this.getProfileByRoleAsync('pro');
    if (profile.creditsBalance < leadPrice) {
      return {
        success: false,
        message: `Sold insuficient (${profile.creditsBalance} credite). Solicitarea costă ${leadPrice} credite.`
      };
    }

    const leads = await this.getLeadsAsync();
    const targetLead = leads.find(l => l.id === leadId);
    if (!targetLead) {
      return { success: false, message: 'Solicitarea nu a fost găsită.' };
    }

    if (targetLead.isClaimedBy(proId)) {
      return { success: true, message: 'Ai deja acces complet la acest lead.' };
    }

    if (targetLead.isFullyClaimed) {
      return { success: false, message: 'Numărul maxim de profesioniști a fost atins.' };
    }

    // Atomic debit
    targetLead.addClaim(proId, proName);
    profile.creditsBalance -= leadPrice;

    // Record Transaction
    const newTx: WalletTransaction = {
      id: `tx-${Date.now()}`,
      type: 'lead_purchase',
      amount: -leadPrice,
      description: `Deblocare lead: ${targetLead.title.slice(0, 36)}...`,
      timestamp: new Date().toLocaleTimeString('ro-RO', { hour: '2-digit', minute: '2-digit' }),
      leadId: targetLead.id,
      balanceAfter: profile.creditsBalance
    };

    const transactions = globalCache.get<WalletTransaction[]>('transactions') || [];
    globalCache.set('transactions', [newTx, ...transactions]);

    // Save states
    globalCache.set('leads', leads.map(l => l.toJSON()));
    globalCache.set('profile_pro', profile.toJSON());

    return {
      success: true,
      message: `Lead deblocat cu succes! S-au debitat ${leadPrice} credite.`,
      updatedProfile: profile
    };
  }

  // --- Transactions & Disputes ---
  public async getTransactionsAsync(): Promise<WalletTransaction[]> {
    await new Promise(resolve => setTimeout(resolve, 100));
    return globalCache.get<WalletTransaction[]>('transactions') || INITIAL_TRANSACTIONS;
  }

  public async getDisputesAsync(): Promise<DisputeRecord[]> {
    await new Promise(resolve => setTimeout(resolve, 100));
    return globalCache.get<DisputeRecord[]>('disputes') || INITIAL_DISPUTES;
  }

  public async getCategoriesAsync(): Promise<ServiceCategory[]> {
    return globalCache.get<ServiceCategory[]>('categories') || SERVICE_CATEGORIES;
  }
}

export const skillinkDataService = SkillinkDataService.getInstance();
