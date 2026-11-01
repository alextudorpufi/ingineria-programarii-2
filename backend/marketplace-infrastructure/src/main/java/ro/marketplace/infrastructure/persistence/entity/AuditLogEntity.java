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

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "entity_name", length = 100, nullable = false)
    private String entityName;

    @Column(name = "entity_id", length = 100, nullable = false)
    private String entityId;

    @Column(name = "action", length = 50, nullable = false)
    private String action;

    @Column(name = "changed_by", length = 100, nullable = false)
    private String changedBy;

    @Column(name = "details", columnDefinition = "text")
    private String details;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
