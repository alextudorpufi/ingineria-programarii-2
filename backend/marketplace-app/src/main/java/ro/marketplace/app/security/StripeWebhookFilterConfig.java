package ro.marketplace.app.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import tools.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.List;

/**
 * Registers the Stripe source-address filter ahead of the security chain so a
 * forged webhook body never reaches signature verification or business logic.
 */
@Configuration
public class StripeWebhookFilterConfig {

    private static final String WEBHOOK_PATH = "/api/payments/stripe/webhook";

    @Bean
    public FilterRegistrationBean<StripeWebhookIpFilter> stripeWebhookIpFilter(
            @Value("${app.stripe.webhook.allowed-cidrs}") String allowedCidrs,
            ObjectMapper objectMapper) {
        List<String> values = Arrays.stream(allowedCidrs.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .toList();
        if (values.isEmpty()) {
            throw new IllegalStateException("app.stripe.webhook.allowed-cidrs must not be empty");
        }
        List<CidrBlock> blocks = values.stream().map(CidrBlock::parse).toList();

        FilterRegistrationBean<StripeWebhookIpFilter> registration =
                new FilterRegistrationBean<>(new StripeWebhookIpFilter(blocks, objectMapper));
        registration.addUrlPatterns(WEBHOOK_PATH);
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        registration.setName("stripeWebhookIpFilter");
        return registration;
    }
}
