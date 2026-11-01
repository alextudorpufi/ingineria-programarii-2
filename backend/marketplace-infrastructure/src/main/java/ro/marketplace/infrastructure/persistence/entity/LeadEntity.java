package ro.marketplace.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ro.marketplace.infrastructure.persistence.audit.AuditableEntity;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "leads")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadEntity extends AuditableEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "category_id", nullable = false)
    private String categoryId;

    @Column(name = "subcategory", nullable = false)
    private String subcategory;

    @Column(name = "description", nullable = false, columnDefinition = "text")
    private String description;

    @Column(name = "location", nullable = false)
    private String location;

    // Budget
    @Column(name = "budget_min", precision = 12, scale = 2, nullable = false)
    private BigDecimal budgetMin;

    @Column(name = "budget_max", precision = 12, scale = 2, nullable = false)
    private BigDecimal budgetMax;

    @Column(name = "budget_currency", length = 20, nullable = false)
    private String budgetCurrency;

    @Column(name = "budget_is_negotiable", nullable = false)
    private boolean budgetIsNegotiable;

    @Column(name = "budget_is_confirmed", nullable = false)
    private boolean budgetIsConfirmed;

    @Column(name = "timeline", nullable = false)
    private String timeline;

    @Column(name = "lead_price", nullable = false)
    private int leadPrice;

    @Column(name = "max_pros", nullable = false)
    private int maxPros;

    /** Authoritative count of reserved professional slots. */
    @Column(name = "current_pros", nullable = false)
    private int currentPros;

    // Client details
    @Column(name = "client_id", nullable = false)
    private UUID clientId;

    @Column(name = "client_name", nullable = false)
    private String clientName;

    @Column(name = "client_phone", length = 50, nullable = false)
    private String clientPhone;

    @Column(name = "client_email", nullable = false)
    private String clientEmail;

    @Column(name = "client_city", nullable = false)
    private String clientCity;

    @Column(name = "client_is_verified", nullable = false)
    private boolean clientIsVerified;

    @Column(name = "status", length = 50, nullable = false)
    private String status;

    // Lead Passport
    @Column(name = "passport_completeness_score", nullable = false)
    private int passportCompletenessScore;

    @Column(name = "passport_coherence_status", length = 50, nullable = false)
    private String passportCoherenceStatus;

    @Column(name = "passport_contact_verified", nullable = false)
    private boolean passportContactVerified;

    @Column(name = "passport_intent_verified", nullable = false)
    private boolean passportIntentVerified;

    @Column(name = "passport_required_deliverables", columnDefinition = "text")
    private String passportRequiredDeliverables;

    @Column(name = "passport_clarifications_remaining", columnDefinition = "text")
    private String passportClarificationsRemaining;

    @Column(name = "passport_extracted_by_ai", nullable = false)
    private boolean passportExtractedByAi;

    @Column(name = "passport_duplicate_risk", length = 50, nullable = false)
    private String passportDuplicateRisk;

    // Match Explanation
    @Column(name = "match_overall_score")
    private Integer matchOverallScore;

    @Column(name = "match_deterministic_fit")
    private Boolean matchDeterministicFit;

    @Column(name = "match_reasons", columnDefinition = "text")
    private String matchReasons;

    @Column(name = "match_semantic_keywords", columnDefinition = "text")
    private String matchSemanticKeywords;

    @Column(name = "match_distance_km", precision = 6, scale = 2)
    private BigDecimal matchDistanceKm;

    @Column(name = "match_expected_response_time", length = 100)
    private String matchExpectedResponseTime;
}
