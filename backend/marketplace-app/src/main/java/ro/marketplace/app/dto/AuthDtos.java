package ro.marketplace.app.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record LoginRequest(
            @NotBlank(message = "Email is required")
            @Email(message = "Invalid email format")
            @Size(max = 255, message = "Email must not exceed 255 characters")
            String email,

            @NotBlank(message = "Password is required")
            @Size(max = 128, message = "Password must not exceed 128 characters")
            String password
    ) {
    }

    public record RegisterRequest(
            @NotBlank(message = "Email is required")
            @Email(message = "Invalid email format")
            @Size(max = 255, message = "Email must not exceed 255 characters")
            String email,

            @NotBlank(message = "Password is required")
            @Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters")
            @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).*$",
                    message = "Password must contain at least one letter and one digit")
            String password,

            @NotBlank(message = "Name is required")
            @Size(max = 255, message = "Name must not exceed 255 characters")
            String name,

            @NotBlank(message = "Role must be 'client' or 'pro'")
            @Size(max = 20, message = "Role must not exceed 20 characters")
            String role,

            @Size(max = 50, message = "Phone must not exceed 50 characters")
            String phone,
            @Size(max = 255, message = "Company name must not exceed 255 characters")
            String companyName
    ) {
    }

    public record UserProfileDto(
            UUID id,
            String email,
            String role,
            String name,
            String companyName,
            String title,
            String phone,
            String avatarUrl,
            String bio,
            boolean verifiedBadge,
            boolean proVerified,
            BigDecimal rating,
            int reviewCount,
            int creditsBalance,
            BigDecimal hourlyRate,
            BigDecimal clientBudget,
            String responseTime,
            int completedJobs,
            BigDecimal trustScore,
            String memberSince,
            List<String> coverageAreas,
            List<String> servicesOffered,
            List<String> categories
    ) {
    }

    public record AuthResponse(
            String token,
            String tokenType,
            long expiresInSeconds,
            UserProfileDto user
    ) {
    }
}
