package ro.marketplace.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Auditable record of free credits granted to a professional by an administrator.
 *
 * <p>Keeping every grant makes it possible to prove that the one-time welcome
 * credit was issued at most once per account.</p>
 */
public record CreditGrant(
        UUID id,
        UUID userId,
        int amount,
        UUID adminId,
        LocalDateTime grantedAt
) {
}
