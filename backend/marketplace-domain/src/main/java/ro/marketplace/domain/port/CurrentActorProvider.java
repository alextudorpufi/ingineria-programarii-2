package ro.marketplace.domain.port;

import java.util.Optional;

/**
 * Supplies the authenticated actor for audit attribution. Implementations must derive the
 * value from the security context, never from client-supplied request fields.
 */
public interface CurrentActorProvider {

    Optional<String> currentActor();
}
