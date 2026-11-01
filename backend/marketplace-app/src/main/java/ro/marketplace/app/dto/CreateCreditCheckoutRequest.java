package ro.marketplace.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCreditCheckoutRequest(
        @NotBlank(message = "Credit bundle is required")
        @Size(max = 50, message = "Credit bundle identifier is too long")
        String bundleId
) {
}
