package ro.marketplace.app.security;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Process-local denylist of revoked JWT IDs ({@code jti}).
 *
 * <p>Access tokens are stateless, so logout and refresh must revoke the old token
 * explicitly. Entries are kept only until the token expires, which bounds memory
 * usage. A multi-instance deployment should replace this with a shared store
 * (for example Redis); the rest of the code depends only on this small surface.</p>
 */
public class RevokedTokenStore {

    private static final Duration FALLBACK_RETENTION = Duration.ofHours(1);

    private final Map<String, Instant> revokedUntil = new ConcurrentHashMap<>();

    /**
     * Revokes a token until its own expiry. Tokens without a {@code jti} are
     * rejected by {@link #isRevoked(String)} because they cannot be revoked safely.
     */
    public void revoke(String jwtId, Instant expiresAt) {
        if (jwtId == null || jwtId.isBlank()) {
            return;
        }
        Instant until = expiresAt != null ? expiresAt : Instant.now().plus(FALLBACK_RETENTION);
        revokedUntil.put(jwtId, until);
        purgeExpired();
    }

    public boolean isRevoked(String jwtId) {
        if (jwtId == null || jwtId.isBlank()) {
            return true;
        }
        Instant until = revokedUntil.get(jwtId);
        if (until == null) {
            return false;
        }
        if (until.isBefore(Instant.now())) {
            revokedUntil.remove(jwtId);
            return false;
        }
        return true;
    }

    private void purgeExpired() {
        Instant now = Instant.now();
        revokedUntil.entrySet().removeIf(entry -> entry.getValue().isBefore(now));
    }
}
