package ro.marketplace.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import ro.marketplace.domain.model.ProfessionalProfile;
import ro.marketplace.infrastructure.persistence.audit.AuditableEntity;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "user_professional")
@Getter
@Setter
@NoArgsConstructor
public class ProfessionalProfileEntity extends AuditableEntity {
    @Id
    @Column(name = "user_id")
    private UUID userId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private List<String> categories = List.of();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "services_offered", nullable = false, columnDefinition = "jsonb")
    private List<String> servicesOffered = List.of();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "coverage_areas", nullable = false, columnDefinition = "jsonb")
    private List<String> coverageAreas = List.of();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private List<String> capabilities = List.of();

    private Boolean supportsOnsite;
    private Boolean supportsRemote;
    private Integer yearsExperience;
    @Column(nullable = false)
    private boolean acceptingLeads;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProfessionalProfile.OnboardingStatus onboardingStatus = ProfessionalProfile.OnboardingStatus.DRAFT;
}
