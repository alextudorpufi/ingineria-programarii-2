package ro.marketplace.infrastructure.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ro.marketplace.infrastructure.persistence.entity.WalletTransactionEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataWalletTransactionRepository extends JpaRepository<WalletTransactionEntity, UUID> {

    /** Sorting is supplied by the caller so paging always carries an id tie-breaker. */
    Page<WalletTransactionEntity> findByUserId(UUID userId, Pageable pageable);

    List<WalletTransactionEntity> findByUserIdAndLeadIdOrderByCreatedAtAscIdAsc(UUID userId, UUID leadId);
}
