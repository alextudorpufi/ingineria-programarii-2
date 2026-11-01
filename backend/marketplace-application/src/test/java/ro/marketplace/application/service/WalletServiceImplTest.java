package ro.marketplace.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ro.marketplace.domain.exception.ConflictException;
import ro.marketplace.domain.model.AuditAction;
import ro.marketplace.domain.model.User;
import ro.marketplace.domain.model.WalletTransaction;
import ro.marketplace.domain.model.WalletTransactionType;
import ro.marketplace.domain.port.UserRepository;
import ro.marketplace.domain.port.WalletTransactionRepository;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WalletServiceImplTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-01-01T10:00:00Z"), ZoneOffset.UTC);

    @Mock
    private UserRepository userRepository;

    @Mock
    private WalletTransactionRepository walletTransactionRepository;

    @Mock
    private AuditService auditService;

    private WalletServiceImpl walletService;

    @BeforeEach
    void setUp() {
        walletService = new WalletServiceImpl(userRepository, walletTransactionRepository, auditService,
                new DirectUnitOfWork(), CLOCK);
    }

    @Test
    @DisplayName("topUpCredits atomically adds credits and creates a ledger entry")
    void topUpCredits_Success() {
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).email("pro@test.ro").creditsBalance(50).build();
        User updated = User.builder().id(userId).email("pro@test.ro").creditsBalance(150).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user), Optional.of(updated));
        when(userRepository.addCredits(userId, 100)).thenReturn(true);
        when(walletTransactionRepository.save(any(WalletTransaction.class))).thenAnswer(inv -> inv.getArgument(0));

        WalletTransaction tx = walletService.topUpCredits(userId, 100, "Reîncărcare pachet");

        assertThat(tx.getAmount()).isEqualTo(100);
        assertThat(tx.getBalanceAfter()).isEqualTo(150);
        assertThat(tx.getType()).isEqualTo(WalletTransactionType.TOP_UP);
        verify(auditService).recordEvent(eq("User"), eq(userId.toString()), eq(AuditAction.CREDIT_TOP_UP),
                eq("pro@test.ro"), any());
    }

    @Test
    @DisplayName("deductCredits uses the atomic update and records the new balance")
    void deductCredits_Success() {
        UUID userId = UUID.randomUUID();
        UUID leadId = UUID.randomUUID();
        User user = User.builder().id(userId).email("pro@test.ro").creditsBalance(50).build();
        User updated = User.builder().id(userId).email("pro@test.ro").creditsBalance(20).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user), Optional.of(updated));
        when(userRepository.deductCredits(userId, 30)).thenReturn(true);
        when(walletTransactionRepository.save(any(WalletTransaction.class))).thenAnswer(inv -> inv.getArgument(0));

        WalletTransaction tx = walletService.deductCredits(userId, 30, "Achiziție lead", leadId);

        assertThat(tx.getAmount()).isEqualTo(-30);
        assertThat(tx.getBalanceAfter()).isEqualTo(20);
        assertThat(tx.getType()).isEqualTo(WalletTransactionType.LEAD_PURCHASE);
        verify(auditService).recordEvent(eq("User"), eq(userId.toString()), eq(AuditAction.CREDIT_DEDUCTION),
                eq("pro@test.ro"), any());
    }

    @Test
    @DisplayName("deductCredits throws ConflictException when the atomic update refuses")
    void deductCredits_InsufficientBalance_ThrowsConflict() {
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).email("pro@test.ro").creditsBalance(10).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.deductCredits(userId, 30)).thenReturn(false);

        assertThatThrownBy(() -> walletService.deductCredits(userId, 30, "Achiziție lead", UUID.randomUUID()))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Insufficient credit balance");
        verify(walletTransactionRepository, never()).save(any());
        verify(auditService, never()).recordEvent(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("refundCredits adds credits and records a refund ledger entry")
    void refundCredits_Success() {
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).email("pro@test.ro").creditsBalance(10).build();
        User updated = User.builder().id(userId).email("pro@test.ro").creditsBalance(40).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user), Optional.of(updated));
        when(userRepository.addCredits(userId, 30)).thenReturn(true);
        when(walletTransactionRepository.save(any(WalletTransaction.class))).thenAnswer(inv -> inv.getArgument(0));

        WalletTransaction tx = walletService.refundCredits(userId, 30, "Rambursare", UUID.randomUUID());

        assertThat(tx.getAmount()).isEqualTo(30);
        assertThat(tx.getType()).isEqualTo(WalletTransactionType.REFUND);
        verify(auditService).recordEvent(eq("User"), eq(userId.toString()), eq(AuditAction.REFUND_APPROVED),
                eq("admin"), any());
    }
}
