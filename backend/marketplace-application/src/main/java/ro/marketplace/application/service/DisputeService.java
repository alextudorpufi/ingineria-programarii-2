package ro.marketplace.application.service;

import ro.marketplace.domain.model.Dispute;
import ro.marketplace.domain.model.PageResult;

import java.util.UUID;

public interface DisputeService {

    Dispute raiseDispute(UUID leadId, UUID proId, String proName, String reason, String details, int creditsRequested);

    Dispute resolveDispute(UUID disputeId, boolean approved, String adminDecisionNote, String adminUsername);

    Dispute getDisputeById(UUID disputeId);

    /**
     * @param proId when present, only that professional's disputes are returned
     */
    PageResult<Dispute> getDisputes(UUID proId, int page, int size);
}
