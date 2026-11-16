package ro.marketplace.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuditActionTest {

    @Test
    @DisplayName("persisted actions are read tolerantly")
    void fromDbValue() {
        assertThat(AuditAction.fromDbValue("CREATE")).isEqualTo(AuditAction.CREATE);
        assertThat(AuditAction.fromDbValue(" create ")).isEqualTo(AuditAction.CREATE);
        assertThat(AuditAction.fromDbValue("Credit_Bonus")).isEqualTo(AuditAction.CREDIT_BONUS);
    }

    @Test
    @DisplayName("unknown actions are rejected explicitly")
    void fromDbValueRejectsUnknownAction() {
        assertThatThrownBy(() -> AuditAction.fromDbValue("SOMETHING_ELSE"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SOMETHING_ELSE");
    }
}
