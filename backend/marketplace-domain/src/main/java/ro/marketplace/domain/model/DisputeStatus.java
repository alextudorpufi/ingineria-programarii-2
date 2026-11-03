package ro.marketplace.domain.model;

import java.util.Arrays;
import java.util.Objects;

/**
 * Dispute lifecycle states. The database stores the lower-case {@link #getDbValue()}.
 */
public enum DisputeStatus {
    IN_ANALIZA("in_analiza"),
    APROBAT("aprobat"),
    RESPINS("respins");

    private final String dbValue;

    DisputeStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    public static DisputeStatus fromDbValue(String value) {
        Objects.requireNonNull(value, "status must not be null");
        String normalized = value.trim();
        return Arrays.stream(values())
                .filter(status -> status.dbValue.equalsIgnoreCase(normalized)
                        || status.name().equalsIgnoreCase(normalized))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown dispute status: " + value));
    }
}
