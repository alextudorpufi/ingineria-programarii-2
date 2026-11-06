package ro.marketplace.app.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import ro.marketplace.domain.model.ProfessionalProfile;
import ro.marketplace.domain.model.User;
import ro.marketplace.domain.port.UserRepository;
import ro.marketplace.application.service.ProfessionalProfileService;
import ro.marketplace.domain.exception.ConflictException;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
class ProfessionalProfileIntegrationTest {
    @Container
    @ServiceConnection
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16-alpine");

    @Autowired
    private UserRepository users;
    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private ProfessionalProfileService profiles;

    @Test
    void editingPersistsWithoutOverwritingAccountDataAndRejectsStaleEdits() {
        User user = account("pro");
        user.setCreditsBalance(120);
        user.setPasswordHash("preserved-password-hash");
        users.save(user);
        var profile = ProfessionalProfile.builder().categories(List.of("it-software"))
                .servicesOffered(List.of("Web development")).capabilities(List.of("Payments"))
                .supportsRemote(true).supportsOnsite(true).coverageAreas(List.of("RO-IS", "RO-B"))
                .yearsExperience(4).acceptingLeads(true)
                .onboardingStatus(ProfessionalProfile.OnboardingStatus.COMPLETE).version(0).build();
        var update = new ProfessionalProfileService.Update("Updated Pro", "Company", "Developer", "123", "Bio", profile);

        User saved = profiles.update(user.getId(), update);
        assertThat(saved.getProfessionalProfile().getVersion()).isEqualTo(1);
        User loaded = users.findById(user.getId()).orElseThrow();
        assertThat(loaded.getName()).isEqualTo("Updated Pro");
        assertThat(loaded.getProfessionalProfile().getCapabilities()).containsExactly("Payments");
        assertThat(loaded.getCoverageAreas()).containsExactly("RO-IS", "RO-B");
        assertThat(jdbc.queryForObject("SELECT coverage_areas::text FROM user_professional WHERE user_id = ?",
                String.class, user.getId())).contains("RO-IS", "RO-B");
        assertThat(loaded.getProfessionalProfile().getYearsExperience()).isEqualTo(4);
        assertThat(loaded.getProfessionalProfile().isAcceptingLeads()).isTrue();
        assertThat(loaded.getCreditsBalance()).isEqualTo(120);
        assertThat(loaded.getPasswordHash()).isEqualTo("preserved-password-hash");
        assertThat(loaded.getRole()).isEqualTo("pro");
        assertThatThrownBy(() -> profiles.update(user.getId(), update)).isInstanceOf(ConflictException.class);
        assertThat(users.findById(user.getId()).orElseThrow().getProfessionalProfile().getVersion()).isEqualTo(1);
    }

    @Test
    void structuredProfessionalProfileSurvivesDatabaseRoundTrip() {
        ProfessionalProfile profile = ProfessionalProfile.builder()
                .categories(List.of("it-software", "marketing"))
                .servicesOffered(List.of("Website", "Branding"))
                .coverageAreas(List.of("Iași", "București"))
                .capabilities(List.of("ecommerce", "payments"))
                .supportsOnsite(false).supportsRemote(true)
                .yearsExperience(5)
                .build();
        User pro = account("pro");
        pro.setProfessionalProfile(profile);
        users.save(pro);

        User loaded = users.findById(pro.getId()).orElseThrow();
        assertThat(loaded.getProfessionalProfile()).usingRecursiveComparison().isEqualTo(profile);
        assertThat(loaded.getCoverageAreas()).containsExactly("Iași", "București");
        assertThat(jdbc.queryForObject("SELECT count(*) FROM user_professional WHERE user_id = ?",
                Integer.class, pro.getId())).isEqualTo(1);
    }

    @Test
    void newProfessionalsGetDraftProfilesAndClientsDoNot() {
        User pro = users.save(account("pro"));
        User client = users.save(account("client"));
        assertThat(pro.getProfessionalProfile().getOnboardingStatus())
                .isEqualTo(ProfessionalProfile.OnboardingStatus.DRAFT);
        assertThat(pro.getProfessionalProfile().isAcceptingLeads()).isFalse();
        assertThat(pro.getProfessionalProfile().getSupportsRemote()).isNull();
        assertThat(client.getProfessionalProfile()).isNull();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM user_professional WHERE user_id = ?",
                Integer.class, client.getId())).isZero();
    }

    private User account(String role) {
        UUID id = UUID.randomUUID();
        return User.builder().id(id).email(id + "@example.com").role(role).name("Test")
                .rating(BigDecimal.ZERO).trustScore(BigDecimal.ZERO).build();
    }
}
