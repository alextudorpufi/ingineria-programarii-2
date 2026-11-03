package ro.marketplace.application.service;

import ro.marketplace.domain.model.Dispute;

import java.util.List;
import java.util.UUID;

public interface DisputeService {

    Dispute raiseDispute(UUID leadId, String leadTitle, UUID proId, String proName, String reason, String details, int creditsRequested);

    Dispute resolveDispute(UUID disputeId, boolean approved, String adminDecisionNote, String adminUsername);

    Dispute getDisputeById(UUID disputeId);

    List<Dispute> getAllDisputes();

    List<Dispute> getDisputesByProId(UUID proId);
}
