package ro.marketplace.infrastructure.persistence.mapper;

import ro.marketplace.domain.model.ProfessionalProfile;
import ro.marketplace.infrastructure.persistence.entity.ProfessionalProfileEntity;
import ro.marketplace.infrastructure.persistence.entity.UserEntity;

public final class ProfessionalProfileMapper {
    private ProfessionalProfileMapper() { }

    static ProfessionalProfile toDomain(ProfessionalProfileEntity entity) {
        if (entity == null) return null;
        return ProfessionalProfile.builder()
                .categories(entity.getCategories())
                .servicesOffered(entity.getServicesOffered())
                .coverageAreas(entity.getCoverageAreas())
                .capabilities(entity.getCapabilities())
                .supportsOnsite(entity.getSupportsOnsite())
                .supportsRemote(entity.getSupportsRemote())
                .yearsExperience(entity.getYearsExperience())
                .acceptingLeads(entity.isAcceptingLeads())
                .onboardingStatus(entity.getOnboardingStatus())
                .version(entity.getVersion() == null ? 0 : entity.getVersion())
                .build();
    }

    static ProfessionalProfileEntity toEntity(ProfessionalProfile profile, UserEntity user) {
        if (profile == null && !"pro".equals(user.getRole())) return null;
        ProfessionalProfile source = profile == null ? ProfessionalProfile.builder().build() : profile;
        ProfessionalProfileEntity entity = new ProfessionalProfileEntity();
        entity.setUserId(user.getId());
        entity.setUser(user);
        updateEntity(source, entity);
        return entity;
    }

    public static void updateEntity(ProfessionalProfile source, ProfessionalProfileEntity entity) {
        entity.setCategories(source.getCategories());
        entity.setServicesOffered(source.getServicesOffered());
        entity.setCoverageAreas(source.getCoverageAreas());
        entity.setCapabilities(source.getCapabilities());
        entity.setSupportsOnsite(source.getSupportsOnsite());
        entity.setSupportsRemote(source.getSupportsRemote());
        entity.setYearsExperience(source.getYearsExperience());
        entity.setAcceptingLeads(source.isAcceptingLeads());
        entity.setOnboardingStatus(source.getOnboardingStatus());
    }
}
