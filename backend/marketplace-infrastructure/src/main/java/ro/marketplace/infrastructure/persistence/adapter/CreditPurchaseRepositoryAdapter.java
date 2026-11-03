package ro.marketplace.infrastructure.persistence.adapter;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ro.marketplace.domain.model.CreditPurchase;
import ro.marketplace.domain.model.CreditPurchaseStatus;
import ro.marketplace.domain.port.CreditPurchaseRepository;
import ro.marketplace.infrastructure.persistence.entity.CreditPurchaseEntity;
import ro.marketplace.infrastructure.persistence.entity.StripeWebhookEventEntity;
import ro.marketplace.infrastructure.persistence.repository.SpringDataCreditPurchaseRepository;
import ro.marketplace.infrastructure.persistence.repository.SpringDataStripeWebhookEventRepository;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Repository
public class CreditPurchaseRepositoryAdapter implements CreditPurchaseRepository {

    private final SpringDataCreditPurchaseRepository purchaseRepository;
    private final SpringDataStripeWebhookEventRepository webhookEventRepository;

    public CreditPurchaseRepositoryAdapter(SpringDataCreditPurchaseRepository purchaseRepository,
                                           SpringDataStripeWebhookEventRepository webhookEventRepository) {
        this.purchaseRepository = Objects.requireNonNull(purchaseRepository, "purchaseRepository must not be null");
        this.webhookEventRepository = Objects.requireNonNull(webhookEventRepository, "webhookEventRepository must not be null");
    }

    @Override
    @Transactional
    public CreditPurchase save(CreditPurchase purchase) {
        Objects.requireNonNull(purchase, "purchase must not be null");
        return toDomain(purchaseRepository.save(toEntity(purchase)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CreditPurchase> findByUserIdAndIdempotencyKey(UUID userId, String idempotencyKey) {
        return purchaseRepository.findByUserIdAndIdempotencyKey(userId, idempotencyKey).map(CreditPurchaseRepositoryAdapter::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CreditPurchase> findByIdAndUserId(UUID purchaseId, UUID userId) {
        return purchaseRepository.findByIdAndUserId(purchaseId, userId).map(CreditPurchaseRepositoryAdapter::toDomain);
    }

    @Override
    @Transactional
    public Optional<CreditPurchase> findByStripeSessionIdForUpdate(String sessionId) {
        return purchaseRepository.findByStripeSessionIdForUpdate(sessionId).map(CreditPurchaseRepositoryAdapter::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasProcessedWebhookEvent(String eventId) {
        return webhookEventRepository.existsById(eventId);
    }

    @Override
    @Transactional
    public void recordWebhookEvent(String eventId, String eventType) {
        webhookEventRepository.save(new StripeWebhookEventEntity(eventId, eventType, LocalDateTime.now()));
    }

    private static CreditPurchaseEntity toEntity(CreditPurchase purchase) {
        return CreditPurchaseEntity.builder()
                .id(purchase.id())
                .userId(purchase.userId())
                .bundleId(purchase.bundleId())
                .credits(purchase.credits())
                .amountMinor(purchase.amountMinor())
                .currency(purchase.currency())
                .status(purchase.status().getDbValue())
                .idempotencyKey(purchase.idempotencyKey())
                .stripeSessionId(purchase.stripeSessionId())
                .checkoutUrl(purchase.checkoutUrl())
                .walletTransactionId(purchase.walletTransactionId())
                .createdAt(purchase.createdAt())
                .updatedAt(purchase.updatedAt())
                .build();
    }

    private static CreditPurchase toDomain(CreditPurchaseEntity entity) {
        return new CreditPurchase(
                entity.getId(),
                entity.getUserId(),
                entity.getBundleId(),
                entity.getCredits(),
                entity.getAmountMinor(),
                entity.getCurrency(),
                CreditPurchaseStatus.fromDbValue(entity.getStatus()),
                entity.getIdempotencyKey(),
                entity.getStripeSessionId(),
                entity.getCheckoutUrl(),
                entity.getWalletTransactionId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
