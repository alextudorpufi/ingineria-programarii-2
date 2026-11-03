package ro.marketplace.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ro.marketplace.domain.exception.ConflictException;
import ro.marketplace.domain.model.CreditPurchase;
import ro.marketplace.domain.model.CreditPurchaseStatus;
import ro.marketplace.domain.model.StripeWebhookEvent;
import ro.marketplace.domain.model.StripeCheckoutSession;
import ro.marketplace.domain.model.WalletTransaction;
import ro.marketplace.domain.port.CreditPurchaseRepository;
import ro.marketplace.domain.port.StripePaymentGateway;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreditPaymentServiceTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-01-01T10:00:00Z"), ZoneOffset.UTC);

    @Mock
    private CreditPurchaseRepository purchaseRepository;

    @Mock
    private StripePaymentGateway paymentGateway;

    @Mock
    private WalletService walletService;

    private CreditPaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new CreditPaymentService(purchaseRepository, paymentGateway, walletService,
                new DirectUnitOfWork(), CLOCK, "http://localhost:3000/");
    }

    @Test
    @DisplayName("checkout uses the server catalog and reuses the idempotent session")
    void createCheckoutUsesServerBundleAndReusesSession() {
        UUID userId = UUID.randomUUID();
        String idempotencyKey = UUID.randomUUID().toString();
        AtomicReference<CreditPurchase> storedPurchase = new AtomicReference<>();
        when(purchaseRepository.findByUserIdAndIdempotencyKey(userId, idempotencyKey))
                .thenAnswer(invocation -> Optional.ofNullable(storedPurchase.get()));
        when(purchaseRepository.save(any(CreditPurchase.class))).thenAnswer(invocation -> {
            CreditPurchase purchase = invocation.getArgument(0);
            storedPurchase.set(purchase);
            return purchase;
        });
        when(paymentGateway.createCheckoutSession(any(), any(), any(), any()))
                .thenReturn(new StripeCheckoutSession("cs_test_123", "https://checkout.stripe.test/session"));

        CreditPurchase created = paymentService.createCheckout(userId, "professional", idempotencyKey);
        CreditPurchase retried = paymentService.createCheckout(userId, "professional", idempotencyKey);

        assertThat(created.credits()).isEqualTo(170);
        assertThat(created.amountMinor()).isEqualTo(15_000);
        assertThat(retried.checkoutUrl()).isEqualTo("https://checkout.stripe.test/session");
        verify(paymentGateway).createCheckoutSession(any(), any(), any(), any());
    }

    @Test
    @DisplayName("one verified Stripe event grants the purchase credits once")
    void processWebhookGrantsCreditsOnlyOnce() {
        UUID purchaseId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        CreditPurchase purchase = purchase(purchaseId, userId);
        WalletTransaction transaction = WalletTransaction.builder().id(UUID.randomUUID()).build();
        Set<String> processedEvents = new HashSet<>();
        AtomicReference<CreditPurchase> storedPurchase = new AtomicReference<>(purchase);
        when(paymentGateway.verifyWebhook("signed payload", "valid signature"))
                .thenReturn(new StripeWebhookEvent("evt_123", "checkout.session.completed", "cs_test_123",
                        purchaseId.toString(), 15_000L, "ron", "paid"));
        when(purchaseRepository.hasProcessedWebhookEvent("evt_123")).thenAnswer(invocation ->
                processedEvents.contains(invocation.getArgument(0)));
        org.mockito.Mockito.doAnswer(invocation -> {
            processedEvents.add(invocation.getArgument(0));
            return null;
        }).when(purchaseRepository).recordWebhookEvent("evt_123", "checkout.session.completed");
        when(purchaseRepository.findByStripeSessionIdForUpdate("cs_test_123"))
                .thenAnswer(invocation -> Optional.of(storedPurchase.get()));
        when(purchaseRepository.save(any(CreditPurchase.class))).thenAnswer(invocation -> {
            CreditPurchase updated = invocation.getArgument(0);
            storedPurchase.set(updated);
            return updated;
        });
        when(walletService.topUpCredits(userId, 170, "Stripe credit purchase professional")).thenReturn(transaction);

        paymentService.processWebhook("signed payload", "valid signature");
        paymentService.processWebhook("signed payload", "valid signature");

        assertThat(storedPurchase.get().status()).isEqualTo(CreditPurchaseStatus.PAID);
        assertThat(storedPurchase.get().walletTransactionId()).isEqualTo(transaction.getId());
        verify(walletService).topUpCredits(userId, 170, "Stripe credit purchase professional");
    }

    @Test
    @DisplayName("amount mismatch fails the purchase without granting credits")
    void processWebhookRejectsAmountMismatch() {
        UUID purchaseId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        CreditPurchase purchase = purchase(purchaseId, userId);
        AtomicReference<CreditPurchase> storedPurchase = new AtomicReference<>(purchase);
        when(paymentGateway.verifyWebhook("signed payload", "valid signature"))
                .thenReturn(new StripeWebhookEvent("evt_456", "checkout.session.completed", "cs_test_123",
                        purchaseId.toString(), 1L, "ron", "paid"));
        when(purchaseRepository.hasProcessedWebhookEvent("evt_456")).thenReturn(false);
        when(purchaseRepository.findByStripeSessionIdForUpdate("cs_test_123"))
                .thenReturn(Optional.of(purchase));
        when(purchaseRepository.save(any(CreditPurchase.class))).thenAnswer(invocation -> {
            CreditPurchase updated = invocation.getArgument(0);
            storedPurchase.set(updated);
            return updated;
        });

        paymentService.processWebhook("signed payload", "valid signature");

        assertThat(storedPurchase.get().status()).isEqualTo(CreditPurchaseStatus.FAILED);
        verify(walletService, never()).topUpCredits(any(), org.mockito.ArgumentMatchers.anyInt(), any());
    }

    @Test
    @DisplayName("idempotency key cannot be reused for another bundle")
    void createCheckoutRejectsDifferentBundleForSameKey() {
        UUID userId = UUID.randomUUID();
        String key = UUID.randomUUID().toString();
        when(purchaseRepository.findByUserIdAndIdempotencyKey(userId, key))
                .thenReturn(Optional.of(purchase(UUID.randomUUID(), userId)));

        assertThatThrownBy(() -> paymentService.createCheckout(userId, "starter", key))
                .isInstanceOf(ConflictException.class);
        verify(paymentGateway, never()).createCheckoutSession(any(), any(), any(), any());
    }

    private CreditPurchase purchase(UUID purchaseId, UUID userId) {
        return new CreditPurchase(purchaseId, userId, "professional", 170, 15_000, "RON",
                CreditPurchaseStatus.PENDING, UUID.randomUUID().toString(), "cs_test_123",
                "https://checkout.stripe.test/session", null,
                java.time.LocalDateTime.now(CLOCK), java.time.LocalDateTime.now(CLOCK));
    }
}
