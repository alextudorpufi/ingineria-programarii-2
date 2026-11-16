package ro.marketplace.domain.model;

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
        return DbEnumValue.parse(value, "dispute reason", values(), DisputeReason::getDbValue);
    }
}
