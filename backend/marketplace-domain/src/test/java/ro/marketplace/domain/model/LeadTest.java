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
}
