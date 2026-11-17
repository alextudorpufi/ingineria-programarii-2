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
import ro.marketplace.domain.model.Customer;
import ro.marketplace.infrastructure.persistence.PageRequests;
import ro.marketplace.infrastructure.persistence.entity.CustomerEntity;
import ro.marketplace.infrastructure.persistence.repository.SpringDataCustomerRepository;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
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

    @Test
    @DisplayName("an update copies onto the managed row so @Version is preserved")
    void updateCopiesOntoManagedEntity() {
        UUID id = UUID.randomUUID();
        CustomerEntity existing = CustomerEntity.builder()
                .id(id).name("Old").email("old@example.com").phone("0700").version(3L)
                .build();
        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        Customer saved = adapter.save(Customer.builder()
                .id(id).name("New").email("new@example.com").version(3L).build());

        assertThat(existing.getName()).isEqualTo("New");
        assertThat(existing.getEmail()).isEqualTo("new@example.com");
        assertThat(existing.getPhone()).isNull();
        assertThat(existing.getVersion()).isEqualTo(3L);
        assertThat(saved.getVersion()).isEqualTo(3L);
        verify(repository).save(existing);
    }

    @Test
    @DisplayName("a customer without a version is inserted, not looked up")
    void createSkipsLookup() {
        CustomerEntity persisted = CustomerEntity.builder()
                .id(UUID.randomUUID()).name("New").email("new@example.com")
                .build();
        when(repository.save(any(CustomerEntity.class))).thenReturn(persisted);

        adapter.save(Customer.builder()
                .id(persisted.getId()).name("New").email("new@example.com").build());

        verify(repository, never()).findById(any());
        verify(repository).save(any(CustomerEntity.class));
    }
}
