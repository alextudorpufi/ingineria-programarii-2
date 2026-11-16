package ro.marketplace.application.service;

import ro.marketplace.application.audit.AuditDetails;
import ro.marketplace.domain.exception.ConflictException;
import ro.marketplace.domain.exception.ResourceNotFoundException;
import ro.marketplace.domain.exception.ValidationException;
import ro.marketplace.domain.model.AuditAction;
import ro.marketplace.domain.model.Dispute;
import ro.marketplace.domain.model.DisputeStatus;
import ro.marketplace.domain.model.Lead;
import ro.marketplace.domain.model.LeadStatus;
import ro.marketplace.domain.model.User;
import ro.marketplace.domain.model.UserRole;
import ro.marketplace.domain.model.WalletTransaction;
import ro.marketplace.domain.model.WalletTransactionType;
import ro.marketplace.domain.port.DisputeRepository;
import ro.marketplace.domain.port.LeadRepository;
import ro.marketplace.domain.port.UnitOfWork;
import ro.marketplace.domain.port.UserRepository;
import ro.marketplace.domain.port.WalletTransactionRepository;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class DisputeServiceImpl implements DisputeService {

    private static final Set<String> ALLOWED_REASONS = Set.of(
            "unreachable_contact",
            "wrong_category",
            "incompatible_budget",
            "duplicate_or_fake");

    private final DisputeRepository disputeRepository;
    private final LeadRepository leadRepository;
    private final UserRepository userRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final WalletService walletService;
    private final AuditService auditService;
    private final UnitOfWork unitOfWork;
    private final Clock clock;

    public DisputeServiceImpl(DisputeRepository disputeRepository,
                              LeadRepository leadRepository,
                              UserRepository userRepository,
                              WalletTransactionRepository walletTransactionRepository,
                              WalletService walletService,
                              AuditService auditService,
                              UnitOfWork unitOfWork,
                              Clock clock) {
        this.disputeRepository = Objects.requireNonNull(disputeRepository, "disputeRepository must not be null");
        this.leadRepository = Objects.requireNonNull(leadRepository, "leadRepository must not be null");
        this.userRepository = Objects.requireNonNull(userRepository, "userRepository must not be null");
        this.walletTransactionRepository = Objects.requireNonNull(walletTransactionRepository,
                "walletTransactionRepository must not be null");
        this.walletService = Objects.requireNonNull(walletService, "walletService must not be null");
        this.auditService = Objects.requireNonNull(auditService, "auditService must not be null");
        this.unitOfWork = Objects.requireNonNull(unitOfWork, "unitOfWork must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    @Override
    public Dispute raiseDispute(UUID leadId, String leadTitle, UUID proId, String proName,
                                String reason, String details, int creditsRequested) {
        Objects.requireNonNull(leadId, "leadId must not be null");
        Objects.requireNonNull(proId, "proId must not be null");
        String normalizedReason = normalizeReason(reason);

        return unitOfWork.executeInTransaction(() -> {
            User pro = userRepository.findById(proId)
                    .orElseThrow(() -> new ResourceNotFoundException("Professional not found with id: " + proId));
            if (pro.getRole() != UserRole.PRO) {
                throw new ValidationException("Only professional accounts can raise a dispute.");
            }
            if (!pro.isProVerified()) {
                throw new ConflictException("The professional account has not been verified by an administrator.");
            }

            Lead lead = leadRepository.findById(leadId)
                    .orElseThrow(() -> new ResourceNotFoundException("Lead not found with id: " + leadId));
            if (!leadRepository.existsClaimByLeadIdAndProId(leadId, proId)) {
                throw new ValidationException("A dispute can only be raised for a lead the professional has purchased.");
            }
            if (disputeRepository.existsOpenDispute(leadId, proId)) {
                throw new ConflictException("An open dispute already exists for this lead.");
            }

            int maxRefund = calculateMaxRefund(proId, leadId, lead);
            int refundRequested = creditsRequested > 0 ? creditsRequested : maxRefund;
            if (refundRequested <= 0 || refundRequested > maxRefund) {
                throw new ValidationException(
                        "creditsRequested must be between 1 and the refundable amount (" + maxRefund + ").");
            }

            Dispute dispute = Dispute.builder()
                    .id(UUID.randomUUID())
                    .leadId(leadId)
                    .leadTitle(lead.getTitle() != null && !lead.getTitle().isBlank()
                            ? lead.getTitle()
                            : (leadTitle != null ? leadTitle : "Solicitare Servicii"))
                    .proId(proId)
                    .proName(proName != null ? proName : "Professional")
                    .reason(normalizedReason)
                    .details(details != null ? details : "")
                    .creditsRequested(refundRequested)
                    .status(DisputeStatus.IN_ANALIZA)
                    .createdAt(LocalDateTime.now(clock))
                    .build();

            LeadStatus previousLeadStatus = lead.getStatus();
            lead.markDisputed();
            if (!leadRepository.updateStatus(leadId, previousLeadStatus, lead.getStatus())) {
                throw new ConflictException("Lead status changed concurrently; please retry.");
            }

            Dispute saved = disputeRepository.save(dispute);
            auditService.recordEvent("Dispute", saved.getId().toString(), AuditAction.RAISE_DISPUTE, proName,
                    AuditDetails.of(
                            "leadId", leadId,
                            "proId", proId,
                            "reason", dispute.getReason(),
                            "creditsRequested", dispute.getCreditsRequested()));
            return saved;
        });
    }

    private static String normalizeReason(String reason) {
        String normalized = reason == null ? "" : reason.trim().toLowerCase(Locale.ROOT);
        if (!ALLOWED_REASONS.contains(normalized)) {
            throw new ValidationException("Motivul sesizării nu este valid. Alege unul dintre motivele disponibile.");
        }
        return normalized;
    }

    /**
     * Refundable amount is what the professional actually paid for this lead minus
     * any refunds already issued for it. The lead's list price is only a fallback
     * for legacy leads that predate the wallet ledger.
     */
    private int calculateMaxRefund(UUID proId, UUID leadId, Lead lead) {
        List<WalletTransaction> ledger = walletTransactionRepository.findByUserIdAndLeadId(proId, leadId);
        int paid = ledger.stream()
                .filter(tx -> tx.getType() == WalletTransactionType.LEAD_PURCHASE)
                .mapToInt(tx -> Math.abs(tx.getAmount()))
                .sum();
        if (paid == 0) {
            return Math.max(lead.getLeadPrice(), 0);
        }
        int alreadyRefunded = ledger.stream()
                .filter(tx -> tx.getType() == WalletTransactionType.REFUND)
                .mapToInt(WalletTransaction::getAmount)
                .sum();
        return Math.max(paid - alreadyRefunded, 0);
    }

    @Override
    public Dispute resolveDispute(UUID disputeId, boolean approved, String adminDecisionNote, String adminUsername) {
        Objects.requireNonNull(disputeId, "disputeId must not be null");

        return unitOfWork.executeInTransaction(() -> {
            Dispute dispute = getDisputeById(disputeId);
            if (dispute.getStatus() != DisputeStatus.IN_ANALIZA) {
                throw new ConflictException("Dispute is already resolved with status: " + dispute.getStatus().getDbValue());
            }

            DisputeStatus newStatus = approved ? DisputeStatus.APROBAT : DisputeStatus.RESPINS;
            dispute.setStatus(newStatus);
            dispute.setAdminDecisionNote(adminDecisionNote);
            dispute.setResolvedAt(LocalDateTime.now(clock));

            String actor = adminUsername != null && !adminUsername.isBlank() ? adminUsername : "admin";
            if (approved) {
                walletService.refundCredits(dispute.getProId(), dispute.getCreditsRequested(),
                        "Rambursare sesizare aprobată #" + disputeId, dispute.getLeadId(), actor);
            }

            Dispute updated = disputeRepository.save(dispute);

            // A lead leaves the disputed state only when no other open dispute remains.
            leadRepository.findById(dispute.getLeadId()).ifPresent(lead -> {
                if (!disputeRepository.existsOpenDisputeForLead(dispute.getLeadId())) {
                    LeadStatus previousLeadStatus = lead.getStatus();
                    lead.reopenAfterDispute();
                    if (lead.getStatus() != previousLeadStatus) {
                        leadRepository.updateStatus(lead.getId(), previousLeadStatus, lead.getStatus());
                    }
                }
            });

            auditService.recordEvent("Dispute", disputeId.toString(), AuditAction.RESOLVE_DISPUTE, actor,
                    AuditDetails.of(
                            "status", newStatus.getDbValue(),
                            "approved", approved,
                            "creditsRequested", dispute.getCreditsRequested(),
                            "proId", dispute.getProId(),
                            "note", adminDecisionNote));
            return updated;
        });
    }

    @Override
    public Dispute getDisputeById(UUID disputeId) {
        Objects.requireNonNull(disputeId, "disputeId must not be null");
        return disputeRepository.findById(disputeId)
                .orElseThrow(() -> new ResourceNotFoundException("Dispute not found with id: " + disputeId));
    }

    @Override
    public List<Dispute> getAllDisputes() {
        return disputeRepository.findAll();
    }

    @Override
    public List<Dispute> getDisputesByProId(UUID proId) {
        Objects.requireNonNull(proId, "proId must not be null");
        return disputeRepository.findByProId(proId);
    }
}
