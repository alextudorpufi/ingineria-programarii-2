package ro.marketplace.app.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ro.marketplace.app.dto.AuthDtos.UserProfileDto;
import ro.marketplace.app.dto.VerifyProfessionalRequest;
import ro.marketplace.app.mapper.UserDtoMapper;
import ro.marketplace.app.security.AuthenticatedUser;
import ro.marketplace.application.service.AuthService;
import ro.marketplace.domain.model.User;

import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AuthService authService;

    public AdminController(AuthService authService) {
        this.authService = Objects.requireNonNull(authService, "authService must not be null");
    }

    @PostMapping("/verify/{userId}")
    public ResponseEntity<UserProfileDto> verifyProfessional(@PathVariable UUID userId,
                                                             @Valid @RequestBody VerifyProfessionalRequest request,
                                                             @AuthenticationPrincipal Jwt jwt) {
        AuthenticatedUser admin = AuthenticatedUser.from(jwt);
        User verified = authService.verifyProfessionalAndGrantCredits(userId, request.credits(), admin.id());
        return ResponseEntity.ok(UserDtoMapper.toProfile(verified));
    }
}
