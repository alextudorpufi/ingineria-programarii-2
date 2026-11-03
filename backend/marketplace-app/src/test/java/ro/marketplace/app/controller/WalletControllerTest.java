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
import ro.marketplace.application.service.WalletService;
import ro.marketplace.domain.model.WalletTransaction;
import ro.marketplace.domain.model.WalletTransactionType;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = WalletController.class)
@Import(SecurityConfig.class)
class WalletControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WalletService walletService;

    @Test
    @DisplayName("GET /api/wallet/{userId}/balance returns the authenticated user's balance")
    void getBalanceReturnsBalance() throws Exception {
        UUID userId = UUID.randomUUID();
        when(walletService.getBalance(userId)).thenReturn(145);

        mockMvc.perform(get("/api/wallet/" + userId + "/balance").with(token(userId, "PRO")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.creditsBalance").value(145));
    }

    @Test
    @DisplayName("GET /api/wallet/{userId}/transactions returns list of transactions")
    void getUserTransactionsReturnsList() throws Exception {
        UUID userId = UUID.randomUUID();
        WalletTransaction tx = WalletTransaction.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .type(WalletTransactionType.LEAD_PURCHASE)
                .amount(-30)
                .description("Achiziție lead #101")
                .balanceAfter(115)
                .createdAt(LocalDateTime.now())
                .build();

        when(walletService.getUserTransactions(userId)).thenReturn(List.of(tx));

        mockMvc.perform(get("/api/wallet/" + userId + "/transactions").with(token(userId, "PRO")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("lead_purchase"))
                .andExpect(jsonPath("$[0].amount").value(-30))
                .andExpect(jsonPath("$[0].balanceAfter").value(115));
    }

    @Test
    @DisplayName("GET /api/wallet/{userId}/balance forbids reading another user's wallet")
    void getBalanceForbidsOtherUsers() throws Exception {
        mockMvc.perform(get("/api/wallet/" + UUID.randomUUID() + "/balance")
                        .with(token(UUID.randomUUID(), "PRO")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/wallet/{userId}/top-up is admin-only")
    void topUpRequiresAdmin() throws Exception {
        UUID userId = UUID.randomUUID();
        mockMvc.perform(post("/api/wallet/" + userId + "/top-up")
                        .with(token(userId, "PRO"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"credits":100}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/wallet/{userId}/top-up credits an account for an admin")
    void topUpAsAdmin() throws Exception {
        UUID userId = UUID.randomUUID();
        WalletTransaction tx = WalletTransaction.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .type(WalletTransactionType.TOP_UP)
                .amount(100)
                .description("Top-up")
                .balanceAfter(200)
                .createdAt(LocalDateTime.now())
                .build();
        when(walletService.topUpCredits(eq(userId), eq(100), any())).thenReturn(tx);

        mockMvc.perform(post("/api/wallet/" + userId + "/top-up")
                        .with(token(UUID.randomUUID(), "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"credits":100}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.creditsBalance").doesNotExist())
                .andExpect(jsonPath("$.balanceAfter").value(200));
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
