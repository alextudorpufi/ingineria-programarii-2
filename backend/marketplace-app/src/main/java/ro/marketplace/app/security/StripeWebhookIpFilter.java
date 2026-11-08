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
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Objects;

/**
 * Rejects webhook calls that do not originate from Stripe's published ranges.
 *
 * <p>The check runs before Spring Security. When the service sits behind a
 * trusted load balancer, configure Spring's forwarded-header support so
 * {@link HttpServletRequest#getRemoteAddr()} reflects the real client.</p>
 */
public class StripeWebhookIpFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(StripeWebhookIpFilter.class);

    private final List<CidrBlock> allowedBlocks;
    private final ObjectMapper objectMapper;

    public StripeWebhookIpFilter(List<CidrBlock> allowedBlocks, ObjectMapper objectMapper) {
        this.allowedBlocks = List.copyOf(Objects.requireNonNull(allowedBlocks, "allowedBlocks must not be null"));
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper must not be null");
        if (this.allowedBlocks.isEmpty()) {
            throw new IllegalArgumentException("At least one allowed Stripe CIDR block is required");
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String remoteAddress = request.getRemoteAddr();
        if (isAllowed(remoteAddress)) {
            chain.doFilter(request, response);
            return;
        }

        log.warn("Rejected Stripe webhook from unauthorized source ip={} forwardedForPresent={}",
                remoteAddress, request.getHeader("X-Forwarded-For") != null);
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN,
                "Webhook calls must originate from Stripe.");
        problem.setTitle("Forbidden");
        problem.setType(URI.create("https://api.marketplace.ro/errors/webhook-source"));
        objectMapper.writeValue(response.getOutputStream(), problem);
    }

    private boolean isAllowed(String remoteAddress) {
        if (remoteAddress == null || remoteAddress.isBlank()) {
            return false;
        }
        try {
            InetAddress address = InetAddress.getByName(remoteAddress);
            return allowedBlocks.stream().anyMatch(block -> block.matches(address));
        } catch (UnknownHostException ex) {
            log.warn("Could not resolve webhook source address: {}", remoteAddress);
            return false;
        }
    }
}
