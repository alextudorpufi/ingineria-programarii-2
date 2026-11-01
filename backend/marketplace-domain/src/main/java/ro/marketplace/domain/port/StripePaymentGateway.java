package ro.marketplace.domain.port;

import ro.marketplace.domain.model.CreditBundle;
import ro.marketplace.domain.model.CreditPurchase;
import ro.marketplace.domain.model.StripeCheckoutSession;
import ro.marketplace.domain.model.StripeWebhookEvent;

public interface StripePaymentGateway {

    StripeCheckoutSession createCheckoutSession(CreditPurchase purchase,
                                                CreditBundle bundle,
                                                String successUrl,
                                                String cancelUrl);

    StripeWebhookEvent verifyWebhook(String payload, String signature);
}
