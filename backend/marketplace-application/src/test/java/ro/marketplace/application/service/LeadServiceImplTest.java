package ro.marketplace.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ro.marketplace.domain.exception.ConflictException;
import ro.marketplace.domain.exception.ResourceNotFoundException;
import ro.marketplace.domain.exception.ValidationException;
import ro.marketplace.domain.model.AuditAction;
import ro.marketplace.domain.model.Lead;
import ro.marketplace.domain.model.LeadClaim;
import ro.marketplace.domain.model.LeadStatus;
import ro.marketplace.domain.port.LeadRepository;
import ro.marketplace.domain.port.ServiceCategoryRepository;

import java.math.BigDecimal;
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
class LeadServiceImplTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-01-01T10:00:00Z"), ZoneOffset.UTC);

    @Mock
    private LeadRepository leadRepository;

    @Mock
    private ServiceCategoryRepository serviceCategoryRepository;

    @Mock
    private WalletService walletService;

    @Mock
    private AuditService auditService;

    private LeadServiceImpl leadService;

    @BeforeEach
    void setUp() {
        leadService = new LeadServiceImpl(leadRepository, serviceCategoryRepository, walletService,
                auditService, new DirectUnitOfWork(), CLOCK);
    }

    @Test
    @DisplayName("claimLead reserves a slot, deducts credits, and records the claim")
    void claimLead_Success() {
        UUID leadId = UUID.randomUUID();
        UUID proId = UUID.randomUUID();

        Lead lead = Lead.builder()
                .id(leadId)
                .title("Magazin Florărie")
                .categoryId("it-software")
                .leadPrice(30)
                .maxPros(4)
                .budgetMin(BigDecimal.valueOf(5000))
                .budgetMax(BigDecimal.valueOf(8000))
                .build();

        when(leadRepository.findById(leadId)).thenReturn(Optional.of(lead));
        when(leadRepository.existsClaimByLeadIdAndProId(leadId, proId)).thenReturn(false);
        when(leadRepository.reserveSlot(leadId)).thenReturn(true);
        when(leadRepository.saveClaim(any(LeadClaim.class))).thenAnswer(inv -> inv.getArgument(0));

        LeadClaim claim = leadService.claimLead(leadId, proId, "DevCraft Studio");

        assertThat(claim).isNotNull();
        assertThat(claim.getProId()).isEqualTo(proId);
        verify(walletService).deductCredits(proId, 30, "Achiziție lead #" + leadId, leadId);
        verify(leadRepository).markAssignedIfFull(leadId);
        verify(auditService).recordEvent(eq("Lead"), eq(leadId.toString()), eq(AuditAction.CLAIM_LEAD),
                eq("DevCraft Studio"), anyString());
    }

    @Test
    @DisplayName("claimLead throws ConflictException if the professional already claimed the lead")
    void claimLead_AlreadyClaimed_ThrowsConflict() {
        UUID leadId = UUID.randomUUID();
        UUID proId = UUID.randomUUID();

        Lead lead = Lead.builder().id(leadId).leadPrice(30).maxPros(4).build();
        when(leadRepository.findById(leadId)).thenReturn(Optional.of(lead));
        when(leadRepository.existsClaimByLeadIdAndProId(leadId, proId)).thenReturn(true);

        assertThatThrownBy(() -> leadService.claimLead(leadId, proId, "DevCraft Studio"))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already claimed");
        verify(leadRepository, never()).reserveSlot(any());
        verify(walletService, never()).deductCredits(any(), anyInt(), any(), any());
    }

    @Test
    @DisplayName("claimLead throws ConflictException when the atomic reservation reports full capacity")
    void claimLead_CapacityFull_ThrowsConflict() {
        UUID leadId = UUID.randomUUID();
        UUID proId = UUID.randomUUID();

        Lead lead = Lead.builder().id(leadId).leadPrice(30).maxPros(4).build();
        when(leadRepository.findById(leadId)).thenReturn(Optional.of(lead));
        when(leadRepository.existsClaimByLeadIdAndProId(leadId, proId)).thenReturn(false);
        when(leadRepository.reserveSlot(leadId)).thenReturn(false);

        assertThatThrownBy(() -> leadService.claimLead(leadId, proId, "DevCraft Studio"))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("maximum capacity");
        verify(walletService, never()).deductCredits(any(), anyInt(), any(), any());
    }

    @Test
    @DisplayName("claimLead releases the reserved slot when the claim cannot be persisted")
    void claimLead_SaveClaimFails_ReleasesSlot() {
        UUID leadId = UUID.randomUUID();
        UUID proId = UUID.randomUUID();

        Lead lead = Lead.builder().id(leadId).leadPrice(30).maxPros(4).build();
        when(leadRepository.findById(leadId)).thenReturn(Optional.of(lead));
        when(leadRepository.existsClaimByLeadIdAndProId(leadId, proId)).thenReturn(false);
        when(leadRepository.reserveSlot(leadId)).thenReturn(true);
        when(leadRepository.saveClaim(any(LeadClaim.class))).thenThrow(new IllegalStateException("db failure"));

        assertThatThrownBy(() -> leadService.claimLead(leadId, proId, "DevCraft Studio"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("db failure");
        verify(leadRepository).releaseSlot(leadId);
    }

    @Test
    @DisplayName("claimLead rejects missing identifiers")
    void claimLead_NullArguments_Throws() {
        assertThatThrownBy(() -> leadService.claimLead(null, UUID.randomUUID(), "Pro"))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> leadService.claimLead(UUID.randomUUID(), null, "Pro"))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("claimLead rejects an unknown lead")
    void claimLead_UnknownLead_ThrowsNotFound() {
        UUID leadId = UUID.randomUUID();
        UUID proId = UUID.randomUUID();
        when(leadRepository.findById(leadId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> leadService.claimLead(leadId, proId, "Pro"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Lead not found");
    }

    @Test
    @DisplayName("createLead validates the service category before persisting")
    void createLead_UnknownCategory_ThrowsValidation() {
        Lead lead = Lead.builder()
                .title("Website")
                .categoryId("does-not-exist")
                .budgetMin(BigDecimal.TEN)
                .budgetMax(BigDecimal.valueOf(20))
                .leadPrice(30)
                .maxPros(4)
                .clientId(UUID.randomUUID())
                .build();
        when(serviceCategoryRepository.existsById("does-not-exist")).thenReturn(false);

        assertThatThrownBy(() -> leadService.createLead(lead))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Unknown service category");
        verify(leadRepository, never()).save(any());
    }

    @Test
    @DisplayName("createLead sets defaults and audits the creation")
    void createLead_Success() {
        Lead lead = Lead.builder()
                .title("Website")
                .categoryId("it-software")
                .workMode(Lead.WorkMode.REMOTE)
                .requiredCapabilities(List.of(" Payments ", "payments"))
                .preferredCapabilities(List.of("PAYMENTS", " Branding ", "branding"))
                .budgetMin(BigDecimal.TEN)
                .budgetMax(BigDecimal.valueOf(20))
                .clientId(UUID.randomUUID())
                .clientEmail("client@example.com")
                .build();
        when(serviceCategoryRepository.existsById("it-software")).thenReturn(true);
        when(leadRepository.save(any(Lead.class))).thenAnswer(inv -> inv.getArgument(0));

        Lead created = leadService.createLead(lead);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getRequiredCapabilities()).containsExactly("Payments");
        assertThat(created.getPreferredCapabilities()).containsExactly("Branding");
        assertThat(created.getStatus()).isEqualTo(LeadStatus.VALIDAT);
        assertThat(created.getLeadPrice()).isEqualTo(30);
        assertThat(created.getMaxPros()).isEqualTo(4);
        assertThat(created.getCreatedAt()).isNotNull();
        verify(auditService).recordEvent(eq("Lead"), eq(created.getId().toString()), eq(AuditAction.CREATE),
                eq("client@example.com"), anyString());
    }

    @Test
    void createLeadRequiresExplicitWorkModeAndBoundedCapabilities() {
        Lead lead = Lead.builder().categoryId("it-software").clientId(UUID.randomUUID())
                .budgetMin(BigDecimal.TEN).budgetMax(BigDecimal.TEN).build();
        when(serviceCategoryRepository.existsById("it-software")).thenReturn(true);
        assertThatThrownBy(() -> leadService.createLead(lead)).isInstanceOf(ValidationException.class)
                .hasMessageContaining("modul de lucru");
        lead.setWorkMode(Lead.WorkMode.ONSITE);
        for (List<String> invalid : List.of(List.of(" "), List.of("x".repeat(256)),
                java.util.Collections.nCopies(51, "Payments"))) {
            lead.setRequiredCapabilities(invalid);
            assertThatThrownBy(() -> leadService.createLead(lead)).isInstanceOf(ValidationException.class)
                    .hasMessageContaining("50 de competențe");
        }
        verify(leadRepository, never()).save(any());
    }

    @Test
    @DisplayName("createLead rejects an inverted budget range")
    void createLead_InvertedBudget_ThrowsValidation() {
        Lead lead = Lead.builder()
                .title("Website")
                .categoryId("it-software")
                .budgetMin(BigDecimal.valueOf(100))
                .budgetMax(BigDecimal.TEN)
                .build();

        assertThatThrownBy(() -> leadService.createLead(lead))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("budgetMin");
        verify(serviceCategoryRepository, never()).existsById(anyString());
    }

    @Test
    @DisplayName("getAllLeads loads all claims in one batch query")
    void getAllLeads_LoadsClaimsInBatch() {
        UUID leadId1 = UUID.randomUUID();
        UUID leadId2 = UUID.randomUUID();
        Lead lead1 = Lead.builder().id(leadId1).title("L1").build();
        Lead lead2 = Lead.builder().id(leadId2).title("L2").build();
        LeadClaim claim = LeadClaim.builder().id(UUID.randomUUID()).leadId(leadId1)
                .proId(UUID.randomUUID()).proName("Pro").build();

        when(leadRepository.findAll()).thenReturn(List.of(lead1, lead2));
        when(leadRepository.findClaimsByLeadIds(List.of(leadId1, leadId2))).thenReturn(List.of(claim));

        List<Lead> leads = leadService.getAllLeads();

        assertThat(leads.get(0).getClaimedPros()).hasSize(1);
        assertThat(leads.get(1).getClaimedPros()).isEmpty();
        verify(leadRepository, never()).findClaimsByLeadId(any());
    }
}
