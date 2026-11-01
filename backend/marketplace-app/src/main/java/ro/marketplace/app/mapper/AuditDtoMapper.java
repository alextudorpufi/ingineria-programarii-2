package ro.marketplace.app.mapper;

import ro.marketplace.app.dto.AuditLogResponse;
import ro.marketplace.domain.model.AuditLog;

public final class AuditDtoMapper {

    private AuditDtoMapper() {
    }

    public static AuditLogResponse toResponse(AuditLog domain) {
        if (domain == null) return null;
        return new AuditLogResponse(
                domain.getId(),
                domain.getEntityName(),
                domain.getEntityId(),
                domain.getAction() != null ? domain.getAction().name() : null,
                domain.getChangedBy(),
                domain.getDetails(),
                domain.getCreatedAt()
        );
    }
}
