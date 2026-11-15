package ro.marketplace.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ro.marketplace.domain.exception.ConflictException;
import ro.marketplace.domain.exception.ValidationException;
import ro.marketplace.domain.model.CreditGrant;
import ro.marketplace.domain.model.User;
import ro.marketplace.domain.port.CreditGrantRepository;
import ro.marketplace.domain.port.PasswordHasher;
import ro.marketplace.domain.port.UserRepository;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-01-01T10:00:00Z"), ZoneOffset.UTC);

    @Mock
    private UserRepository userRepository;

    @Mock
    private CreditGrantRepository creditGrantRepository;

    @Mock
    private PasswordHasher passwordHasher;

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(userRepository, creditGrantRepository, passwordHasher,
                new DirectUnitOfWork(), CLOCK);
    }

    @Test
    @DisplayName("public registration never grants credits and leaves the professional unverified")
    void registerDoesNotGrantCredits() {
        when(userRepository.existsByEmail("pro@example.com")).thenReturn(false);
        when(passwordHasher.encode("Secret123!")).thenReturn("hash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User registered = authService.register("pro@example.com", "Secret123!", "Pro", "pro", null, null);

        assertThat(registered.getCreditsBalance()).isZero();
        assertThat(registered.isProVerified()).isFalse();
        assertThat(registered.getRating()).isEqualByComparingTo("0.00");
        assertThat(registered.getTrustScore()).isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("registration enforces password rules even without the web layer")
    void registerRejectsWeakPasswords() {
        assertThatThrownBy(() -> authService.register("pro@example.com", "short1", "Pro", "pro", null, null))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("between 8 and 128");
        assertThatThrownBy(() -> authService.register("pro@example.com", "onlyletters", "Pro", "pro", null, null))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("letter and one digit");
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("failed login increments the counter and eventually applies a lockout")
    void failedLoginRegistersAttempt() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .email("pro@example.com")
                .passwordHash("stored-hash")
                .role("pro")
                .loginAttemptsCount(9)
                .build();
        when(userRepository.findByEmail("pro@example.com")).thenReturn(Optional.of(user));
        when(passwordHasher.matches("wrong", "stored-hash")).thenReturn(false);

        assertThat(authService.authenticate("pro@example.com", "wrong")).isEmpty();

        verify(userRepository).registerFailedLogin(eq(userId), eq(10), any());
    }

    @Test
    @DisplayName("locked accounts cannot authenticate even with the correct password")
    void lockedAccountCannotAuthenticate() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .email("pro@example.com")
                .passwordHash("stored-hash")
                .role("pro")
                .lockoutUntil(CLOCK.instant().atZone(ZoneOffset.UTC).toLocalDateTime().plusMinutes(10))
                .build();
        when(userRepository.findByEmail("pro@example.com")).thenReturn(Optional.of(user));

        assertThat(authService.authenticate("pro@example.com", "Secret123!")).isEmpty();

        verify(passwordHasher, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("unknown accounts still perform a dummy password verification")
    void unknownAccountUsesDummyHash() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThat(authService.authenticate("missing@example.com", "Secret123!")).isEmpty();

        verify(passwordHasher).matches(eq("Secret123!"), anyString());
    }

    @Test
    @DisplayName("an expired lockout is cleared before the next attempt is evaluated")
    void expiredLockoutStartsFreshAttemptWindow() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .email("pro@example.com")
                .passwordHash("stored-hash")
                .role("pro")
                .loginAttemptsCount(10)
                .lockoutUntil(CLOCK.instant().atZone(ZoneOffset.UTC).toLocalDateTime().minusMinutes(1))
                .build();
        when(userRepository.findByEmail("pro@example.com")).thenReturn(Optional.of(user));
        when(passwordHasher.matches("Secret123!", "stored-hash")).thenReturn(true);

        assertThat(authService.authenticate("pro@example.com", "Secret123!")).isPresent();

        verify(userRepository).resetLoginState(userId);
    }

    @Test
    @DisplayName("administrator verification grants credits exactly once and records the grant")
    void verifyProfessionalGrantsCredits() {
        UUID userId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        User unverified = User.builder()
                .id(userId)
                .email("pro@example.com")
                .role("pro")
                .proVerified(false)
                .creditsBalance(0)
                .build();
        User verified = User.builder()
                .id(userId)
                .email("pro@example.com")
                .role("pro")
                .proVerified(true)
                .creditsBalance(30)
                .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(unverified), Optional.of(verified));
        when(creditGrantRepository.sumAmountByUserId(userId)).thenReturn(0L);
        when(userRepository.verifyProAndAddCredits(userId, 30)).thenReturn(true);
        when(creditGrantRepository.save(any(CreditGrant.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User result = authService.verifyProfessionalAndGrantCredits(userId, 30, adminId);

        assertThat(result.isProVerified()).isTrue();
        assertThat(result.getCreditsBalance()).isEqualTo(30);

        ArgumentCaptor<CreditGrant> captor = ArgumentCaptor.forClass(CreditGrant.class);
        verify(creditGrantRepository).save(captor.capture());
        assertThat(captor.getValue().userId()).isEqualTo(userId);
        assertThat(captor.getValue().adminId()).isEqualTo(adminId);
        assertThat(captor.getValue().amount()).isEqualTo(30);
    }

    @Test
    @DisplayName("an already verified professional cannot be granted welcome credits twice")
    void verifyProfessionalRejectsSecondGrant() {
        UUID userId = UUID.randomUUID();
        User verified = User.builder()
                .id(userId)
                .email("pro@example.com")
                .role("pro")
                .proVerified(true)
                .creditsBalance(30)
                .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(verified));

        assertThatThrownBy(() -> authService.verifyProfessionalAndGrantCredits(userId, 30, UUID.randomUUID()))
                .isInstanceOf(ConflictException.class);

        verify(creditGrantRepository, never()).save(any());
        verify(userRepository, never()).verifyProAndAddCredits(any(), anyInt());
    }
}
