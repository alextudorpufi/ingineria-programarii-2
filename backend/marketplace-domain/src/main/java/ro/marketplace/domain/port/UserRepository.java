package ro.marketplace.domain.port;

import ro.marketplace.domain.model.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    User save(User user);

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
}
