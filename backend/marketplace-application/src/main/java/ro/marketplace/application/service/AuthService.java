package ro.marketplace.application.service;

import ro.marketplace.domain.model.User;

import java.util.Optional;
import java.util.UUID;

/**
 * Authentication use cases. Implementations never expose whether an email
 * exists during login: the caller receives an empty Optional for any invalid
 * email/password combination.
 */
public interface AuthService {

    Optional<User> authenticate(String email, String rawPassword);

    User register(String email, String rawPassword, String name, String role,
                  String phone, String companyName);

    Optional<User> findById(UUID id);

    /**
     * True only when an administrator has verified the professional account.
     */
    boolean isVerifiedProfessional(UUID userId);

    /**
     * Marks a professional as verified and grants the one-time welcome credits
     * in a single transaction, recording the grant for audit purposes.
     */
    User verifyProfessionalAndGrantCredits(UUID userId, int credits, UUID adminId);
}
