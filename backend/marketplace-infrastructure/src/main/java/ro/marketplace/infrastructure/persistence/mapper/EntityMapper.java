package ro.marketplace.infrastructure.persistence.mapper;

import ro.marketplace.domain.model.AuditAction;
import ro.marketplace.domain.model.AuditLog;
import ro.marketplace.domain.model.CreditGrant;
import ro.marketplace.domain.model.Customer;
import ro.marketplace.domain.model.Dispute;
import ro.marketplace.domain.model.DisputeStatus;
import ro.marketplace.domain.model.Lead;
import ro.marketplace.domain.model.LeadClaim;
import ro.marketplace.domain.model.LeadStatus;
import ro.marketplace.domain.model.User;
import ro.marketplace.domain.model.WalletTransaction;
import ro.marketplace.domain.model.WalletTransactionType;
import ro.marketplace.infrastructure.persistence.entity.AuditLogEntity;
import ro.marketplace.infrastructure.persistence.entity.CreditGrantEntity;
import ro.marketplace.infrastructure.persistence.entity.CustomerEntity;
import ro.marketplace.infrastructure.persistence.entity.DisputeEntity;
import ro.marketplace.infrastructure.persistence.entity.LeadClaimEntity;
import ro.marketplace.infrastructure.persistence.entity.LeadEntity;
import ro.marketplace.infrastructure.persistence.entity.UserEntity;
import ro.marketplace.infrastructure.persistence.entity.WalletTransactionEntity;

import java.util.ArrayList;

public final class EntityMapper {

    private EntityMapper() {
        // Utility class
    }

