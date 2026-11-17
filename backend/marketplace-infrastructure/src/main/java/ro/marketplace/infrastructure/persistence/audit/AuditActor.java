package ro.marketplace.infrastructure.persistence.audit;

import ro.marketplace.domain.port.CurrentActorProvider;

import java.util.Objects;

/**
 * Resolves the actor written to the bounded {@code created_by}/{@code updated_by}
 * columns. Native bulk updates bypass JPA auditing, so they need the same fallback
 * and truncation the JPA auditor and the audit service already apply.
 */
public final class AuditActor {

    /** {@code users.updated_by} and {@code audit_logs.changed_by} are VARCHAR(100). */
    private static final int MAX_ACTOR_LENGTH = 100;
    private static final String SYSTEM = "system";

    private AuditActor() {
    }

    public static String resolve(CurrentActorProvider currentActorProvider) {
        Objects.requireNonNull(currentActorProvider, "currentActorProvider must not be null");
        return currentActorProvider.currentActor()
                .filter(actor -> !actor.isBlank())
                .map(actor -> actor.length() > MAX_ACTOR_LENGTH
                        ? actor.substring(0, MAX_ACTOR_LENGTH)
                        : actor)
                .orElse(SYSTEM);
    }
}
