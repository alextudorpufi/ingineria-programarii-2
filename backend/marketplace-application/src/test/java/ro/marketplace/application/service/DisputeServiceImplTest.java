package ro.marketplace.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ro.marketplace.domain.exception.ConflictException;
import ro.marketplace.domain.model.AuditAction;
import ro.marketplace.domain.model.Dispute;
import ro.marketplace.domain.model.DisputeStatus;
import ro.marketplace.domain.model.Lead;
import ro.marketplace.domain.model.LeadClaim;
import ro.marketplace.domain.model.User;
import ro.marketplace.domain.port.DisputeRepository;
import ro.marketplace.domain.port.LeadRepository;
import ro.marketplace.domain.port.UserRepository;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DisputeServiceImplTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-01-01T10:00:00Z"), ZoneOffset.UTC);

    @Mock
    private DisputeRepository disputeRepository;

    @Mock
    private LeadRepository leadRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private WalletService walletService;

    @Mock
    private AuditService auditService;

    private DisputeServiceImpl disputeService;

    @BeforeEach
    void setUp() {
        disputeService = new DisputeServiceImpl(disputeRepository, leadRepository, userRepository,
                walletService, auditService, new DirectUnitOfWork(), CLOCK);
    }

    @Test
    @DisplayName("raiseDispute creates a dispute in analysis")
    void raiseDispute_Success() {
        UUID leadId = UUID.randomUUID();
        UUID proId = UUID.randomUUID();
        when(userRepository.findById(proId)).thenReturn(Optional.of(User.builder()
                .id(proId)
                .role("pro")
                .proVerified(true)
                .build()));
        when(leadRepository.findById(leadId)).thenReturn(Optional.of(Lead.builder()
                .id(leadId)
                .title("Lead")
                .leadPrice(30)
                .build()));
        when(leadRepository.findClaimsByLeadId(leadId)).thenReturn(List.of(LeadClaim.builder()
                .leadId(leadId)
                .proId(proId)
                .build()));
        when(disputeRepository.existsOpenDispute(leadId, proId)).thenReturn(false);
        when(disputeRepository.save(any(Dispute.class))).thenAnswer(inv -> inv.getArgument(0));

        Dispute dispute = disputeService.raiseDispute(leadId, "Lead", proId, "Pro",
                "unreachable_contact", "details", 30);

        assertThat(dispute.getStatus()).isEqualTo(DisputeStatus.IN_ANALIZA);
        assertThat(dispute.getProId()).isEqualTo(proId);
        verify(auditService).recordEvent(eq("Dispute"), anyString(), eq(AuditAction.RAISE_DISPUTE), eq("Pro"), anyString());
    }

    @Test
    @DisplayName("resolveDispute refunds and persists in one unit of work")
    void resolveDispute_Approved_Refunds() {
        UUID disputeId = UUID.randomUUID();
        UUID leadId = UUID.randomUUID();
        UUID proId = UUID.randomUUID();
        Dispute dispute = Dispute.builder()
                .id(disputeId)
                .leadId(leadId)
                .proId(proId)
                .status(DisputeStatus.IN_ANALIZA)
                .creditsRequested(30)
                .build();

        when(disputeRepository.findById(disputeId)).thenReturn(Optional.of(dispute));
        when(disputeRepository.save(any(Dispute.class))).thenAnswer(inv -> inv.getArgument(0));

        Dispute resolved = disputeService.resolveDispute(disputeId, true, "approved", "admin@marketplace.ro");

        assertThat(resolved.getStatus()).isEqualTo(DisputeStatus.APROBAT);
        assertThat(resolved.getResolvedAt()).isNotNull();
        verify(walletService).refundCredits(proId, 30, "Rambursare sesizare aprobată #" + disputeId, leadId,
                "admin@marketplace.ro");
        verify(auditService).recordEvent(eq("Dispute"), eq(disputeId.toString()),
                eq(AuditAction.RESOLVE_DISPUTE), eq("admin@marketplace.ro"), anyString());
    }

    @Test
    @DisplayName("resolveDispute rejects a dispute that is already resolved")
    void resolveDispute_AlreadyResolved_ThrowsConflict() {
        UUID disputeId = UUID.randomUUID();
        Dispute dispute = Dispute.builder()
                .id(disputeId)
                .status(DisputeStatus.APROBAT)
                .creditsRequested(30)
                .build();
        when(disputeRepository.findById(disputeId)).thenReturn(Optional.of(dispute));

        assertThatThrownBy(() -> disputeService.resolveDispute(disputeId, true, "again", "admin"))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already resolved");
        verify(walletService, never()).refundCredits(any(), anyInt(), any(), any(), any());
    }

    @Test
    @DisplayName("resolveDispute does not refund a rejected dispute")
    void resolveDispute_Rejected_DoesNotRefund() {
        UUID disputeId = UUID.randomUUID();
        Dispute dispute = Dispute.builder()
                .id(disputeId)
                .proId(UUID.randomUUID())
                .status(DisputeStatus.IN_ANALIZA)
                .creditsRequested(30)
                .build();
        when(disputeRepository.findById(disputeId)).thenReturn(Optional.of(dispute));
        when(disputeRepository.save(any(Dispute.class))).thenAnswer(inv -> inv.getArgument(0));

        Dispute resolved = disputeService.resolveDispute(disputeId, false, "rejected", "admin");

        assertThat(resolved.getStatus()).isEqualTo(DisputeStatus.RESPINS);
        verify(walletService, never()).refundCredits(any(), anyInt(), any(), any(), any());
    }
}
