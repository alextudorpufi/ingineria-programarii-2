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
import ro.marketplace.app.security.AuthenticatedUser;
import ro.marketplace.app.security.JwtTokenProvider;
import ro.marketplace.application.service.AuthService;
import ro.marketplace.domain.model.User;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthController(AuthService authService, JwtTokenProvider jwtTokenProvider) {
        this.authService = Objects.requireNonNull(authService, "authService must not be null");
        this.jwtTokenProvider = Objects.requireNonNull(jwtTokenProvider, "jwtTokenProvider must not be null");
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
        AuthenticatedUser authenticatedUser = AuthenticatedUser.from(jwt);
        User user = authService.findById(authenticatedUser.id())
                .orElseThrow(() -> new BadCredentialsException("Utilizatorul nu a fost găsit în baza de date."));

        return ResponseEntity.ok(toUserProfileDto(user));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        // Stateless JWTs: the client discards the token. Kept for an explicit API contract.
        return ResponseEntity.noContent().build();
    }

    private AuthResponse authResponse(User user) {
        return new AuthResponse(
                jwtTokenProvider.generateToken(user),
                "Bearer",
                jwtTokenProvider.getExpiresInSeconds(),
                toUserProfileDto(user)
        );
    }

    private static UserProfileDto toUserProfileDto(User user) {
        return new UserProfileDto(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.getName(),
                user.getCompanyName(),
                user.getTitle(),
                user.getPhone(),
                user.getAvatarUrl(),
                user.getBio(),
                user.isVerifiedBadge(),
                user.getRating(),
                user.getReviewCount(),
                user.getCreditsBalance(),
                user.getHourlyRate(),
                user.getClientBudget(),
                user.getResponseTime(),
                user.getCompletedJobs(),
                user.getTrustScore(),
                user.getMemberSince(),
                safeList(user.getCoverageAreas()),
                safeList(user.getServicesOffered()),
                safeList(user.getCategories())
        );
    }

    private static List<String> safeList(List<String> values) {
        return values == null ? List.of() : values;
    }
}
