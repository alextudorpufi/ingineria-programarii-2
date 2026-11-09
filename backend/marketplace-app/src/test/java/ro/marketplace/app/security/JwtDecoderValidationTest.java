package ro.marketplace.app.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import ro.marketplace.domain.model.User;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Exercises the claim validation wired into {@link SecurityConfig#jwtDecoder}
 * without starting a Spring context.
 */
class JwtDecoderValidationTest {

    private static final String SECRET = "unit-test-secret-key-with-at-least-32-bytes";
    private static final String ISSUER = "marketplace-backend";
    private static final String AUDIENCE = "marketplace-api";
    private static final long TOKEN_VERSION = 1L;

    private final RevokedTokenStore revokedTokenStore = new RevokedTokenStore();

    @Test
    @DisplayName("a correctly issued token is accepted")
    void validTokenIsAccepted() {
        JwtDecoder decoder = decoder(ISSUER, AUDIENCE, TOKEN_VERSION);
        User user = user();
        String token = provider(ISSUER, AUDIENCE, TOKEN_VERSION).generateToken(user);

        Jwt jwt = decoder.decode(token);

        assertThat(jwt.getSubject()).isEqualTo(user.getId().toString());
        assertThat(jwt.getAudience()).containsExactly(AUDIENCE);
        assertThat(jwt.getId()).isNotBlank();
    }

    @Test
    @DisplayName("tokens with a foreign issuer are rejected")
    void wrongIssuerIsRejected() {
        JwtDecoder decoder = decoder(ISSUER, AUDIENCE, TOKEN_VERSION);
        String token = provider("attacker-issuer", AUDIENCE, TOKEN_VERSION).generateToken(user());

        assertThatThrownBy(() -> decoder.decode(token)).isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("tokens for another audience are rejected")
    void wrongAudienceIsRejected() {
        JwtDecoder decoder = decoder(ISSUER, AUDIENCE, TOKEN_VERSION);
        String token = provider(ISSUER, "some-other-api", TOKEN_VERSION).generateToken(user());

        assertThatThrownBy(() -> decoder.decode(token)).isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("tokens below the accepted token version are rejected")
    void outdatedTokenVersionIsRejected() {
        JwtDecoder decoder = decoder(ISSUER, AUDIENCE, TOKEN_VERSION + 1);
        String token = provider(ISSUER, AUDIENCE, TOKEN_VERSION).generateToken(user());

        assertThatThrownBy(() -> decoder.decode(token)).isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("a revoked token fails immediately, not at expiry")
    void revokedTokenIsRejected() {
        JwtDecoder decoder = decoder(ISSUER, AUDIENCE, TOKEN_VERSION);
        String token = provider(ISSUER, AUDIENCE, TOKEN_VERSION).generateToken(user());

        Jwt beforeRevocation = decoder.decode(token);
        revokedTokenStore.revoke(beforeRevocation.getId(), beforeRevocation.getExpiresAt());

        assertThatThrownBy(() -> decoder.decode(token)).isInstanceOf(JwtException.class);
    }

    private JwtDecoder decoder(String issuer, String audience, long minimumVersion) {
        return new SecurityConfig(new ObjectMapper())
                .jwtDecoder(SECRET, issuer, audience, minimumVersion, revokedTokenStore);
    }

    private static JwtTokenProvider provider(String issuer, String audience, long version) {
        return new JwtTokenProvider(SECRET, issuer, audience, version);
    }

    private static User user() {
        return User.builder()
                .id(UUID.randomUUID())
                .email("pro@example.com")
                .name("Pro User")
                .role("pro")
                .build();
    }
}
