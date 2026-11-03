package ro.marketplace.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ro.marketplace.infrastructure.persistence.entity.LeadClaimEntity;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataLeadClaimRepository extends JpaRepository<LeadClaimEntity, UUID> {

    List<LeadClaimEntity> findByLeadId(UUID leadId);

    List<LeadClaimEntity> findByLeadIdIn(Collection<UUID> leadIds);

    boolean existsByLeadIdAndProId(UUID leadId, UUID proId);
}
