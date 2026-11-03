package ro.marketplace.app.dto;

import ro.marketplace.domain.model.CreditBundle;

public record CreditBundleResponse(
        String id,
        String name,
        String tag,
        int credits,
        int bonusCredits,
        int totalCredits,
        long amountMinor,
        String currency,
        boolean popular
) {
    public static CreditBundleResponse from(CreditBundle bundle) {
        return new CreditBundleResponse(bundle.id(), bundle.name(), bundle.tag(), bundle.credits(),
                bundle.bonusCredits(), bundle.totalCredits(), bundle.amountMinor(), bundle.currency(),
                bundle.popular());
    }
}
