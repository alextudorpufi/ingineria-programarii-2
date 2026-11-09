package ro.marketplace.domain.port;

import ro.marketplace.domain.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    User save(User user);

    /** Updates only editable profile fields, preserving credentials, roles and credits. */
    User updateProfessionalProfile(User user, long expectedProfileVersion);

    Optional<User> findById(UUID id);

    Optional<User> findByEmail(String email);

    List<User> findAll();

    List<User> findByRole(String role);

    boolean existsByEmail(String email);

    void deleteById(UUID id);

    /**
     * Atomically adds credits. Returns {@code false} when the user does not exist.
     */
    boolean addCredits(UUID userId, int amount);

    /**
     * Atomically deducts credits only when the balance is sufficient.
     * Returns {@code false} when the user does not exist or the balance is too low.
     */
    boolean deductCredits(UUID userId, int amount);

    /**
     * Increments the consecutive failed-login counter and applies a lockout window
     * once the counter reaches {@code maxAttempts}. Returns {@code false} when the
     * user does not exist.
     */
    boolean registerFailedLogin(UUID userId, int maxAttempts, LocalDateTime lockoutUntil);

    /**
     * Clears the failed-login counter and any active lockout after a successful login.
     */
    boolean resetLoginState(UUID userId);

    /**
     * Atomically marks a professional as verified and credits the balance.
     * Returns {@code false} when the user is missing, is not a professional,
     * or has already been verified.
     */
    boolean verifyProAndAddCredits(UUID userId, int amount);
}
