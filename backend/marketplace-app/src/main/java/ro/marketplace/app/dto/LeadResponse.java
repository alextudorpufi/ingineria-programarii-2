package ro.marketplace.app.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record LeadResponse(
        UUID id,
        String title,
        String category,
        String subcategory,
        String description,
        String location,
        BudgetDto budget,
        String timeline,
        int leadPrice,
        int maxPros,
        List<ClaimedProDto> claimedPros,
        ClientSummaryDto client,
        String status,
        LeadPassportDto leadPassport,
        MatchExplanationDto matchExplanation,
        LocalDateTime createdAt,
        boolean ownedByCurrentUser
) {
    public record BudgetDto(
            BigDecimal min,
            BigDecimal max,
            String currency,
            boolean isNegotiable,
            boolean isConfirmed
    ) {}

    public record ClaimedProDto(
            UUID proId,
            String proName,
            LocalDateTime claimedAt
    ) {}

    public record ClientSummaryDto(
            UUID id,
            String name,
            String phone,
            String email,
            String city,
            boolean isVerified
    ) {}

    public record LeadPassportDto(
            int completenessScore,
            String coherenceStatus,
            boolean contactVerified,
            boolean intentVerified,
            List<String> requiredDeliverables,
            List<String> clarificationsRemaining,
            boolean extractedByAI,
            String duplicateRisk
    ) {}

    public record MatchExplanationDto(
            Integer overallScore,
            Boolean deterministicFit,
            List<String> reasons,
            List<String> semanticKeywords,
            BigDecimal distanceKm,
            String expectedResponseTime
    ) {}
}
