package ro.marketplace.infrastructure.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import ro.marketplace.domain.port.CurrentActorProvider;
import ro.marketplace.infrastructure.persistence.audit.AuditActor;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;

@Configuration
@EnableJpaAuditing(
        auditorAwareRef = "auditorProvider",
        dateTimeProviderRef = "auditingDateTimeProvider")
public class JpaAuditConfig {

    @Bean
    public AuditorAware<String> auditorProvider(CurrentActorProvider currentActorProvider) {
        // Background jobs have no security context; without the fallback the
        // bounded columns would receive null instead of 'system'.
        return () -> Optional.of(AuditActor.resolve(currentActorProvider));
    }

    @Bean
    public DateTimeProvider auditingDateTimeProvider(Clock clock) {
        // Reuse the application clock so @CreatedDate/@LastModifiedDate agree with
        // the timestamps the services write and tests can control them.
        return () -> Optional.of(LocalDateTime.now(clock));
    }
}
