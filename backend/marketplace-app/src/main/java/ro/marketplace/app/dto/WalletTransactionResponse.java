package ro.marketplace.app.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record WalletTransactionResponse(
        UUID id,
        UUID userId,
        UUID leadId,
        String type,
        int amount,
        String description,
        int balanceAfter,
        LocalDateTime createdAt
) {}
