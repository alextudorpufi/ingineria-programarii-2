package ro.marketplace.app.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import ro.marketplace.domain.model.AuditAction;
import ro.marketplace.domain.model.AuditLog;
import ro.marketplace.domain.port.AuditLogRepository;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
class AppendOnlyLedgerIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16-alpine");

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Test
    @DisplayName("A replayed audit entry fails instead of overwriting history")
    void duplicateAuditEntryFailsInsteadOfOverwriting() {
        UUID id = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 10, 0);
        AuditLog entry = AuditLog.builder()
                .id(id)
                .entityName("Lead")
                .entityId(UUID.randomUUID().toString())
                .action(AuditAction.CREATE)
                .changedBy("admin")
                .createdAt(createdAt)
                .build();

        auditLogRepository.append(entry);

        assertThatThrownBy(() -> auditLogRepository.append(entry))
                .isInstanceOf(RuntimeException.class);

        assertThat(auditLogRepository.findRecentByEntityNameAndEntityId(
                "Lead", entry.getEntityId(), 10))
                .hasSize(1)
                .first()
                .satisfies(log -> assertThat(log.getCreatedAt()).isEqualTo(createdAt));
    }
}
