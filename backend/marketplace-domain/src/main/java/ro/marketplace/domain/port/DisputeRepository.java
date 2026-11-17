package ro.marketplace.domain.port;

import ro.marketplace.domain.model.Dispute;
import ro.marketplace.domain.model.PageResult;

import java.util.Optional;
import java.util.UUID;

public interface DisputeRepository {

    Dispute save(Dispute dispute);

    Optional<Dispute> findById(UUID id);

    PageResult<Dispute> findPage(int page, int size);

    PageResult<Dispute> findPageByProId(UUID proId, int page, int size);

    /**
     * @return {@code true} when the professional already has an unresolved dispute
     *         for the same lead
     */
    boolean existsOpenDispute(UUID leadId, UUID proId);

    /**
     * @return {@code true} when any professional still has an unresolved dispute
     *         for the lead, so the lead must stay in the disputed state
     */
    boolean existsOpenDisputeForLead(UUID leadId);
}
