package ro.marketplace.application.service;

import ro.marketplace.domain.exception.ConflictException;
import ro.marketplace.domain.exception.ResourceNotFoundException;
import ro.marketplace.domain.exception.ValidationException;
import ro.marketplace.domain.model.CreditGrant;
import ro.marketplace.domain.model.ProfessionalProfile;
import ro.marketplace.domain.model.User;
import ro.marketplace.domain.port.CreditGrantRepository;
import ro.marketplace.domain.port.PasswordHasher;
import ro.marketplace.domain.port.UnitOfWork;
import ro.marketplace.domain.port.UserRepository;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class AuthServiceImpl implements AuthService {

    private static final String ROLE_CLIENT = "client";
    private static final String ROLE_PRO = "pro";
    private static final int MAX_FAILED_LOGIN_ATTEMPTS = 10;
    private static final Duration LOCKOUT_DURATION = Duration.ofMinutes(30);
    private static final int MAX_LIFETIME_FREE_CREDITS = 30;
    /**
     * BCrypt silently truncates inputs after 72 bytes. Without this guard two
     * different long passwords could authenticate the same account.
     */
    private static final int BCRYPT_MAX_PASSWORD_BYTES = 72;

    private final UserRepository userRepository;
    private final CreditGrantRepository creditGrantRepository;
    private final PasswordHasher passwordHasher;
    private final UnitOfWork unitOfWork;
    private final Clock clock;

    public AuthServiceImpl(UserRepository userRepository,
                           CreditGrantRepository creditGrantRepository,
                           PasswordHasher passwordHasher,
                           UnitOfWork unitOfWork,
                           Clock clock) {
        this.userRepository = Objects.requireNonNull(userRepository, "userRepository must not be null");
        this.creditGrantRepository = Objects.requireNonNull(creditGrantRepository,
                "creditGrantRepository must not be null");
        this.passwordHasher = Objects.requireNonNull(passwordHasher, "passwordHasher must not be null");
        this.unitOfWork = Objects.requireNonNull(unitOfWork, "unitOfWork must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    @Override
    public Optional<User> authenticate(String email, String rawPassword) {
        if (email == null || email.isBlank() || rawPassword == null || rawPassword.isEmpty()) {
            return Optional.empty();
        }
        // Reject over-long candidate passwords before hashing: BCrypt would ignore
        // everything past 72 bytes, which can produce false matches.
        if (exceedsBcryptLimit(rawPassword)) {
            return Optional.empty();
        }
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        Optional<User> candidate = userRepository.findByEmail(normalizedEmail);
        if (candidate.isEmpty()) {
            return Optional.empty();
        }

        User user = candidate.get();
        LocalDateTime now = LocalDateTime.now(clock);
        if (user.getLockoutUntil() != null && user.getLockoutUntil().isAfter(now)) {
            return Optional.empty();
        }

        if (user.getPasswordHash() != null
                && passwordHasher.matches(rawPassword, user.getPasswordHash())) {
            if (user.getLoginAttemptsCount() > 0 || user.getLockoutUntil() != null) {
                userRepository.resetLoginState(user.getId());
            }
            return Optional.of(user);
        }

        userRepository.registerFailedLogin(user.getId(), MAX_FAILED_LOGIN_ATTEMPTS,
                now.plus(LOCKOUT_DURATION));
        return Optional.empty();
    }

    @Override
    public User register(String email, String rawPassword, String name, String role,
                         String phone, String companyName) {
        String normalizedEmail = requireText(email, "Email").toLowerCase(Locale.ROOT);
        String normalizedName = requireText(name, "Name");
        requireText(rawPassword, "Password");
        if (exceedsBcryptLimit(rawPassword)) {
            throw new ValidationException("Password must not exceed 72 bytes when UTF-8 encoded.");
        }

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
                    // Welcome credits are granted only after administrator verification.
                    .proVerified(false)
                    .creditsBalance(0)
                    .rating(BigDecimal.valueOf(5.00))
                    .reviewCount(0)
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

    @Override
    public boolean isVerifiedProfessional(UUID userId) {
        Objects.requireNonNull(userId, "userId must not be null");
        return userRepository.findById(userId)
                .map(User::isProVerified)
                .orElse(false);
    }

    @Override
    public User verifyProfessionalAndGrantCredits(UUID userId, int credits, UUID adminId) {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(adminId, "adminId must not be null");
        if (credits <= 0) {
            throw new ValidationException("Credits to grant must be positive.");
        }

        return unitOfWork.executeInTransaction(() -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
            String role = user.getRole() == null ? "" : user.getRole().trim().toLowerCase(Locale.ROOT);
            if (!ROLE_PRO.equals(role)) {
                throw new ValidationException("Only professional accounts can be verified.");
            }
            if (user.isProVerified()) {
                throw new ConflictException("The professional account is already verified.");
            }
            long alreadyGranted = creditGrantRepository.sumAmountByUserId(userId);
            if (alreadyGranted + credits > MAX_LIFETIME_FREE_CREDITS) {
                throw new ValidationException("The lifetime free-credit limit for this account would be exceeded.");
            }
            if (!userRepository.verifyProAndAddCredits(userId, credits)) {
                throw new ConflictException("The professional account is already verified.");
            }

            creditGrantRepository.save(new CreditGrant(
                    UUID.randomUUID(),
                    userId,
                    credits,
                    adminId,
                    LocalDateTime.now(clock)
            ));

            return userRepository.findById(userId).orElse(user);
        });
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(fieldName + " is required.");
        }
        return value.trim();
    }

    private static boolean exceedsBcryptLimit(String rawPassword) {
        return rawPassword.getBytes(StandardCharsets.UTF_8).length > BCRYPT_MAX_PASSWORD_BYTES;
    }
}
