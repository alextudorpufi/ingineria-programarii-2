package ro.marketplace.infrastructure.persistence.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ro.marketplace.infrastructure.persistence.entity.WalletTransactionEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataWalletTransactionRepository extends JpaRepository<WalletTransactionEntity, UUID> {

    List<WalletTransactionEntity> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
}
