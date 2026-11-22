package ro.marketplace.app.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import ro.marketplace.application.service.CustomerService;
import ro.marketplace.domain.exception.ConflictException;
import ro.marketplace.domain.model.Customer;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
class CustomerOptimisticLockingIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16-alpine");

    @Autowired
    private CustomerService customerService;

    @Test
    @DisplayName("Customer updates bump the version and reject a stale form version")
    void versionIncrementsAndStaleVersionIsRejected() {
        Customer created = customerService.createCustomer(Customer.builder()
                .name("Ion Popescu")
                .email("optimistic-" + UUID.randomUUID() + "@example.com")
                .build());

        Customer loaded = customerService.getCustomerById(created.getId());
        assertThat(loaded.getVersion()).isNotNull();
        long initialVersion = loaded.getVersion();

        customerService.updateCustomer(created.getId(), Customer.builder()
                .name("Ion Updated")
                .email(loaded.getEmail())
                .version(initialVersion)
                .build());

        Customer reloaded = customerService.getCustomerById(created.getId());
        assertThat(reloaded.getVersion()).isEqualTo(initialVersion + 1);
        assertThat(reloaded.getName()).isEqualTo("Ion Updated");

        assertThatThrownBy(() -> customerService.updateCustomer(created.getId(), Customer.builder()
                .name("Ion Stale")
                .email(loaded.getEmail())
                .version(initialVersion)
                .build()))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("concurrently");
    }
}
