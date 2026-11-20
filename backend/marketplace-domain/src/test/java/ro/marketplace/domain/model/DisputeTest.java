package ro.marketplace.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ro.marketplace.domain.exception.ConflictException;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DisputeTest {

    private static Dispute dispute(DisputeStatus status) {
        return Dispute.builder()
                .id(UUID.randomUUID())
                .status(status)
                .creditsRequested(30)
                .build();
    }

    @Test
    @DisplayName("an open dispute can be approved")
    void resolveApproved() {
        Dispute dispute = dispute(DisputeStatus.IN_ANALIZA);
        LocalDateTime resolvedAt = LocalDateTime.now();

        dispute.resolve(true, "approved", resolvedAt);

        assertThat(dispute.getStatus()).isEqualTo(DisputeStatus.APROBAT);
        assertThat(dispute.getAdminDecisionNote()).isEqualTo("approved");
        assertThat(dispute.getResolvedAt()).isEqualTo(resolvedAt);
    }

    @Test
    @DisplayName("an open dispute can be rejected")
    void resolveRejected() {
        Dispute dispute = dispute(DisputeStatus.IN_ANALIZA);

        dispute.resolve(false, "rejected", LocalDateTime.now());

        assertThat(dispute.getStatus()).isEqualTo(DisputeStatus.RESPINS);
    }

    @Test
    @DisplayName("a resolved dispute cannot be resolved again")
    void resolveTwiceIsRejected() {
        Dispute dispute = dispute(DisputeStatus.APROBAT);

        assertThatThrownBy(() -> dispute.resolve(false, "again", LocalDateTime.now()))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already resolved");
    }
}
