package ro.marketplace.app.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import ro.marketplace.application.service.ProfessionalProfileService;
import ro.marketplace.domain.model.ProfessionalProfile;
import ro.marketplace.domain.model.User;

import java.util.List;

public record ProfessionalProfileDto(
        @NotBlank(message = "Numele este obligatoriu.") @Size(max = 255) String name,
        @Size(max = 255) String companyName,
        @Size(max = 255) String title,
        @Size(max = 50) String phone,
        @Size(max = 5000) String bio,
        @NotNull @Size(max = 20) List<@NotBlank @Size(max = 255) String> categories,
        @NotNull @Size(max = 30) List<@NotBlank @Size(max = 255) String> servicesOffered,
        @NotNull @Size(max = 42) List<@NotBlank @Size(max = 255) String> coverageAreas,
        @NotNull @Size(max = 50) List<@NotBlank @Size(max = 255) String> capabilities,
        Boolean supportsOnsite,
        Boolean supportsRemote,
        @Min(0) @Max(100) Integer yearsExperience,
        @NotNull Boolean acceptingLeads,
        @NotNull ProfessionalProfile.OnboardingStatus onboardingStatus,
        @NotNull @PositiveOrZero Long version
) {
    public ProfessionalProfileService.Update toUpdate() {
        ProfessionalProfile profile = ProfessionalProfile.builder()
                .categories(categories).servicesOffered(servicesOffered).coverageAreas(coverageAreas)
                .capabilities(capabilities).supportsOnsite(supportsOnsite).supportsRemote(supportsRemote)
                .yearsExperience(yearsExperience).acceptingLeads(acceptingLeads)
                .onboardingStatus(onboardingStatus).version(version).build();
        return new ProfessionalProfileService.Update(name, companyName, title, phone, bio, profile);
    }

    public static ProfessionalProfileDto from(User user) {
        ProfessionalProfile profile = user.getProfessionalProfile() == null
                ? ProfessionalProfile.builder().build() : user.getProfessionalProfile();
        return new ProfessionalProfileDto(user.getName(), user.getCompanyName(), user.getTitle(),
                user.getPhone(), user.getBio(), profile.getCategories(), profile.getServicesOffered(),
                profile.getCoverageAreas(), profile.getCapabilities(), profile.getSupportsOnsite(),
                profile.getSupportsRemote(), profile.getYearsExperience(), profile.isAcceptingLeads(),
                profile.getOnboardingStatus(), profile.getVersion());
    }
}
