package ro.marketplace;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ro.marketplace.domain.port.AuditLogRepository;
import ro.marketplace.domain.port.CurrentActorProvider;
import ro.marketplace.domain.port.CustomerRepository;
import ro.marketplace.domain.port.DisputeRepository;
import ro.marketplace.domain.port.LeadRepository;
import ro.marketplace.domain.port.ServiceCategoryRepository;
import ro.marketplace.domain.port.UnitOfWork;
import ro.marketplace.domain.port.UserRepository;
import ro.marketplace.domain.port.WalletTransactionRepository;
import ro.marketplace.infrastructure.persistence.repository.SpringDataCreditPurchaseRepository;
import ro.marketplace.infrastructure.persistence.repository.SpringDataStripeWebhookEventRepository;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.autoconfigure.exclude=" +
                "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration," +
                "org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration," +
                "org.springframework.boot.data.jpa.autoconfigure.JpaRepositoriesAutoConfiguration," +
                "org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration"
})
class BackendApplicationTests {

    @MockitoBean
    private CustomerRepository customerRepository;

    @MockitoBean
    private AuditLogRepository auditLogRepository;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private WalletTransactionRepository walletTransactionRepository;

    @MockitoBean
    private LeadRepository leadRepository;

    @MockitoBean
    private DisputeRepository disputeRepository;

    @MockitoBean
    private ServiceCategoryRepository serviceCategoryRepository;

    @MockitoBean
    private UnitOfWork unitOfWork;

    @MockitoBean
    private CurrentActorProvider currentActorProvider;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMappingContext;

    @MockitoBean
    private SpringDataCreditPurchaseRepository creditPurchaseRepository;

    @MockitoBean
    private SpringDataStripeWebhookEventRepository stripeWebhookEventRepository;

    @Test
    void contextLoads() {
    }
}
