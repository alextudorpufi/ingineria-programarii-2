package ro.marketplace.domain.model;

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
        return DbEnumValue.parse(value, "wallet transaction type", values(), WalletTransactionType::getDbValue);
    }
}
