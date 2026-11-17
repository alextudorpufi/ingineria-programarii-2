package ro.marketplace.domain.model;

/**
 * Non-negative credit balance. Arithmetic is overflow-checked so a corrupted
 * ledger can never wrap around into a valid-looking balance.
 */
public record Credits(int value) {

    public static final Credits ZERO = new Credits(0);

    public Credits {
        if (value < 0) {
            throw new IllegalArgumentException("Credits must not be negative");
        }
    }

    public static Credits of(int value) {
        return value == 0 ? ZERO : new Credits(value);
    }

    public Credits plus(Credits other) {
        return of(Math.addExact(value, other.value));
    }

    public Credits minus(Credits other) {
        if (value < other.value) {
            throw new IllegalArgumentException("Insufficient credits: " + value + " < " + other.value);
        }
        return of(value - other.value);
    }

    public boolean isAtLeast(Credits other) {
        return value >= other.value;
    }

    public int asInt() {
        return value;
    }
}
