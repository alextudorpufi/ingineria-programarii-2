package ro.marketplace.application.service;

import ro.marketplace.domain.exception.ConflictException;
import ro.marketplace.domain.exception.ValidationException;
import ro.marketplace.domain.model.User;
import ro.marketplace.domain.model.ProfessionalProfile;
import ro.marketplace.domain.port.PasswordHasher;
import ro.marketplace.domain.port.UnitOfWork;
import ro.marketplace.domain.port.UserRepository;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class AuthServiceImpl implements AuthService {

    private static final String ROLE_CLIENT = "client";
    private static final String ROLE_PRO = "pro";
    private static final int PRO_WELCOME_CREDITS = 30;

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final UnitOfWork unitOfWork;

    public AuthServiceImpl(UserRepository userRepository,
                           PasswordHasher passwordHasher,
                           UnitOfWork unitOfWork) {
        this.userRepository = Objects.requireNonNull(userRepository, "userRepository must not be null");
        this.passwordHasher = Objects.requireNonNull(passwordHasher, "passwordHasher must not be null");
        this.unitOfWork = Objects.requireNonNull(unitOfWork, "unitOfWork must not be null");
    }

    @Override
    public Optional<User> authenticate(String email, String rawPassword) {
        if (email == null || email.isBlank() || rawPassword == null || rawPassword.isEmpty()) {
            return Optional.empty();
        }
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        return userRepository.findByEmail(normalizedEmail)
                .filter(user -> user.getPasswordHash() != null
                        && passwordHasher.matches(rawPassword, user.getPasswordHash()));
    }

    @Override
    public User register(String email, String rawPassword, String name, String role,
                         String phone, String companyName) {
        String normalizedEmail = requireText(email, "Email").toLowerCase(Locale.ROOT);
        String normalizedName = requireText(name, "Name");
        requireText(rawPassword, "Password");

        String normalizedRole = role == null ? "" : role.trim().toLowerCase(Locale.ROOT);
        if (!ROLE_CLIENT.equals(normalizedRole) && !ROLE_PRO.equals(normalizedRole)) {
            throw new ValidationException("Public registration allows only the 'client' or 'pro' roles.");
        }

        return unitOfWork.executeInTransaction(() -> {
            if (userRepository.existsByEmail(normalizedEmail)) {
                throw new ConflictException("An account already exists for this email address.");
            }

            User newUser = User.builder()
                    .id(UUID.randomUUID())
                    .email(normalizedEmail)
                    .passwordHash(passwordHasher.encode(rawPassword))
                    .role(normalizedRole)
                    .name(normalizedName)
                    .companyName(companyName)
                    .phone(phone)
                    .verifiedBadge(false)
                    .rating(BigDecimal.valueOf(5.00))
                    .reviewCount(0)
                    .creditsBalance(ROLE_PRO.equals(normalizedRole) ? PRO_WELCOME_CREDITS : 0)
                    .completedJobs(0)
                    .trustScore(BigDecimal.valueOf(100.00))
                    .professionalProfile(ROLE_PRO.equals(normalizedRole) ? ProfessionalProfile.builder().build() : null)
                    .build();

            return userRepository.save(newUser);
        });
    }

    @Override
    public Optional<User> findById(UUID id) {
        Objects.requireNonNull(id, "id must not be null");
        return userRepository.findById(id);
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(fieldName + " is required.");
        }
        return value.trim();
    }
}
