/**
 * Types for Skillink Marketplace
 * Grounded in the project proposal "Ingineria Programării 2"
 */

export type UserRole = 'client' | 'pro' | 'admin';
export type AppDeviceMode = 'desktop' | 'mobile';

export interface ServiceCategory {
  id: string;
  name: string;
  iconName: string;
  description: string;
  avgLeadPrice: number; // in credits / RON
  subcategories: string[];
}

export interface LeadPassport {
  completenessScore: number; // 0 - 100%
  coherenceStatus: 'Excelentă' | 'Bună' | 'Necesită clarificări';
  contactVerified: boolean;
  intentVerified: boolean;
  requiredDeliverables: string[];
  clarificationsRemaining: string[];
  extractedByAI: boolean;
  duplicateRisk: 'Scăzut (< 2%)' | 'Moderat' | 'Elevat';
}

export interface MatchExplanation {
  overallScore: number; // 0 - 100
  deterministicFit: boolean;
  reasons: string[];
  semanticKeywords: string[];
  distanceKm?: number;
  expectedResponseTime: string;
}

export interface Lead {
  id: string;
  title: string;
  category: string;
  subcategory: string;
  description: string;
  location: string;
  budget: {
    min: number;
    max: number;
    currency: string;
    isNegotiable: boolean;
    isConfirmed: boolean;
  };
  timeline: string;
  leadPrice: number; // Credits to purchase this lead (e.g. 30 RON/credits)
  maxPros: number; // Default 4-6 pros per proposal
  claimedPros: {
    proId: string;
    proName: string;
    claimedAt: string;
  }[];
  client: {
    id: string;
    name: string;
    phone: string; // Masked until unlocked
    email: string; // Masked until unlocked
    city: string;
    isVerified: boolean;
  };
  createdAt: string;
  status: 'validat' | 'in_asteptare' | 'atribuit' | 'finalizat' | 'disputat';
  leadPassport: LeadPassport;
  matchExplanation?: MatchExplanation;
}

export interface ProfessionalProfile {
  id: string;
  name: string;
  companyName: string;
  avatarUrl?: string;
  rating: number;
  reviewCount: number;
  categories: string[];
  servicesOffered: string[];
  coverageArea: string[];
  creditsBalance: number;
  hourlyRate?: number;
  bio: string;
  verifiedBadge: boolean;
  responseTime: string;
  completedJobs: number;
}

export interface ChatMessage {
  id: string;
  leadId: string;
  senderId: string;
  senderName: string;
  senderRole: 'client' | 'pro' | 'admin' | 'system';
  content: string;
  timestamp: string;
  isOffer?: boolean;
  offerAmount?: number;
}

export interface DisputeRecord {
  id: string;
  leadId: string;
  leadTitle: string;
  proId: string;
  proName: string;
  reason: 
    | 'wrong_category' 
    | 'unreachable_contact' 
    | 'incompatible_budget' 
    | 'duplicate_or_fake';
  details: string;
  creditsRequested: number;
  createdAt: string;
  status: 'in_analiza' | 'aprobat' | 'respins';
  adminDecisionNote?: string;
  resolvedAt?: string;
}

export interface WalletTransaction {
  id: string;
  type: 'lead_purchase' | 'refund' | 'top_up' | 'bonus';
  amount: number; // positive or negative
  description: string;
  timestamp: string;
  leadId?: string;
  balanceAfter: number;
}

export interface AIChatMessage {
  id: string;
  sender: 'ai' | 'user';
  text: string;
  timestamp: string;
  suggestedPicks?: string[];
  extractedSpec?: Partial<Lead>;
}
