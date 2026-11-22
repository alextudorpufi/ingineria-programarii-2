package ro.marketplace.domain.model;

public enum CreditPurchaseStatus {
    PENDING("pending"),
    PAID("paid"),
    FAILED("failed"),
    EXPIRED("expired");

    private final String dbValue;

    CreditPurchaseStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    public static CreditPurchaseStatus fromDbValue(String value) {
        return DbEnumValue.parse(value, "credit purchase status", values(), CreditPurchaseStatus::getDbValue);
    }
}
