package ro.marketplace.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DisputeReasonTest {

    @Test
    @DisplayName("catalog round-trips through db values")
    void fromDbValue() {
        assertThat(DisputeReason.fromDbValue("unreachable_contact"))
                .isEqualTo(DisputeReason.UNREACHABLE_CONTACT);
        assertThat(DisputeReason.fromDbValue(" WRONG_CATEGORY "))
                .isEqualTo(DisputeReason.WRONG_CATEGORY);
        assertThat(DisputeReason.fromDbValue("duplicate_or_fake"))
                .isEqualTo(DisputeReason.DUPLICATE_OR_FAKE);
    }

    @Test
    @DisplayName("unknown reasons are rejected")
    void fromDbValueRejectsUnknownReason() {
        assertThatThrownBy(() -> DisputeReason.fromDbValue("made_up"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
