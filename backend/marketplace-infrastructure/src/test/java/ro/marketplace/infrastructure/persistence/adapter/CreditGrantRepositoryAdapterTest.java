package ro.marketplace.infrastructure.persistence.adapter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ro.marketplace.domain.model.CreditGrant;
import ro.marketplace.infrastructure.persistence.entity.CreditGrantEntity;
import ro.marketplace.infrastructure.persistence.repository.SpringDataCreditGrantRepository;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreditGrantRepositoryAdapterTest {

    @Mock
    private SpringDataCreditGrantRepository repository;

    private CreditGrantRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new CreditGrantRepositoryAdapter(repository);
    }

    @Test
    @DisplayName("credit grants are appended so the single-grant invariant fails loudly")
    void appendPersists() {
        CreditGrant grant = new CreditGrant(
                UUID.randomUUID(),
                UUID.randomUUID(),
                10,
                UUID.randomUUID(),
                LocalDateTime.of(2026, 1, 1, 10, 0));
        when(repository.save(any(CreditGrantEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        CreditGrant appended = adapter.append(grant);

        verify(repository).save(any(CreditGrantEntity.class));
        assertThat(appended.amount()).isEqualTo(10);
        assertThat(appended.userId()).isEqualTo(grant.userId());
    }
}
