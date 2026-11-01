package ro.marketplace.app.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import ro.marketplace.app.security.SecurityConfig;
import ro.marketplace.application.service.CreditPaymentService;
import ro.marketplace.domain.model.CreditPurchase;
import ro.marketplace.domain.model.CreditPurchaseStatus;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {CreditPaymentController.class, StripeWebhookController.class})
@Import(SecurityConfig.class)
class CreditPaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreditPaymentService creditPaymentService;

    @Test
    @DisplayName("credit bundle catalog requires an authenticated professional")
    void bundleCatalogRequiresProfessional() throws Exception {
        mockMvc.perform(get("/api/wallet/bundles").with(token(UUID.randomUUID(), "CLIENT")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("checkout session is created for the authenticated professional")
    void checkoutSessionIsCreated() throws Exception {
        UUID userId = UUID.randomUUID();
        String key = UUID.randomUUID().toString();
        CreditPurchase purchase = new CreditPurchase(UUID.randomUUID(), userId, "starter", 50, 5_000,
                "RON", CreditPurchaseStatus.PENDING, key, "cs_test_123",
                "https://checkout.stripe.test/session", null,
                LocalDateTime.now(), LocalDateTime.now());
        when(creditPaymentService.createCheckout(userId, "starter", key)).thenReturn(purchase);

        mockMvc.perform(post("/api/wallet/purchases")
                        .with(token(userId, "PRO"))
                        .header("Idempotency-Key", key)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bundleId\":\"starter\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.purchaseId").value(purchase.id().toString()))
                .andExpect(jsonPath("$.checkoutUrl").value("https://checkout.stripe.test/session"));
    }

    @Test
    @DisplayName("Stripe webhook is publicly reachable and delegates signature verification")
    void webhookIsPublic() throws Exception {
        mockMvc.perform(post("/api/payments/stripe/webhook")
                        .header("Stripe-Signature", "signature")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"evt_test\"}"))
                .andExpect(status().isOk());

        verify(creditPaymentService).processWebhook("{\"id\":\"evt_test\"}", "signature");
    }

    @Test
    @DisplayName("checkout CORS preflight allows the idempotency header")
    void checkoutPreflightAllowsIdempotencyHeader() throws Exception {
        mockMvc.perform(options("/api/wallet/purchases")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "authorization,content-type,idempotency-key"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .header().string("Access-Control-Allow-Headers",
                                org.hamcrest.Matchers.containsStringIgnoringCase("idempotency-key")));
    }

    @Test
    @DisplayName("purchase status is scoped to the authenticated professional")
    void purchaseStatusUsesAuthenticatedOwner() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID purchaseId = UUID.randomUUID();
        CreditPurchase purchase = new CreditPurchase(purchaseId, userId, "starter", 50, 5_000,
                "RON", CreditPurchaseStatus.PAID, UUID.randomUUID().toString(), "cs_test_123",
                "https://checkout.stripe.test/session", UUID.randomUUID(),
                LocalDateTime.now(), LocalDateTime.now());
        when(creditPaymentService.getPurchase(purchaseId, userId)).thenReturn(purchase);

        mockMvc.perform(get("/api/wallet/purchases/" + purchaseId).with(token(userId, "PRO")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("paid"))
                .andExpect(jsonPath("$.credits").value(50));

        verify(creditPaymentService).getPurchase(purchaseId, userId);
    }

    private static RequestPostProcessor token(UUID userId, String... roles) {
        List<GrantedAuthority> authorities = Arrays.stream(roles)
                .map(role -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + role))
                .toList();
        return jwt().jwt(jwt -> jwt
                        .subject(userId.toString())
                        .claim("email", "user@marketplace.ro")
                        .claim("roles", List.of(roles)))
                .authorities(authorities.toArray(GrantedAuthority[]::new));
    }
}
