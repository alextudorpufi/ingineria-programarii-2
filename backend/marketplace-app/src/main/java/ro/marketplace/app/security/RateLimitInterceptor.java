package ro.marketplace.app.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.web.servlet.HandlerInterceptor;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Objects;

/**
 * Enforces per-IP request budgets on public authentication endpoints.
 *
 * <p>Uses {@link HttpServletRequest#getRemoteAddr()} rather than a forwarded
 * header, because an unvalidated {@code X-Forwarded-For} would let an attacker
 * rotate identities for free. Behind a trusted load balancer, enable Spring's
 * forwarded-header support instead.</p>
 */
public class RateLimitInterceptor implements HandlerInterceptor {

    private final boolean enabled;
    private final Map<String, InMemoryRateLimiter.Rule> rules;
    private final InMemoryRateLimiter limiter;
    private final ObjectMapper objectMapper;

    public RateLimitInterceptor(boolean enabled,
                                Map<String, InMemoryRateLimiter.Rule> rules,
                                InMemoryRateLimiter limiter,
                                ObjectMapper objectMapper) {
        this.enabled = enabled;
        this.rules = Objects.requireNonNull(rules, "rules must not be null");
        this.limiter = Objects.requireNonNull(limiter, "limiter must not be null");
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper must not be null");
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        if (!enabled) {
            return true;
        }
        InMemoryRateLimiter.Rule rule = rules.get(request.getRequestURI());
        if (rule == null) {
            return true;
        }

        String key = request.getRemoteAddr() + "|" + request.getRequestURI();
        InMemoryRateLimiter.Decision decision = limiter.tryConsume(key, rule);
        if (decision.allowed()) {
            return true;
        }

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setHeader("Retry-After", Long.toString(decision.retryAfterSeconds()));
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.TOO_MANY_REQUESTS,
                "Too many attempts. Please retry after " + decision.retryAfterSeconds() + " seconds.");
        problem.setTitle("Too Many Requests");
        problem.setType(URI.create("https://api.marketplace.ro/errors/rate-limited"));
        objectMapper.writeValue(response.getOutputStream(), problem);
        return false;
    }
}
