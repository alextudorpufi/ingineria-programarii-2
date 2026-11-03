package ro.marketplace.domain.model;

import java.util.Arrays;
import java.util.Objects;

/**
 * Ledger entry kinds. The database stores the lower-case {@link #getDbValue()}.
 */
public enum WalletTransactionType {
    LEAD_PURCHASE("lead_purchase"),
    REFUND("refund"),
    TOP_UP("top_up"),
    BONUS("bonus");

    private final String dbValue;

    WalletTransactionType(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    public static WalletTransactionType fromDbValue(String value) {
        Objects.requireNonNull(value, "type must not be null");
        String normalized = value.trim();
        return Arrays.stream(values())
                .filter(type -> type.dbValue.equalsIgnoreCase(normalized)
                        || type.name().equalsIgnoreCase(normalized))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown wallet transaction type: " + value));
    }
}
