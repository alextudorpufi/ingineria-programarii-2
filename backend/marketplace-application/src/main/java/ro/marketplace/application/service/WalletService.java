package ro.marketplace.application.service;

import ro.marketplace.domain.model.WalletTransaction;

import java.util.List;
import java.util.UUID;

public interface WalletService {

    WalletTransaction topUpCredits(UUID userId, int credits, String description);

    WalletTransaction deductCredits(UUID userId, int credits, String description, UUID leadId);

    WalletTransaction refundCredits(UUID userId, int credits, String description, UUID leadId);

    List<WalletTransaction> getUserTransactions(UUID userId);

    int getBalance(UUID userId);
}
