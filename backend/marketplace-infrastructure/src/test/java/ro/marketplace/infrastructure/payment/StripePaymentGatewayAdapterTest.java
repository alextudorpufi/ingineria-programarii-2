package ro.marketplace.infrastructure.payment;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ro.marketplace.domain.model.StripeWebhookEvent;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HexFormat;

import static org.assertj.core.api.Assertions.assertThat;

class StripePaymentGatewayAdapterTest {

    private static final String WEBHOOK_SECRET = "whsec_test_secret";

    @Test
    @DisplayName("verified Checkout Session webhook is parsed when the event API version differs from the SDK")
    void verifyWebhookDeserializesSessionAcrossApiVersions() throws Exception {
        String purchaseId = "a18d37ad-f82d-4432-a70f-21ebaa531810";
        String payload = """
                {
                  "id": "evt_test_checkout",
                  "object": "event",
                  "api_version": "2024-06-20",
                  "type": "checkout.session.completed",
                  "data": {
                    "object": {
                      "id": "cs_test_checkout",
                      "object": "checkout.session",
                      "amount_total": 15000,
                      "currency": "ron",
                      "payment_status": "paid",
                      "metadata": {"purchaseId": "%s"}
                    }
                  }
                }
                """.formatted(purchaseId);
        long timestamp = Instant.now().getEpochSecond();
        String signature = signatureFor(timestamp, payload);
        StripePaymentGatewayAdapter gateway = new StripePaymentGatewayAdapter("sk_test_key", WEBHOOK_SECRET);

        StripeWebhookEvent event = gateway.verifyWebhook(payload, signature);

        assertThat(event.id()).isEqualTo("evt_test_checkout");
        assertThat(event.type()).isEqualTo("checkout.session.completed");
        assertThat(event.sessionId()).isEqualTo("cs_test_checkout");
        assertThat(event.purchaseId()).isEqualTo(purchaseId);
        assertThat(event.amountTotal()).isEqualTo(15_000);
        assertThat(event.currency()).isEqualTo("ron");
        assertThat(event.paymentStatus()).isEqualTo("paid");
    }

    private static String signatureFor(long timestamp, String payload) throws Exception {
        String signedPayload = timestamp + "." + payload;
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(WEBHOOK_SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        String digest = HexFormat.of().formatHex(mac.doFinal(signedPayload.getBytes(StandardCharsets.UTF_8)));
        return "t=" + timestamp + ",v1=" + digest;
    }
}
