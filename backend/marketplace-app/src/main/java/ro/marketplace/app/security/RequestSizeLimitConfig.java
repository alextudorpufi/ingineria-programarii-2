package ro.marketplace.app.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import tools.jackson.databind.ObjectMapper;

/**
 * Registers the request body size cap for every endpoint.
 */
@Configuration
public class RequestSizeLimitConfig {

    @Bean
    public FilterRegistrationBean<RequestSizeLimitFilter> requestSizeLimitFilter(
            @Value("${app.security.request-size-limit.max-bytes:2097152}") long maxBytes,
            ObjectMapper objectMapper) {
        FilterRegistrationBean<RequestSizeLimitFilter> registration =
                new FilterRegistrationBean<>(new RequestSizeLimitFilter(maxBytes, objectMapper));
        // The cap runs before the rate limiter: reject oversized payloads before
        // spending any per-IP budget on them.
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 5);
        registration.addUrlPatterns("/*");
        return registration;
    }
}
