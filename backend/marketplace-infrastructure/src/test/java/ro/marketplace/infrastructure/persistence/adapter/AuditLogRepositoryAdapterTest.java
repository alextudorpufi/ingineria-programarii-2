package ro.marketplace.infrastructure.persistence.adapter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ro.marketplace.domain.model.AuditAction;
import ro.marketplace.domain.model.AuditLog;
import ro.marketplace.infrastructure.persistence.entity.AuditLogEntity;
import ro.marketplace.infrastructure.persistence.repository.SpringDataAuditLogRepository;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditLogRepositoryAdapterTest {

    @Mock
    private SpringDataAuditLogRepository repository;

    private AuditLogRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new AuditLogRepositoryAdapter(repository);
    }

    @Test
    @DisplayName("audit entries are appended through the insert-only entity contract")
    void appendPersists() {
        AuditLog log = AuditLog.builder()
                .id(UUID.randomUUID())
                .entityName("Lead")
                .entityId(UUID.randomUUID().toString())
                .action(AuditAction.CREATE)
                .changedBy("admin")
                .createdAt(LocalDateTime.of(2026, 1, 1, 10, 0))
                .build();
        when(repository.save(any(AuditLogEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        AuditLog appended = adapter.append(log);

        verify(repository).save(any(AuditLogEntity.class));
        assertThat(appended.getAction()).isEqualTo(AuditAction.CREATE);
        assertThat(appended.getChangedBy()).isEqualTo("admin");
    }
}
