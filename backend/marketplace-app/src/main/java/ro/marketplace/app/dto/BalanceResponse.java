package ro.marketplace.app.dto;

import java.util.UUID;

public record BalanceResponse(
        UUID userId,
        int creditsBalance
) {}
