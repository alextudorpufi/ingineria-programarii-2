package ro.marketplace.domain.port;

import ro.marketplace.domain.model.AuditLog;

import java.util.List;
import java.util.UUID;

public interface AuditLogRepository {

    AuditLog save(AuditLog auditLog);

    /**
     * Most recent entries for one entity. {@code limit} makes the bound
     * explicit so callers never assume the result is complete.
     */
    List<AuditLog> findRecentByEntityNameAndEntityId(String entityName, String entityId, int limit);

    List<AuditLog> findRecent(int limit);
}
