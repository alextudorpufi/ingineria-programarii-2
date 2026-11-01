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

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "disputes")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DisputeEntity extends AuditableEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "lead_id", nullable = false)
    private UUID leadId;

    @Column(name = "lead_title", nullable = false)
    private String leadTitle;

    @Column(name = "pro_id", nullable = false)
    private UUID proId;

    @Column(name = "pro_name", nullable = false)
    private String proName;

    @Column(name = "reason", length = 100, nullable = false)
    private String reason;

    @Column(name = "details", columnDefinition = "text", nullable = false)
    private String details;

    @Column(name = "credits_requested", nullable = false)
    private int creditsRequested;

    @Column(name = "status", length = 50, nullable = false)
    private String status;

    @Column(name = "admin_decision_note", columnDefinition = "text")
    private String adminDecisionNote;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;
}
