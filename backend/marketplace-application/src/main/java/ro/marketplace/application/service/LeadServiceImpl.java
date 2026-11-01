package ro.marketplace.application.service;

import ro.marketplace.application.audit.AuditDetails;
import ro.marketplace.domain.exception.ConflictException;
import ro.marketplace.domain.exception.ResourceNotFoundException;
import ro.marketplace.domain.exception.ValidationException;
import ro.marketplace.domain.model.AuditAction;
import ro.marketplace.domain.model.Lead;
import ro.marketplace.domain.model.LeadClaim;
import ro.marketplace.domain.model.LeadStatus;
import ro.marketplace.domain.port.LeadRepository;
import ro.marketplace.domain.port.ServiceCategoryRepository;
import ro.marketplace.domain.port.UnitOfWork;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class LeadServiceImpl implements LeadService {

    private static final int DEFAULT_MAX_PROS = 4;
    private static final int MAX_MAX_PROS = 100;
    private static final int DEFAULT_LEAD_PRICE = 30;
    private static final int MAX_LEAD_PRICE = 10_000;

    private final LeadRepository leadRepository;
    private final ServiceCategoryRepository serviceCategoryRepository;
    private final WalletService walletService;
    private final AuditService auditService;
    private final UnitOfWork unitOfWork;
    private final Clock clock;

    public LeadServiceImpl(LeadRepository leadRepository,
                           ServiceCategoryRepository serviceCategoryRepository,
                           WalletService walletService,
                           AuditService auditService,
                           UnitOfWork unitOfWork,
                           Clock clock) {
        this.leadRepository = Objects.requireNonNull(leadRepository, "leadRepository must not be null");
        this.serviceCategoryRepository = Objects.requireNonNull(serviceCategoryRepository, "serviceCategoryRepository must not be null");
        this.walletService = Objects.requireNonNull(walletService, "walletService must not be null");
        this.auditService = Objects.requireNonNull(auditService, "auditService must not be null");
        this.unitOfWork = Objects.requireNonNull(unitOfWork, "unitOfWork must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    @Override
    public Lead createLead(Lead lead) {
        Objects.requireNonNull(lead, "lead must not be null");
        return unitOfWork.executeInTransaction(() -> {
            validateBudget(lead);
            if (lead.getClientId() == null) {
                throw new ValidationException("clientId is required");
            }
            if (lead.getCategoryId() == null || !serviceCategoryRepository.existsById(lead.getCategoryId())) {
                throw new ValidationException("Unknown service category: " + lead.getCategoryId());
            }
            if (lead.getId() == null) {
                lead.setId(UUID.randomUUID());
            }
            if (lead.getCreatedAt() == null) {
                lead.setCreatedAt(LocalDateTime.now(clock));
            }
            if (lead.getStatus() == null) {
                lead.setStatus(LeadStatus.VALIDAT);
            }
            if (lead.getMaxPros() <= 0) {
                lead.setMaxPros(DEFAULT_MAX_PROS);
            }
            if (lead.getMaxPros() > MAX_MAX_PROS) {
                throw new ValidationException("maxPros must not exceed " + MAX_MAX_PROS);
            }
            if (lead.getLeadPrice() <= 0) {
                lead.setLeadPrice(DEFAULT_LEAD_PRICE);
            }
            if (lead.getLeadPrice() > MAX_LEAD_PRICE) {
                throw new ValidationException("leadPrice must not exceed " + MAX_LEAD_PRICE);
            }

            Lead saved = leadRepository.save(lead);
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
    public List<Lead> getAllLeads() {
        List<Lead> leads = leadRepository.findAll();
        attachClaims(leads);
        return leads;
    }

    @Override
    public List<Lead> getLeadsByCategory(String categoryId) {
        Objects.requireNonNull(categoryId, "categoryId must not be null");
        List<Lead> leads = leadRepository.findByCategoryId(categoryId);
        attachClaims(leads);
        return leads;
    }

    @Override
    public List<Lead> getLeadsByStatus(LeadStatus status) {
        Objects.requireNonNull(status, "status must not be null");
        List<Lead> leads = leadRepository.findByStatus(status);
        attachClaims(leads);
        return leads;
    }

    @Override
    public LeadClaim claimLead(UUID leadId, UUID proId, String proName) {
        Objects.requireNonNull(leadId, "leadId must not be null");
        Objects.requireNonNull(proId, "proId must not be null");

        return unitOfWork.executeInTransaction(() -> {
            Lead lead = leadRepository.findById(leadId)
                    .orElseThrow(() -> new ResourceNotFoundException("Lead not found with id: " + leadId));

            // Idempotency guard. The UNIQUE (lead_id, pro_id) constraint remains the hard backstop.
            if (leadRepository.existsClaimByLeadIdAndProId(leadId, proId)) {
                throw new ConflictException("Professional " + proId + " has already claimed lead " + leadId);
            }

            // Atomic capacity reservation: exactly one of N concurrent claimants wins each slot.
            if (!leadRepository.reserveSlot(leadId)) {
                throw new ConflictException("Lead has reached its maximum capacity of "
                        + lead.getMaxPros() + " professionals");
            }

            try {
                walletService.deductCredits(proId, lead.getLeadPrice(), "Achiziție lead #" + leadId, leadId);

                LeadClaim claim = LeadClaim.builder()
                        .id(UUID.randomUUID())
                        .leadId(leadId)
                        .proId(proId)
                        .proName(proName != null && !proName.isBlank() ? proName : "Professional")
                        .claimedAt(LocalDateTime.now(clock))
                        .build();

                LeadClaim savedClaim = leadRepository.saveClaim(claim);
                leadRepository.markAssignedIfFull(leadId);

                auditService.recordEvent("Lead", leadId.toString(), AuditAction.CLAIM_LEAD, claim.getProName(),
                        AuditDetails.of(
                                "leadId", leadId,
                                "proId", proId,
                                "leadPrice", lead.getLeadPrice()));
                return savedClaim;
            } catch (RuntimeException ex) {
                // Compensation inside the same unit of work: the reservation is rolled back with it.
                leadRepository.releaseSlot(leadId);
                throw ex;
            }
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

    private void validateBudget(Lead lead) {
        if (lead.getBudgetMin() != null && lead.getBudgetMax() != null
                && lead.getBudgetMin().compareTo(lead.getBudgetMax()) > 0) {
            throw new ValidationException("budgetMin must not exceed budgetMax");
        }
    }
}
