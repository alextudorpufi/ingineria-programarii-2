package ro.marketplace.domain.model;

import ro.marketplace.domain.exception.ConflictException;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreditPurchase(
        UUID id,
        UUID userId,
        String bundleId,
        int credits,
        long amountMinor,
        String currency,
        CreditPurchaseStatus status,
        String idempotencyKey,
        String stripeSessionId,
        String checkoutUrl,
        UUID walletTransactionId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public CreditPurchase {
        if (credits <= 0) {
            throw new IllegalArgumentException("Credit purchase must contain a positive number of credits");
        }
        if (amountMinor <= 0) {
            throw new IllegalArgumentException("Credit purchase amount must be positive");
        }
    }

    public CreditPurchase withCheckoutSession(String sessionId, String url, LocalDateTime updated) {
        requirePending("attach a checkout session to");
        return new CreditPurchase(id, userId, bundleId, credits, amountMinor, currency, status,
                idempotencyKey, sessionId, url, walletTransactionId, createdAt, updated);
    }

    /** Marks a pending purchase as paid exactly once. */
    public CreditPurchase markPaid(UUID transactionId, LocalDateTime updated) {
        requirePending("mark as paid");
        return transition(CreditPurchaseStatus.PAID, transactionId, updated);
    }

    /** Marks a pending purchase as failed. Paid purchases are immutable. */
    public CreditPurchase markFailed(LocalDateTime updated) {
        requirePending("mark as failed");
        return transition(CreditPurchaseStatus.FAILED, null, updated);
    }

    /** Marks a pending purchase as expired. Paid purchases are immutable. */
    public CreditPurchase markExpired(LocalDateTime updated) {
        requirePending("mark as expired");
        return transition(CreditPurchaseStatus.EXPIRED, null, updated);
    }

    private CreditPurchase transition(CreditPurchaseStatus newStatus, UUID transactionId, LocalDateTime updated) {
        return new CreditPurchase(id, userId, bundleId, credits, amountMinor, currency, newStatus,
                idempotencyKey, stripeSessionId, checkoutUrl, transactionId, createdAt, updated);
    }

    private void requirePending(String action) {
        if (status != CreditPurchaseStatus.PENDING) {
            throw new ConflictException("Cannot " + action + " a purchase in status "
                    + (status != null ? status.getDbValue() : "unknown"));
        }
    }
}
