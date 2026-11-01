package ro.marketplace.infrastructure.persistence.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ro.marketplace.infrastructure.persistence.audit.AuditableEntity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity extends AuditableEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "role", nullable = false)
    private String role;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "title")
    private String title;

    @Column(name = "phone")
    private String phone;

    @Column(name = "avatar_url", length = 1024)
    private String avatarUrl;

    @Column(name = "bio", columnDefinition = "text")
    private String bio;

    @Column(name = "verified_badge", nullable = false)
    private boolean verifiedBadge;

    @Column(name = "rating", precision = 3, scale = 2, nullable = false)
    private BigDecimal rating;

    @Column(name = "review_count", nullable = false)
    private int reviewCount;

    @Column(name = "credits_balance", nullable = false)
    private int creditsBalance;

    @Column(name = "hourly_rate", precision = 10, scale = 2)
    private BigDecimal hourlyRate;

    @Column(name = "client_budget", precision = 12, scale = 2)
    private BigDecimal clientBudget;

    @Column(name = "response_time")
    private String responseTime;

    @Column(name = "completed_jobs", nullable = false)
    private int completedJobs;

    @Column(name = "trust_score", precision = 5, scale = 2, nullable = false)
    private BigDecimal trustScore;

    @Column(name = "member_since")
    private String memberSince;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_coverage_areas", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "area")
    @Builder.Default
    private List<String> coverageAreas = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_services_offered", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "service_name")
    @Builder.Default
    private List<String> servicesOffered = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_categories", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "category_name")
    @Builder.Default
    private List<String> categories = new ArrayList<>();
}
