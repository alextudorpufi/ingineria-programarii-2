package ro.marketplace.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ro.marketplace.infrastructure.persistence.entity.StripeWebhookEventEntity;

@Repository
public interface SpringDataStripeWebhookEventRepository extends JpaRepository<StripeWebhookEventEntity, String> {
}
