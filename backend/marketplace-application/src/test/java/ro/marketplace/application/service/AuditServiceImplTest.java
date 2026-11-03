package ro.marketplace.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ro.marketplace.domain.model.AuditAction;
import ro.marketplace.domain.model.AuditLog;
import ro.marketplace.domain.port.AuditLogRepository;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditServiceImplTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-01-01T10:00:00Z"), ZoneOffset.UTC);

    @Mock
    private AuditLogRepository auditLogRepository;

    private AuditServiceImpl auditService;

    @BeforeEach
    void setUp() {
        auditService = new AuditServiceImpl(auditLogRepository, CLOCK, Optional::empty);
    }

    @Test
    @DisplayName("recordEvent saves audit log with a typed action")
    void recordEvent_Success() {
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(inv -> inv.getArgument(0));

        AuditLog log = auditService.recordEvent("Lead", "lead-123", AuditAction.CREATE,
                "alex@marketplace.ro", "Created lead");

        assertThat(log).isNotNull();
        assertThat(log.getEntityName()).isEqualTo("Lead");
        assertThat(log.getAction()).isEqualTo(AuditAction.CREATE);
        assertThat(log.getChangedBy()).isEqualTo("alex@marketplace.ro");
        assertThat(log.getCreatedAt()).isEqualTo(java.time.LocalDateTime.of(2026, 1, 1, 10, 0));
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    @DisplayName("recordEvent prefers the authenticated actor over caller-supplied text")
    void recordEvent_PrefersAuthenticatedActor() {
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(inv -> inv.getArgument(0));
        AuditServiceImpl authenticated = new AuditServiceImpl(auditLogRepository, CLOCK,
                () -> Optional.of("jwt-user@marketplace.ro"));

        AuditLog log = authenticated.recordEvent("Lead", "lead-1", AuditAction.CREATE,
                "forged-client-value", "{}");

        assertThat(log.getChangedBy()).isEqualTo("jwt-user@marketplace.ro");
    }

    @Test
    @DisplayName("getEntityAuditTrail returns audit history for entity")
    void getEntityAuditTrail_Success() {
        AuditLog l1 = AuditLog.builder().id(UUID.randomUUID()).entityName("Lead").entityId("lead-1")
                .action(AuditAction.CREATE).build();
        when(auditLogRepository.findByEntityNameAndEntityId("Lead", "lead-1")).thenReturn(List.of(l1));

        List<AuditLog> trail = auditService.getEntityAuditTrail("Lead", "lead-1");
        assertThat(trail).hasSize(1);
    }
}
