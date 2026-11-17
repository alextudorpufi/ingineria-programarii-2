package ro.marketplace.infrastructure.persistence.adapter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ro.marketplace.domain.model.WalletTransaction;
import ro.marketplace.domain.model.WalletTransactionType;
import ro.marketplace.infrastructure.persistence.PageRequests;
import ro.marketplace.infrastructure.persistence.entity.WalletTransactionEntity;
import ro.marketplace.infrastructure.persistence.repository.SpringDataWalletTransactionRepository;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WalletTransactionRepositoryAdapterTest {

    @Mock
    private SpringDataWalletTransactionRepository repository;

    private WalletTransactionRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new WalletTransactionRepositoryAdapter(repository);
    }

    @Test
    @DisplayName("ledger entries are appended through the insert-only entity contract")
    void appendPersists() {
        WalletTransaction transaction = WalletTransaction.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .type(WalletTransactionType.TOP_UP)
                .amount(50)
                .description("Top up")
                .balanceAfter(50)
                .createdAt(LocalDateTime.of(2026, 1, 1, 10, 0))
                .build();
        when(repository.save(any(WalletTransactionEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        WalletTransaction appended = adapter.append(transaction);

        verify(repository).save(any(WalletTransactionEntity.class));
        assertThat(appended.getId()).isEqualTo(transaction.getId());
        assertThat(appended.getBalanceAfter()).isEqualTo(50);
    }

    @Test
    @DisplayName("paging is capped and sorted by createdAt with an id tie-breaker")
    void pagingIsBoundedAndStable() {
        UUID userId = UUID.randomUUID();
        when(repository.findByUserId(eq(userId), any(Pageable.class)))
                .thenAnswer(inv -> Page.empty(inv.getArgument(1)));

        adapter.findPageByUserId(userId, 0, 10_000);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findByUserId(eq(userId), captor.capture());
        Pageable pageable = captor.getValue();
        assertThat(pageable.getPageSize()).isEqualTo(PageRequests.MAX_PAGE_SIZE);
        assertThat(pageable.getSort()).containsExactly(
                org.springframework.data.domain.Sort.Order.desc("createdAt"),
                org.springframework.data.domain.Sort.Order.desc("id"));
    }
}
