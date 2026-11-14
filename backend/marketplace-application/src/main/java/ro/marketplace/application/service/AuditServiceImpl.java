package ro.marketplace.application.service;

import ro.marketplace.domain.model.AuditAction;
import ro.marketplace.domain.model.AuditLog;
import ro.marketplace.domain.port.AuditLogRepository;
import ro.marketplace.domain.port.CurrentActorProvider;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class AuditServiceImpl implements AuditService {

    private static final int MAX_SHORT_FIELD_LENGTH = 100;

    private final AuditLogRepository auditLogRepository;
    private final Clock clock;
    private final CurrentActorProvider currentActorProvider;

    public AuditServiceImpl(AuditLogRepository auditLogRepository,
                            Clock clock,
                            CurrentActorProvider currentActorProvider) {
        this.auditLogRepository = Objects.requireNonNull(auditLogRepository, "auditLogRepository must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
        this.currentActorProvider = Objects.requireNonNull(currentActorProvider, "currentActorProvider must not be null");
    }

    @Override
    public AuditLog recordEvent(String entityName, String entityId, AuditAction action, String changedBy, String details) {
        Objects.requireNonNull(entityName, "entityName must not be null");
        Objects.requireNonNull(entityId, "entityId must not be null");
        Objects.requireNonNull(action, "action must not be null");

        // Prefer the authenticated principal; the explicit value is only a fallback for
        // internal/system-triggered events outside an HTTP request.
        String actor = currentActorProvider.currentActor()
                .filter(actorName -> !actorName.isBlank())
                .orElseGet(() -> changedBy != null && !changedBy.isBlank() ? changedBy : "system");

        AuditLog log = AuditLog.builder()
                .id(UUID.randomUUID())
                .entityName(truncate(entityName, MAX_SHORT_FIELD_LENGTH))
                .entityId(truncate(entityId, MAX_SHORT_FIELD_LENGTH))
                .action(action)
                .changedBy(truncate(actor, MAX_SHORT_FIELD_LENGTH))
                .details(details)
                .createdAt(LocalDateTime.now(clock))
                .build();

        return auditLogRepository.save(log);
    }

    /**
     * Database columns are bounded; truncating here avoids a failed audit insert
     * (and therefore a rolled-back business transaction) when a caller supplies a
     * long identifier or display name.
     */
    private static String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    @Override
    public List<AuditLog> getEntityAuditTrail(String entityName, String entityId) {
        Objects.requireNonNull(entityName, "entityName must not be null");
        Objects.requireNonNull(entityId, "entityId must not be null");
        return auditLogRepository.findByEntityNameAndEntityId(entityName, entityId);
    }

    @Override
    public List<AuditLog> getRecentAuditLogs(int limit) {
        int safeLimit = limit > 0 ? Math.min(limit, 200) : 50;
        return auditLogRepository.findRecent(safeLimit);
    }
}
