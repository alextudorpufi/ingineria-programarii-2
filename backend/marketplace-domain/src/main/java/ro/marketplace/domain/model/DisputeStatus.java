package ro.marketplace.domain.model;

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
        return DbEnumValue.parse(value, "dispute status", values(), DisputeStatus::getDbValue);
    }
}
