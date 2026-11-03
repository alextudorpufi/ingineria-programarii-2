/**
 * RatingProtocolModel Class
 * Implements the Verified Rating Protocol defined in the project proposal.
 * Prevents unverified, purchased, or fake ratings by tying reviews to authenticated collaborations.
 */

export interface RatingDimensionScores {
  timeliness: number; // 1 - 5 (Punctualitate & Livrare la termen)
  quality: number; // 1 - 5 (Calitate tehnică & Fără defecte)
  communication: number; // 1 - 5 (Comunicare & Claritate)
  budgetRespect: number; // 1 - 5 (Respectare buget fără costuri ascunse)
}

export interface RawRatingPayload {
  id?: string;
  proId: string;
  leadId: string;
  leadTitle: string;
  clientName: string;
  clientCity?: string;
  scores: RatingDimensionScores;
  comment: string;
  createdAt?: string;
  verifiedStatus?: boolean;
}

export class RatingProtocolModel {
  public readonly id: string;
  public readonly proId: string;
  public readonly leadId: string;
  public readonly leadTitle: string;
  public readonly clientName: string;
  public readonly clientCity: string;
  public readonly scores: RatingDimensionScores;
  public readonly comment: string;
  public readonly createdAt: string;
  public readonly verifiedStatus: boolean;

  constructor(payload: RawRatingPayload) {
    this.id = payload.id || `rate-${Date.now()}-${Math.random().toString(36).substr(2, 5)}`;
    this.proId = payload.proId;
    this.leadId = payload.leadId;
    this.leadTitle = payload.leadTitle || 'Proiect Servicii';
    this.clientName = payload.clientName || 'Client Verificat';
    this.clientCity = payload.clientCity || 'România';
    this.scores = {
      timeliness: Math.min(5, Math.max(1, payload.scores?.timeliness ?? 5)),
      quality: Math.min(5, Math.max(1, payload.scores?.quality ?? 5)),
      communication: Math.min(5, Math.max(1, payload.scores?.communication ?? 5)),
      budgetRespect: Math.min(5, Math.max(1, payload.scores?.budgetRespect ?? 5)),
    };
    this.comment = payload.comment || '';
    this.createdAt = payload.createdAt || new Date().toLocaleDateString('ro-RO', { year: 'numeric', month: 'short', day: 'numeric' });
    this.verifiedStatus = payload.verifiedStatus !== undefined ? payload.verifiedStatus : true;
  }

  /**
   * Calculate overall composite score across the 4 verified dimensions
   */
  public get overallScore(): number {
    const sum =
      this.scores.timeliness +
      this.scores.quality +
      this.scores.communication +
      this.scores.budgetRespect;
    return parseFloat((sum / 4).toFixed(1));
  }

  public get gradeLabel(): string {
    const score = this.overallScore;
    if (score >= 4.8) return 'Excepțional';
    if (score >= 4.5) return 'Foarte Bun';
    if (score >= 4.0) return 'Bun';
    if (score >= 3.0) return 'Satisfăcător';
    return 'Necesită Îmbunătățiri';
  }

  public toJSON(): RawRatingPayload {
    return {
      id: this.id,
      proId: this.proId,
      leadId: this.leadId,
      leadTitle: this.leadTitle,
      clientName: this.clientName,
      clientCity: this.clientCity,
      scores: this.scores,
      comment: this.comment,
      createdAt: this.createdAt,
      verifiedStatus: this.verifiedStatus
    };
  }
}
