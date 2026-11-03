package ro.marketplace.app.security;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Server-derived identity. Controllers must never trust client-asserted user IDs or roles.
 */
public record AuthenticatedUser(UUID id, String email, String displayName, Set<String> roles) {

    public AuthenticatedUser {
        Objects.requireNonNull(id, "id must not be null");
        roles = roles == null ? Set.of() : Set.copyOf(roles);
    }

    /**
     * Builds a {@link AuthenticatedUser} for endpoints that may be reached anonymously.
     * Returns {@code null} when no valid JWT is present so callers can apply
     * "no contact visibility" masking to public reads.
     */
    public static AuthenticatedUser optional(Jwt jwt) {
        return jwt == null ? null : from(jwt);
    }

    public static AuthenticatedUser from(Jwt jwt) {
        if (jwt == null) {
            throw new BadCredentialsException("Authentication is required");
        }
        UUID userId;
        try {
            userId = UUID.fromString(jwt.getSubject());
        } catch (RuntimeException ex) {
            throw new BadCredentialsException("JWT subject must be a user UUID");
        }

        String email = jwt.getClaimAsString("email");
        String displayName = jwt.getClaimAsString("name");
        if (displayName == null || displayName.isBlank()) {
            displayName = email != null && !email.isBlank() ? email : userId.toString();
        }

        Set<String> roles = new LinkedHashSet<>();
        List<String> roleList = jwt.getClaimAsStringList("roles");
        if (roleList != null) {
            roleList.stream()
                    .filter(Objects::nonNull)
                    .map(role -> role.trim().toUpperCase(Locale.ROOT))
                    .filter(role -> !role.isEmpty())
                    .forEach(roles::add);
        }
        String singleRole = jwt.getClaimAsString("role");
        if (singleRole != null && !singleRole.isBlank()) {
            roles.add(singleRole.trim().toUpperCase(Locale.ROOT));
        }

        return new AuthenticatedUser(userId, email, displayName, roles);
    }

    public boolean isAdmin() {
        return roles.contains("ADMIN");
    }

    public boolean isProfessional() {
        return roles.contains("PRO") || roles.contains("PROFESSIONAL");
    }
}
