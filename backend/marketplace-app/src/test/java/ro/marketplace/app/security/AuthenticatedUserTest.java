package ro.marketplace.app.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AuthenticatedUserTest {
    @Test
    void professionalChecksRecognizeProButNeverTheRemovedAlias() {
        assertThat(AuthenticatedUser.from(token("pro")).isProfessional()).isTrue();
        assertThat(AuthenticatedUser.from(token("PRO")).isProfessional()).isTrue();
        assertThat(AuthenticatedUser.from(token("PROFESSIONAL")).isProfessional()).isFalse();
        assertThat(AuthenticatedUser.from(token("CLIENT")).isProfessional()).isFalse();
    }

    private Jwt token(String role) {
        return Jwt.withTokenValue("test").header("alg", "HS256")
                .subject(UUID.randomUUID().toString()).issuedAt(Instant.now())
                .claim("role", role).claim("roles", List.of(role)).build();
    }
}
