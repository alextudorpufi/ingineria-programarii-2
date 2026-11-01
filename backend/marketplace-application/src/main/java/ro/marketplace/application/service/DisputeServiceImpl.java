package ro.marketplace.application.service;

import ro.marketplace.application.audit.AuditDetails;
import ro.marketplace.domain.exception.ConflictException;
import ro.marketplace.domain.exception.ResourceNotFoundException;
import ro.marketplace.domain.exception.ValidationException;
import ro.marketplace.domain.model.AuditAction;
import ro.marketplace.domain.model.Dispute;
import ro.marketplace.domain.model.DisputeStatus;
import ro.marketplace.domain.port.DisputeRepository;
import ro.marketplace.domain.port.UnitOfWork;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class DisputeServiceImpl implements DisputeService {

    private final DisputeRepository disputeRepository;
    private final WalletService walletService;
    private final AuditService auditService;
    private final UnitOfWork unitOfWork;
    private final Clock clock;

    public DisputeServiceImpl(DisputeRepository disputeRepository,
                              WalletService walletService,
                              AuditService auditService,
                              UnitOfWork unitOfWork,
                              Clock clock) {
        this.disputeRepository = Objects.requireNonNull(disputeRepository, "disputeRepository must not be null");
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
        if (creditsRequested > 10_000) {
            throw new ValidationException("creditsRequested must not exceed 10000");
        }

        return unitOfWork.executeInTransaction(() -> {
            Dispute dispute = Dispute.builder()
                    .id(UUID.randomUUID())
                    .leadId(leadId)
                    .leadTitle(leadTitle != null ? leadTitle : "Solicitare Servicii")
                    .proId(proId)
                    .proName(proName != null ? proName : "Professional")
                    .reason(reason != null ? reason : "unreachable_contact")
                    .details(details != null ? details : "")
                    .creditsRequested(creditsRequested > 0 ? creditsRequested : 30)
                    .status(DisputeStatus.IN_ANALIZA)
                    .createdAt(LocalDateTime.now(clock))
                    .build();

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
                        "Rambursare sesizare aprobată #" + disputeId, dispute.getLeadId());
            }

            Dispute updated = disputeRepository.save(dispute);
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
