package ro.marketplace.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserTest {

    @Test
    @DisplayName("new users default to the least-privileged role")
    void defaultRoleIsClient() {
        assertThat(User.builder().build().getRole()).isEqualTo(UserRole.CLIENT);
    }

    @Test
    @DisplayName("compatibility profile getters expose immutable snapshots")
    void profileGettersAreImmutable() {
        User user = User.builder()
                .professionalProfile(ProfessionalProfile.builder()
                        .categories(List.of("it-software"))
                        .servicesOffered(List.of("Web"))
                        .coverageAreas(List.of("RO-IS"))
                        .build())
                .build();

        assertThat(user.getCategories()).containsExactly("it-software");
        assertThat(user.getServicesOffered()).containsExactly("Web");
        assertThat(user.getCoverageAreas()).containsExactly("RO-IS");
        assertThatThrownBy(() -> user.getCategories().add("design"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("profile getters are empty when no profile exists")
    void profileGettersWithoutProfile() {
        User user = User.builder().build();

        assertThat(user.getCategories()).isEmpty();
        assertThat(user.getServicesOffered()).isEmpty();
        assertThat(user.getCoverageAreas()).isEmpty();
    }
}
