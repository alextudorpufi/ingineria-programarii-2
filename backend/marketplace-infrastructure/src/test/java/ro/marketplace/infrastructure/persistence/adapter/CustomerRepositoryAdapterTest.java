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
import ro.marketplace.infrastructure.persistence.repository.SpringDataCustomerRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerRepositoryAdapterTest {

    @Mock
    private SpringDataCustomerRepository repository;

    private CustomerRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new CustomerRepositoryAdapter(repository);
    }

    @Test
    @DisplayName("paging is bounded and sorted by createdAt with an id tie-breaker")
    void pagingIsBoundedAndStable() {
        when(repository.findAll(any(Pageable.class)))
                .thenAnswer(inv -> Page.empty(inv.getArgument(0)));

        adapter.findPage(0, 10_000);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findAll(captor.capture());
        Pageable pageable = captor.getValue();
        assertThat(pageable.getPageSize()).isEqualTo(PageRequests.MAX_PAGE_SIZE);
        assertThat(pageable.getSort()).containsExactly(
                Sort.Order.desc("createdAt"), Sort.Order.desc("id"));
    }
}
