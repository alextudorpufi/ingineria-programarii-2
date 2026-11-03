package ro.marketplace.application.service;

import ro.marketplace.domain.model.AuditAction;
import ro.marketplace.domain.model.AuditLog;

import java.util.List;

public interface AuditService {

    AuditLog recordEvent(String entityName, String entityId, AuditAction action, String changedBy, String details);

    List<AuditLog> getEntityAuditTrail(String entityName, String entityId);

    List<AuditLog> getRecentAuditLogs(int limit);
}
