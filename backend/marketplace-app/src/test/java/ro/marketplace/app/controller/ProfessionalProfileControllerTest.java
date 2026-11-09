package ro.marketplace.app.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import ro.marketplace.app.security.SecurityConfig;
import ro.marketplace.application.service.ProfessionalProfileService;
import ro.marketplace.domain.exception.ConflictException;
import ro.marketplace.domain.model.ProfessionalProfile;
import ro.marketplace.domain.model.User;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProfessionalProfileController.class)
@Import(SecurityConfig.class)
class ProfessionalProfileControllerTest {
    private static final String URL = "/api/professional-profile/me";
    private static final String BODY = """
            {"name":"Pro","categories":[],"servicesOffered":[],"coverageAreas":[],
             "capabilities":[],"supportsOnsite":null,"supportsRemote":true,
             "yearsExperience":null,"acceptingLeads":false,"onboardingStatus":"DRAFT","version":0}
            """;
    private final UUID userId = UUID.randomUUID();
    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private ProfessionalProfileService profiles;

    @Test
    void getReturnsOnlyOwnEditableFields() throws Exception {
        when(profiles.get(userId)).thenReturn(user());
        mvc.perform(get(URL).with(token("PRO"))).andExpect(status().isOk())
                .andExpect(jsonPath("$.supportsRemote").value(true))
                .andExpect(jsonPath("$.version").value(0))
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.creditsBalance").doesNotExist());
        verify(profiles).get(userId);
    }

    @Test
    void writesUseTokenOwnerAndReturnSavedProfile() throws Exception {
        when(profiles.update(eq(userId), any())).thenReturn(user());
        mvc.perform(put(URL).with(token("PRO")).contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Pro"));
        verify(profiles).update(eq(userId), any());
    }

    @Test
    void anonymousAndClientWritesAreDenied() throws Exception {
        mvc.perform(put(URL).contentType(MediaType.APPLICATION_JSON).content(BODY)).andExpect(status().isUnauthorized());
        mvc.perform(put(URL).with(token("CLIENT")).contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isForbidden());
    }

    @Test
    void removedProfessionalAliasDoesNotGrantReadOrWriteAccess() throws Exception {
        mvc.perform(get(URL).with(token("PROFESSIONAL"))).andExpect(status().isForbidden());
        mvc.perform(put(URL).with(token("PROFESSIONAL")).contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isForbidden());
    }

    @Test
    void invalidExperienceAndStaleVersionsAreRejected() throws Exception {
        mvc.perform(put(URL).with(token("PRO")).contentType(MediaType.APPLICATION_JSON)
                        .content(BODY.replace("\"yearsExperience\":null", "\"yearsExperience\":-1")))
                .andExpect(status().isBadRequest());
        when(profiles.update(eq(userId), any())).thenThrow(new ConflictException("Reîncarcă profilul."));
        mvc.perform(put(URL).with(token("PRO")).contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isConflict());
    }

    private User user() {
        return User.builder().id(userId).name("Pro").role("pro").passwordHash("never-return")
                .professionalProfile(ProfessionalProfile.builder().supportsRemote(true).build()).build();
    }

    private RequestPostProcessor token(String role) {
        return jwt().jwt(token -> token.subject(userId.toString()).claim("role", role))
                .authorities(new SimpleGrantedAuthority("ROLE_" + role));
    }
}
