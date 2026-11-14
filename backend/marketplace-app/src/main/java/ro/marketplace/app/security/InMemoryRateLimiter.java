package ro.marketplace.app.security;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Small in-process token-bucket rate limiter for public authentication endpoints.
 *
 * <p>The application runs as a single instance in this project; a horizontally
 * scaled deployment should swap this for a shared Redis-backed implementation.
 * Keep the public surface unchanged when doing so.</p>
 */
public final class InMemoryRateLimiter {

    private static final int MAX_TRACKED_KEYS = 10_000;

    public record Rule(int capacity, Duration period) {
        public Rule {
            if (capacity <= 0) {
                throw new IllegalArgumentException("capacity must be positive");
            }
            if (period == null || period.isZero() || period.isNegative()) {
                throw new IllegalArgumentException("period must be positive");
            }
        }
    }

    public record Decision(boolean allowed, long retryAfterSeconds) {
    }

    private final Map<String, TokenBucket> buckets = new ConcurrentHashMap<>();
    private final Clock clock;

    public InMemoryRateLimiter() {
        this(Clock.systemUTC());
    }

    public InMemoryRateLimiter(Clock clock) {
        this.clock = clock;
    }

    public Decision tryConsume(String key, Rule rule) {
        if (buckets.size() >= MAX_TRACKED_KEYS) {
            removeIdleBuckets(rule);
            // Hard bound: never let a key flood grow the map without limit.
            if (buckets.size() >= MAX_TRACKED_KEYS) {
                buckets.keySet().stream().findFirst().ifPresent(buckets::remove);
            }
        }
        TokenBucket bucket = buckets.computeIfAbsent(key, ignored -> new TokenBucket(rule));
        return bucket.tryConsume(rule);
    }

    public void clear() {
        buckets.clear();
    }

    private void removeIdleBuckets(Rule rule) {
        Instant now = clock.instant();
        buckets.entrySet().removeIf(entry -> entry.getValue().isIdle(rule, now));
    }

    private final class TokenBucket {

        private double tokens;
        private Instant lastRefill;

        private TokenBucket(Rule rule) {
            this.tokens = rule.capacity();
            this.lastRefill = clock.instant();
        }

        private synchronized Decision tryConsume(Rule rule) {
            refill(rule);
            if (tokens >= 1.0) {
                tokens -= 1.0;
                return new Decision(true, 0);
            }
            return new Decision(false, secondsUntilNextToken(rule));
        }

        private void refill(Rule rule) {
            Instant now = clock.instant();
            long elapsedMillis = Duration.between(lastRefill, now).toMillis();
            if (elapsedMillis <= 0) {
                return;
            }
            double tokensPerMillis = rule.capacity() / (double) rule.period().toMillis();
            tokens = Math.min(rule.capacity(), tokens + elapsedMillis * tokensPerMillis);
            lastRefill = now;
        }

        private long secondsUntilNextToken(Rule rule) {
            double tokensPerMillis = rule.capacity() / (double) rule.period().toMillis();
            long missingMillis = (long) Math.ceil((1.0 - tokens) / tokensPerMillis);
            return Math.max(1, (missingMillis + 999) / 1000);
        }

        private synchronized boolean isIdle(Rule rule, Instant now) {
            return tokens >= rule.capacity()
                    && Duration.between(lastRefill, now).compareTo(rule.period()) > 0;
        }
    }
}
