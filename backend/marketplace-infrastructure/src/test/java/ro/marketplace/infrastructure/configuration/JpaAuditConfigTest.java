package ro.marketplace.infrastructure.configuration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.domain.AuditorAware;
import ro.marketplace.domain.port.CurrentActorProvider;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class JpaAuditConfigTest {

    private final JpaAuditConfig config = new JpaAuditConfig();
    private final Clock fixedClock =
            Clock.fixed(Instant.parse("2026-01-01T10:00:00Z"), ZoneOffset.UTC);

    @Test
    @DisplayName("auditing timestamps come from the shared application clock")
    void auditingUsesClock() {
        DateTimeProvider provider = config.auditingDateTimeProvider(fixedClock);

        assertThat(provider.getNow()).contains(LocalDateTime.of(2026, 1, 1, 10, 0));
    }

    @Test
    @DisplayName("the auditor falls back to system outside a request")
    void auditorFallsBackToSystem() {
        CurrentActorProvider anonymous = Optional::empty;
        AuditorAware<String> auditor = config.auditorProvider(anonymous);

        assertThat(auditor.getCurrentAuditor()).contains("system");
    }

    @Test
    @DisplayName("the auditor keeps a bounded authenticated actor")
    void auditorTruncatesLongActor() {
        CurrentActorProvider actor = () -> Optional.of("x".repeat(150));
        AuditorAware<String> auditor = config.auditorProvider(actor);

        assertThat(auditor.getCurrentAuditor()).hasValueSatisfying(value ->
                assertThat(value).hasSize(100));
    }
}
