package ro.marketplace.application.service;

import ro.marketplace.domain.exception.ResourceNotFoundException;
import ro.marketplace.domain.exception.ValidationException;
import ro.marketplace.domain.model.ProfessionalProfile;
import ro.marketplace.domain.model.RomanianCounty;
import ro.marketplace.domain.model.User;
import ro.marketplace.domain.port.ServiceCategoryRepository;
import ro.marketplace.domain.port.UnitOfWork;
import ro.marketplace.domain.port.UserRepository;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ProfessionalProfileServiceImpl implements ProfessionalProfileService {
    private final UserRepository users;
    private final ServiceCategoryRepository categories;
    private final UnitOfWork unitOfWork;

    public ProfessionalProfileServiceImpl(UserRepository users, ServiceCategoryRepository categories,
                                          UnitOfWork unitOfWork) {
        this.users = Objects.requireNonNull(users);
        this.categories = Objects.requireNonNull(categories);
        this.unitOfWork = Objects.requireNonNull(unitOfWork);
    }

    @Override
    public User get(UUID userId) {
        User user = users.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profilul nu a fost găsit."));
        if (!"pro".equals(user.getRole())) {
            throw new ValidationException("Doar profesioniștii pot modifica acest profil.");
        }
        return user;
    }

    @Override
    public User update(UUID userId, Update update) {
        Objects.requireNonNull(update);
        return unitOfWork.executeInTransaction(() -> {
            User user = get(userId);
            ProfessionalProfile profile = normalize(update.profile());
            validate(profile);
            if (update.name() == null || update.name().isBlank()) {
                throw new ValidationException("Numele este obligatoriu.");
            }
            user.setName(update.name().trim());
            user.setCompanyName(trim(update.companyName()));
            user.setTitle(trim(update.title()));
            user.setPhone(trim(update.phone()));
            user.setBio(trim(update.bio()));
            user.setProfessionalProfile(profile);
            return users.updateProfessionalProfile(user, profile.getVersion());
        });
    }

    private ProfessionalProfile normalize(ProfessionalProfile source) {
        Objects.requireNonNull(source);
        return ProfessionalProfile.builder()
                .categories(clean(source.getCategories()))
                .servicesOffered(clean(source.getServicesOffered()))
                .coverageAreas(clean(source.getCoverageAreas()))
                .capabilities(clean(source.getCapabilities()))
                .supportsOnsite(source.getSupportsOnsite()).supportsRemote(source.getSupportsRemote())
                .yearsExperience(source.getYearsExperience()).acceptingLeads(source.isAcceptingLeads())
                .onboardingStatus(source.getOnboardingStatus()).version(source.getVersion()).build();
    }

    private void validate(ProfessionalProfile profile) {
        if (profile.getVersion() < 0 || profile.getOnboardingStatus() == null
                || (profile.getYearsExperience() != null
                && (profile.getYearsExperience() < 0 || profile.getYearsExperience() > 100))) {
            throw new ValidationException("Verifică experiența și starea profilului.");
        }
        if (profile.getOnboardingStatus() == ProfessionalProfile.OnboardingStatus.DRAFT) {
            if (profile.isAcceptingLeads()) {
                throw new ValidationException("Completează profilul înainte de a accepta cereri.");
            }
            return;
        }
        if (profile.getCategories().isEmpty() || profile.getServicesOffered().isEmpty()) {
            throw new ValidationException("Alege cel puțin o categorie și adaugă un serviciu.");
        }
        if (profile.getCategories().stream().anyMatch(id -> !categories.existsById(id))) {
            throw new ValidationException("Înlocuiește categoriile vechi cu opțiuni din lista de categorii.");
        }
        validateDelivery(profile);
    }

    private void validateDelivery(ProfessionalProfile profile) {
        if (profile.getCoverageAreas().stream().anyMatch(id -> !RomanianCounty.isKnownId(id))) {
            throw new ValidationException("Înlocuiește zonele vechi cu județe din lista de acoperire.");
        }
        if (!Boolean.TRUE.equals(profile.getSupportsOnsite()) && !Boolean.TRUE.equals(profile.getSupportsRemote())) {
            throw new ValidationException("Selectează cel puțin un mod de lucru: la locație sau la distanță.");
        }
        if (Boolean.TRUE.equals(profile.getSupportsOnsite()) && profile.getCoverageAreas().isEmpty()) {
            throw new ValidationException("Adaugă zona de acoperire: selectează cel puțin un județ pentru lucrul la locație.");
        }
    }

    private List<String> clean(List<String> values) {
        return values == null ? List.of() : values.stream().filter(Objects::nonNull)
                .map(String::trim).filter(value -> !value.isEmpty()).distinct().toList();
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}
