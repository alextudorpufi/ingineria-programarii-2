package ro.marketplace.domain.model;

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
        return DbEnumValue.parse(value, "lead status", values(), LeadStatus::getDbValue);
    }
}
