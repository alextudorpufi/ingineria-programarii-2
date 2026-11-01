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
import ro.marketplace.application.service.DisputeService;
import ro.marketplace.domain.model.Dispute;
import ro.marketplace.domain.model.DisputeStatus;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = DisputeController.class)
@Import(SecurityConfig.class)
class DisputeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DisputeService disputeService;

    @Test
    @DisplayName("GET /api/disputes returns dispute list for an admin")
    void getAllDisputesReturnsList() throws Exception {
        UUID id = UUID.randomUUID();
        Dispute dispute = Dispute.builder()
                .id(id)
                .leadId(UUID.randomUUID())
                .leadTitle("Reparație centrală")
                .proId(UUID.randomUUID())
                .proName("Alexandru Munteanu")
                .reason("unreachable_contact")
                .details("Număr de telefon nefuncțional")
                .creditsRequested(25)
                .status(DisputeStatus.IN_ANALIZA)
                .createdAt(LocalDateTime.now())
                .build();

        when(disputeService.getAllDisputes()).thenReturn(List.of(dispute));

        mockMvc.perform(get("/api/disputes").with(token("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id.toString()))
                .andExpect(jsonPath("$[0].reason").value("unreachable_contact"))
                .andExpect(jsonPath("$[0].creditsRequested").value(25))
                .andExpect(jsonPath("$[0].status").value("in_analiza"));
    }

    @Test
    @DisplayName("POST /api/disputes/{id}/resolve lets an admin resolve a dispute")
    void resolveDisputeAsAdmin() throws Exception {
        UUID id = UUID.randomUUID();
        Dispute resolved = Dispute.builder()
                .id(id)
                .proId(UUID.randomUUID())
                .status(DisputeStatus.APROBAT)
                .creditsRequested(30)
                .createdAt(LocalDateTime.now())
                .build();
        when(disputeService.resolveDispute(eq(id), eq(true), any(), anyString())).thenReturn(resolved);

        mockMvc.perform(post("/api/disputes/" + id + "/resolve")
                        .with(token("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"approved":true,"adminDecisionNote":"approved"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("aprobat"));
    }

    @Test
    @DisplayName("POST /api/disputes/{id}/resolve rejects a non-admin token")
    void resolveDisputeRejectsNonAdmin() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(post("/api/disputes/" + id + "/resolve")
                        .with(token("PRO"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"approved":true}
                                """))
                .andExpect(status().isForbidden());
    }

    private static RequestPostProcessor token(String... roles) {
        List<GrantedAuthority> authorities = Arrays.stream(roles)
                .map(role -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + role))
                .toList();
        return jwt().jwt(jwt -> jwt
                        .subject(UUID.randomUUID().toString())
                        .claim("email", "admin@marketplace.ro")
                        .claim("roles", List.of(roles)))
                .authorities(authorities.toArray(GrantedAuthority[]::new));
    }
}
