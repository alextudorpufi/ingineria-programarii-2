package ro.marketplace.app.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ro.marketplace.application.service.CreditPaymentService;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

@RestController
@RequestMapping("/api/payments/stripe")
public class StripeWebhookController {

    private static final Logger log = LoggerFactory.getLogger(StripeWebhookController.class);
    private static final int MAX_WEBHOOK_BODY_BYTES = 65_536;

    private final CreditPaymentService creditPaymentService;

    public StripeWebhookController(CreditPaymentService creditPaymentService) {
        this.creditPaymentService = Objects.requireNonNull(creditPaymentService, "creditPaymentService must not be null");
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> receiveWebhook(@RequestBody String payload,
                                               @RequestHeader("Stripe-Signature") String signature) {
        int bodyBytes = payload == null ? 0 : payload.getBytes(StandardCharsets.UTF_8).length;
        if (bodyBytes > MAX_WEBHOOK_BODY_BYTES) {
            log.warn("Rejected oversized Stripe webhook: bodyBytes={}", bodyBytes);
            return ResponseEntity.badRequest().build();
        }

        creditPaymentService.processWebhook(payload, signature);
        return ResponseEntity.ok().build();
    }
}
