package ro.marketplace.domain.model;

/**
 * Account roles. The database stores the lower-case {@link #getDbValue()}.
 * Centralising the allowed values keeps authorisation checks from drifting into
 * ad-hoc string comparisons.
 */
public enum UserRole {
    CLIENT("client"),
    PRO("pro"),
    ADMIN("admin");

    private final String dbValue;

    UserRole(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    public boolean isClient() {
        return this == CLIENT;
    }

    public boolean isPro() {
        return this == PRO;
    }

    public boolean isAdmin() {
        return this == ADMIN;
    }

    public static UserRole fromDbValue(String value) {
        return DbEnumValue.parse(value, "user role", values(), UserRole::getDbValue);
    }

    /**
     * Roles that may be chosen through public registration. Administrators can
     * only be provisioned out of band.
     */
    public static UserRole fromRegistrationRole(String value) {
        UserRole role = fromDbValue(value);
        if (role.isAdmin()) {
            throw new IllegalArgumentException("Public registration cannot create administrators.");
        }
        return role;
    }
}
