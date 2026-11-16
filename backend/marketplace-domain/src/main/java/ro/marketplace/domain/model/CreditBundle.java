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
    public CreditBundle {
        if (credits < 0 || bonusCredits < 0) {
            throw new IllegalArgumentException("Bundle credits must not be negative");
        }
    }

    public int totalCredits() {
        return Math.addExact(credits, bonusCredits);
    }
}
