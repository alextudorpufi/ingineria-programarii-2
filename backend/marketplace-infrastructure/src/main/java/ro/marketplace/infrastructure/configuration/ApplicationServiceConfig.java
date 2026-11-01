package ro.marketplace.infrastructure.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ro.marketplace.application.service.AuditService;
import ro.marketplace.application.service.AuditServiceImpl;
import ro.marketplace.application.service.CustomerService;
import ro.marketplace.application.service.CustomerServiceImpl;
import ro.marketplace.application.service.DisputeService;
import ro.marketplace.application.service.DisputeServiceImpl;
import ro.marketplace.application.service.LeadService;
import ro.marketplace.application.service.LeadServiceImpl;
import ro.marketplace.application.service.WalletService;
import ro.marketplace.application.service.WalletServiceImpl;
import ro.marketplace.domain.port.AuditLogRepository;
import ro.marketplace.domain.port.CurrentActorProvider;
import ro.marketplace.domain.port.CustomerRepository;
import ro.marketplace.domain.port.DisputeRepository;
import ro.marketplace.domain.port.LeadRepository;
import ro.marketplace.domain.port.ServiceCategoryRepository;
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
