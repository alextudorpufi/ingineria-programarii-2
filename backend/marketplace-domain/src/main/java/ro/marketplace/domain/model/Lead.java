package ro.marketplace.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ro.marketplace.domain.exception.ConflictException;
import ro.marketplace.domain.exception.ValidationException;

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

    /**
     * Capacity is a platform policy, not a client preference: a lead may be sold
     * to at most this many professionals.
     */
    public static final int DEFAULT_MAX_PROS = 4;
    public static final int DEFAULT_LEAD_PRICE = 30;
    private static final int MAX_CAPABILITIES = 50;
    private static final int MAX_CAPABILITY_LENGTH = 255;

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

    public List<String> getRequiredCapabilities() {
        return requiredCapabilities == null ? List.of() : List.copyOf(requiredCapabilities);
    }

    public void setRequiredCapabilities(List<String> requiredCapabilities) {
        this.requiredCapabilities = requiredCapabilities == null ? List.of() : List.copyOf(requiredCapabilities);
    }

    public List<String> getPreferredCapabilities() {
        return preferredCapabilities == null ? List.of() : List.copyOf(preferredCapabilities);
    }

    public void setPreferredCapabilities(List<String> preferredCapabilities) {
        this.preferredCapabilities = preferredCapabilities == null ? List.of() : List.copyOf(preferredCapabilities);
    }

    public List<LeadClaim> getClaimedPros() {
        return claimedPros == null ? List.of() : List.copyOf(claimedPros);
    }

    public void setClaimedPros(List<LeadClaim> claimedPros) {
        this.claimedPros = claimedPros == null ? List.of() : List.copyOf(claimedPros);
    }

    /**
     * Validates the client-supplied part of a lead before it is priced and
     * persisted. Unknown categories are checked by the application because they
     * require a repository lookup.
     */
    public void validateForCreation() {
        if (budgetMin != null && budgetMax != null && budgetMin.compareTo(budgetMax) > 0) {
            throw new ValidationException("budgetMin must not exceed budgetMax");
        }
        if (budgetMin != null && budgetMin.signum() < 0 || budgetMax != null && budgetMax.signum() < 0) {
            throw new ValidationException("Budget values must not be negative");
        }
    }

    /**
     * Applies the platform's immutable listing policy: the authoritative category
     * price and the maximum number of buyers. Client-supplied values are ignored.
     */
    public void applyPlatformPolicy(int categoryPrice) {
        this.status = LeadStatus.VALIDAT;
        this.leadPrice = categoryPrice > 0 ? categoryPrice : DEFAULT_LEAD_PRICE;
        this.maxPros = DEFAULT_MAX_PROS;
    }

    /**
     * Denormalises the authenticated client's contact details onto the lead so
     * the sold lead remains an immutable snapshot.
     */
    public void snapshotClient(User client) {
        this.clientName = client.getName();
        this.clientEmail = client.getEmail();
        this.clientPhone = client.getPhone() != null ? client.getPhone().trim() : "";
    }

    /**
     * Trims, de-duplicates and bounds the matching requirements. Preferred
     * capabilities already present in the required list are dropped.
     */
    public void normalizeRequirements() {
        if (workMode == null) {
            throw new ValidationException("Selectează modul de lucru: la locație sau la distanță.");
        }
        List<String> required = normalizeCapabilities(requiredCapabilities);
        List<String> preferred = normalizeCapabilities(preferredCapabilities).stream()
                .filter(value -> required.stream().noneMatch(value::equalsIgnoreCase))
                .toList();
        this.requiredCapabilities = required;
        this.preferredCapabilities = preferred;
    }

    private static List<String> normalizeCapabilities(List<String> values) {
        if (values == null) {
            return List.of();
        }
        if (values.size() > MAX_CAPABILITIES || values.stream()
                .anyMatch(value -> value == null || value.isBlank() || value.length() > MAX_CAPABILITY_LENGTH)) {
            throw new ValidationException("Folosește maximum " + MAX_CAPABILITIES
                    + " de competențe în fiecare listă, cu 1–" + MAX_CAPABILITY_LENGTH + " de caractere fiecare.");
        }
        List<String> normalized = new ArrayList<>();
        for (String value : values) {
            String trimmed = value.trim();
            if (normalized.stream().noneMatch(trimmed::equalsIgnoreCase)) {
                normalized.add(trimmed);
            }
        }
        return List.copyOf(normalized);
    }

    /** A lead accepts new claims only before it is assigned, closed, or disputed. */
    public boolean isOpenForClaims() {        return status == LeadStatus.VALIDAT || status == LeadStatus.IN_ASTEPTARE;
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
