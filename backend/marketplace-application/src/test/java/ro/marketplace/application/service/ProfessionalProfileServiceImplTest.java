package ro.marketplace.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ro.marketplace.domain.exception.ValidationException;
import ro.marketplace.domain.model.ProfessionalProfile;
import ro.marketplace.domain.model.RomanianCounty;
import ro.marketplace.domain.model.User;
import ro.marketplace.domain.port.ServiceCategoryRepository;
import ro.marketplace.domain.port.UserRepository;

import java.util.List;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProfessionalProfileServiceImplTest {
    private final UserRepository users = mock(UserRepository.class);
    private final ServiceCategoryRepository categories = mock(ServiceCategoryRepository.class);
    private final UUID userId = UUID.randomUUID();
    private final ProfessionalProfileService service = new ProfessionalProfileServiceImpl(users, categories, new DirectUnitOfWork());

    @BeforeEach
    void setup() {
        when(users.findById(userId)).thenReturn(Optional.of(User.builder().id(userId).role("pro")
                .name("Pro").creditsBalance(120).verifiedBadge(false).build()));
        when(users.updateProfessionalProfile(any(), anyLong())).thenAnswer(call -> call.getArgument(0));
        when(categories.existsById("it-software")).thenReturn(true);
    }

    @Test
    void draftsPreserveUnknownsAndNormalizeLists() {
        var profile = ProfessionalProfile.builder().servicesOffered(List.of(" Web ", "Web", " "))
                .coverageAreas(List.of("București & Ilfov")).build();
        User updated = service.update(userId, update(profile));
        assertThat(updated.getServicesOffered()).containsExactly("Web");
        assertThat(updated.getCoverageAreas()).containsExactly("București & Ilfov");
        assertThat(updated.getProfessionalProfile().getSupportsRemote()).isNull();
        assertThat(updated.getProfessionalProfile().getYearsExperience()).isNull();
        assertThat(updated.getName()).isEqualTo("Updated Pro");
        assertThat(updated.getCreditsBalance()).isEqualTo(120);
        assertThat(updated.isVerifiedBadge()).isFalse();
        assertThat(updated.getRole()).isEqualTo("pro");
    }

    @Test
    void onsiteProfilesAcceptEveryCountyCodeAndDeduplicateSelections() {
        var countyIds = Arrays.stream(RomanianCounty.values()).map(RomanianCounty::id).toList();
        var profile = ProfessionalProfile.builder().categories(List.of("it-software"))
                .servicesOffered(List.of("Web")).supportsOnsite(true).coverageAreas(countyIds)
                .onboardingStatus(ProfessionalProfile.OnboardingStatus.COMPLETE).build();
        assertThat(service.update(userId, update(profile)).getCoverageAreas()).hasSize(42)
                .contains("RO-IS", "RO-B", "RO-IF");
        var duplicate = ProfessionalProfile.builder().coverageAreas(List.of(" RO-IS ", "RO-IS")).build();
        assertThat(service.update(userId, update(duplicate)).getCoverageAreas()).containsExactly("RO-IS");
    }

    @Test
    void completedProfilesRejectLegacyLocationsAndUnknownCodes() {
        for (String area : List.of("Iași", "București & Ilfov", "RO-UNKNOWN")) {
            var profile = ProfessionalProfile.builder().categories(List.of("it-software"))
                    .servicesOffered(List.of("Web")).supportsOnsite(true).coverageAreas(List.of(area))
                    .onboardingStatus(ProfessionalProfile.OnboardingStatus.COMPLETE).build();
            assertThatThrownBy(() -> service.update(userId, update(profile)))
                    .isInstanceOf(ValidationException.class).hasMessageContaining("zonele vechi");
        }
    }

    @Test
    void completedRemoteProfilesCanHaveNoGeographicCoverage() {
        var profile = ProfessionalProfile.builder().categories(List.of("it-software"))
                .servicesOffered(List.of("Web")).supportsRemote(true).acceptingLeads(true)
                .onboardingStatus(ProfessionalProfile.OnboardingStatus.COMPLETE).build();
        assertThat(service.update(userId, update(profile)).getProfessionalProfile().isAcceptingLeads()).isTrue();
    }

    @Test
    void completedProfilesRequireKnownCategories() {
        var profile = ProfessionalProfile.builder().categories(List.of("Unmapped legacy label"))
                .servicesOffered(List.of("Web")).supportsRemote(true)
                .onboardingStatus(ProfessionalProfile.OnboardingStatus.COMPLETE).build();
        assertThatThrownBy(() -> service.update(userId, update(profile)))
                .isInstanceOf(ValidationException.class).hasMessageContaining("categoriile vechi");
    }

    @Test
    void onsiteProfilesRequireCoverageAndDraftsCannotAcceptLeads() {
        var onsite = ProfessionalProfile.builder().categories(List.of("it-software"))
                .servicesOffered(List.of("Web")).supportsOnsite(true)
                .onboardingStatus(ProfessionalProfile.OnboardingStatus.COMPLETE).build();
        assertThatThrownBy(() -> service.update(userId, update(onsite)))
                .isInstanceOf(ValidationException.class).hasMessageContaining("zona de acoperire");
        assertThatThrownBy(() -> service.update(userId, update(ProfessionalProfile.builder().acceptingLeads(true).build())))
                .isInstanceOf(ValidationException.class).hasMessageContaining("Completează profilul");
    }

    @Test
    void missingWorkModeAndNonProfessionalAccountsAreRejected() {
        var profile = ProfessionalProfile.builder().categories(List.of("it-software"))
                .servicesOffered(List.of("Web")).onboardingStatus(ProfessionalProfile.OnboardingStatus.COMPLETE).build();
        assertThatThrownBy(() -> service.update(userId, update(profile)))
                .isInstanceOf(ValidationException.class).hasMessageContaining("mod de lucru");
        when(users.findById(userId)).thenReturn(Optional.of(User.builder().id(userId).role("client").build()));
        assertThatThrownBy(() -> service.get(userId)).isInstanceOf(ValidationException.class);
    }

    private ProfessionalProfileService.Update update(ProfessionalProfile profile) {
        return new ProfessionalProfileService.Update(" Updated Pro ", "Company", "Developer", null, "Bio", profile);
    }
}
