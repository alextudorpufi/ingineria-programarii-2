package ro.marketplace.app.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record RaiseDisputeRequest(
        @NotNull(message = "leadId is required")
        UUID leadId,

        @Size(max = 255, message = "Lead title must not exceed 255 characters")
        String leadTitle,

        @NotBlank(message = "reason is required")
        @Size(max = 100, message = "Reason must not exceed 100 characters")
        String reason,

        @NotBlank(message = "details is required")
        String details,

        @Min(value = 1, message = "creditsRequested must be at least 1")
        @Max(value = 10_000, message = "creditsRequested must not exceed 10000")
        int creditsRequested
) {}
