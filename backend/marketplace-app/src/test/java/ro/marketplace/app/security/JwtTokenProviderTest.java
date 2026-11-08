package ro.marketplace.app.security;

import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ro.marketplace.domain.model.User;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private static final String SECRET = "unit-test-secret-key-with-at-least-32-bytes";

    @Test
    @DisplayName("issued tokens carry iss, aud, jti, and token_version claims")
    void tokenContainsHardeningClaims() throws Exception {
        JwtTokenProvider provider = new JwtTokenProvider(SECRET, "marketplace-backend",
                "marketplace-api", 1);
        User user = User.builder()
                .id(UUID.randomUUID())
                .email("pro@example.com")
                .name("Pro User")
                .role("pro")
                .build();

        SignedJWT token = SignedJWT.parse(provider.generateToken(user));

        assertThat(token.getJWTClaimsSet().getIssuer()).isEqualTo("marketplace-backend");
        assertThat(token.getJWTClaimsSet().getAudience()).containsExactly("marketplace-api");
        assertThat(token.getJWTClaimsSet().getJWTID()).isNotBlank();
        assertThat(token.getJWTClaimsSet().getLongClaim("token_version")).isEqualTo(1L);
        assertThat(token.getJWTClaimsSet().getSubject()).isEqualTo(user.getId().toString());
        assertThat(token.getJWTClaimsSet().getStringClaim("role")).isEqualTo("PRO");
    }

    @Test
    @DisplayName("each issuance gets a fresh jti so a single token can be revoked")
    void jtiIsUniquePerToken() throws Exception {
        JwtTokenProvider provider = new JwtTokenProvider(SECRET, "marketplace-backend",
                "marketplace-api", 1);
        User user = User.builder().id(UUID.randomUUID()).role("client").build();

        String first = SignedJWT.parse(provider.generateToken(user)).getJWTClaimsSet().getJWTID();
        String second = SignedJWT.parse(provider.generateToken(user)).getJWTClaimsSet().getJWTID();

        assertThat(first).isNotEqualTo(second);
    }

    @Test
    @DisplayName("a token without jti is treated as revoked and rejected")
    void tokensWithoutJtiAreRejected() {
        RevokedTokenStore store = new RevokedTokenStore();

        assertThat(store.isRevoked(null)).isTrue();
        assertThat(store.isRevoked("")).isTrue();
    }
}
