package ro.marketplace.application.service;

import ro.marketplace.domain.model.PageResult;
import ro.marketplace.domain.model.WalletTransaction;

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

    /**
     * Grants platform bonus credits (for example the one-time welcome grant) and
     * records a {@code bonus} ledger entry so the balance is always reconcilable.
     *
     * @param actor the administrator or system component that authorised the grant
     */
    WalletTransaction grantBonusCredits(UUID userId, int credits, String description, String actor);

    PageResult<WalletTransaction> getUserTransactions(UUID userId, int page, int size);

    int getBalance(UUID userId);
}
