/**
 * LeadModel Class
 * Decouples backend API responses from UI components.
 * Supports flexible mappings (snake_case, nested objects, or flat payloads).
 */

import { LeadPassport, MatchExplanation } from '../types';

export interface RawLeadApiPayload {
  id?: string;
  lead_id?: string;
  title?: string;
  category?: string;
  subcategory?: string;
  description?: string;
  location?: string;
  city?: string;
  budget?: {
    min?: number;
    max?: number;
    currency?: string;
    isNegotiable?: boolean;
    isConfirmed?: boolean;
  };
  budget_min?: number;
  budget_max?: number;
  budget_currency?: string;
  timeline?: string;
  leadPrice?: number;
  lead_price?: number;
  maxPros?: number;
  max_pros?: number;
  claimedPros?: Array<{
    proId: string;
    proName: string;
    claimedAt: string;
  }>;
  claimed_pros?: Array<{
    pro_id: string;
    pro_name: string;
    claimed_at: string;
  }>;
  client?: {
    id?: string;
    name?: string;
    phone?: string;
    email?: string;
    city?: string;
    isVerified?: boolean;
  };
  createdAt?: string;
  created_at?: string;
  status?: 'validat' | 'in_asteptare' | 'atribuit' | 'finalizat' | 'disputat';
  leadPassport?: LeadPassport;
  lead_passport?: LeadPassport;
  matchExplanation?: MatchExplanation;
  match_explanation?: MatchExplanation;
}

export class LeadModel {
  public id: string;
  public title: string;
  public category: string;
  public subcategory: string;
  public description: string;
  public location: string;
  public budget: {
    min: number;
    max: number;
    currency: string;
    isNegotiable: boolean;
    isConfirmed: boolean;
  };
  public timeline: string;
  public leadPrice: number;
  public maxPros: number;
  public claimedPros: Array<{
    proId: string;
    proName: string;
    claimedAt: string;
  }>;
  public client: {
    id: string;
    name: string;
    phone: string;
    email: string;
    city: string;
    isVerified: boolean;
  };
  public createdAt: string;
  public status: 'validat' | 'in_asteptare' | 'atribuit' | 'finalizat' | 'disputat';
  public leadPassport: LeadPassport;
  public matchExplanation?: MatchExplanation;

  constructor(raw: RawLeadApiPayload) {
    this.id = raw.id || raw.lead_id || `lead-${Date.now()}`;
    this.title = raw.title || 'Solicitare de servicii';
    this.category = raw.category || 'IT & Dezvoltare Software';
    this.subcategory = raw.subcategory || 'General';
    this.description = raw.description || '';
    this.location = raw.location || raw.city || 'România (Remote)';

    // Flexible budget parser
    const bMin = raw.budget?.min ?? raw.budget_min ?? 2000;
    const bMax = raw.budget?.max ?? raw.budget_max ?? 5000;
    const bCurr = raw.budget?.currency ?? raw.budget_currency ?? 'RON';
    this.budget = {
      min: bMin,
      max: bMax,
      currency: bCurr,
      isNegotiable: raw.budget?.isNegotiable ?? true,
      isConfirmed: raw.budget?.isConfirmed ?? true,
    };

    this.timeline = raw.timeline || 'În decurs de 1 lună';
    this.leadPrice = raw.leadPrice ?? raw.lead_price ?? 30;
    this.maxPros = raw.maxPros ?? raw.max_pros ?? 4;

    // Normalizing claimed pros
    if (raw.claimedPros) {
      this.claimedPros = raw.claimedPros;
    } else if (raw.claimed_pros) {
      this.claimedPros = raw.claimed_pros.map(p => ({
        proId: p.pro_id,
        proName: p.pro_name,
        claimedAt: p.claimed_at,
      }));
    } else {
      this.claimedPros = [];
    }

    // Client normalization
    this.client = {
      id: raw.client?.id || `client-${Date.now()}`,
      name: raw.client?.name || 'Client Skillink',
      phone: raw.client?.phone || '+40 7** *** ***',
      email: raw.client?.email || 'contact@client.ro',
      city: raw.client?.city || this.location,
      isVerified: raw.client?.isVerified ?? true,
    };

    this.createdAt = raw.createdAt || raw.created_at || 'Recent';
    this.status = raw.status || 'validat';

    // Lead Passport
    this.leadPassport = raw.leadPassport || raw.lead_passport || {
      completenessScore: 85,
      coherenceStatus: 'Excelentă',
      contactVerified: true,
      intentVerified: true,
      requiredDeliverables: ['Analiză cerințe specifice'],
      clarificationsRemaining: [],
      extractedByAI: true,
      duplicateRisk: 'Scăzut (< 2%)'
    };

    this.matchExplanation = raw.matchExplanation || raw.match_explanation;
  }

  public get formattedBudget(): string {
    return `${this.budget.min.toLocaleString('ro-RO')} - ${this.budget.max.toLocaleString('ro-RO')} ${this.budget.currency}`;
  }

  public get isFullyClaimed(): boolean {
    return this.claimedPros.length >= this.maxPros;
  }

  public get remainingSlots(): number {
    return Math.max(0, this.maxPros - this.claimedPros.length);
  }

  public isClaimedBy(proId: string): boolean {
    return this.claimedPros.some(p => p.proId === proId);
  }

  public addClaim(proId: string, proName: string): boolean {
    if (this.isFullyClaimed || this.isClaimedBy(proId)) return false;
    this.claimedPros.push({
      proId,
      proName,
      claimedAt: 'Chiar acum'
    });
    return true;
  }

  public toJSON(): RawLeadApiPayload {
    return {
      id: this.id,
      title: this.title,
      category: this.category,
      subcategory: this.subcategory,
      description: this.description,
      location: this.location,
      budget: this.budget,
      timeline: this.timeline,
      leadPrice: this.leadPrice,
      maxPros: this.maxPros,
      claimedPros: this.claimedPros,
      client: this.client,
      createdAt: this.createdAt,
      status: this.status,
      leadPassport: this.leadPassport,
      matchExplanation: this.matchExplanation
    };
  }
}
