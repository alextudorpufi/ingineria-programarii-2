package ro.marketplace.domain.port;

import java.util.function.Supplier;

/**
 * Transaction boundary owned by the application layer, implemented by infrastructure.
 *
 * <p>Keeping this port in the domain module lets use cases be atomic without importing
 * Spring's {@code @Transactional}. Implementations must join an already-active transaction
 * (propagation REQUIRED) so nested use cases participate in the caller's unit of work.</p>
 */
public interface UnitOfWork {

    <T> T executeInTransaction(Supplier<T> work);
}
