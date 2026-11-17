package ro.marketplace.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CreditPurchaseStatusTest {

    @Test
    @DisplayName("persisted values round-trip and ignore case and spaces")
    void fromDbValue() {
        assertThat(CreditPurchaseStatus.fromDbValue("pending")).isEqualTo(CreditPurchaseStatus.PENDING);
        assertThat(CreditPurchaseStatus.fromDbValue(" PAID ")).isEqualTo(CreditPurchaseStatus.PAID);
        assertThat(CreditPurchaseStatus.fromDbValue("Failed")).isEqualTo(CreditPurchaseStatus.FAILED);
    }

    @Test
    @DisplayName("unknown or missing values are rejected")
    void rejectsUnknownValues() {
        assertThatThrownBy(() -> CreditPurchaseStatus.fromDbValue("cancelled"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> CreditPurchaseStatus.fromDbValue(null))
                .isInstanceOf(NullPointerException.class);
    }
}
