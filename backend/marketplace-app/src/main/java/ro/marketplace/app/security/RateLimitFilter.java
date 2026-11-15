package ro.marketplace.app.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.UriUtils;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

/**
 * Enforces per-IP request budgets on public authentication endpoints.
 *
 * <p>Implemented as a servlet filter rather than a {@code HandlerInterceptor}:
 * Spring MVC's interceptor path matching does not run for matrix-parameter
 * variants such as {@code /api/auth/login;x=1}, which would let a caller bypass
 * the limiter. A filter sees every request and canonicalizes the path before
 * looking up a rule.</p>
 *
 * <p>Uses {@link HttpServletRequest#getRemoteAddr()} rather than a forwarded
 * header, because an unvalidated {@code X-Forwarded-For} would let an attacker
 * rotate identities for free. Behind a trusted load balancer, enable Spring's
 * forwarded-header support instead.</p>
 */
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);

    private final boolean enabled;
    private final Map<String, InMemoryRateLimiter.Rule> rules;
    private final InMemoryRateLimiter limiter;
    private final ObjectMapper objectMapper;

    public RateLimitFilter(boolean enabled,
                           Map<String, InMemoryRateLimiter.Rule> rules,
                           InMemoryRateLimiter limiter,
                           ObjectMapper objectMapper) {
        this.enabled = enabled;
        this.rules = Objects.requireNonNull(rules, "rules must not be null");
        this.limiter = Objects.requireNonNull(limiter, "limiter must not be null");
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper must not be null");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (!enabled) {
            chain.doFilter(request, response);
            return;
        }

        String path = canonicalPath(request);
        InMemoryRateLimiter.Rule rule = rules.get(path);
        if (rule == null) {
            chain.doFilter(request, response);
            return;
        }

        log.debug("rate-limit check uri='{}' canonical='{}'", request.getRequestURI(), path);
        String key = request.getRemoteAddr() + "|" + path;
        InMemoryRateLimiter.Decision decision = limiter.tryConsume(key, rule);
        if (decision.allowed()) {
            chain.doFilter(request, response);
            return;
        }

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setHeader("Retry-After", Long.toString(decision.retryAfterSeconds()));
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.TOO_MANY_REQUESTS,
                "Too many attempts. Please retry after " + decision.retryAfterSeconds() + " seconds.");
        problem.setTitle("Too Many Requests");
        problem.setType(URI.create("https://api.marketplace.ro/errors/rate-limited"));
        objectMapper.writeValue(response.getOutputStream(), problem);
    }

    /**
     * Normalizes the path the same way Spring MVC does before handler mapping:
     * remove matrix parameters and percent-decode. Matching the raw request URI
     * would let {@code /api/auth/login;x=1} or an encoded variant reach the
     * controller while bypassing the limiter lookup.
     */
    private static String canonicalPath(HttpServletRequest request) {
        String path = request.getServletPath();
        if (path == null || path.isEmpty()) {
            path = request.getRequestURI();
        }
        // Decode and strip iteratively: an attacker can double-encode the matrix
        // delimiter ({@code %253B}) so a single decode still leaves it intact.
        for (int i = 0; i < 4; i++) {
            path = stripMatrixParameters(path);
            String decoded;
            try {
                decoded = UriUtils.decode(path, StandardCharsets.UTF_8);
            } catch (IllegalArgumentException ex) {
                return path;
            }
            if (decoded.equals(path)) {
                return path;
            }
            path = decoded;
        }
        return stripMatrixParameters(path);
    }

    private static String stripMatrixParameters(String path) {
        int matrix = path.indexOf(';');
        return matrix >= 0 ? path.substring(0, matrix) : path;
    }
}
