package ro.marketplace.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ro.marketplace.infrastructure.persistence.entity.CreditGrantEntity;

import java.util.UUID;

@Repository
public interface SpringDataCreditGrantRepository extends JpaRepository<CreditGrantEntity, UUID> {

    @Query("SELECT COALESCE(SUM(g.amount), 0) FROM CreditGrantEntity g WHERE g.userId = :userId")
    long sumAmountByUserId(@Param("userId") UUID userId);
}
