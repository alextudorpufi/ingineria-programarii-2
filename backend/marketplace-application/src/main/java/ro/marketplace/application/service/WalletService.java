package ro.marketplace.application.service;

import ro.marketplace.domain.model.WalletTransaction;

import java.util.List;
import java.util.UUID;

public interface WalletService {

    WalletTransaction topUpCredits(UUID userId, int credits, String description);

    WalletTransaction deductCredits(UUID userId, int credits, String description, UUID leadId);

    WalletTransaction refundCredits(UUID userId, int credits, String description, UUID leadId);

    /**
     * @param actor the administrator or system component that approved the refund;
     *              recorded in the audit trail instead of a hard-coded "admin"
     */
    WalletTransaction refundCredits(UUID userId, int credits, String description, UUID leadId, String actor);

    List<WalletTransaction> getUserTransactions(UUID userId);

    int getBalance(UUID userId);
}
