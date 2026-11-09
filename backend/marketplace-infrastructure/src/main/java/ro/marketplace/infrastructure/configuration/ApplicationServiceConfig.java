package ro.marketplace.infrastructure.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import ro.marketplace.application.service.AuditService;
import ro.marketplace.application.service.AuditServiceImpl;
import ro.marketplace.application.service.AuthService;
import ro.marketplace.application.service.AuthServiceImpl;
import ro.marketplace.application.service.ProfessionalProfileService;
import ro.marketplace.application.service.ProfessionalProfileServiceImpl;
import ro.marketplace.application.service.CreditPaymentService;
import ro.marketplace.application.service.CustomerService;
import ro.marketplace.application.service.CustomerServiceImpl;
import ro.marketplace.application.service.DisputeService;
import ro.marketplace.application.service.DisputeServiceImpl;
import ro.marketplace.application.service.LeadService;
import ro.marketplace.application.service.LeadServiceImpl;
import ro.marketplace.application.service.WalletService;
import ro.marketplace.application.service.WalletServiceImpl;
import ro.marketplace.domain.port.AuditLogRepository;
import ro.marketplace.domain.port.CreditGrantRepository;
import ro.marketplace.domain.port.CurrentActorProvider;
import ro.marketplace.domain.port.CreditPurchaseRepository;
import ro.marketplace.domain.port.CustomerRepository;
import ro.marketplace.domain.port.DisputeRepository;
import ro.marketplace.domain.port.LeadRepository;
import ro.marketplace.domain.port.PasswordHasher;
import ro.marketplace.domain.port.ServiceCategoryRepository;
import ro.marketplace.domain.port.StripePaymentGateway;
import ro.marketplace.domain.port.UnitOfWork;
import ro.marketplace.domain.port.UserRepository;
import ro.marketplace.domain.port.WalletTransactionRepository;

import java.time.Clock;

@Configuration
public class ApplicationServiceConfig {

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

    @Bean
    public CustomerService customerService(CustomerRepository customerRepository,
                                           UnitOfWork unitOfWork,
                                           Clock clock) {
        return new CustomerServiceImpl(customerRepository, unitOfWork, clock);
    }

    @Bean
    public AuthService authService(UserRepository userRepository,
                                   CreditGrantRepository creditGrantRepository,
                                   PasswordHasher passwordHasher,
                                   UnitOfWork unitOfWork,
                                   Clock clock) {
        return new AuthServiceImpl(userRepository, creditGrantRepository, passwordHasher, unitOfWork, clock);
    }

    @Bean
    public ProfessionalProfileService professionalProfileService(UserRepository users,
                                                                 ServiceCategoryRepository categories,
                                                                 UnitOfWork unitOfWork) {
        return new ProfessionalProfileServiceImpl(users, categories, unitOfWork);
    }

    @Bean
    public AuditService auditService(AuditLogRepository auditLogRepository,
                                     Clock clock,
                                     CurrentActorProvider currentActorProvider) {
        return new AuditServiceImpl(auditLogRepository, clock, currentActorProvider);
    }

    @Bean
    public WalletService walletService(UserRepository userRepository,
                                       WalletTransactionRepository walletTransactionRepository,
                                       AuditService auditService,
                                       UnitOfWork unitOfWork,
                                       Clock clock) {
        return new WalletServiceImpl(userRepository, walletTransactionRepository, auditService, unitOfWork, clock);
    }

    @Bean
    public CreditPaymentService creditPaymentService(CreditPurchaseRepository creditPurchaseRepository,
                                                     StripePaymentGateway stripePaymentGateway,
                                                     WalletService walletService,
                                                     UnitOfWork unitOfWork,
                                                     Clock clock,
                                                     @Value("${app.frontend-url:http://localhost:3000}") String frontendUrl) {
        return new CreditPaymentService(creditPurchaseRepository, stripePaymentGateway, walletService,
                unitOfWork, clock, frontendUrl);
    }

    @Bean
    public LeadService leadService(LeadRepository leadRepository,
                                   ServiceCategoryRepository serviceCategoryRepository,
                                   WalletService walletService,
                                   AuditService auditService,
                                   UnitOfWork unitOfWork,
                                   Clock clock) {
        return new LeadServiceImpl(leadRepository, serviceCategoryRepository, walletService,
                auditService, unitOfWork, clock);
    }

    @Bean
    public DisputeService disputeService(DisputeRepository disputeRepository,
                                         WalletService walletService,
                                         AuditService auditService,
                                         UnitOfWork unitOfWork,
                                         Clock clock) {
        return new DisputeServiceImpl(disputeRepository, walletService, auditService, unitOfWork, clock);
    }
}
