package ro.marketplace.infrastructure.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ro.marketplace.infrastructure.persistence.entity.DisputeEntity;

import java.util.UUID;

@Repository
public interface SpringDataDisputeRepository extends JpaRepository<DisputeEntity, UUID> {

    Page<DisputeEntity> findByProIdOrderByCreatedAtDesc(UUID proId, Pageable pageable);

    boolean existsByLeadIdAndProIdAndStatus(UUID leadId, UUID proId, String status);

    boolean existsByLeadIdAndStatus(UUID leadId, String status);
}
