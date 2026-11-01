package ro.marketplace.app.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ro.marketplace.application.service.CreditPaymentService;

import java.util.Objects;

@RestController
@RequestMapping("/api/payments/stripe")
public class StripeWebhookController {

    private final CreditPaymentService creditPaymentService;

    public StripeWebhookController(CreditPaymentService creditPaymentService) {
        this.creditPaymentService = Objects.requireNonNull(creditPaymentService, "creditPaymentService must not be null");
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> receiveWebhook(@RequestBody String payload,
                                               @RequestHeader("Stripe-Signature") String signature) {
        creditPaymentService.processWebhook(payload, signature);
        return ResponseEntity.ok().build();
    }
}
