package ro.marketplace.infrastructure.persistence.adapter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ro.marketplace.domain.port.CurrentActorProvider;
import ro.marketplace.infrastructure.persistence.entity.UserEntity;
import ro.marketplace.infrastructure.persistence.repository.SpringDataUserRepository;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserRepositoryAdapterTest {

    private static final LocalDateTime UPDATED_AT = LocalDateTime.of(2026, 1, 1, 10, 0);
    private static final Clock CLOCK =
            Clock.fixed(Instant.parse("2026-01-01T10:00:00Z"), ZoneOffset.UTC);

    @Mock
    private SpringDataUserRepository repository;

    @Mock
    private CurrentActorProvider currentActorProvider;

    private UserRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new UserRepositoryAdapter(repository, currentActorProvider, CLOCK);
    }

    @Test
    @DisplayName("verification uses the domain role value and records the actor")
    void verifyProUsesDomainRole() {
        UUID userId = UUID.randomUUID();
        when(currentActorProvider.currentActor()).thenReturn(Optional.of("admin@example.com"));
        when(repository.verifyPro(userId, "pro", "admin@example.com", UPDATED_AT)).thenReturn(1);

        assertThat(adapter.verifyPro(userId)).isTrue();

        verify(repository).verifyPro(userId, "pro", "admin@example.com", UPDATED_AT);
    }

    @Test
    @DisplayName("system-triggered lockout still gets an audit actor")
    void registerFailedLoginFallsBackToSystem() {
        UUID userId = UUID.randomUUID();
        LocalDateTime lockoutUntil = LocalDateTime.of(2026, 1, 1, 10, 0);
        when(currentActorProvider.currentActor()).thenReturn(Optional.empty());
        when(repository.registerFailedLogin(userId, 5, lockoutUntil, "system", UPDATED_AT)).thenReturn(1);

        assertThat(adapter.registerFailedLogin(userId, 5, lockoutUntil)).isTrue();

        verify(repository).registerFailedLogin(userId, 5, lockoutUntil, "system", UPDATED_AT);
    }

    @Test
    @DisplayName("adding credits uses overflow-checked domain arithmetic")
    void addCreditsUpdatesBalance() {
        UUID userId = UUID.randomUUID();
        UserEntity user = UserEntity.builder().id(userId).creditsBalance(40).build();
        when(repository.findByIdForUpdate(userId)).thenReturn(Optional.of(user));

        OptionalInt balance = adapter.addCredits(userId, 25);

        assertThat(balance).hasValue(65);
        assertThat(user.getCreditsBalance()).isEqualTo(65);
        verify(repository).flush();
    }

    @Test
    @DisplayName("non-positive credit amounts are rejected before touching the balance")
    void rejectsNonPositiveAmounts() {
        UUID userId = UUID.randomUUID();

        assertThatThrownBy(() -> adapter.addCredits(userId, 0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> adapter.deductCredits(userId, -5))
                .isInstanceOf(IllegalArgumentException.class);
        verify(repository, never()).findByIdForUpdate(userId);
    }

    @Test
    @DisplayName("deducting more than the balance returns empty and leaves the row intact")
    void deductRejectsInsufficientBalance() {
        UUID userId = UUID.randomUUID();
        UserEntity user = UserEntity.builder().id(userId).creditsBalance(10).build();
        when(repository.findByIdForUpdate(userId)).thenReturn(Optional.of(user));

        OptionalInt balance = adapter.deductCredits(userId, 11);

        assertThat(balance).isEmpty();
        assertThat(user.getCreditsBalance()).isEqualTo(10);
        verify(repository, never()).flush();
    }

    @Test
    @DisplayName("deducting an affordable amount returns the remaining balance")
    void deductUpdatesBalance() {
        UUID userId = UUID.randomUUID();
        UserEntity user = UserEntity.builder().id(userId).creditsBalance(10).build();
        when(repository.findByIdForUpdate(userId)).thenReturn(Optional.of(user));

        OptionalInt balance = adapter.deductCredits(userId, 4);

        assertThat(balance).hasValue(6);
        assertThat(user.getCreditsBalance()).isEqualTo(6);
    }
}
