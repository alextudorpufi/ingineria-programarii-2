package ro.marketplace.app.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryRateLimiterTest {

    private static final InMemoryRateLimiter.Rule LOGIN_RULE =
            new InMemoryRateLimiter.Rule(5, Duration.ofHours(1));

    @Test
    @DisplayName("the sixth login attempt within an hour is rejected")
    void rejectsAttemptAboveCapacity() {
        MutableClock clock = new MutableClock(Instant.parse("2026-01-01T10:00:00Z"));
        InMemoryRateLimiter limiter = new InMemoryRateLimiter(clock);

        for (int attempt = 1; attempt <= 5; attempt++) {
            assertThat(limiter.tryConsume("127.0.0.1|login", LOGIN_RULE).allowed()).isTrue();
        }

        InMemoryRateLimiter.Decision decision = limiter.tryConsume("127.0.0.1|login", LOGIN_RULE);
        assertThat(decision.allowed()).isFalse();
        assertThat(decision.retryAfterSeconds()).isPositive();
    }

    @Test
    @DisplayName("the bucket refills after the configured period")
    void refillsAfterPeriod() {
        MutableClock clock = new MutableClock(Instant.parse("2026-01-01T10:00:00Z"));
        InMemoryRateLimiter limiter = new InMemoryRateLimiter(clock);

        for (int attempt = 1; attempt <= 5; attempt++) {
            limiter.tryConsume("ip|login", LOGIN_RULE);
        }
        assertThat(limiter.tryConsume("ip|login", LOGIN_RULE).allowed()).isFalse();

        clock.advance(Duration.ofHours(1));

        assertThat(limiter.tryConsume("ip|login", LOGIN_RULE).allowed()).isTrue();
    }

    @Test
    @DisplayName("different client addresses have independent budgets")
    void budgetsAreIndependentPerClient() {
        InMemoryRateLimiter limiter = new InMemoryRateLimiter();

        for (int attempt = 1; attempt <= 5; attempt++) {
            limiter.tryConsume("ip-a|login", LOGIN_RULE);
        }

        assertThat(limiter.tryConsume("ip-a|login", LOGIN_RULE).allowed()).isFalse();
        assertThat(limiter.tryConsume("ip-b|login", LOGIN_RULE).allowed()).isTrue();
    }

    private static final class MutableClock extends Clock {

        private Instant instant;

        private MutableClock(Instant instant) {
            this.instant = instant;
        }

        private void advance(Duration duration) {
            instant = instant.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }
}
