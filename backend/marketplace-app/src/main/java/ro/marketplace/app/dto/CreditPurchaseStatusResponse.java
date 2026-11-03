package ro.marketplace.app.dto;

import ro.marketplace.domain.model.CreditPurchase;

import java.util.UUID;

public record CreditPurchaseStatusResponse(UUID purchaseId, String status, int credits) {

    public static CreditPurchaseStatusResponse from(CreditPurchase purchase) {
        return new CreditPurchaseStatusResponse(
                purchase.id(), purchase.status().getDbValue(), purchase.credits());
    }
}
