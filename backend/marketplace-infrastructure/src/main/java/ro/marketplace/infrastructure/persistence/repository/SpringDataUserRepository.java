package ro.marketplace.infrastructure.persistence.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ro.marketplace.infrastructure.persistence.entity.UserEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataUserRepository extends JpaRepository<UserEntity, UUID> {

    Optional<UserEntity> findByEmail(String email);

    List<UserEntity> findByRole(String role, Pageable pageable);

    boolean existsByEmail(String email);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE users SET credits_balance = credits_balance + :amount, "
            + "version = version + 1, updated_at = now() WHERE id = :userId", nativeQuery = true)
    int addCredits(@Param("userId") UUID userId, @Param("amount") int amount);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE users SET credits_balance = credits_balance - :amount, "
            + "version = version + 1, updated_at = now() "
            + "WHERE id = :userId AND credits_balance >= :amount", nativeQuery = true)
    int deductCredits(@Param("userId") UUID userId, @Param("amount") int amount);
}
