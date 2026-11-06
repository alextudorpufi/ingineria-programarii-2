package ro.marketplace.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/** Structured professional data; migrated labels remain unvalidated until onboarding. */
@Getter
@Builder
public class ProfessionalProfile {
    public enum OnboardingStatus { DRAFT, COMPLETE }

    @Builder.Default
    private List<String> categories = List.of();
    @Builder.Default
    private List<String> servicesOffered = List.of();
    @Builder.Default
    private List<String> coverageAreas = List.of();
    @Builder.Default
    private List<String> capabilities = List.of();
    private Boolean supportsOnsite;
    private Boolean supportsRemote;
    private Integer yearsExperience;
    private boolean acceptingLeads;
    @Builder.Default
    private long version = 0;
    @Builder.Default
    private OnboardingStatus onboardingStatus = OnboardingStatus.DRAFT;
}
