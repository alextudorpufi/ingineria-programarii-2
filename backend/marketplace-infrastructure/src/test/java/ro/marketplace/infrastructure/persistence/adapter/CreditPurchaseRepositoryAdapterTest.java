package ro.marketplace.infrastructure.persistence.adapter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ro.marketplace.infrastructure.persistence.entity.StripeWebhookEventEntity;
import ro.marketplace.infrastructure.persistence.repository.SpringDataCreditPurchaseRepository;
import ro.marketplace.infrastructure.persistence.repository.SpringDataStripeWebhookEventRepository;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CreditPurchaseRepositoryAdapterTest {

    @Mock
    private SpringDataCreditPurchaseRepository purchaseRepository;

    @Mock
    private SpringDataStripeWebhookEventRepository webhookEventRepository;

    private CreditPurchaseRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(Instant.parse("2026-01-01T10:00:00Z"), ZoneOffset.UTC);
        adapter = new CreditPurchaseRepositoryAdapter(
                purchaseRepository, webhookEventRepository, fixedClock);
    }

    @Test
    @DisplayName("webhook receipt time comes from the injected clock")
    void recordWebhookEventUsesClock() {
        adapter.recordWebhookEvent("evt_1", "checkout.session.completed");

        ArgumentCaptor<StripeWebhookEventEntity> captor =
                ArgumentCaptor.forClass(StripeWebhookEventEntity.class);
        verify(webhookEventRepository).save(captor.capture());
        StripeWebhookEventEntity event = captor.getValue();
        assertThat(event.getEventId()).isEqualTo("evt_1");
        assertThat(event.getEventType()).isEqualTo("checkout.session.completed");
        assertThat(event.getReceivedAt()).isEqualTo(LocalDateTime.of(2026, 1, 1, 10, 0));
    }
}
