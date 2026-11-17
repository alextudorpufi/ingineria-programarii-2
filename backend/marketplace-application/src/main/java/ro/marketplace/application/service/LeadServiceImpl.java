package ro.marketplace.application.service;

import ro.marketplace.application.audit.AuditDetails;
import ro.marketplace.domain.exception.ConflictException;
import ro.marketplace.domain.exception.ResourceNotFoundException;
import ro.marketplace.domain.exception.ValidationException;
import ro.marketplace.domain.model.AuditAction;
import ro.marketplace.domain.model.Lead;
import ro.marketplace.domain.model.LeadClaim;
import ro.marketplace.domain.model.LeadStatus;
import ro.marketplace.domain.model.User;
import ro.marketplace.domain.model.UserRole;
import ro.marketplace.domain.port.LeadRepository;
import ro.marketplace.domain.port.ServiceCategoryRepository;
import ro.marketplace.domain.port.UnitOfWork;
import ro.marketplace.domain.port.UserRepository;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class LeadServiceImpl implements LeadService {

    private final LeadRepository leadRepository;
    private final ServiceCategoryRepository serviceCategoryRepository;
    private final UserRepository userRepository;
    private final WalletService walletService;
    private final AuditService auditService;
    private final UnitOfWork unitOfWork;
    private final Clock clock;

    public LeadServiceImpl(LeadRepository leadRepository,
                           ServiceCategoryRepository serviceCategoryRepository,
                           UserRepository userRepository,
                           WalletService walletService,
                           AuditService auditService,
                           UnitOfWork unitOfWork,
                           Clock clock) {
        this.leadRepository = Objects.requireNonNull(leadRepository, "leadRepository must not be null");
        this.serviceCategoryRepository = Objects.requireNonNull(serviceCategoryRepository, "serviceCategoryRepository must not be null");
        this.userRepository = Objects.requireNonNull(userRepository, "userRepository must not be null");
        this.walletService = Objects.requireNonNull(walletService, "walletService must not be null");
        this.auditService = Objects.requireNonNull(auditService, "auditService must not be null");
        this.unitOfWork = Objects.requireNonNull(unitOfWork, "unitOfWork must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    @Override
    public Lead createLead(Lead lead) {
        Objects.requireNonNull(lead, "lead must not be null");
        return unitOfWork.executeInTransaction(() -> {
            lead.validateForCreation();
            if (lead.getClientId() == null) {
                throw new ValidationException("clientId is required");
            }
            int categoryPrice = lead.getCategoryId() == null
                    ? -1
                    : serviceCategoryRepository.findAvgLeadPriceById(lead.getCategoryId()).orElse(-1);
            if (categoryPrice < 0) {
                throw new ValidationException("Unknown service category: " + lead.getCategoryId());
            }
            lead.normalizeRequirements();
            if (lead.getId() == null) {
                lead.setId(UUID.randomUUID());
            }
            if (lead.getCreatedAt() == null) {
                lead.setCreatedAt(LocalDateTime.now(clock));
            }
            // New leads always start in the platform's initial state; callers cannot
            // skip the workflow by posting a different status or price.
            lead.applyPlatformPolicy(categoryPrice);

            // The contact snapshot must belong to the authenticated account. A client
            // can no longer publish somebody else's name, email, or phone number.
            User client = userRepository.findById(lead.getClientId())
                    .orElseThrow(() -> new ValidationException("Unknown client account."));
            if (client.getRole() != UserRole.CLIENT && client.getRole() != UserRole.ADMIN) {
                throw new ValidationException("Only client accounts can publish leads.");
            }
            lead.snapshotClient(client);

            Lead saved = leadRepository.create(lead);
            auditService.recordEvent("Lead", saved.getId().toString(), AuditAction.CREATE, saved.getClientEmail(),
                    AuditDetails.of(
                            "categoryId", saved.getCategoryId(),
                            "clientId", saved.getClientId(),
                            "leadPrice", saved.getLeadPrice(),
                            "maxPros", saved.getMaxPros()));
            return saved;
        });
    }

    @Override
    public Lead getLeadById(UUID id) {
        Objects.requireNonNull(id, "id must not be null");
        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lead not found with id: " + id));
        lead.setClaimedPros(leadRepository.findClaimsByLeadId(id));
        return lead;
    }

    @Override
    public LeadPage getLeadsPage(String categoryId, LeadStatus status, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        List<Lead> leads;
        long totalElements;

        if (categoryId != null && !categoryId.isBlank()) {
            leads = leadRepository.findPageByCategoryId(categoryId, safePage, safeSize);
            totalElements = leadRepository.countByCategoryId(categoryId);
        } else if (status != null) {
            leads = leadRepository.findPageByStatus(status, safePage, safeSize);
            totalElements = leadRepository.countByStatus(status);
        } else {
            leads = leadRepository.findPage(safePage, safeSize);
            totalElements = leadRepository.countAll();
        }

        attachClaims(leads);
        return new LeadPage(leads, totalElements, safePage, safeSize);
    }

    @Override
    public LeadClaim claimLead(UUID leadId, UUID proId, String proName) {
        Objects.requireNonNull(leadId, "leadId must not be null");
        Objects.requireNonNull(proId, "proId must not be null");

        return unitOfWork.executeInTransaction(() -> {
            Lead lead = leadRepository.findById(leadId)
                    .orElseThrow(() -> new ResourceNotFoundException("Lead not found with id: " + leadId));

            User pro = userRepository.findById(proId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + proId));
            if (!pro.isProVerified()) {
                throw new ConflictException(
                        "The professional account has not been verified by an administrator.");
            }
            if (lead.getClientId() != null && lead.getClientId().equals(proId)) {
                throw new ConflictException("A professional cannot claim their own lead.");
            }
            if (!lead.isOpenForClaims()) {
                throw new ConflictException("Lead " + leadId + " is no longer open for claims (status="
                        + (lead.getStatus() != null ? lead.getStatus().getDbValue() : "unknown") + ")");
            }

            // Idempotency guard. The UNIQUE (lead_id, pro_id) constraint remains the hard backstop.
            if (leadRepository.existsClaimByLeadIdAndProId(leadId, proId)) {
                throw new ConflictException("Professional " + proId + " has already claimed lead " + leadId);
            }

            // Atomic capacity reservation: exactly one of N concurrent claimants wins each slot.
            if (!leadRepository.reserveSlot(leadId)) {
                throw new ConflictException("Lead has reached its maximum capacity of "
                        + lead.getMaxPros() + " professionals");
            }

            walletService.deductCredits(proId, lead.getLeadPrice(), "Achiziție lead #" + leadId, leadId);

            LeadClaim claim = LeadClaim.builder()
                    .id(UUID.randomUUID())
                    .leadId(leadId)
                    .proId(proId)
                    .proName(proName != null && !proName.isBlank() ? proName : "Professional")
                    .claimedAt(LocalDateTime.now(clock))
                    .build();

            LeadClaim savedClaim = leadRepository.createClaim(claim);
            leadRepository.markAssignedIfFull(leadId);

            auditService.recordEvent("Lead", leadId.toString(), AuditAction.CLAIM_LEAD, claim.getProName(),
                    AuditDetails.of(
                            "leadId", leadId,
                            "proId", proId,
                            "leadPrice", lead.getLeadPrice()));
            return savedClaim;
        });
    }

    @Override
    public Lead closeLead(UUID leadId, String actor) {
        Objects.requireNonNull(leadId, "leadId must not be null");
        return unitOfWork.executeInTransaction(() -> {
            Lead lead = leadRepository.findById(leadId)
                    .orElseThrow(() -> new ResourceNotFoundException("Lead not found with id: " + leadId));
            LeadStatus previous = lead.getStatus();
            lead.close();
            if (!leadRepository.updateStatus(leadId, previous, lead.getStatus())) {
                throw new ConflictException("Lead status changed concurrently; please retry.");
            }
            Lead updated = leadRepository.findById(leadId).orElse(lead);
            auditService.recordEvent("Lead", leadId.toString(), AuditAction.STATUS_CHANGE,
                    actor != null && !actor.isBlank() ? actor : "admin",
                    AuditDetails.of("previousStatus", previous, "status", updated.getStatus()));
            return updated;
        });
    }

    private void attachClaims(List<Lead> leads) {
        if (leads.isEmpty()) {
            return;
        }
        List<UUID> leadIds = leads.stream().map(Lead::getId).toList();
        Map<UUID, List<LeadClaim>> claimsByLead = leadRepository.findClaimsByLeadIds(leadIds).stream()
                .collect(Collectors.groupingBy(LeadClaim::getLeadId));
        leads.forEach(lead -> lead.setClaimedPros(claimsByLead.getOrDefault(lead.getId(), List.of())));
    }
}
