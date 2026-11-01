package ro.marketplace.infrastructure.persistence.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ro.marketplace.infrastructure.persistence.entity.CreditPurchaseEntity;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataCreditPurchaseRepository extends JpaRepository<CreditPurchaseEntity, UUID> {

    Optional<CreditPurchaseEntity> findByUserIdAndIdempotencyKey(UUID userId, String idempotencyKey);

    Optional<CreditPurchaseEntity> findByIdAndUserId(UUID id, UUID userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select purchase from CreditPurchaseEntity purchase where purchase.stripeSessionId = :sessionId")
    Optional<CreditPurchaseEntity> findByStripeSessionIdForUpdate(@Param("sessionId") String sessionId);
}
