package ro.marketplace.app.dto;

import java.util.UUID;

public record CreditCheckoutResponse(UUID purchaseId, String checkoutUrl) {
}
