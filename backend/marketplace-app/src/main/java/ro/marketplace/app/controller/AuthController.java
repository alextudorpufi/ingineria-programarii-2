package ro.marketplace.app.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ro.marketplace.app.dto.AuthDtos.AuthResponse;
import ro.marketplace.app.dto.AuthDtos.LoginRequest;
import ro.marketplace.app.dto.AuthDtos.RegisterRequest;
import ro.marketplace.app.dto.AuthDtos.UserProfileDto;
import ro.marketplace.app.mapper.UserDtoMapper;
import ro.marketplace.app.security.AuthenticatedUser;
import ro.marketplace.app.security.JwtTokenProvider;
import ro.marketplace.app.security.RevokedTokenStore;
import ro.marketplace.application.service.AuthService;
import ro.marketplace.domain.model.User;

import java.util.Objects;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RevokedTokenStore revokedTokenStore;

    public AuthController(AuthService authService,
                          JwtTokenProvider jwtTokenProvider,
                          RevokedTokenStore revokedTokenStore) {
        this.authService = Objects.requireNonNull(authService, "authService must not be null");
        this.jwtTokenProvider = Objects.requireNonNull(jwtTokenProvider, "jwtTokenProvider must not be null");
        this.revokedTokenStore = Objects.requireNonNull(revokedTokenStore, "revokedTokenStore must not be null");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        User user = authService.authenticate(request.email(), request.password())
                .orElseThrow(() -> new BadCredentialsException("Email sau parolă incorectă."));

        return ResponseEntity.ok(authResponse(user));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        User saved = authService.register(
                request.email(),
                request.password(),
                request.name(),
                request.role(),
                request.phone(),
                request.companyName()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(authResponse(saved));
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileDto> getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        User user = currentUser(jwt);
        return ResponseEntity.ok(UserDtoMapper.toProfile(user));
    }

    /**
     * Rotates the current access token: the presented token is revoked and a new
     * one is issued for the same user. Replaying the old token immediately fails
     * at the JWT decoder because its {@code jti} is on the denylist.
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            throw new BadCredentialsException("A valid bearer token is required to refresh the session.");
        }
        User user = currentUser(jwt);
        revokedTokenStore.revoke(jwt.getId(), jwt.getExpiresAt());
        return ResponseEntity.ok(authResponse(user));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal Jwt jwt) {
        if (jwt != null) {
            revokedTokenStore.revoke(jwt.getId(), jwt.getExpiresAt());
        }
        return ResponseEntity.noContent().build();
    }

    private User currentUser(Jwt jwt) {
        AuthenticatedUser authenticatedUser = AuthenticatedUser.from(jwt);
        return authService.findById(authenticatedUser.id())
                .orElseThrow(() -> new BadCredentialsException("Utilizatorul nu a fost găsit în baza de date."));
    }

    private AuthResponse authResponse(User user) {
        return new AuthResponse(
                jwtTokenProvider.generateToken(user),
                "Bearer",
                jwtTokenProvider.getExpiresInSeconds(),
                UserDtoMapper.toProfile(user)
        );
    }
}
