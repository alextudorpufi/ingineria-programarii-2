package ro.marketplace.app.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import ro.marketplace.app.security.SecurityConfig;
import ro.marketplace.application.service.LeadService;
import ro.marketplace.domain.model.Lead;
import ro.marketplace.domain.model.LeadClaim;
import ro.marketplace.domain.model.LeadStatus;

import java.math.BigDecimal;
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

@WebMvcTest(controllers = LeadController.class)
@Import(SecurityConfig.class)
class LeadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LeadService leadService;

    @Test
    @DisplayName("GET /api/leads returns lead list with nested budget and passport")
    void getAllLeadsReturnsList() throws Exception {
        UUID leadId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();

        Lead lead = lead(leadId, clientId);

        when(leadService.getAllLeads()).thenReturn(List.of(lead));

        mockMvc.perform(get("/api/leads").with(token(clientId, "CLIENT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(leadId.toString()))
                .andExpect(jsonPath("$[0].title").value("Magazin online pentru florărie"))
                .andExpect(jsonPath("$[0].budget.min").value(5000))
                .andExpect(jsonPath("$[0].budget.max").value(8500))
                .andExpect(jsonPath("$[0].budget.currency").value("RON"))
                .andExpect(jsonPath("$[0].leadPassport.completenessScore").value(88))
                .andExpect(jsonPath("$[0].claimedPros[0].proName").value("WebImpact Solutions"));
    }

    @Test
    @DisplayName("GET /api/leads masks client contact details for unrelated users")
    void getAllLeadsMasksContactDetails() throws Exception {
        UUID leadId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();
        Lead lead = lead(leadId, clientId);

        when(leadService.getAllLeads()).thenReturn(List.of(lead));

        mockMvc.perform(get("/api/leads").with(token(UUID.randomUUID(), "PRO")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].client.phone").value("+407 ••• ••• 05"))
                .andExpect(jsonPath("$[0].client.email").value("e•••••@florariapetale.ro"));
    }

    @Test
    @DisplayName("POST /api/leads/{id}/claim derives the professional from the token")
    void claimLeadUsesTokenIdentity() throws Exception {
        UUID leadId = UUID.randomUUID();
        UUID proId = UUID.randomUUID();
        LeadClaim claim = LeadClaim.builder()
                .id(UUID.randomUUID())
                .leadId(leadId)
                .proId(proId)
                .proName("Pro User")
                .claimedAt(LocalDateTime.now())
                .build();

        when(leadService.claimLead(eq(leadId), eq(proId), eq("Pro User"))).thenReturn(claim);

        mockMvc.perform(post("/api/leads/" + leadId + "/claim")
                        .with(token(proId, "PRO")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.proId").value(proId.toString()))
                .andExpect(jsonPath("$.proName").value("Pro User"));
    }

    @Test
    @DisplayName("POST /api/leads/{id}/claim rejects a client token")
    void claimLeadRejectsClientToken() throws Exception {
        UUID leadId = UUID.randomUUID();
        mockMvc.perform(post("/api/leads/" + leadId + "/claim")
                        .with(token(UUID.randomUUID(), "CLIENT")))
                .andExpect(status().isForbidden());
    }

    private static Lead lead(UUID leadId, UUID clientId) {
        return Lead.builder()
                .id(leadId)
                .title("Magazin online pentru florărie")
                .categoryId("it-software")
                .subcategory("E-commerce & Plăți Online")
                .description("Magazin online flori")
                .location("București & Ilfov")
                .budgetMin(BigDecimal.valueOf(5000))
                .budgetMax(BigDecimal.valueOf(8500))
                .budgetCurrency("RON")
                .budgetIsNegotiable(true)
                .budgetIsConfirmed(true)
                .timeline("3 - 4 săptămâni")
                .leadPrice(30)
                .maxPros(4)
                .currentPros(1)
                .clientId(clientId)
                .clientName("Elena Dobreanu")
                .clientPhone("+40 723 891 405")
                .clientEmail("elena.dobreanu@florariapetale.ro")
                .clientCity("București")
                .clientIsVerified(true)
                .status(LeadStatus.VALIDAT)
                .passportCompletenessScore(88)
                .passportCoherenceStatus("Excelentă")
                .passportContactVerified(true)
                .passportIntentVerified(true)
                .passportExtractedByAi(true)
                .passportDuplicateRisk("Scăzut (< 2%)")
                .claimedPros(List.of(
                        LeadClaim.builder()
                                .id(UUID.randomUUID())
                                .leadId(leadId)
                                .proId(UUID.randomUUID())
                                .proName("WebImpact Solutions")
                                .claimedAt(LocalDateTime.now())
                                .build()
                ))
                .createdAt(LocalDateTime.now())
                .build();
    }

    private static RequestPostProcessor token(UUID userId, String... roles) {
        List<GrantedAuthority> authorities = Arrays.stream(roles)
                .map(role -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + role))
                .toList();
        return jwt().jwt(jwt -> jwt
                        .subject(userId.toString())
                        .claim("email", "user@marketplace.ro")
                        .claim("name", "Pro User")
                        .claim("roles", List.of(roles)))
                .authorities(authorities.toArray(GrantedAuthority[]::new));
    }
}
