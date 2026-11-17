package ro.marketplace.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ro.marketplace.domain.exception.ConflictException;
import ro.marketplace.domain.exception.ResourceNotFoundException;
import ro.marketplace.domain.exception.ValidationException;
import ro.marketplace.domain.model.Customer;
import ro.marketplace.domain.model.PageResult;
import ro.marketplace.domain.port.CustomerRepository;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-01-01T10:00:00Z"), ZoneOffset.UTC);

    @Mock
    private CustomerRepository customerRepository;

    private CustomerServiceImpl customerService;

    @BeforeEach
    void setUp() {
        customerService = new CustomerServiceImpl(customerRepository, new DirectUnitOfWork(), CLOCK);
    }

    @Test
    @DisplayName("createCustomer saves new customer successfully")
    void createCustomer_Success() {
        Customer input = Customer.builder()
                .name("Ion Popescu")
                .email("ion@example.com")
                .phone("+40700000000")
                .build();

        when(customerRepository.existsByEmail("ion@example.com")).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));

        Customer created = customerService.createCustomer(input);

        assertThat(created).isNotNull();
        assertThat(created.getId()).isNotNull();
        assertThat(created.getCreatedAt()).isNotNull();
        assertThat(created.getEmail()).isEqualTo("ion@example.com");
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    @DisplayName("createCustomer throws ConflictException when email already exists")
    void createCustomer_DuplicateEmail_ThrowsConflict() {
        Customer input = Customer.builder()
                .name("Ion Popescu")
                .email("ion@example.com")
                .build();

        when(customerRepository.existsByEmail("ion@example.com")).thenReturn(true);

        assertThatThrownBy(() -> customerService.createCustomer(input))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("getCustomerById returns customer when found")
    void getCustomerById_Success() {
        UUID id = UUID.randomUUID();
        Customer customer = Customer.builder().id(id).name("Maria").email("maria@example.com").build();
        when(customerRepository.findById(id)).thenReturn(Optional.of(customer));

        Customer result = customerService.getCustomerById(id);
        assertThat(result.getName()).isEqualTo("Maria");
    }

    @Test
    @DisplayName("getCustomerById throws ResourceNotFoundException when not found")
    void getCustomerById_NotFound_ThrowsException() {
        UUID id = UUID.randomUUID();
        when(customerRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.getCustomerById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("not found");
    }

    @Test
    @DisplayName("getCustomers returns a page with its total")
    void getCustomers_Success() {
        Customer c1 = Customer.builder().id(UUID.randomUUID()).name("C1").build();
        Customer c2 = Customer.builder().id(UUID.randomUUID()).name("C2").build();
        when(customerRepository.findPage(0, 20))
                .thenReturn(new PageResult<>(List.of(c1, c2), 2, 0, 20));

        PageResult<Customer> page = customerService.getCustomers(0, 20);
        assertThat(page.content()).hasSize(2);
        assertThat(page.totalElements()).isEqualTo(2);
    }

    @Test
    @DisplayName("updateCustomer rejects blank required fields before touching storage")
    void updateCustomer_BlankName_ThrowsValidation() {
        assertThatThrownBy(() -> customerService.updateCustomer(UUID.randomUUID(),
                Customer.builder().name("  ").email("valid@example.com").build()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Numele");
        verify(customerRepository, never()).findById(any());
    }

    @Test
    @DisplayName("updateCustomer rejects a stale client version before saving")
    void updateCustomer_StaleVersion_ThrowsConflict() {
        UUID id = UUID.randomUUID();
        Customer existing = Customer.builder()
                .id(id).name("Ion").email("ion@example.com").version(2L).build();
        when(customerRepository.findById(id)).thenReturn(Optional.of(existing));

        Customer stale = Customer.builder()
                .name("Ion Updated").email("ion@example.com").version(1L).build();

        assertThatThrownBy(() -> customerService.updateCustomer(id, stale))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("concurrently");
        verify(customerRepository, never()).save(any());
    }

    @Test
    @DisplayName("deleteCustomer removes an existing customer")
    void deleteCustomer_Success() {
        UUID id = UUID.randomUUID();
        when(customerRepository.findById(id))
                .thenReturn(Optional.of(Customer.builder().id(id).name("C").email("c@example.com").build()));

        customerService.deleteCustomer(id);

        verify(customerRepository).deleteById(id);
    }
}
