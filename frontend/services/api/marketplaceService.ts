import { apiFetch } from './apiClient';
import type { RawLeadApiPayload } from '@/models/LeadModel';
import type { AuthRole } from './authService';
import type { CreditBundle, CreditPurchaseStatus, DisputeRecord, WalletTransaction } from '@/types';

interface RawWalletTransaction {
  id: string;
  userId: string;
  leadId?: string | null;
  type: 'lead_purchase' | 'refund' | 'top_up' | 'bonus';
  amount: number;
  description: string;
  balanceAfter: number;
  createdAt?: string | null;
}

interface RawDispute {
  id: string;
  leadId: string;
  leadTitle: string;
  proId: string;
  proName: string;
  reason: DisputeRecord['reason'];
  details: string;
  creditsRequested: number;
  status: DisputeRecord['status'];
  adminDecisionNote?: string | null;
  resolvedAt?: string | null;
  createdAt?: string | null;
}

export interface ClaimedProPayload {
  proId: string;
  proName: string;
  claimedAt: string;
}

export interface WalletBalance {
  userId: string;
  creditsBalance: number;
}

interface CreditCheckoutResponse {
  purchaseId: string;
  checkoutUrl: string;
}

export interface RaiseDisputePayload {
  leadId: string;
  leadTitle: string;
  reason: DisputeRecord['reason'];
  details: string;
  creditsRequested: number;
}

export interface CreateLeadPayload {
  title: string;
  categoryId: string;
  subcategory: string;
  description: string;
  location: string;
  budgetMin: number;
  budgetMax: number;
  budgetCurrency?: string;
  budgetIsNegotiable?: boolean;
  budgetIsConfirmed?: boolean;
  timeline: string;
  leadPrice?: number;
  maxPros?: number;
  clientName: string;
  clientPhone: string;
  clientEmail: string;
  clientCity: string;
}

function formatDateTime(value?: string | null): string {
  if (!value) return '';
  const parsed = new Date(value);
  if (Number.isNaN(parsed.getTime())) return value;
  return parsed.toLocaleString('ro-RO', {
    day: '2-digit',
    month: 'short',
    hour: '2-digit',
    minute: '2-digit',
  });
}

function toWalletTransaction(raw: RawWalletTransaction): WalletTransaction {
  return {
    id: raw.id,
    type: raw.type,
    amount: raw.amount,
    description: raw.description,
    timestamp: formatDateTime(raw.createdAt),
    leadId: raw.leadId ?? undefined,
    balanceAfter: raw.balanceAfter,
  };
}

function toDispute(raw: RawDispute): DisputeRecord {
  return {
    id: raw.id,
    leadId: raw.leadId,
    leadTitle: raw.leadTitle,
    proId: raw.proId,
    proName: raw.proName,
    reason: raw.reason,
    details: raw.details,
    creditsRequested: raw.creditsRequested,
    createdAt: formatDateTime(raw.createdAt),
    status: raw.status,
    adminDecisionNote: raw.adminDecisionNote ?? undefined,
    resolvedAt: raw.resolvedAt ? formatDateTime(raw.resolvedAt) : undefined,
  };
}

export const marketplaceService = {
  getLeads: async (limit = 100): Promise<RawLeadApiPayload[]> => {
    return apiFetch<RawLeadApiPayload[]>(`/api/leads?limit=${limit}`);
  },

  getLeadById: async (id: string): Promise<RawLeadApiPayload> => {
    return apiFetch<RawLeadApiPayload>(`/api/leads/${id}`);
  },

  claimLead: async (id: string): Promise<ClaimedProPayload> => {
    return apiFetch<ClaimedProPayload>(`/api/leads/${id}/claim`, { method: 'POST' });
  },

  createLead: async (payload: CreateLeadPayload): Promise<RawLeadApiPayload> => {
    return apiFetch<RawLeadApiPayload>('/api/leads', {
      method: 'POST',
      body: JSON.stringify(payload),
    });
  },

  getWalletTransactions: async (userId: string): Promise<WalletTransaction[]> => {
    const transactions = await apiFetch<RawWalletTransaction[]>(`/api/wallet/${userId}/transactions`);
    return transactions.map(toWalletTransaction);
  },

  getWalletBalance: async (userId: string): Promise<WalletBalance> => {
    return apiFetch<WalletBalance>(`/api/wallet/${userId}/balance`);
  },

  topUpCredits: async (userId: string, credits: number, description: string): Promise<WalletTransaction> => {
    const created = await apiFetch<RawWalletTransaction>(`/api/wallet/${userId}/top-up`, {
      method: 'POST',
      body: JSON.stringify({ credits, description }),
    });
    return toWalletTransaction(created);
  },

  getCreditBundles: async (): Promise<CreditBundle[]> => {
    return apiFetch<CreditBundle[]>('/api/wallet/bundles');
  },

  createCreditCheckout: async (bundleId: string, idempotencyKey: string): Promise<CreditCheckoutResponse> => {
    return apiFetch<CreditCheckoutResponse>('/api/wallet/purchases', {
      method: 'POST',
      headers: { 'Idempotency-Key': idempotencyKey },
      body: JSON.stringify({ bundleId }),
    });
  },

  getCreditPurchaseStatus: async (purchaseId: string): Promise<CreditPurchaseStatus> => {
    return apiFetch<CreditPurchaseStatus>(`/api/wallet/purchases/${encodeURIComponent(purchaseId)}`);
  },

  getDisputes: async (userId: string, role: AuthRole): Promise<DisputeRecord[]> => {
    if (role === 'client') {
      // Clients see guarantee information in the UI, but the disputes queue is
      // restricted to professionals and administrators by the API contract.
      return [];
    }
    const query = role === 'admin' ? '' : `?proId=${encodeURIComponent(userId)}`;
    const disputes = await apiFetch<RawDispute[]>(`/api/disputes${query}`);
    return disputes.map(toDispute);
  },

  raiseDispute: async (payload: RaiseDisputePayload): Promise<DisputeRecord> => {
    const created = await apiFetch<RawDispute>('/api/disputes', {
      method: 'POST',
      body: JSON.stringify(payload),
    });
    return toDispute(created);
  },

  resolveDispute: async (id: string, approved: boolean, note: string): Promise<DisputeRecord> => {
    const resolved = await apiFetch<RawDispute>(`/api/disputes/${id}/resolve`, {
      method: 'POST',
      body: JSON.stringify({ approved, adminDecisionNote: note }),
    });
    return toDispute(resolved);
  },
};
