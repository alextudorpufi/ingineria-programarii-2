/**
 * ProfileModel Class
 * Decouples backend API structure from UI components.
 * Supports multiple roles: 'pro' | 'client' | 'admin'
 * Aggregates ratings according to the multi-dimensional Rating Protocol.
 */

import { RatingProtocolModel } from '@/models/RatingProtocolModel';
import { UserRole } from '@/types';

export interface RawProfilePayload {
  id: string;
  role?: UserRole;
  name: string;
  companyName: string;
  title?: string;
  email?: string;
  phone?: string;
  avatarUrl?: string;
  rating?: number;
  reviewCount?: number;
  category?: string;
  categories?: string[];
  servicesOffered?: string[];
  coverageArea?: string[];
  creditsBalance?: number;
  hourlyRate?: number;
  clientBudget?: number;
  bio?: string;
  verifiedBadge?: boolean;
  responseTime?: string;
  completedJobs?: number;
  trustScore?: number;
  certifications?: string[];
  memberSince?: string;
  stats?: Record<string, string | number>;
}

export class ProfileModel {
  public id: string;
  public role: UserRole;
  public name: string;
  public companyName: string;
  public title: string;
  public email: string;
  public phone: string;
  public avatarUrl: string;
  public rating: number;
  public reviewCount: number;
  public categories: string[];
  public servicesOffered: string[];
  public coverageArea: string[];
  public creditsBalance: number;
  public hourlyRate: number;
  public clientBudget: number;
  public bio: string;
  public verifiedBadge: boolean;
  public responseTime: string;
  public completedJobs: number;
  public trustScore: number;
  public certifications: string[];
  public memberSince: string;
  public stats: Record<string, string | number>;
  public ratings: RatingProtocolModel[] = [];

  constructor(data: RawProfilePayload) {
    this.id = data.id;
    this.role = data.role || 'pro';
    this.name = data.name ?? '';
    this.companyName = data.companyName ?? '';
    this.title = data.title || (data.role === 'client' ? 'Client Verificat' : data.role === 'admin' ? 'Administrator Platformă' : 'Profesionist Autorizat');
    this.email = data.email ?? '';
    this.phone = data.phone ?? '';
    this.avatarUrl = data.avatarUrl ?? '';
    this.rating = data.rating ?? 0;
    this.reviewCount = data.reviewCount ?? 0;
    this.categories = data.categories ?? (data.category ? [data.category] : []);
    this.servicesOffered = data.servicesOffered ?? [];
    this.coverageArea = data.coverageArea ?? [];
    this.creditsBalance = data.creditsBalance ?? 0;
    this.hourlyRate = data.hourlyRate ?? 0;
    this.clientBudget = data.clientBudget ?? 0;
    this.bio = data.bio ?? '';
    this.verifiedBadge = data.verifiedBadge ?? false;
    this.responseTime = data.responseTime ?? '';
    this.completedJobs = data.completedJobs ?? 0;
    this.trustScore = data.trustScore ?? 0;
    this.memberSince = data.memberSince ?? '';
    this.stats = data.stats || {};
    this.certifications = data.certifications ?? [];
  }

  public setRatings(ratings: RatingProtocolModel[]): void {
    this.ratings = ratings;
  }

  public addRating(rating: RatingProtocolModel): void {
    this.ratings = [rating, ...this.ratings];
    this.completedJobs += 1;
  }

  public get averageRating(): number {
    if (this.ratings.length === 0) return this.reviewCount > 0 ? this.rating : 0;
    const total = this.ratings.reduce((acc, r) => acc + r.overallScore, 0);
    return parseFloat((total / this.ratings.length).toFixed(1));
  }

  public get dimensionBreakdown(): {
    timeliness: number;
    quality: number;
    communication: number;
    budgetRespect: number;
  } {
    if (this.ratings.length === 0) {
      const score = this.reviewCount > 0 ? this.rating : 0;
      return { timeliness: score, quality: score, communication: score, budgetRespect: score };
    }

    const n = this.ratings.length;
    const sums = this.ratings.reduce(
      (acc, r) => ({
        timeliness: acc.timeliness + r.scores.timeliness,
        quality: acc.quality + r.scores.quality,
        communication: acc.communication + r.scores.communication,
        budgetRespect: acc.budgetRespect + r.scores.budgetRespect
      }),
      { timeliness: 0, quality: 0, communication: 0, budgetRespect: 0 }
    );

    return {
      timeliness: parseFloat((sums.timeliness / n).toFixed(1)),
      quality: parseFloat((sums.quality / n).toFixed(1)),
      communication: parseFloat((sums.communication / n).toFixed(1)),
      budgetRespect: parseFloat((sums.budgetRespect / n).toFixed(1))
    };
  }

  public toJSON(): RawProfilePayload {
    return {
      id: this.id,
      role: this.role,
      name: this.name,
      companyName: this.companyName,
      title: this.title,
      email: this.email,
      phone: this.phone,
      avatarUrl: this.avatarUrl,
      rating: this.rating,
      reviewCount: this.reviewCount,
      categories: this.categories,
      servicesOffered: this.servicesOffered,
      coverageArea: this.coverageArea,
      creditsBalance: this.creditsBalance,
      hourlyRate: this.hourlyRate,
      clientBudget: this.clientBudget,
      bio: this.bio,
      verifiedBadge: this.verifiedBadge,
      responseTime: this.responseTime,
      completedJobs: this.completedJobs,
      trustScore: this.trustScore,
      certifications: this.certifications,
      memberSince: this.memberSince,
      stats: this.stats
    };
  }
}
