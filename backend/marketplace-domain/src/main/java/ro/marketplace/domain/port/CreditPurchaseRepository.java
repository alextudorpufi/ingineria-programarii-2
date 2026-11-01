package ro.marketplace.domain.port;

import ro.marketplace.domain.model.CreditPurchase;

import java.util.Optional;
import java.util.UUID;

public interface CreditPurchaseRepository {

    CreditPurchase save(CreditPurchase purchase);

    Optional<CreditPurchase> findByUserIdAndIdempotencyKey(UUID userId, String idempotencyKey);

    Optional<CreditPurchase> findByIdAndUserId(UUID purchaseId, UUID userId);

    Optional<CreditPurchase> findByStripeSessionIdForUpdate(String sessionId);

    boolean hasProcessedWebhookEvent(String eventId);

    void recordWebhookEvent(String eventId, String eventType);
}
