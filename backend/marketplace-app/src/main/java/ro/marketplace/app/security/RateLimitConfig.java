package ro.marketplace.app.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Wires the authentication rate limits. Limits are configurable so tests and
 * small deployments can tune them without code changes.
 */
@Configuration
public class RateLimitConfig implements WebMvcConfigurer {

    private static final String LOGIN_PATH = "/api/auth/login";
    private static final String REGISTER_PATH = "/api/auth/register";
    private static final String REFRESH_PATH = "/api/auth/refresh";

    private final Map<String, InMemoryRateLimiter.Rule> rules;
    private final RateLimitInterceptor interceptor;

    public RateLimitConfig(@Value("${app.security.rate-limit.enabled:true}") boolean enabled,
                           @Value("${app.security.rate-limit.login.capacity:5}") int loginCapacity,
                           @Value("${app.security.rate-limit.login.window:PT1H}") Duration loginWindow,
                           @Value("${app.security.rate-limit.register.capacity:3}") int registerCapacity,
                           @Value("${app.security.rate-limit.register.window:P1D}") Duration registerWindow,
                           @Value("${app.security.rate-limit.refresh.capacity:10}") int refreshCapacity,
                           @Value("${app.security.rate-limit.refresh.window:PT5M}") Duration refreshWindow,
                           ObjectMapper objectMapper) {
        this.rules = Map.of(
                LOGIN_PATH, new InMemoryRateLimiter.Rule(loginCapacity, loginWindow),
                REGISTER_PATH, new InMemoryRateLimiter.Rule(registerCapacity, registerWindow),
                REFRESH_PATH, new InMemoryRateLimiter.Rule(refreshCapacity, refreshWindow)
        );
        this.interceptor = new RateLimitInterceptor(enabled, rules, new InMemoryRateLimiter(), objectMapper);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(interceptor).addPathPatterns(List.copyOf(rules.keySet()));
    }
}
