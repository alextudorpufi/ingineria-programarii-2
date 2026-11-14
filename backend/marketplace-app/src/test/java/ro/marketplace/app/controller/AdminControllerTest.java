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
import ro.marketplace.application.service.AuditService;
import ro.marketplace.application.service.AuthService;
import ro.marketplace.domain.model.User;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminController.class)
@Import(SecurityConfig.class)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private AuditService auditService;

    @Test
    @DisplayName("POST /api/admin/verify/{id} is forbidden for non-administrators")
    void verifyRequiresAdmin() throws Exception {
        mockMvc.perform(post("/api/admin/verify/" + UUID.randomUUID())
                        .with(token(UUID.randomUUID(), "PRO"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"credits\":30}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/admin/verify/{id} grants credits through the auth service")
    void verifyDelegatesToService() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        User verified = User.builder()
                .id(userId)
                .email("pro@example.com")
                .role("pro")
                .name("Pro")
                .proVerified(true)
                .creditsBalance(30)
                .rating(BigDecimal.valueOf(5))
                .trustScore(BigDecimal.valueOf(100))
                .build();
        when(authService.verifyProfessionalAndGrantCredits(userId, 30, adminId)).thenReturn(verified);

        mockMvc.perform(post("/api/admin/verify/" + userId)
                        .with(token(adminId, "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"credits\":30}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.creditsBalance").value(30));

        verify(authService).verifyProfessionalAndGrantCredits(eq(userId), eq(30), eq(adminId));
    }

    private static RequestPostProcessor token(UUID userId, String... roles) {
        List<GrantedAuthority> authorities = Arrays.stream(roles)
                .map(role -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + role))
                .toList();
        return jwt().jwt(jwt -> jwt
                        .subject(userId.toString())
                        .claim("email", "admin@marketplace.ro")
                        .claim("roles", List.of(roles)))
                .authorities(authorities.toArray(GrantedAuthority[]::new));
    }
}
