package ro.marketplace.app.security;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ro.marketplace.domain.model.User;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Issues HMAC-SHA256 access tokens. The signing key is the same shared secret
 * used by {@code SecurityConfig#jwtDecoder}, so the resource server can verify
 * tokens locally without a round-trip.
 */
@Component
public class JwtTokenProvider {

    private static final long VALIDITY_IN_MILLISECONDS = 86_400_000L; // 24 hours

    private final byte[] secretKeyBytes;

    public JwtTokenProvider(@Value("${app.security.jwt.secret}") String secret) {
        byte[] keyBytes = secret == null ? new byte[0] : secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalArgumentException("JWT secret must be at least 32 bytes (256 bits)");
        }
        this.secretKeyBytes = keyBytes;
    }

    public String generateToken(User user) {
        Objects.requireNonNull(user, "user must not be null");
        try {
            JWSSigner signer = new MACSigner(secretKeyBytes);
            Date now = new Date();
            Date validity = new Date(now.getTime() + VALIDITY_IN_MILLISECONDS);

            String roleUpper = user.getRole() == null
                    ? "CLIENT"
                    : user.getRole().trim().toUpperCase(Locale.ROOT);

            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .subject(user.getId().toString())
                    .claim("email", user.getEmail())
                    .claim("name", user.getName())
                    .claim("role", roleUpper)
                    .claim("roles", List.of(roleUpper))
                    .issueTime(now)
                    .expirationTime(validity)
                    .build();

            SignedJWT signedJwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);
            signedJwt.sign(signer);
            return signedJwt.serialize();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to generate JWT token", ex);
        }
    }

    public long getExpiresInSeconds() {
        return VALIDITY_IN_MILLISECONDS / 1000;
    }
}
