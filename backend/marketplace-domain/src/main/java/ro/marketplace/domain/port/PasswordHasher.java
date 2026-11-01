package ro.marketplace.domain.port;

/**
 * Password hashing port. Keeps the application layer free of Spring Security
 * (or any other password hashing implementation) while still allowing the
 * authentication use case to verify and create credentials.
 */
public interface PasswordHasher {

    String encode(String rawPassword);

    boolean matches(String rawPassword, String encodedPassword);
}
