package ro.marketplace.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserRoleTest {

    @Test
    @DisplayName("db values round-trip and ignore case and surrounding spaces")
    void fromDbValue() {
        assertThat(UserRole.fromDbValue("client")).isEqualTo(UserRole.CLIENT);
        assertThat(UserRole.fromDbValue(" PRO ")).isEqualTo(UserRole.PRO);
        assertThat(UserRole.fromDbValue("Admin")).isEqualTo(UserRole.ADMIN);
        assertThat(UserRole.fromDbValue("PRO")).isEqualTo(UserRole.PRO);
    }

    @Test
    @DisplayName("unknown roles are rejected instead of silently defaulting")
    void fromDbValueRejectsUnknownRole() {
        assertThatThrownBy(() -> UserRole.fromDbValue("superuser"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("public registration cannot create administrators")
    void fromRegistrationRoleRejectsAdmin() {
        assertThat(UserRole.fromRegistrationRole("pro")).isEqualTo(UserRole.PRO);
        assertThatThrownBy(() -> UserRole.fromRegistrationRole("admin"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("role predicates are exhaustive")
    void predicates() {
        assertThat(UserRole.CLIENT.isClient()).isTrue();
        assertThat(UserRole.PRO.isPro()).isTrue();
        assertThat(UserRole.ADMIN.isAdmin()).isTrue();
        assertThat(UserRole.PRO.isAdmin()).isFalse();
    }
}
