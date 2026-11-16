package ro.marketplace.domain.model;

import java.util.Arrays;
import java.util.Objects;

/**
 * Catalog of valid dispute reasons. The database stores the lower-case
 * {@link #getDbValue()}.
 */
public enum DisputeReason {
    UNREACHABLE_CONTACT("unreachable_contact"),
    WRONG_CATEGORY("wrong_category"),
    INCOMPATIBLE_BUDGET("incompatible_budget"),
    DUPLICATE_OR_FAKE("duplicate_or_fake");

    private final String dbValue;

    DisputeReason(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    public static DisputeReason fromDbValue(String value) {
        Objects.requireNonNull(value, "reason must not be null");
        String normalized = value.trim();
        return Arrays.stream(values())
                .filter(reason -> reason.dbValue.equalsIgnoreCase(normalized)
                        || reason.name().equalsIgnoreCase(normalized))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown dispute reason: " + value));
    }
}
