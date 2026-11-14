package ro.marketplace.domain.port;

import ro.marketplace.domain.model.Dispute;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DisputeRepository {

    Dispute save(Dispute dispute);

    Optional<Dispute> findById(UUID id);

    List<Dispute> findAll();

    List<Dispute> findByProId(UUID proId);

    List<Dispute> findByStatus(String status);

    /**
     * @return {@code true} when the professional already has an unresolved dispute
     *         for the same lead
     */
    boolean existsOpenDispute(UUID leadId, UUID proId);
}
