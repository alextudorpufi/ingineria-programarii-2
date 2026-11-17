package ro.marketplace.domain.port;

import ro.marketplace.domain.model.AuditLog;

import java.util.List;

public interface AuditLogRepository {

    /** Appends an immutable audit entry. Reusing an id must fail, never overwrite. */
    AuditLog append(AuditLog auditLog);

    List<AuditLog> findRecentByEntityNameAndEntityId(String entityName, String entityId, int limit);

    List<AuditLog> findRecent(int limit);
}
