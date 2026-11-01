package ro.marketplace.app.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import ro.marketplace.app.dto.CreateLeadRequest;
import ro.marketplace.app.dto.LeadResponse;
import ro.marketplace.app.mapper.LeadDtoMapper;
import ro.marketplace.app.security.AuthenticatedUser;
import ro.marketplace.application.service.LeadService;
import ro.marketplace.domain.model.Lead;
import ro.marketplace.domain.model.LeadClaim;
import ro.marketplace.domain.model.LeadStatus;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/api/leads")
public class LeadController {

    private final LeadService leadService;

    public LeadController(LeadService leadService) {
        this.leadService = Objects.requireNonNull(leadService, "leadService must not be null");
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('CLIENT','ADMIN')")
    public ResponseEntity<LeadResponse> createLead(@Valid @RequestBody CreateLeadRequest request,
                                                   @AuthenticationPrincipal Jwt jwt) {
        AuthenticatedUser user = AuthenticatedUser.from(jwt);
        Lead toCreate = LeadDtoMapper.toDomain(request);
        toCreate.setClientId(user.id());
        Lead created = leadService.createLead(toCreate);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(LeadDtoMapper.toResponse(created, canViewContact(created, user)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LeadResponse> getLeadById(@PathVariable UUID id,
                                                    @AuthenticationPrincipal Jwt jwt) {
        AuthenticatedUser user = AuthenticatedUser.from(jwt);
        Lead lead = leadService.getLeadById(id);
        return ResponseEntity.ok(LeadDtoMapper.toResponse(lead, canViewContact(lead, user)));
    }

    @GetMapping
    public ResponseEntity<List<LeadResponse>> getAllLeads(
            @RequestParam(name = "category", required = false) String categoryId,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "limit", required = false, defaultValue = "100") int limit,
            @AuthenticationPrincipal Jwt jwt) {
        AuthenticatedUser user = AuthenticatedUser.from(jwt);
        List<Lead> leads;
        if (categoryId != null && !categoryId.isBlank()) {
            leads = leadService.getLeadsByCategory(categoryId);
        } else if (status != null && !status.isBlank()) {
            leads = leadService.getLeadsByStatus(LeadStatus.fromDbValue(status));
        } else {
            leads = leadService.getAllLeads();
        }

        int effectiveLimit = limit > 0 ? limit : 100;
        List<LeadResponse> responses = leads.stream()
                .limit(effectiveLimit)
                .map(lead -> LeadDtoMapper.toResponse(lead, canViewContact(lead, user)))
                .toList();
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/{id}/claim")
    @PreAuthorize("hasAnyRole('PRO','PROFESSIONAL','ADMIN')")
    public ResponseEntity<LeadResponse.ClaimedProDto> claimLead(@PathVariable UUID id,
                                                                @AuthenticationPrincipal Jwt jwt) {
        AuthenticatedUser user = AuthenticatedUser.from(jwt);
        LeadClaim claim = leadService.claimLead(id, user.id(), user.displayName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new LeadResponse.ClaimedProDto(claim.getProId(), claim.getProName(), claim.getClaimedAt()));
    }

    private boolean canViewContact(Lead lead, AuthenticatedUser user) {
        if (user.isAdmin() || user.id().equals(lead.getClientId())) {
            return true;
        }
        return lead.getClaimedPros() != null && lead.getClaimedPros().stream()
                .anyMatch(claim -> user.id().equals(claim.getProId()));
    }
}
