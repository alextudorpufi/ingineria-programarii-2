package ro.marketplace.app.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ro.marketplace.app.dto.DisputeResponse;
import ro.marketplace.app.dto.RaiseDisputeRequest;
import ro.marketplace.app.dto.ResolveDisputeRequest;
import ro.marketplace.app.mapper.DisputeDtoMapper;
import ro.marketplace.app.security.AuthenticatedUser;
import ro.marketplace.application.service.DisputeService;
import ro.marketplace.domain.model.Dispute;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/api/disputes")
public class DisputeController {

    private final DisputeService disputeService;

    public DisputeController(DisputeService disputeService) {
        this.disputeService = Objects.requireNonNull(disputeService, "disputeService must not be null");
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','PRO')")
    public ResponseEntity<List<DisputeResponse>> getAllDisputes(
            @RequestParam(name = "proId", required = false) UUID proId,
            @AuthenticationPrincipal Jwt jwt) {
        AuthenticatedUser user = AuthenticatedUser.from(jwt);
        // Administrators may inspect the whole queue or filter by professional;
        // professionals are always scoped to their own disputes, regardless of the query.
        UUID effectiveProId = user.isAdmin() ? proId : user.id();
        List<Dispute> disputes = (effectiveProId != null)
                ? disputeService.getDisputesByProId(effectiveProId)
                : disputeService.getAllDisputes();
        List<DisputeResponse> responses = disputes.stream()
                .map(DisputeDtoMapper::toResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DisputeResponse> getDisputeById(@PathVariable UUID id,
                                                         @AuthenticationPrincipal Jwt jwt) {
        AuthenticatedUser user = AuthenticatedUser.from(jwt);
        Dispute dispute = disputeService.getDisputeById(id);
        if (!user.isAdmin() && !user.id().equals(dispute.getProId())) {
            throw new AccessDeniedException("Dispute does not belong to the authenticated professional");
        }
        return ResponseEntity.ok(DisputeDtoMapper.toResponse(dispute));
    }

    @PostMapping
    @PreAuthorize("hasRole('PRO')")
    public ResponseEntity<DisputeResponse> raiseDispute(@Valid @RequestBody RaiseDisputeRequest request,
                                                        @AuthenticationPrincipal Jwt jwt) {
        AuthenticatedUser user = AuthenticatedUser.from(jwt);
        Dispute dispute = disputeService.raiseDispute(
                request.leadId(),
                request.leadTitle(),
                user.id(),
                user.displayName(),
                request.reason(),
                request.details(),
                request.creditsRequested()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(DisputeDtoMapper.toResponse(dispute));
    }

    @PostMapping("/{id}/resolve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DisputeResponse> resolveDispute(
            @PathVariable UUID id,
            @Valid @RequestBody ResolveDisputeRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        AuthenticatedUser user = AuthenticatedUser.from(jwt);
        String adminActor = user.email() != null && !user.email().isBlank()
                ? user.email() : user.id().toString();
        Dispute dispute = disputeService.resolveDispute(
                id,
                request.approved(),
                request.adminDecisionNote(),
                adminActor
        );
        return ResponseEntity.ok(DisputeDtoMapper.toResponse(dispute));
    }
}
