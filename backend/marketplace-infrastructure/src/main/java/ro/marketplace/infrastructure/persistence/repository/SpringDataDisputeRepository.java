package ro.marketplace.infrastructure.persistence.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ro.marketplace.infrastructure.persistence.entity.DisputeEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataDisputeRepository extends JpaRepository<DisputeEntity, UUID> {

    List<DisputeEntity> findByProIdOrderByCreatedAtDesc(UUID proId, Pageable pageable);

    List<DisputeEntity> findByStatus(String status, Pageable pageable);
}
