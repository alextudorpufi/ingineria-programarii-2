package ro.marketplace.domain.model;

/**
 * Canonical business actions recorded in the audit ledger.
 * Keeping this as an enum prevents illegal, stringly-typed actions.
 */
public enum AuditAction {
    CREATE,
    UPDATE,
    DELETE,
    STATUS_CHANGE,
    CLAIM_LEAD,
    CREDIT_DEDUCTION,
    CREDIT_TOP_UP,
    CREDIT_BONUS,
    REFUND_APPROVED,
    RAISE_DISPUTE,
    RESOLVE_DISPUTE,
    VERIFY_PROFESSIONAL;

    /**
     * Reads persisted actions tolerantly: case and surrounding spaces are
     * ignored so historical rows written before this enum still load.
     */
    public static AuditAction fromDbValue(String value) {
        return DbEnumValue.parse(value, "audit action", values(), AuditAction::name);
    }
}
