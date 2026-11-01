package ro.marketplace.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "lead_claims",
        uniqueConstraints = @UniqueConstraint(name = "uq_lead_claims_lead_pro", columnNames = {"lead_id", "pro_id"})
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadClaimEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "lead_id", nullable = false)
    private UUID leadId;

    @Column(name = "pro_id", nullable = false)
    private UUID proId;

    @Column(name = "pro_name", nullable = false)
    private String proName;

    @Column(name = "claimed_at", nullable = false)
    private LocalDateTime claimedAt;
}
