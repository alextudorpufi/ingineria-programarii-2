package ro.marketplace.app.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SecurityConfigCorsTest {

    private final SecurityConfig securityConfig = new SecurityConfig(new ObjectMapper());

    @Test
    @DisplayName("development may allow localhost origins")
    void developmentAllowsLocalhost() {
        Environment environment = environmentWithProfiles("dev");

        assertThat(securityConfig.corsConfigurationSource("http://localhost:3000", environment))
                .isNotNull();
    }

    @Test
    @DisplayName("the production profile refuses a localhost origin")
    void productionRejectsLocalhost() {
        Environment environment = environmentWithProfiles("prod");

        assertThatThrownBy(() -> securityConfig.corsConfigurationSource("http://localhost:3000", environment))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("localhost");
    }

    @Test
    @DisplayName("an empty origin list fails fast")
    void emptyOriginsAreRejected() {
        Environment environment = environmentWithProfiles("dev");

        assertThatThrownBy(() -> securityConfig.corsConfigurationSource(" , ", environment))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must not be empty");
    }

    @Test
    @DisplayName("non-http origins are rejected")
    void invalidSchemeIsRejected() {
        Environment environment = environmentWithProfiles("dev");

        assertThatThrownBy(() -> securityConfig.corsConfigurationSource("ftp://example.com", environment))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("http");
    }

    private static Environment environmentWithProfiles(String... profiles) {
        Environment environment = mock(Environment.class);
        when(environment.getActiveProfiles()).thenReturn(profiles);
        return environment;
    }
}
