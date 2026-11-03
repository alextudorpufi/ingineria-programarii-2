package ro.marketplace.infrastructure.payment;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.exception.EventDataObjectDeserializationException;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.RequestOptions;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ro.marketplace.application.exception.StripeConfigurationException;
import ro.marketplace.application.exception.StripeProviderException;
import ro.marketplace.application.exception.StripeWebhookVerificationException;
import ro.marketplace.domain.model.CreditBundle;
import ro.marketplace.domain.model.CreditPurchase;
import ro.marketplace.domain.model.StripeCheckoutSession;
import ro.marketplace.domain.model.StripeWebhookEvent;
import ro.marketplace.domain.port.StripePaymentGateway;

import java.util.Locale;
import java.util.Objects;
import java.util.Set;

@Component
public class StripePaymentGatewayAdapter implements StripePaymentGateway {

    private static final Set<String> SUPPORTED_EVENTS = Set.of(
            "checkout.session.completed",
            "checkout.session.async_payment_succeeded",
            "checkout.session.async_payment_failed",
            "checkout.session.expired");

    private final String secretKey;
    private final String webhookSecret;

    public StripePaymentGatewayAdapter(@Value("${app.stripe.secret-key:}") String secretKey,
                                       @Value("${app.stripe.webhook-secret:}") String webhookSecret) {
        this.secretKey = secretKey;
        this.webhookSecret = webhookSecret;
    }

    @Override
    public StripeCheckoutSession createCheckoutSession(CreditPurchase purchase,
                                                       CreditBundle bundle,
                                                       String successUrl,
                                                       String cancelUrl) {
        requireTestSecretKey();
        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                .setClientReferenceId(purchase.id().toString())
                .putMetadata("purchaseId", purchase.id().toString())
                .addLineItem(SessionCreateParams.LineItem.builder()
                        .setQuantity(1L)
                        .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency(bundle.currency().toLowerCase(Locale.ROOT))
                                .setUnitAmount(bundle.amountMinor())
                                .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                        .setName(bundle.name() + " - " + bundle.totalCredits() + " credite")
                                        .build())
                                .build())
                        .build())
                .build();
        RequestOptions requestOptions = RequestOptions.builder()
                .setApiKey(secretKey)
                .setIdempotencyKey("credit-purchase-" + purchase.id())
                .build();
        try {
            Session session = Session.create(params, requestOptions);
            if (session.getUrl() == null || session.getUrl().isBlank()) {
                throw new StripeProviderException("Stripe did not return a Checkout URL", null);
            }
            return new StripeCheckoutSession(session.getId(), session.getUrl());
        } catch (StripeException exception) {
            throw new StripeProviderException("Stripe could not create the Checkout Session", exception);
        }
    }

    @Override
    public StripeWebhookEvent verifyWebhook(String payload, String signature) {
        if (webhookSecret == null || webhookSecret.isBlank()) {
            throw new StripeConfigurationException("Stripe webhook signing secret is not configured");
        }
        try {
            com.stripe.model.Event event = Webhook.constructEvent(payload, signature, webhookSecret);
            if (!SUPPORTED_EVENTS.contains(event.getType())) {
                return new StripeWebhookEvent(event.getId(), event.getType(), null, null, null, null, null);
            }
            StripeObject stripeObject;
            try {
                var deserializer = event.getDataObjectDeserializer();
                var compatibleObject = deserializer.getObject();
                stripeObject = compatibleObject.isPresent()
                        ? compatibleObject.orElseThrow()
                        : deserializer.deserializeUnsafe();
            } catch (EventDataObjectDeserializationException exception) {
                throw new StripeWebhookVerificationException(
                        "Stripe webhook does not contain a valid Checkout Session", exception);
            }
            if (!(stripeObject instanceof Session session)) {
                throw new StripeWebhookVerificationException(
                        "Stripe webhook object is not a Checkout Session", null);
            }
            String purchaseId = session.getMetadata() == null
                    ? null
                    : session.getMetadata().get("purchaseId");
            return new StripeWebhookEvent(event.getId(), event.getType(), session.getId(), purchaseId,
                    session.getAmountTotal(), session.getCurrency(), session.getPaymentStatus());
        } catch (SignatureVerificationException exception) {
            throw new StripeWebhookVerificationException("Stripe webhook signature or payload is invalid", exception);
        }
    }

    private void requireTestSecretKey() {
        if (secretKey == null || secretKey.isBlank()) {
            throw new StripeConfigurationException("Stripe test secret key is not configured");
        }
        if (!secretKey.startsWith("sk_test_")) {
            throw new StripeConfigurationException("Only Stripe test-mode secret keys are accepted");
        }
    }
}
