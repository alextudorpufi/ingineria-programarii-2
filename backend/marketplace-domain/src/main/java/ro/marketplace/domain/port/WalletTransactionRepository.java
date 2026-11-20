package ro.marketplace.domain.port;

import ro.marketplace.domain.model.PageResult;
import ro.marketplace.domain.model.WalletTransaction;

import java.util.List;
import java.util.UUID;

public interface WalletTransactionRepository {

    /**
     * Appends an immutable ledger entry. Reusing an existing id must fail instead
     * of rewriting history.
     */
    WalletTransaction append(WalletTransaction transaction);

    PageResult<WalletTransaction> findPageByUserId(UUID userId, int page, int size);

    /**
     * Ledger entries for one professional and one lead, oldest first. Used to
     * cap refunds at what was actually paid and not already returned.
     */
    List<WalletTransaction> findByUserIdAndLeadId(UUID userId, UUID leadId);
}
