package ro.marketplace.app.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import ro.marketplace.app.security.SecurityConfig;
import ro.marketplace.application.service.AuditService;
import ro.marketplace.domain.model.AuditAction;
import ro.marketplace.domain.model.AuditLog;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuditController.class)
@Import(SecurityConfig.class)
class AuditControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuditService auditService;

    @Test
    @DisplayName("GET /api/audit/recent returns audit logs for an admin")
    void getRecentAuditLogsReturnsLogs() throws Exception {
        UUID id = UUID.randomUUID();
        AuditLog log = AuditLog.builder()
                .id(id)
                .entityName("Lead")
                .entityId("lead-101")
                .action(AuditAction.CREATE)
                .changedBy("elena@florariapetale.ro")
                .details("Created lead")
                .createdAt(LocalDateTime.now())
                .build();

        when(auditService.getRecentAuditLogs(50)).thenReturn(List.of(log));

        mockMvc.perform(get("/api/audit/recent?limit=50").with(adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id.toString()))
                .andExpect(jsonPath("$[0].entityName").value("Lead"))
                .andExpect(jsonPath("$[0].action").value("CREATE"))
                .andExpect(jsonPath("$[0].changedBy").value("elena@florariapetale.ro"));
    }

    @Test
    @DisplayName("GET /api/audit/recent rejects an unauthenticated request with 401")
    void getRecentAuditLogsRejectsAnonymous() throws Exception {
        mockMvc.perform(get("/api/audit/recent"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("Authentication Required"));
    }

    @Test
    @DisplayName("GET /api/audit/recent rejects a non-admin token")
    void getRecentAuditLogsRejectsNonAdmin() throws Exception {
        mockMvc.perform(get("/api/audit/recent").with(token("CLIENT")))
                .andExpect(status().isForbidden());
    }

    private static RequestPostProcessor adminToken() {
        return token("ADMIN");
    }

    private static RequestPostProcessor token(String... roles) {
        List<GrantedAuthority> authorities = java.util.Arrays.stream(roles)
                .map(role -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + role))
                .toList();
        return jwt().jwt(jwt -> jwt
                        .subject(UUID.randomUUID().toString())
                        .claim("email", "admin@marketplace.ro")
                        .claim("roles", List.of(roles)))
                .authorities(authorities.toArray(GrantedAuthority[]::new));
    }
}
