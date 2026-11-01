package ro.marketplace.domain.model;

import java.util.Arrays;
import java.util.Objects;

/**
 * Lead lifecycle states. The database stores the lower-case {@link #getDbValue()}.
 */
public enum LeadStatus {
    VALIDAT("validat"),
    IN_ASTEPTARE("in_asteptare"),
    ATRIBUIT("atribuit"),
    FINALIZAT("finalizat"),
    DISPUTAT("disputat");

    private final String dbValue;

    LeadStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    public static LeadStatus fromDbValue(String value) {
        Objects.requireNonNull(value, "status must not be null");
        String normalized = value.trim();
        return Arrays.stream(values())
                .filter(status -> status.dbValue.equalsIgnoreCase(normalized)
                        || status.name().equalsIgnoreCase(normalized))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown lead status: " + value));
    }
}