    // Customer
    public static Customer toDomain(CustomerEntity entity) {
        if (entity == null) return null;
        return Customer.builder()
                .id(entity.getId())
                .name(entity.getName())
                .email(entity.getEmail())
                .phone(entity.getPhone())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public static CustomerEntity toEntity(Customer domain) {
        if (domain == null) return null;
        return CustomerEntity.builder()
                .id(domain.getId())
                .name(domain.getName())
                .email(domain.getEmail())
                .phone(domain.getPhone())
                .createdAt(domain.getCreatedAt())
                .build();
    }

    // User
    public static User toDomain(UserEntity entity) {
        if (entity == null) return null;
        return User.builder()
                .id(entity.getId())
                .email(entity.getEmail())
                .passwordHash(entity.getPasswordHash())
                .role(entity.getRole())
                .name(entity.getName())
                .companyName(entity.getCompanyName())
                .title(entity.getTitle())
                .phone(entity.getPhone())
                .avatarUrl(entity.getAvatarUrl())
                .bio(entity.getBio())
                .verifiedBadge(entity.isVerifiedBadge())
                .rating(entity.getRating())
                .reviewCount(entity.getReviewCount())
                .creditsBalance(entity.getCreditsBalance())
                .hourlyRate(entity.getHourlyRate())
                .clientBudget(entity.getClientBudget())
                .responseTime(entity.getResponseTime())
                .completedJobs(entity.getCompletedJobs())
                .trustScore(entity.getTrustScore())
                .memberSince(entity.getMemberSince())
                .proVerified(entity.isProVerified())
                .loginAttemptsCount(entity.getLoginAttemptsCount())
                .lockoutUntil(entity.getLockoutUntil())
                .coverageAreas(entity.getCoverageAreas() != null ? new ArrayList<>(entity.getCoverageAreas()) : new ArrayList<>())
                .servicesOffered(entity.getServicesOffered() != null ? new ArrayList<>(entity.getServicesOffered()) : new ArrayList<>())
                .categories(entity.getCategories() != null ? new ArrayList<>(entity.getCategories()) : new ArrayList<>())
                .build();
    }

    public static UserEntity toEntity(User domain) {
        if (domain == null) return null;
        return UserEntity.builder()
                .id(domain.getId())
                .email(domain.getEmail())
                .passwordHash(domain.getPasswordHash())
                .role(domain.getRole())
                .name(domain.getName())
                .companyName(domain.getCompanyName())
                .title(domain.getTitle())
                .phone(domain.getPhone())
                .avatarUrl(domain.getAvatarUrl())
                .bio(domain.getBio())
                .verifiedBadge(domain.isVerifiedBadge())
                .rating(domain.getRating())
                .reviewCount(domain.getReviewCount())
                .creditsBalance(domain.getCreditsBalance())
                .hourlyRate(domain.getHourlyRate())
                .clientBudget(domain.getClientBudget())
                .responseTime(domain.getResponseTime())
                .completedJobs(domain.getCompletedJobs())
                .trustScore(domain.getTrustScore())
                .memberSince(domain.getMemberSince())
                .proVerified(domain.isProVerified())
                .loginAttemptsCount(domain.getLoginAttemptsCount())
                .lockoutUntil(domain.getLockoutUntil())
                .coverageAreas(domain.getCoverageAreas() != null ? new ArrayList<>(domain.getCoverageAreas()) : new ArrayList<>())
                .servicesOffered(domain.getServicesOffered() != null ? new ArrayList<>(domain.getServicesOffered()) : new ArrayList<>())
                .categories(domain.getCategories() != null ? new ArrayList<>(domain.getCategories()) : new ArrayList<>())
                .build();
    }

    // Lead
    public static Lead toDomain(LeadEntity entity) {
        if (entity == null) return null;
        return Lead.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .categoryId(entity.getCategoryId())
                .subcategory(entity.getSubcategory())
                .description(entity.getDescription())
                .location(entity.getLocation())
                .budgetMin(entity.getBudgetMin())
                .budgetMax(entity.getBudgetMax())
                .budgetCurrency(entity.getBudgetCurrency())
                .budgetIsNegotiable(entity.isBudgetIsNegotiable())
                .budgetIsConfirmed(entity.isBudgetIsConfirmed())
                .timeline(entity.getTimeline())
                .leadPrice(entity.getLeadPrice())
                .maxPros(entity.getMaxPros())
                .currentPros(entity.getCurrentPros())
                .clientId(entity.getClientId())
                .clientName(entity.getClientName())
                .clientPhone(entity.getClientPhone())
                .clientEmail(entity.getClientEmail())
                .clientCity(entity.getClientCity())
                .clientIsVerified(entity.isClientIsVerified())
                .status(entity.getStatus() != null ? LeadStatus.fromDbValue(entity.getStatus()) : null)
                .passportCompletenessScore(entity.getPassportCompletenessScore())
                .passportCoherenceStatus(entity.getPassportCoherenceStatus())
                .passportContactVerified(entity.isPassportContactVerified())
                .passportIntentVerified(entity.isPassportIntentVerified())
                .passportRequiredDeliverables(entity.getPassportRequiredDeliverables())
                .passportClarificationsRemaining(entity.getPassportClarificationsRemaining())
                .passportExtractedByAi(entity.isPassportExtractedByAi())
                .passportDuplicateRisk(entity.getPassportDuplicateRisk())
                .matchOverallScore(entity.getMatchOverallScore())
                .matchDeterministicFit(entity.getMatchDeterministicFit())
                .matchReasons(entity.getMatchReasons())
                .matchSemanticKeywords(entity.getMatchSemanticKeywords())
                .matchDistanceKm(entity.getMatchDistanceKm())
                .matchExpectedResponseTime(entity.getMatchExpectedResponseTime())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public static LeadEntity toEntity(Lead domain) {
        if (domain == null) return null;
        return LeadEntity.builder()
                .id(domain.getId())
                .title(domain.getTitle())
                .categoryId(domain.getCategoryId())
                .subcategory(domain.getSubcategory())
                .description(domain.getDescription())
                .location(domain.getLocation())
                .budgetMin(domain.getBudgetMin())
                .budgetMax(domain.getBudgetMax())
                .budgetCurrency(domain.getBudgetCurrency())
                .budgetIsNegotiable(domain.isBudgetIsNegotiable())
                .budgetIsConfirmed(domain.isBudgetIsConfirmed())
                .timeline(domain.getTimeline())
                .leadPrice(domain.getLeadPrice())
                .maxPros(domain.getMaxPros())
                .currentPros(domain.getCurrentPros())
                .clientId(domain.getClientId())
                .clientName(domain.getClientName())
                .clientPhone(domain.getClientPhone())
                .clientEmail(domain.getClientEmail())
                .clientCity(domain.getClientCity())
                .clientIsVerified(domain.isClientIsVerified())
                // claimedPros is intentionally not persisted here: claims are stored in lead_claims.
                .status(domain.getStatus() != null ? domain.getStatus().getDbValue() : null)
                .passportCompletenessScore(domain.getPassportCompletenessScore())
                .passportCoherenceStatus(domain.getPassportCoherenceStatus())
                .passportContactVerified(domain.isPassportContactVerified())
                .passportIntentVerified(domain.isPassportIntentVerified())
                .passportRequiredDeliverables(domain.getPassportRequiredDeliverables())
                .passportClarificationsRemaining(domain.getPassportClarificationsRemaining())
                .passportExtractedByAi(domain.isPassportExtractedByAi())
                .passportDuplicateRisk(domain.getPassportDuplicateRisk())
                .matchOverallScore(domain.getMatchOverallScore())
                .matchDeterministicFit(domain.getMatchDeterministicFit())
                .matchReasons(domain.getMatchReasons())
                .matchSemanticKeywords(domain.getMatchSemanticKeywords())
                .matchDistanceKm(domain.getMatchDistanceKm())
                .matchExpectedResponseTime(domain.getMatchExpectedResponseTime())
                .build();
    }

    // LeadClaim
    public static LeadClaim toDomain(LeadClaimEntity entity) {
        if (entity == null) return null;
        return LeadClaim.builder()
                .id(entity.getId())
                .leadId(entity.getLeadId())
                .proId(entity.getProId())
                .proName(entity.getProName())
                .claimedAt(entity.getClaimedAt())
                .build();
    }

    public static LeadClaimEntity toEntity(LeadClaim domain) {
        if (domain == null) return null;
        return LeadClaimEntity.builder()
                .id(domain.getId())
                .leadId(domain.getLeadId())
                .proId(domain.getProId())
                .proName(domain.getProName())
                .claimedAt(domain.getClaimedAt())
                .build();
    }

    // WalletTransaction
    public static WalletTransaction toDomain(WalletTransactionEntity entity) {
        if (entity == null) return null;
        return WalletTransaction.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .leadId(entity.getLeadId())
                .type(entity.getType() != null ? WalletTransactionType.fromDbValue(entity.getType()) : null)
                .amount(entity.getAmount())
                .description(entity.getDescription())
                .balanceAfter(entity.getBalanceAfter())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public static WalletTransactionEntity toEntity(WalletTransaction domain) {
        if (domain == null) return null;
        return WalletTransactionEntity.builder()
                .id(domain.getId())
                .userId(domain.getUserId())
                .leadId(domain.getLeadId())
                .type(domain.getType() != null ? domain.getType().getDbValue() : null)
                .amount(domain.getAmount())
                .description(domain.getDescription())
                .balanceAfter(domain.getBalanceAfter())
                .createdAt(domain.getCreatedAt())
                .build();
    }

    // Dispute
    public static Dispute toDomain(DisputeEntity entity) {
        if (entity == null) return null;
        return Dispute.builder()
                .id(entity.getId())
                .leadId(entity.getLeadId())
                .leadTitle(entity.getLeadTitle())
                .proId(entity.getProId())
                .proName(entity.getProName())
                .reason(entity.getReason())
                .details(entity.getDetails())
                .creditsRequested(entity.getCreditsRequested())
                .status(entity.getStatus() != null ? DisputeStatus.fromDbValue(entity.getStatus()) : null)
                .adminDecisionNote(entity.getAdminDecisionNote())
                .resolvedAt(entity.getResolvedAt())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public static DisputeEntity toEntity(Dispute domain) {
        if (domain == null) return null;
        return DisputeEntity.builder()
                .id(domain.getId())
                .leadId(domain.getLeadId())
                .leadTitle(domain.getLeadTitle())
                .proId(domain.getProId())
                .proName(domain.getProName())
                .reason(domain.getReason())
                .details(domain.getDetails())
                .creditsRequested(domain.getCreditsRequested())
                .status(domain.getStatus() != null ? domain.getStatus().getDbValue() : null)
                .adminDecisionNote(domain.getAdminDecisionNote())
                .resolvedAt(domain.getResolvedAt())
                .build();
    }

    // AuditLog
    public static AuditLog toDomain(AuditLogEntity entity) {
        if (entity == null) return null;
        return AuditLog.builder()
                .id(entity.getId())
                .entityName(entity.getEntityName())
                .entityId(entity.getEntityId())
                .action(entity.getAction() != null ? AuditAction.valueOf(entity.getAction()) : null)
                .changedBy(entity.getChangedBy())
                .details(entity.getDetails())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public static AuditLogEntity toEntity(AuditLog domain) {
        if (domain == null) return null;
        return AuditLogEntity.builder()
                .id(domain.getId())
                .entityName(domain.getEntityName())
                .entityId(domain.getEntityId())
                .action(domain.getAction() != null ? domain.getAction().name() : null)
                .changedBy(domain.getChangedBy())
                .details(domain.getDetails())
                .createdAt(domain.getCreatedAt())
                .build();
    }

    // CreditGrant
    public static CreditGrant toDomain(CreditGrantEntity entity) {
        if (entity == null) return null;
        return new CreditGrant(
                entity.getId(),
                entity.getUserId(),
                entity.getAmount(),
                entity.getAdminId(),
                entity.getGrantedAt()
        );
    }

    public static CreditGrantEntity toEntity(CreditGrant domain) {
        if (domain == null) return null;
        return CreditGrantEntity.builder()
                .id(domain.id())
                .userId(domain.userId())
                .amount(domain.amount())
                .adminId(domain.adminId())
                .grantedAt(domain.grantedAt())
                .build();
    }
}
