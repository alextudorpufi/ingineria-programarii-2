package ro.marketplace.infrastructure.persistence.adapter;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import ro.marketplace.domain.port.UnitOfWork;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Spring implementation of the application transaction boundary.
 *
 * <p>Propagation REQUIRED makes nested use cases join the caller's transaction, so a
 * claim + wallet deduction + audit entry either all commit or all roll back.</p>
 */
@Repository
public class SpringUnitOfWorkAdapter implements UnitOfWork {

    private final TransactionTemplate transactionTemplate;

    public SpringUnitOfWorkAdapter(PlatformTransactionManager transactionManager) {
        Objects.requireNonNull(transactionManager, "transactionManager must not be null");
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        this.transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        this.transactionTemplate.setName("marketplace-unit-of-work");
    }

    @Override
    public <T> T executeInTransaction(Supplier<T> work) {
        Objects.requireNonNull(work, "work must not be null");
        return transactionTemplate.execute(status -> work.get());
    }
}
