package ro.marketplace.domain.port;

import ro.marketplace.domain.model.WalletTransaction;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WalletTransactionRepository {

    WalletTransaction save(WalletTransaction transaction);

    Optional<WalletTransaction> findById(UUID id);

    List<WalletTransaction> findByUserId(UUID userId);

    /**
     * Ledger entries for one professional and one lead, oldest first. Used to
     * cap refunds at what was actually paid and not already returned.
     */
    List<WalletTransaction> findByUserIdAndLeadId(UUID userId, UUID leadId);
}
