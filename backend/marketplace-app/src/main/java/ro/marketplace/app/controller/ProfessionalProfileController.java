package ro.marketplace.app.controller;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ro.marketplace.app.dto.ProfessionalProfileDto;
import ro.marketplace.app.security.AuthenticatedUser;
import ro.marketplace.application.service.ProfessionalProfileService;

import java.util.Objects;

@RestController
@RequestMapping("/api/professional-profile/me")
@PreAuthorize("hasRole('PRO')")
public class ProfessionalProfileController {
    private final ProfessionalProfileService profiles;

    public ProfessionalProfileController(ProfessionalProfileService profiles) {
        this.profiles = Objects.requireNonNull(profiles);
    }

    @GetMapping
    public ProfessionalProfileDto get(@AuthenticationPrincipal Jwt jwt) {
        return ProfessionalProfileDto.from(profiles.get(AuthenticatedUser.from(jwt).id()));
    }

    @PutMapping
    public ProfessionalProfileDto update(@AuthenticationPrincipal Jwt jwt,
                                         @Valid @RequestBody ProfessionalProfileDto request) {
        return ProfessionalProfileDto.from(profiles.update(AuthenticatedUser.from(jwt).id(), request.toUpdate()));
    }
}
