package ro.marketplace.infrastructure.persistence.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ro.marketplace.infrastructure.persistence.entity.AuditLogEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataAuditLogRepository extends JpaRepository<AuditLogEntity, UUID> {

    /** Newest first, with id as a deterministic tie-breaker for same-timestamp rows. */
    List<AuditLogEntity> findByEntityNameAndEntityIdOrderByCreatedAtDescIdDesc(
            String entityName, String entityId, Pageable pageable);

    @Query("SELECT a FROM AuditLogEntity a ORDER BY a.createdAt DESC, a.id DESC")
    List<AuditLogEntity> findRecentAuditLogs(Pageable pageable);
}
