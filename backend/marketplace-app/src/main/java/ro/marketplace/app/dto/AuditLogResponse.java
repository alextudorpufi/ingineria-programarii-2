package ro.marketplace.app.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record AuditLogResponse(
        UUID id,
        String entityName,
        String entityId,
        String action,
        String changedBy,
        String details,
        LocalDateTime createdAt
) {}
