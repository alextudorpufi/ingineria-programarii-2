package ro.marketplace.infrastructure.persistence.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ro.marketplace.infrastructure.persistence.entity.LeadEntity;

import java.time.LocalDateTime;
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
    @Query(value = "UPDATE leads SET current_pros = current_pros + 1, version = version + 1, "
            + "updated_at = :updatedAt, updated_by = :actor "
            + "WHERE id = :leadId AND current_pros < max_pros "
            + "AND status IN (:validatedStatus, :waitingStatus)", nativeQuery = true)
    int reserveSlot(@Param("leadId") UUID leadId,
                    @Param("validatedStatus") String validatedStatus,
                    @Param("waitingStatus") String waitingStatus,
                    @Param("actor") String actor,
                    @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * Marks a lead as assigned only while it is still claimable, so a finalized or
     * disputed lead can never be moved back into the assigned state.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE leads SET status = :assignedStatus, version = version + 1, "
            + "updated_at = :updatedAt, updated_by = :actor "
            + "WHERE id = :leadId AND current_pros >= max_pros "
            + "AND status IN (:validatedStatus, :waitingStatus)", nativeQuery = true)
    int markAssignedIfFull(@Param("leadId") UUID leadId,
                           @Param("assignedStatus") String assignedStatus,
                           @Param("validatedStatus") String validatedStatus,
                           @Param("waitingStatus") String waitingStatus,
                           @Param("actor") String actor,
                           @Param("updatedAt") LocalDateTime updatedAt);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE leads SET status = :newStatus, version = version + 1, "
            + "updated_at = :updatedAt, updated_by = :actor "
            + "WHERE id = :leadId AND status = :expectedStatus", nativeQuery = true)
    int updateStatus(@Param("leadId") UUID leadId,
                     @Param("expectedStatus") String expectedStatus,
                     @Param("newStatus") String newStatus,
                     @Param("actor") String actor,
                     @Param("updatedAt") LocalDateTime updatedAt);
}
