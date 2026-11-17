package ro.marketplace.infrastructure.persistence.adapter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ro.marketplace.infrastructure.persistence.PageRequests;
import ro.marketplace.infrastructure.persistence.repository.SpringDataDisputeRepository;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DisputeRepositoryAdapterTest {

    @Mock
    private SpringDataDisputeRepository repository;

    private DisputeRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new DisputeRepositoryAdapter(repository);
    }

    @Test
    @DisplayName("pro disputes are paged with a stable createdAt/id ordering")
    void pagingByProIsBoundedAndStable() {
        UUID proId = UUID.randomUUID();
        when(repository.findByProId(eq(proId), any(Pageable.class)))
                .thenAnswer(inv -> Page.empty(inv.getArgument(1)));

        adapter.findPageByProId(proId, 0, 10_000);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findByProId(eq(proId), captor.capture());
        Pageable pageable = captor.getValue();
        assertThat(pageable.getPageSize()).isEqualTo(PageRequests.MAX_PAGE_SIZE);
        assertThat(pageable.getSort()).containsExactly(
                Sort.Order.desc("createdAt"), Sort.Order.desc("id"));
    }

    @Test
    @DisplayName("admin dispute paging uses the same bounded ordering")
    void pagingIsBoundedAndStable() {
        when(repository.findAll(any(Pageable.class)))
                .thenAnswer(inv -> Page.empty(inv.getArgument(0)));

        adapter.findPage(0, 10_000);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findAll(captor.capture());
        assertThat(captor.getValue().getPageSize()).isEqualTo(PageRequests.MAX_PAGE_SIZE);
    }
}
