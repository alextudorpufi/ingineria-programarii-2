package ro.marketplace.infrastructure.persistence.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ro.marketplace.infrastructure.persistence.entity.LeadEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataLeadRepository extends JpaRepository<LeadEntity, UUID> {

    List<LeadEntity> findByCategoryId(String categoryId, Pageable pageable);

    List<LeadEntity> findByStatus(String status, Pageable pageable);

    long countByCategoryId(String categoryId);

    long countByStatus(String status);

    /**
     * Atomic slot reservation. Exactly one concurrent transaction can increment each slot.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE leads SET current_pros = current_pros + 1, version = version + 1, updated_at = now() "
            + "WHERE id = :leadId AND current_pros < max_pros "
            + "AND status IN ('validat', 'in_asteptare')", nativeQuery = true)
    int reserveSlot(@Param("leadId") UUID leadId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE leads SET current_pros = current_pros - 1, version = version + 1, updated_at = now() "
            + "WHERE id = :leadId AND current_pros > 0", nativeQuery = true)
    int releaseSlot(@Param("leadId") UUID leadId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE leads SET status = 'atribuit', version = version + 1, updated_at = now() "
            + "WHERE id = :leadId AND current_pros >= max_pros AND status <> 'atribuit'", nativeQuery = true)
    int markAssignedIfFull(@Param("leadId") UUID leadId);
}
