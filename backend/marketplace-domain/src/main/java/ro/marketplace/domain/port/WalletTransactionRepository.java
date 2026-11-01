package ro.marketplace.domain.port;

import ro.marketplace.domain.model.WalletTransaction;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WalletTransactionRepository {

    WalletTransaction save(WalletTransaction transaction);

    Optional<WalletTransaction> findById(UUID id);

    List<WalletTransaction> findByUserId(UUID userId);
}
