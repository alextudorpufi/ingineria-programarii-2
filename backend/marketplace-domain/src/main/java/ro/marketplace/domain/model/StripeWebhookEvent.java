package ro.marketplace.domain.model;

public record StripeWebhookEvent(
        String id,
        String type,
        String sessionId,
        String purchaseId,
        Long amountTotal,
        String currency,
        String paymentStatus
) {
}
