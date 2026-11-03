package ro.marketplace.domain.model;

public record CreditBundle(
        String id,
        String name,
        String tag,
        int credits,
        int bonusCredits,
        long amountMinor,
        String currency,
        boolean popular
) {
    public int totalCredits() {
        return credits + bonusCredits;
    }
}
