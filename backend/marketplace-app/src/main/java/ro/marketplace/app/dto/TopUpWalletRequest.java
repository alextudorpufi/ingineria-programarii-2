package ro.marketplace.app.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record TopUpWalletRequest(
        @NotNull(message = "Credits amount is required")
        @Min(value = 1, message = "Credits amount must be at least 1")
        @Max(value = 10_000, message = "Credits amount must not exceed 10000")
        int credits,

        String description
) {}
