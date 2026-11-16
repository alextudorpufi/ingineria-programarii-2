package ro.marketplace.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ro.marketplace.domain.exception.ConflictException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LeadTest {

    private static Lead lead(LeadStatus status, int currentPros, int maxPros) {
        return Lead.builder()
                .status(status)
                .currentPros(currentPros)
                .maxPros(maxPros)
                .build();
    }

    @Test
    @DisplayName("only validated and waiting leads are open for claims")
    void openForClaims() {
        assertThat(lead(LeadStatus.VALIDAT, 0, 4).isOpenForClaims()).isTrue();
        assertThat(lead(LeadStatus.IN_ASTEPTARE, 0, 4).isOpenForClaims()).isTrue();
        assertThat(lead(LeadStatus.ATRIBUIT, 4, 4).isOpenForClaims()).isFalse();
        assertThat(lead(LeadStatus.DISPUTAT, 4, 4).isOpenForClaims()).isFalse();
        assertThat(lead(LeadStatus.FINALIZAT, 4, 4).isOpenForClaims()).isFalse();
    }

    @Test
    @DisplayName("a validated lead becomes disputed")
    void markDisputed() {
        Lead lead = lead(LeadStatus.VALIDAT, 1, 4);

        lead.markDisputed();

        assertThat(lead.getStatus()).isEqualTo(LeadStatus.DISPUTAT);
    }

    @Test
    @DisplayName("a finalized lead cannot be disputed")
    void markDisputedRejectsFinalizedLead() {
        Lead lead = lead(LeadStatus.FINALIZAT, 4, 4);

        assertThatThrownBy(lead::markDisputed)
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("finalized");
    }

    @Test
    @DisplayName("a full lead returns to assigned after the dispute is resolved")
    void reopenFullLead() {
        Lead lead = lead(LeadStatus.DISPUTAT, 4, 4);

        lead.reopenAfterDispute();

        assertThat(lead.getStatus()).isEqualTo(LeadStatus.ATRIBUIT);
    }

    @Test
    @DisplayName("a lead with free capacity returns to validated after the dispute is resolved")
    void reopenLeadWithFreeCapacity() {
        Lead lead = lead(LeadStatus.DISPUTAT, 1, 4);

        lead.reopenAfterDispute();

        assertThat(lead.getStatus()).isEqualTo(LeadStatus.VALIDAT);
    }

    @Test
    @DisplayName("reopening a lead that is not disputed is a no-op")
    void reopenNonDisputedLeadIsNoOp() {
        Lead lead = lead(LeadStatus.ATRIBUIT, 4, 4);

        lead.reopenAfterDispute();

        assertThat(lead.getStatus()).isEqualTo(LeadStatus.ATRIBUIT);
    }

    @Test
    @DisplayName("a lead can be finalized")
    void closeLead() {
        Lead lead = lead(LeadStatus.ATRIBUIT, 4, 4);

        lead.close();

        assertThat(lead.getStatus()).isEqualTo(LeadStatus.FINALIZAT);
    }

    @Test
    @DisplayName("a disputed lead must be resolved before it can be closed")
    void closeDisputedLeadIsRejected() {
        Lead lead = lead(LeadStatus.DISPUTAT, 4, 4);

        assertThatThrownBy(lead::close)
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("resolved");
    }

    @Test
    @DisplayName("closing an already finalized lead is idempotent")
    void closeFinalizedLeadIsIdempotent() {
        Lead lead = lead(LeadStatus.FINALIZAT, 4, 4);

        lead.close();

        assertThat(lead.getStatus()).isEqualTo(LeadStatus.FINALIZAT);
    }

    @Test
    @DisplayName("capacity is reached only when the slot counter meets the limit")
    void hasReachedCapacity() {
        assertThat(lead(LeadStatus.VALIDAT, 3, 4).hasReachedCapacity()).isFalse();
        assertThat(lead(LeadStatus.VALIDAT, 4, 4).hasReachedCapacity()).isTrue();
        assertThat(lead(LeadStatus.VALIDAT, 0, 0).hasReachedCapacity()).isFalse();
    }

    @Test
    @DisplayName("creation rejects an inverted or negative budget")
    void validateForCreation() {
        Lead inverted = Lead.builder()
                .budgetMin(java.math.BigDecimal.TEN)
                .budgetMax(java.math.BigDecimal.ONE)
                .build();
        assertThatThrownBy(inverted::validateForCreation)
                .isInstanceOf(ro.marketplace.domain.exception.ValidationException.class)
                .hasMessageContaining("budgetMin");

        Lead negative = Lead.builder().budgetMin(java.math.BigDecimal.valueOf(-1)).build();
        assertThatThrownBy(negative::validateForCreation)
                .isInstanceOf(ro.marketplace.domain.exception.ValidationException.class);
    }

    @Test
    @DisplayName("platform policy overrides client price and capacity")
    void applyPlatformPolicy() {
        Lead lead = Lead.builder().leadPrice(1).maxPros(99).build();

        lead.applyPlatformPolicy(45);

        assertThat(lead.getStatus()).isEqualTo(LeadStatus.VALIDAT);
        assertThat(lead.getLeadPrice()).isEqualTo(45);
        assertThat(lead.getMaxPros()).isEqualTo(Lead.DEFAULT_MAX_PROS);
    }

    @Test
    @DisplayName("a zero category price falls back to the platform default")
    void applyPlatformPolicyFallsBackToDefaultPrice() {
        Lead lead = Lead.builder().build();

        lead.applyPlatformPolicy(0);

        assertThat(lead.getLeadPrice()).isEqualTo(Lead.DEFAULT_LEAD_PRICE);
    }

    @Test
    @DisplayName("requirements are trimmed, de-duplicated, and preferred values drop duplicates")
    void normalizeRequirements() {
        Lead lead = Lead.builder()
                .workMode(Lead.WorkMode.REMOTE)
                .requiredCapabilities(java.util.List.of(" Payments ", "payments"))
                .preferredCapabilities(java.util.List.of("PAYMENTS", " Branding ", "branding"))
                .build();

        lead.normalizeRequirements();

        assertThat(lead.getRequiredCapabilities()).containsExactly("Payments");
        assertThat(lead.getPreferredCapabilities()).containsExactly("Branding");
    }

    @Test
    @DisplayName("requirements cannot be normalized without a work mode")
    void normalizeRequirementsRequiresWorkMode() {
        Lead lead = Lead.builder().build();

        assertThatThrownBy(lead::normalizeRequirements)
                .isInstanceOf(ro.marketplace.domain.exception.ValidationException.class)
                .hasMessageContaining("modul de lucru");
    }

    @Test
    @DisplayName("collection getters expose immutable snapshots")
    void collectionsAreImmutable() {
        Lead lead = Lead.builder()
                .requiredCapabilities(java.util.List.of("Payments"))
                .preferredCapabilities(java.util.List.of("Branding"))
                .build();
        LeadClaim claim = LeadClaim.builder().proName("Pro").build();
        java.util.List<LeadClaim> claims = new java.util.ArrayList<>();
        claims.add(claim);
        lead.setClaimedPros(claims);

        assertThatThrownBy(() -> lead.getRequiredCapabilities().add("Hacking"))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> lead.getClaimedPros().clear())
                .isInstanceOf(UnsupportedOperationException.class);

        claims.add(LeadClaim.builder().proName("Other").build());
        lead.setRequiredCapabilities(java.util.List.of("Payments"));
        assertThat(lead.getClaimedPros()).hasSize(1);
        assertThat(lead.getRequiredCapabilities()).containsExactly("Payments");
    }
}
