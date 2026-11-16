package ro.marketplace.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ro.marketplace.domain.exception.ConflictException;

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
    public enum WorkMode { ONSITE, REMOTE }

    private UUID id;
    private String title;
    private String categoryId;
    private String subcategory;
    private String description;
    private String location;
    private WorkMode workMode;
    @Builder.Default
    private List<String> requiredCapabilities = List.of();
    @Builder.Default
    private List<String> preferredCapabilities = List.of();

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

    /** A lead accepts new claims only before it is assigned, closed, or disputed. */
    public boolean isOpenForClaims() {
        return status == LeadStatus.VALIDAT || status == LeadStatus.IN_ASTEPTARE;
    }

    public boolean hasReachedCapacity() {
        return maxPros > 0 && currentPros >= maxPros;
    }

    /**
     * Marks the lead as disputed. A finalized lead is immutable, so it can no
     * longer be disputed.
     */
    public void markDisputed() {
        if (status == LeadStatus.FINALIZAT) {
            throw new ConflictException("A finalized lead cannot be disputed.");
        }
        this.status = LeadStatus.DISPUTAT;
    }

    /**
     * Restores the lead after the last open dispute is resolved: full leads are
     * assigned again, otherwise the lead becomes claimable again.
     */
    public void reopenAfterDispute() {
        if (status != LeadStatus.DISPUTAT) {
            return;
        }
        this.status = hasReachedCapacity() ? LeadStatus.ATRIBUIT : LeadStatus.VALIDAT;
    }

    /**
     * Closes a lead. Disputed leads must be resolved first so that a pending
     * refund can never be silently discarded.
     */
    public void close() {
        if (status == LeadStatus.DISPUTAT) {
            throw new ConflictException("A disputed lead must be resolved before it can be closed.");
        }
        if (status == LeadStatus.FINALIZAT) {
            return;
        }
        this.status = LeadStatus.FINALIZAT;
    }
}
