package ro.marketplace.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ro.marketplace.domain.exception.ConflictException;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CreditPurchaseTest {

    private static CreditPurchase purchase(CreditPurchaseStatus status) {
        return new CreditPurchase(UUID.randomUUID(), UUID.randomUUID(), "starter", 50, 5_000,
                "RON", status, "idem", null, null, null,
                LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    @DisplayName("a pending purchase can be marked paid once")
    void markPaid() {
        UUID transactionId = UUID.randomUUID();

        CreditPurchase paid = purchase(CreditPurchaseStatus.PENDING)
                .markPaid(transactionId, LocalDateTime.now());

        assertThat(paid.status()).isEqualTo(CreditPurchaseStatus.PAID);
        assertThat(paid.walletTransactionId()).isEqualTo(transactionId);
    }

    @Test
    @DisplayName("a pending purchase can fail or expire")
    void markFailedAndExpired() {
        assertThat(purchase(CreditPurchaseStatus.PENDING).markFailed(LocalDateTime.now()).status())
                .isEqualTo(CreditPurchaseStatus.FAILED);
        assertThat(purchase(CreditPurchaseStatus.PENDING).markExpired(LocalDateTime.now()).status())
                .isEqualTo(CreditPurchaseStatus.EXPIRED);
    }

    @Test
    @DisplayName("terminal purchases cannot be resurrected")
    void terminalStatusIsImmutable() {
        assertThatThrownBy(() -> purchase(CreditPurchaseStatus.FAILED)
                .markPaid(UUID.randomUUID(), LocalDateTime.now()))
                .isInstanceOf(ConflictException.class);
        assertThatThrownBy(() -> purchase(CreditPurchaseStatus.PAID)
                .markFailed(LocalDateTime.now()))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("a checkout session can only be attached while pending")
    void checkoutSessionRequiresPendingStatus() {
        assertThat(purchase(CreditPurchaseStatus.PENDING)
                .withCheckoutSession("cs", "https://checkout", LocalDateTime.now()).stripeSessionId())
                .isEqualTo("cs");
        assertThatThrownBy(() -> purchase(CreditPurchaseStatus.PAID)
                .withCheckoutSession("cs", "https://checkout", LocalDateTime.now()))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("credits and amount must be positive")
    void rejectsEmptyPurchases() {
        assertThatThrownBy(() -> new CreditPurchase(UUID.randomUUID(), UUID.randomUUID(), "starter",
                0, 5_000, "RON", CreditPurchaseStatus.PENDING, "idem", null, null, null,
                LocalDateTime.now(), LocalDateTime.now()))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new CreditPurchase(UUID.randomUUID(), UUID.randomUUID(), "starter",
                50, 0, "RON", CreditPurchaseStatus.PENDING, "idem", null, null, null,
                LocalDateTime.now(), LocalDateTime.now()))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
