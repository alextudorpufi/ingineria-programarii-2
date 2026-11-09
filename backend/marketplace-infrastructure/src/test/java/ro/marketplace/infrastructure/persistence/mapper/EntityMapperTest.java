package ro.marketplace.infrastructure.persistence.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ro.marketplace.domain.model.AuditAction;
import ro.marketplace.domain.model.AuditLog;
import ro.marketplace.domain.model.Dispute;
import ro.marketplace.domain.model.DisputeStatus;
import ro.marketplace.domain.model.Lead;
import ro.marketplace.domain.model.LeadClaim;
import ro.marketplace.domain.model.LeadStatus;
import ro.marketplace.domain.model.User;
import ro.marketplace.domain.model.ProfessionalProfile;
import ro.marketplace.domain.model.WalletTransaction;
import ro.marketplace.domain.model.WalletTransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EntityMapperTest {

    @Test
    @DisplayName("Lead survives a domain -> entity -> domain round trip")
    void leadRoundTrip() {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.of(2026, 1, 1, 10, 0);
        Lead lead = Lead.builder()
                .id(id)
                .title("Website")
                .categoryId("it-software")
                .subcategory("Website Prezentare")
                .description("Description")
                .location("Bucharest")
                .budgetMin(BigDecimal.TEN)
                .budgetMax(BigDecimal.valueOf(20))
                .budgetCurrency("RON")
                .budgetIsNegotiable(true)
                .budgetIsConfirmed(true)
                .timeline("2 weeks")
                .leadPrice(30)
                .maxPros(4)
                .currentPros(2)
                .clientId(UUID.randomUUID())
                .clientName("Client")
                .clientPhone("+40700000000")
                .clientEmail("client@example.com")
                .clientCity("Bucharest")
                .clientIsVerified(true)
                .status(LeadStatus.ATRIBUIT)
                .passportCompletenessScore(88)
                .passportCoherenceStatus("Excelentă")
                .passportContactVerified(true)
                .passportIntentVerified(true)
                .passportRequiredDeliverables("a;b")
                .passportClarificationsRemaining("c")
                .passportExtractedByAi(true)
                .passportDuplicateRisk("Low")
                .matchOverallScore(90)
                .matchDeterministicFit(true)
                .matchReasons("r")
                .matchSemanticKeywords("k")
                .matchDistanceKm(BigDecimal.ONE)
                .matchExpectedResponseTime("< 15 min")
                .createdAt(now)
                .build();

        Lead mapped = EntityMapper.toDomain(EntityMapper.toEntity(lead));

        assertThat(mapped).usingRecursiveComparison().ignoringFields("createdAt").isEqualTo(lead);
        assertThat(mapped.getCurrentPros()).isEqualTo(2);
        assertThat(mapped.getStatus()).isEqualTo(LeadStatus.ATRIBUIT);
    }

    @Test
    @DisplayName("User collection fields survive the round trip")
    void userRoundTrip() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .email("pro@example.com")
                .role("pro")
                .name("Pro")
                .companyName("Company")
                .title("Title")
                .phone("+40700000000")
                .avatarUrl("https://example.com/avatar.png")
                .bio("Bio")
                .verifiedBadge(true)
                .rating(BigDecimal.valueOf(4.9))
                .reviewCount(10)
                .creditsBalance(100)
                .hourlyRate(BigDecimal.valueOf(150))
                .clientBudget(BigDecimal.valueOf(5000))
                .responseTime("< 15 min")
                .completedJobs(20)
                .trustScore(BigDecimal.valueOf(99))
                .memberSince("2025")
                .professionalProfile(ProfessionalProfile.builder()
                        .coverageAreas(List.of("Bucharest"))
                        .servicesOffered(List.of("Web"))
                        .categories(List.of("IT"))
                        .supportsRemote(true)
                        .build())
                .build();

        User mapped = EntityMapper.toDomain(EntityMapper.toEntity(user));

        assertThat(mapped).usingRecursiveComparison().isEqualTo(user);
    }

    @Test
    @DisplayName("Dispute status and wallet type map to and from database values")
    void enumRoundTrips() {
        Dispute dispute = Dispute.builder()
                .id(UUID.randomUUID())
                .leadId(UUID.randomUUID())
                .leadTitle("Title")
                .proId(UUID.randomUUID())
                .proName("Pro")
                .reason("reason")
                .details("details")
                .creditsRequested(30)
                .status(DisputeStatus.APROBAT)
                .createdAt(LocalDateTime.now())
                .build();
        assertThat(EntityMapper.toDomain(EntityMapper.toEntity(dispute)).getStatus())
                .isEqualTo(DisputeStatus.APROBAT);

        WalletTransaction tx = WalletTransaction.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .type(WalletTransactionType.REFUND)
                .amount(30)
                .description("refund")
                .balanceAfter(60)
                .createdAt(LocalDateTime.now())
                .build();
        WalletTransaction mappedTx = EntityMapper.toDomain(EntityMapper.toEntity(tx));
        assertThat(mappedTx.getType()).isEqualTo(WalletTransactionType.REFUND);
        assertThat(mappedTx.getBalanceAfter()).isEqualTo(60);
    }

    @Test
    @DisplayName("Audit action survives the round trip")
    void auditActionRoundTrip() {
        AuditLog log = AuditLog.builder()
                .id(UUID.randomUUID())
                .entityName("Lead")
                .entityId("1")
                .action(AuditAction.CLAIM_LEAD)
                .changedBy("actor")
                .details("{}")
                .createdAt(LocalDateTime.now())
                .build();

        AuditLog mapped = EntityMapper.toDomain(EntityMapper.toEntity(log));

        assertThat(mapped.getAction()).isEqualTo(AuditAction.CLAIM_LEAD);
    }

    @Test
    @DisplayName("Claims map without leaking into the lead entity")
    void claimRoundTrip() {
        LeadClaim claim = LeadClaim.builder()
                .id(UUID.randomUUID())
                .leadId(UUID.randomUUID())
                .proId(UUID.randomUUID())
                .proName("Pro")
                .claimedAt(LocalDateTime.now())
                .build();

        LeadClaim mapped = EntityMapper.toDomain(EntityMapper.toEntity(claim));

        assertThat(mapped).usingRecursiveComparison().isEqualTo(claim);
    }
}
