package ro.marketplace.domain.model;

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
    public CreditPurchase withCheckoutSession(String sessionId, String url, LocalDateTime updated) {
        return new CreditPurchase(id, userId, bundleId, credits, amountMinor, currency, status,
                idempotencyKey, sessionId, url, walletTransactionId, createdAt, updated);
    }

    public CreditPurchase withStatus(CreditPurchaseStatus newStatus,
                                     UUID transactionId,
                                     LocalDateTime updated) {
        return new CreditPurchase(id, userId, bundleId, credits, amountMinor, currency, newStatus,
                idempotencyKey, stripeSessionId, checkoutUrl, transactionId, createdAt, updated);
    }
}
