package ro.marketplace.domain.model;

import java.util.Arrays;
import java.util.Objects;

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
        Objects.requireNonNull(value, "action must not be null");
        String normalized = value.trim();
        return Arrays.stream(values())
                .filter(action -> action.name().equalsIgnoreCase(normalized))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown audit action: " + value));
    }
}
