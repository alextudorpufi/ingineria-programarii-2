package ro.marketplace.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProfessionalProfileTest {

    @Test
    @DisplayName("collection getters return immutable snapshots")
    void gettersCopyCollections() {
        ProfessionalProfile profile = ProfessionalProfile.builder()
                .categories(List.of("it-software"))
                .servicesOffered(List.of("Web"))
                .coverageAreas(List.of("RO-IS"))
                .capabilities(List.of("Payments"))
                .build();

        assertThatThrownBy(() -> profile.getCategories().add("design"))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> profile.getCapabilities().clear())
                .isInstanceOf(UnsupportedOperationException.class);

        assertThat(profile.getCategories()).containsExactly("it-software");
        assertThat(profile.getServicesOffered()).containsExactly("Web");
        assertThat(profile.getCoverageAreas()).containsExactly("RO-IS");
        assertThat(profile.getCapabilities()).containsExactly("Payments");
    }

    @Test
    @DisplayName("a null collection is exposed as an empty list")
    void nullCollectionsAreEmpty() {
        ProfessionalProfile profile = ProfessionalProfile.builder()
                .categories(null)
                .servicesOffered(null)
                .coverageAreas(null)
                .capabilities(null)
                .build();

        assertThat(profile.getCategories()).isEmpty();
        assertThat(profile.getServicesOffered()).isEmpty();
        assertThat(profile.getCoverageAreas()).isEmpty();
        assertThat(profile.getCapabilities()).isEmpty();
    }
}
