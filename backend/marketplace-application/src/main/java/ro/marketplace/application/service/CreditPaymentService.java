package ro.marketplace.application.service;

import ro.marketplace.domain.exception.ConflictException;
import ro.marketplace.domain.exception.ResourceNotFoundException;
import ro.marketplace.domain.model.CreditBundle;
import ro.marketplace.domain.model.CreditPurchase;
import ro.marketplace.domain.model.CreditPurchaseStatus;
import ro.marketplace.domain.model.StripeCheckoutSession;
import ro.marketplace.domain.model.StripeWebhookEvent;
import ro.marketplace.domain.model.WalletTransaction;
import ro.marketplace.domain.port.CreditPurchaseRepository;
import ro.marketplace.domain.port.StripePaymentGateway;
import ro.marketplace.domain.port.UnitOfWork;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class CreditPaymentService {

    private static final String CURRENCY = "RON";
    private static final List<CreditBundle> BUNDLES = List.of(
            new CreditBundle("starter", "Pachet Starter", "Testare Rapidă", 50, 0, 5_000, CURRENCY, false),
            new CreditBundle("professional", "Pachet Pro Recomandat", "Cel Mai Popular", 150, 20, 15_000, CURRENCY, true),
            new CreditBundle("studio", "Pachet Studio / Agenție", "Economisești 20%", 400, 80, 35_000, CURRENCY, false)
    );

    private final CreditPurchaseRepository creditPurchaseRepository;
    private final StripePaymentGateway stripePaymentGateway;
    private final WalletService walletService;
    private final UnitOfWork unitOfWork;
    private final Clock clock;
    private final String frontendUrl;

    public CreditPaymentService(CreditPurchaseRepository creditPurchaseRepository,
                                StripePaymentGateway stripePaymentGateway,
                                WalletService walletService,
                                UnitOfWork unitOfWork,
                                Clock clock,
                                String frontendUrl) {
        this.creditPurchaseRepository = Objects.requireNonNull(creditPurchaseRepository, "creditPurchaseRepository must not be null");
        this.stripePaymentGateway = Objects.requireNonNull(stripePaymentGateway, "stripePaymentGateway must not be null");
        this.walletService = Objects.requireNonNull(walletService, "walletService must not be null");
        this.unitOfWork = Objects.requireNonNull(unitOfWork, "unitOfWork must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
        this.frontendUrl = Objects.requireNonNull(frontendUrl, "frontendUrl must not be null").replaceAll("/+$", "");
    }

    public List<CreditBundle> getBundles() {
        return BUNDLES;
    }

    public CreditPurchase createCheckout(UUID userId, String bundleId, String idempotencyKey) {
        Objects.requireNonNull(userId, "userId must not be null");
        if (idempotencyKey == null || idempotencyKey.isBlank() || idempotencyKey.length() > 255) {
            throw new IllegalArgumentException("A valid Idempotency-Key header is required");
        }

        CreditBundle bundle = findBundle(bundleId);
        CreditPurchase purchase = unitOfWork.executeInTransaction(() -> getOrCreatePurchase(userId, bundle, idempotencyKey));
        if (purchase.status() != CreditPurchaseStatus.PENDING) {
            throw new ConflictException("This credit purchase can no longer be checked out");
        }
        if (purchase.checkoutUrl() != null) {
            return purchase;
        }

        StripeCheckoutSession checkoutSession = stripePaymentGateway.createCheckoutSession(
                purchase,
                bundle,
                frontendUrl + "/?walletCheckout=success&purchaseId=" + purchase.id(),
                frontendUrl + "/?walletCheckout=cancelled");
        return unitOfWork.executeInTransaction(() -> {
            CreditPurchase currentPurchase = creditPurchaseRepository
                    .findByUserIdAndIdempotencyKey(userId, idempotencyKey)
                    .orElseThrow(() -> new ResourceNotFoundException("Credit purchase not found"));
            if (currentPurchase.checkoutUrl() != null) {
                return currentPurchase;
            }
            return creditPurchaseRepository.save(currentPurchase.withCheckoutSession(
                    checkoutSession.id(), checkoutSession.url(), LocalDateTime.now(clock)));
        });
    }

    public CreditPurchase getPurchase(UUID purchaseId, UUID userId) {
        return creditPurchaseRepository.findByIdAndUserId(purchaseId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Credit purchase not found"));
    }

    public void processWebhook(String payload, String signature) {
        StripeWebhookEvent event = stripePaymentGateway.verifyWebhook(payload, signature);
        if (!isSupportedEvent(event.type())) {
            return;
        }

        unitOfWork.executeInTransaction(() -> {
            if (creditPurchaseRepository.hasProcessedWebhookEvent(event.id())) {
                return null;
            }
            creditPurchaseRepository.recordWebhookEvent(event.id(), event.type());

            CreditPurchase purchase = creditPurchaseRepository.findByStripeSessionIdForUpdate(event.sessionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Credit purchase for Stripe session not found"));
            if (!purchase.id().toString().equals(event.purchaseId())) {
                markFailed(purchase);
                return null;
            }

            if (event.type().equals("checkout.session.expired")) {
                updatePendingStatus(purchase, CreditPurchaseStatus.EXPIRED);
                return null;
            }
            if (event.type().equals("checkout.session.async_payment_failed")) {
                updatePendingStatus(purchase, CreditPurchaseStatus.FAILED);
                return null;
            }
            if (!"paid".equalsIgnoreCase(event.paymentStatus())) {
                return null;
            }
            if (!matchesExpectedPayment(purchase, event)) {
                markFailed(purchase);
                return null;
            }
            if (purchase.status() != CreditPurchaseStatus.PENDING) {
                return null;
            }

            WalletTransaction transaction = walletService.topUpCredits(
                    purchase.userId(), purchase.credits(), "Stripe credit purchase " + purchase.bundleId());
            creditPurchaseRepository.save(purchase.withStatus(
                    CreditPurchaseStatus.PAID, transaction.getId(), LocalDateTime.now(clock)));
            return null;
        });
    }

    private CreditPurchase getOrCreatePurchase(UUID userId, CreditBundle bundle, String idempotencyKey) {
        return creditPurchaseRepository.findByUserIdAndIdempotencyKey(userId, idempotencyKey)
                .map(existing -> {
                    if (!existing.bundleId().equals(bundle.id())) {
                        throw new ConflictException("Idempotency key was already used for a different bundle");
                    }
                    return existing;
                })
                .orElseGet(() -> creditPurchaseRepository.save(new CreditPurchase(
                        UUID.randomUUID(),
                        userId,
                        bundle.id(),
                        bundle.totalCredits(),
                        bundle.amountMinor(),
                        bundle.currency(),
                        CreditPurchaseStatus.PENDING,
                        idempotencyKey,
                        null,
                        null,
                        null,
                        LocalDateTime.now(clock),
                        LocalDateTime.now(clock))));
    }

    private CreditBundle findBundle(String bundleId) {
        return BUNDLES.stream()
                .filter(bundle -> bundle.id().equals(bundleId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown credit bundle"));
    }

    private boolean matchesExpectedPayment(CreditPurchase purchase, StripeWebhookEvent event) {
        return event.amountTotal() != null
                && event.amountTotal() == purchase.amountMinor()
                && purchase.currency().equalsIgnoreCase(event.currency());
    }

    private void updatePendingStatus(CreditPurchase purchase, CreditPurchaseStatus status) {
        if (purchase.status() == CreditPurchaseStatus.PENDING) {
            creditPurchaseRepository.save(purchase.withStatus(status, null, LocalDateTime.now(clock)));
        }
    }

    private void markFailed(CreditPurchase purchase) {
        updatePendingStatus(purchase, CreditPurchaseStatus.FAILED);
    }

    private boolean isSupportedEvent(String eventType) {
        return eventType != null && switch (eventType) {
            case "checkout.session.completed",
                    "checkout.session.async_payment_succeeded",
                    "checkout.session.async_payment_failed",
                    "checkout.session.expired" -> true;
            default -> false;
        };
    }
}
