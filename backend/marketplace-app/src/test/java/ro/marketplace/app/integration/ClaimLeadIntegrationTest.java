package ro.marketplace.app.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ro.marketplace.application.service.LeadService;
import ro.marketplace.domain.exception.ConflictException;
import ro.marketplace.domain.model.Lead;
import ro.marketplace.domain.model.LeadStatus;
import ro.marketplace.domain.model.User;
import ro.marketplace.domain.port.LeadRepository;
import ro.marketplace.domain.port.UserRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
class ClaimLeadIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16-alpine");

    @Autowired
    private LeadService leadService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LeadRepository leadRepository;

    @Test
    @DisplayName("Concurrent claims never exceed capacity and never lose credits")
    void concurrentClaimsNeverExceedCapacity() throws Exception {
        User client = createUser("client@example.com", 0, "client");
        Lead lead = createLead(client, 4, 30);

        List<User> pros = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            pros.add(createUser("pro" + i + "@example.com", 100, "pro"));
        }

        int threads = pros.size();
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<Boolean>> futures = new ArrayList<>();
        try {
            for (User pro : pros) {
                futures.add(pool.submit(() -> {
                    start.await();
                    try {
                        leadService.claimLead(lead.getId(), pro.getId(), pro.getName());
                        return true;
                    } catch (ConflictException ex) {
                        return false;
                    }
                }));
            }
            start.countDown();

            int successes = 0;
            for (Future<Boolean> future : futures) {
                if (future.get()) {
                    successes++;
                }
            }

            assertThat(successes).isEqualTo(4);
        } finally {
            pool.shutdownNow();
        }

        assertThat(leadRepository.findClaimsByLeadId(lead.getId())).hasSize(4);
        Lead reloaded = leadRepository.findById(lead.getId()).orElseThrow();
        assertThat(reloaded.getCurrentPros()).isEqualTo(4);
        assertThat(reloaded.getStatus()).isEqualTo(LeadStatus.ATRIBUIT);

        long winners = pros.stream()
                .filter(pro -> userRepository.findById(pro.getId()).orElseThrow().getCreditsBalance() == 70)
                .count();
        long untouched = pros.stream()
                .filter(pro -> userRepository.findById(pro.getId()).orElseThrow().getCreditsBalance() == 100)
                .count();
        assertThat(winners).isEqualTo(4);
        assertThat(untouched).isEqualTo(4);
    }

    @Test
    @DisplayName("A failed claim rolls back the reservation and the credit deduction")
    void failedClaimDoesNotLoseCredits() {
        User client = createUser("client2@example.com", 0, "client");
        User poorPro = createUser("poor@example.com", 10, "pro");
        Lead lead = createLead(client, 4, 30);

        assertThatThrownBy(() -> leadService.claimLead(lead.getId(), poorPro.getId(), poorPro.getName()))
                .isInstanceOf(ConflictException.class);

        assertThat(leadRepository.findClaimsByLeadId(lead.getId())).isEmpty();
        assertThat(leadRepository.findById(lead.getId()).orElseThrow().getCurrentPros()).isZero();
        assertThat(userRepository.findById(poorPro.getId()).orElseThrow().getCreditsBalance()).isEqualTo(10);
    }

    @Test
    @DisplayName("Claiming the same lead twice charges credits only once")
    void duplicateClaimIsIdempotent() {
        User client = createUser("client3@example.com", 0, "client");
        User pro = createUser("pro-dup@example.com", 100, "pro");
        Lead lead = createLead(client, 4, 30);

        leadService.claimLead(lead.getId(), pro.getId(), pro.getName());

        assertThatThrownBy(() -> leadService.claimLead(lead.getId(), pro.getId(), pro.getName()))
                .isInstanceOf(ConflictException.class);
        assertThat(leadRepository.findClaimsByLeadId(lead.getId())).hasSize(1);
        assertThat(userRepository.findById(pro.getId()).orElseThrow().getCreditsBalance()).isEqualTo(70);
    }

    private User createUser(String email, int credits, String role) {
        UUID id = UUID.randomUUID();
        User user = User.builder()
                .id(id)
                .email(email)
                .role(role)
                .name("User " + id)
                .proVerified("pro".equalsIgnoreCase(role))
                .rating(BigDecimal.valueOf(5))
                .trustScore(BigDecimal.valueOf(100))
                .creditsBalance(credits)
                .build();
        return userRepository.save(user);
    }

    @Test
    void matchingRequirementsPersistAndLegacyWorkModeRemainsUnknown() {
        User client = createUser("matching-client@example.com", 0, "client");
        Lead legacy = createLead(client, 4, 30);
        UUID legacyId = legacy.getId();
        assertThat(legacy.getWorkMode()).isNull();
        assertThat(legacy.getRequiredCapabilities()).isEmpty();

        legacy.setId(null);
        legacy.setWorkMode(Lead.WorkMode.REMOTE);
        legacy.setRequiredCapabilities(List.of(" Payments ", "payments"));
        legacy.setPreferredCapabilities(List.of("PAYMENTS", "Branding"));
        Lead created = leadService.createLead(legacy);
        Lead loaded = leadRepository.findById(created.getId()).orElseThrow();
        assertThat(loaded.getWorkMode()).isEqualTo(Lead.WorkMode.REMOTE);
        assertThat(loaded.getRequiredCapabilities()).containsExactly("Payments");
        assertThat(loaded.getPreferredCapabilities()).containsExactly("Branding");
        assertThat(leadRepository.findById(legacyId).orElseThrow().getWorkMode()).isNull();
    }

    private Lead createLead(User client, int maxPros, int leadPrice) {
        Lead lead = Lead.builder()
                .id(UUID.randomUUID())
                .title("Integration lead")
                .categoryId("it-software")
                .subcategory("Website Prezentare")
                .description("Description")
                .location("Bucharest")
                .budgetMin(BigDecimal.valueOf(1000))
                .budgetMax(BigDecimal.valueOf(2000))
                .budgetCurrency("RON")
                .budgetIsNegotiable(true)
                .budgetIsConfirmed(true)
                .timeline("2 weeks")
                .leadPrice(leadPrice)
                .maxPros(maxPros)
                .currentPros(0)
                .clientId(client.getId())
                .clientName(client.getName())
                .clientPhone("+40700000000")
                .clientEmail(client.getEmail())
                .clientCity("Bucharest")
                .clientIsVerified(true)
                .status(LeadStatus.VALIDAT)
                .passportCompletenessScore(85)
                .passportCoherenceStatus("Excelentă")
                .passportContactVerified(true)
                .passportIntentVerified(true)
                .passportExtractedByAi(true)
                .passportDuplicateRisk("Low")
                .build();
        return leadRepository.save(lead);
    }
}
