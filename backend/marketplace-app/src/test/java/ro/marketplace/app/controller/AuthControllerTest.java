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
import ro.marketplace.app.security.JwtTokenProvider;
import ro.marketplace.app.security.SecurityConfig;
import ro.marketplace.application.service.AuthService;
import ro.marketplace.domain.model.User;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("POST /api/auth/login returns a bearer token for valid credentials")
    void loginReturnsToken() throws Exception {
        User admin = user(UUID.randomUUID(), "skilling@contact.ro", "admin");
        when(authService.authenticate("skilling@contact.ro", "admin")).thenReturn(Optional.of(admin));
        when(jwtTokenProvider.generateToken(any(User.class))).thenReturn("test-jwt-token");
        when(jwtTokenProvider.getExpiresInSeconds()).thenReturn(86_400L);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"skilling@contact.ro\",\"password\":\"admin\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("test-jwt-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.role").value("admin"))
                .andExpect(jsonPath("$.user.email").value("skilling@contact.ro"));
    }

    @Test
    @DisplayName("POST /api/auth/login rejects invalid credentials")
    void loginRejectsInvalidCredentials() throws Exception {
        when(authService.authenticate(eq("skilling@contact.ro"), eq("wrong-password")))
                .thenReturn(Optional.empty());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"skilling@contact.ro\",\"password\":\"wrong-password\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/auth/me returns the token owner profile")
    void meReturnsProfile() throws Exception {
        UUID userId = UUID.randomUUID();
        User user = user(userId, "client_1@skillink.ro", "client");
        when(authService.findById(userId)).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/auth/me").with(token(userId, "CLIENT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.email").value("client_1@skillink.ro"));
    }

    @Test
    @DisplayName("POST /api/auth/register creates a professional account")
    void registerCreatesUser() throws Exception {
        UUID userId = UUID.randomUUID();
        User created = user(userId, "new.pro@skillink.ro", "pro");
        when(authService.register("new.pro@skillink.ro", "Secret123!", "New Pro", "pro", null, null))
                .thenReturn(created);
        when(jwtTokenProvider.generateToken(created)).thenReturn("created-jwt-token");
        when(jwtTokenProvider.getExpiresInSeconds()).thenReturn(86_400L);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"new.pro@skillink.ro\",\"password\":\"Secret123!\","
                                + "\"name\":\"New Pro\",\"role\":\"pro\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("created-jwt-token"))
                .andExpect(jsonPath("$.user.role").value("pro"));
    }

    private static User user(UUID id, String email, String role) {
        return User.builder()
                .id(id)
                .email(email)
                .role(role)
                .name("Test User")
                .creditsBalance(100)
                .rating(BigDecimal.valueOf(5.00))
                .trustScore(BigDecimal.valueOf(100.00))
                .coverageAreas(List.of())
                .servicesOffered(List.of())
                .categories(List.of())
                .build();
    }

    private static RequestPostProcessor token(UUID userId, String... roles) {
        List<GrantedAuthority> authorities = Arrays.stream(roles)
                .map(role -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + role))
                .toList();
        return jwt().jwt(jwt -> jwt
                        .subject(userId.toString())
                        .claim("email", "user@marketplace.ro")
                        .claim("name", "Test User")
                        .claim("roles", List.of(roles)))
                .authorities(authorities.toArray(GrantedAuthority[]::new));
    }
}
