package ro.marketplace.app.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.Map;

/**
 * Wires the authentication rate limits. Limits are configurable so tests and
 * small deployments can tune them without code changes.
 */
@Configuration
public class RateLimitConfig {

    private static final String LOGIN_PATH = "/api/auth/login";
    private static final String REGISTER_PATH = "/api/auth/register";
    private static final String REFRESH_PATH = "/api/auth/refresh";

    @Bean
    public FilterRegistrationBean<RateLimitFilter> rateLimitFilter(
            @Value("${app.security.rate-limit.enabled:true}") boolean enabled,
            @Value("${app.security.rate-limit.login.capacity:5}") int loginCapacity,
            @Value("${app.security.rate-limit.login.window:PT1H}") Duration loginWindow,
            @Value("${app.security.rate-limit.register.capacity:3}") int registerCapacity,
            @Value("${app.security.rate-limit.register.window:P1D}") Duration registerWindow,
            @Value("${app.security.rate-limit.refresh.capacity:10}") int refreshCapacity,
            @Value("${app.security.rate-limit.refresh.window:PT5M}") Duration refreshWindow,
            ObjectMapper objectMapper) {
        Map<String, InMemoryRateLimiter.Rule> rules = Map.of(
                LOGIN_PATH, new InMemoryRateLimiter.Rule(loginCapacity, loginWindow),
                REGISTER_PATH, new InMemoryRateLimiter.Rule(registerCapacity, registerWindow),
                REFRESH_PATH, new InMemoryRateLimiter.Rule(refreshCapacity, refreshWindow)
        );
        RateLimitFilter filter = new RateLimitFilter(enabled, rules, new InMemoryRateLimiter(), objectMapper);

        FilterRegistrationBean<RateLimitFilter> registration = new FilterRegistrationBean<>(filter);
        // Run early, before the security filter chain, so a flood is rejected
        // without touching authentication or the database.
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
        registration.addUrlPatterns("/*");
        return registration;
    }
}
