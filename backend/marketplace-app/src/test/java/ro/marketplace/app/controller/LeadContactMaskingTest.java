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

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Proves the masking contract of {@code LeadDtoMapper} in both directions:
 * anonymous readers never see contact details, while a professional who has
 * claimed the lead does.
 */
@WebMvcTest(controllers = LeadController.class)
@Import(SecurityConfig.class)
class LeadContactMaskingTest {

    private static final String CLIENT_PHONE = "+40 723 891 405";
    private static final String CLIENT_EMAIL = "elena.dobreanu@florariapetale.ro";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LeadService leadService;

    @Test
    @DisplayName("anonymous lead reads never expose the client's phone or email")
    void anonymousReadIsMasked() throws Exception {
        UUID leadId = UUID.randomUUID();
        Lead lead = lead(leadId, UUID.randomUUID(), List.of());
        when(leadService.getLeadById(leadId)).thenReturn(lead);

        mockMvc.perform(get("/api/leads/" + leadId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.client.phone").value("•••• ••• 05"))
                .andExpect(jsonPath("$.client.email").value("e•••••@florariapetale.ro"));
    }

    @Test
    @DisplayName("a professional who claimed the lead sees the full contact details")
    void claimedProfessionalSeesContact() throws Exception {
        UUID leadId = UUID.randomUUID();
        UUID proId = UUID.randomUUID();
        LeadClaim claim = LeadClaim.builder()
                .id(UUID.randomUUID())
                .leadId(leadId)
                .proId(proId)
                .proName("Pro User")
                .claimedAt(LocalDateTime.now())
                .build();
        when(leadService.getLeadById(leadId)).thenReturn(lead(leadId, UUID.randomUUID(), List.of(claim)));

        mockMvc.perform(get("/api/leads/" + leadId).with(token(proId, "PRO")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.client.phone").value(CLIENT_PHONE))
                .andExpect(jsonPath("$.client.email").value(CLIENT_EMAIL));
    }

    @Test
    @DisplayName("an unrelated professional still sees masked contact details")
    void unrelatedProfessionalSeesMaskedContact() throws Exception {
        UUID leadId = UUID.randomUUID();
        Lead lead = lead(leadId, UUID.randomUUID(), List.of());
        when(leadService.getLeadById(leadId)).thenReturn(lead);

        mockMvc.perform(get("/api/leads/" + leadId).with(token(UUID.randomUUID(), "PRO")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.client.phone").value("•••• ••• 05"))
                .andExpect(jsonPath("$.client.email").value("e•••••@florariapetale.ro"));
    }

    private static Lead lead(UUID leadId, UUID clientId, List<LeadClaim> claims) {
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
                .currentPros(claims.size())
                .clientId(clientId)
                .clientName("Elena Dobreanu")
                .clientPhone(CLIENT_PHONE)
                .clientEmail(CLIENT_EMAIL)
                .clientCity("București")
                .clientIsVerified(true)
                .status(LeadStatus.VALIDAT)
                .passportCompletenessScore(88)
                .passportCoherenceStatus("Excelentă")
                .passportContactVerified(true)
                .passportIntentVerified(true)
                .passportExtractedByAi(true)
                .passportDuplicateRisk("Scăzut (< 2%)")
                .claimedPros(claims)
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
