package ro.marketplace.app.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ResolveDisputeRequest(
        @NotNull(message = "approved decision is required")
        Boolean approved,

        @Size(max = 4000, message = "Admin decision note must not exceed 4000 characters")
        String adminDecisionNote
) {}
