package ro.marketplace.infrastructure.persistence.adapter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ro.marketplace.domain.port.CurrentActorProvider;
import ro.marketplace.infrastructure.persistence.repository.SpringDataUserRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserRepositoryAdapterTest {

    @Mock
    private SpringDataUserRepository repository;

    @Mock
    private CurrentActorProvider currentActorProvider;

    private UserRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new UserRepositoryAdapter(repository, currentActorProvider);
    }

    @Test
    @DisplayName("verification uses the domain role value and records the actor")
    void verifyProUsesDomainRole() {
        UUID userId = UUID.randomUUID();
        when(currentActorProvider.currentActor()).thenReturn(Optional.of("admin@example.com"));
        when(repository.verifyPro(userId, "pro", "admin@example.com")).thenReturn(1);

        assertThat(adapter.verifyPro(userId)).isTrue();

        verify(repository).verifyPro(userId, "pro", "admin@example.com");
    }

    @Test
    @DisplayName("system-triggered lockout still gets an audit actor")
    void registerFailedLoginFallsBackToSystem() {
        UUID userId = UUID.randomUUID();
        LocalDateTime lockoutUntil = LocalDateTime.of(2026, 1, 1, 10, 0);
        when(currentActorProvider.currentActor()).thenReturn(Optional.empty());
        when(repository.registerFailedLogin(userId, 5, lockoutUntil, "system")).thenReturn(1);

        assertThat(adapter.registerFailedLogin(userId, 5, lockoutUntil)).isTrue();

        verify(repository).registerFailedLogin(userId, 5, lockoutUntil, "system");
    }
}
