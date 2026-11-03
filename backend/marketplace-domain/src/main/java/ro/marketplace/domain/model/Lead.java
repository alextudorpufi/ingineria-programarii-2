package ro.marketplace.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Lead {
    private UUID id;
    private String title;
    private String categoryId;
    private String subcategory;
    private String description;
    private String location;

    // Budget
    private BigDecimal budgetMin;
    private BigDecimal budgetMax;
    private String budgetCurrency;
    private boolean budgetIsNegotiable;
    private boolean budgetIsConfirmed;

    private String timeline;
    private int leadPrice;
    private int maxPros;

    // Client contact snapshot. Deliberately denormalized so a lead remains an immutable
    // record of what the professional purchased, even if the client profile changes later.
    private UUID clientId;
    private String clientName;
    private String clientPhone;
    private String clientEmail;
    private String clientCity;
    private boolean clientIsVerified;

    private LeadStatus status;

    /** Number of reserved professional slots; the authoritative capacity counter. */
    private int currentPros;

    // Lead Passport
    private int passportCompletenessScore;
    private String passportCoherenceStatus;
    private boolean passportContactVerified;
    private boolean passportIntentVerified;
    private String passportRequiredDeliverables;
    private String passportClarificationsRemaining;
    private boolean passportExtractedByAi;
    private String passportDuplicateRisk;

    // Match Explanation
    private Integer matchOverallScore;
    private Boolean matchDeterministicFit;
    private String matchReasons;
    private String matchSemanticKeywords;
    private BigDecimal matchDistanceKm;
    private String matchExpectedResponseTime;

    private LocalDateTime createdAt;

    @Builder.Default
    private List<LeadClaim> claimedPros = new ArrayList<>();
}
