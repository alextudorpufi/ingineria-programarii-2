package ro.marketplace.app.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;
import ro.marketplace.domain.model.Lead;

public record CreateLeadRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 255, message = "Title must not exceed 255 characters")
        String title,

        @NotBlank(message = "Category ID is required")
        @Size(max = 100, message = "Category ID must not exceed 100 characters")
        String categoryId,

        @NotBlank(message = "Subcategory is required")
        @Size(max = 255, message = "Subcategory must not exceed 255 characters")
        String subcategory,

        @NotBlank(message = "Description is required")
        String description,

        @NotBlank(message = "Location is required")
        @Size(max = 255, message = "Location must not exceed 255 characters")
        String location,

        @NotNull(message = "Minimum budget is required")
        @DecimalMin(value = "0.0", message = "Budget min must be positive")
        @Digits(integer = 12, fraction = 2, message = "Budget min has an invalid format")
        BigDecimal budgetMin,

        @NotNull(message = "Maximum budget is required")
        @DecimalMin(value = "0.0", message = "Budget max must be positive")
        @Digits(integer = 12, fraction = 2, message = "Budget max has an invalid format")
        BigDecimal budgetMax,

        @Size(max = 20, message = "Budget currency must not exceed 20 characters")
        String budgetCurrency,
        Boolean budgetIsNegotiable,
        Boolean budgetIsConfirmed,

        @NotBlank(message = "Timeline is required")
        @Size(max = 255, message = "Timeline must not exceed 255 characters")
        String timeline,

        @Positive(message = "Lead price must be positive")
        @Max(value = 10_000, message = "Lead price must not exceed 10000")
        Integer leadPrice,

        @Positive(message = "Max pros must be positive")
        @Max(value = 100, message = "Max pros must not exceed 100")
        Integer maxPros,

        @NotBlank(message = "Client name is required")
        @Size(max = 255, message = "Client name must not exceed 255 characters")
        String clientName,

        @NotBlank(message = "Client phone is required")
        @Size(max = 50, message = "Client phone must not exceed 50 characters")
        String clientPhone,

        @NotBlank(message = "Client email is required")
        @Email(message = "Client email must be valid")
        @Size(max = 255, message = "Client email must not exceed 255 characters")
        String clientEmail,

        @NotBlank(message = "Client city is required")
        @Size(max = 255, message = "Client city must not exceed 255 characters")
        String clientCity,

        @NotNull(message = "Selectează modul de lucru: la locație sau la distanță.")
        Lead.WorkMode workMode,

        @Size(max = 50) List<@NotBlank @Size(max = 255) String> requiredCapabilities,
        @Size(max = 50) List<@NotBlank @Size(max = 255) String> preferredCapabilities
) {

    @AssertTrue(message = "budgetMin must not exceed budgetMax")
    public boolean isBudgetConsistent() {
        return budgetMin == null || budgetMax == null || budgetMin.compareTo(budgetMax) <= 0;
    }
}
