package ro.marketplace.application.service;

import ro.marketplace.domain.port.UnitOfWork;

import java.util.function.Supplier;

/**
 * Test double that executes work immediately, mirroring REQUIRED propagation semantics.
 */
final class DirectUnitOfWork implements UnitOfWork {

    @Override
    public <T> T executeInTransaction(Supplier<T> work) {
        return work.get();
    }
}
