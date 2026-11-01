package ro.marketplace.app.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ro.marketplace.app.dto.AuditLogResponse;
import ro.marketplace.app.mapper.AuditDtoMapper;
import ro.marketplace.application.service.AuditService;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/audit")
@PreAuthorize("hasRole('ADMIN')")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = Objects.requireNonNull(auditService, "auditService must not be null");
    }

    @GetMapping("/recent")
    public ResponseEntity<List<AuditLogResponse>> getRecentAuditLogs(
            @RequestParam(name = "limit", defaultValue = "50") int limit) {
        List<AuditLogResponse> logs = auditService.getRecentAuditLogs(limit).stream()
                .map(AuditDtoMapper::toResponse)
                .toList();
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/{entityName}/{entityId}")
    public ResponseEntity<List<AuditLogResponse>> getEntityAuditTrail(
            @PathVariable String entityName,
            @PathVariable String entityId) {
        List<AuditLogResponse> logs = auditService.getEntityAuditTrail(entityName, entityId).stream()
                .map(AuditDtoMapper::toResponse)
                .toList();
        return ResponseEntity.ok(logs);
    }
}
