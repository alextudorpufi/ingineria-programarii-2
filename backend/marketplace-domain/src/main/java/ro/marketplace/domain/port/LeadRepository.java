package ro.marketplace.domain.port;

import ro.marketplace.domain.model.Lead;
import ro.marketplace.domain.model.LeadClaim;
import ro.marketplace.domain.model.LeadStatus;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LeadRepository {

    Lead save(Lead lead);

    Optional<Lead> findById(UUID id);

    List<Lead> findAll();

    List<Lead> findByStatus(LeadStatus status);

    List<Lead> findByCategoryId(String categoryId);

    void deleteById(UUID id);

    LeadClaim saveClaim(LeadClaim claim);

    List<LeadClaim> findClaimsByLeadId(UUID leadId);

    /**
     * Batch variant used by list use cases to avoid an N+1 query per lead.
     */
    List<LeadClaim> findClaimsByLeadIds(Collection<UUID> leadIds);

    boolean existsClaimByLeadIdAndProId(UUID leadId, UUID proId);

    /**
     * Atomically reserves one professional slot using a conditional UPDATE.
     *
     * @return {@code true} when a slot was reserved, {@code false} when the lead is full
     */
    boolean reserveSlot(UUID leadId);

    /**
     * Releases a previously reserved slot (compensation for a failed claim).
     */
    void releaseSlot(UUID leadId);

    /**
     * Marks the lead as assigned when the authoritative counter is full.
     */
    void markAssignedIfFull(UUID leadId);
}
