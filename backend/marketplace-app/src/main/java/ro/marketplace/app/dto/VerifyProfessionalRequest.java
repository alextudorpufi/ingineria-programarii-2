package ro.marketplace.app.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * Administrator approval payload for a professional account. The upper bound
 * matches the lifetime free-credit cap enforced by the auth service.
 */
public record VerifyProfessionalRequest(
        @Min(value = 1, message = "At least one credit must be granted")
        @Max(value = 30, message = "A single grant must not exceed 30 credits")
        int credits
) {
}
