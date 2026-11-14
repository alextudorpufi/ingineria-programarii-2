package ro.marketplace.app.mapper;

import ro.marketplace.app.dto.CreateLeadRequest;
import ro.marketplace.app.dto.LeadResponse;
import ro.marketplace.domain.model.Lead;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Maps leads to API responses.
 *
 * <p>This mapper must NEVER expose client contact fields when
 * {@code includeContactDetails} is {@code false}. Anonymous reads, unrelated
 * professionals, and every list endpoint rely on that invariant; the integration
 * test {@code LeadContactMaskingTest} enforces it in both directions.</p>
 */
public final class LeadDtoMapper {

    private LeadDtoMapper() {
    }

    public static Lead toDomain(CreateLeadRequest req) {
        if (req == null) return null;
        int completeness = completenessScore(req);
        return Lead.builder()
                .title(req.title())
                .categoryId(req.categoryId())
                .subcategory(req.subcategory())
                .description(req.description())
                .location(req.location())
                .workMode(req.workMode())
                .requiredCapabilities(req.requiredCapabilities() == null ? List.of() : req.requiredCapabilities())
                .preferredCapabilities(req.preferredCapabilities() == null ? List.of() : req.preferredCapabilities())
                .budgetMin(req.budgetMin())
                .budgetMax(req.budgetMax())
                .budgetCurrency(req.budgetCurrency() != null && !req.budgetCurrency().isBlank()
                        ? req.budgetCurrency().trim() : "RON")
                .budgetIsNegotiable(req.budgetIsNegotiable() == null || req.budgetIsNegotiable())
                .budgetIsConfirmed(req.budgetIsConfirmed() == null || req.budgetIsConfirmed())
                .timeline(req.timeline())
                .leadPrice(req.leadPrice() != null ? req.leadPrice() : 30)
                .maxPros(req.maxPros() != null ? req.maxPros() : 4)
                .clientName(req.clientName())
                .clientPhone(req.clientPhone())
                .clientEmail(req.clientEmail())
                .clientCity(req.clientCity())
                // Trust indicators must reflect checks that actually happened. A
                // lead created through this endpoint has not been independently
                // verified, AI-extracted, or de-duplicated, so do not claim so.
                .clientIsVerified(false)
                .passportCompletenessScore(completeness)
                .passportCoherenceStatus(coherenceStatus(completeness))
                .passportContactVerified(false)
                .passportIntentVerified(false)
                .passportExtractedByAi(false)
                .passportDuplicateRisk("Nedeterminat")
                .build();
    }

    /**
     * Percentage of the key passport fields the client actually supplied.
     */
    private static int completenessScore(CreateLeadRequest req) {
        Object[] fields = {
                req.title(), req.categoryId(), req.subcategory(), req.description(),
                req.location(), req.budgetMin(), req.budgetMax(), req.timeline(),
                req.workMode(), req.clientEmail(), req.clientPhone(), req.clientCity()
        };
        int filled = 0;
        for (Object field : fields) {
            if (field instanceof String text ? !text.isBlank() : field != null) {
                filled++;
            }
        }
        return Math.round(filled * 100f / fields.length);
    }

    private static String coherenceStatus(int completeness) {
        if (completeness >= 85) return "Complet";
        if (completeness >= 60) return "Parțial";
        return "Incomplet";
    }

    public static LeadResponse toResponse(Lead domain) {
        return toResponse(domain, false);
    }

    /**
     * @param includeContactDetails true for the lead owner, an administrator, or a professional
     *                              who has already unlocked (claimed) the lead
     */
    public static LeadResponse toResponse(Lead domain, boolean includeContactDetails) {
        return toResponse(domain, includeContactDetails, false);
    }

    public static LeadResponse toResponse(Lead domain, boolean includeContactDetails, boolean ownedByCurrentUser) {
        if (domain == null) return null;

        LeadResponse.BudgetDto budgetDto = new LeadResponse.BudgetDto(
                domain.getBudgetMin(),
                domain.getBudgetMax(),
                domain.getBudgetCurrency(),
                domain.isBudgetIsNegotiable(),
                domain.isBudgetIsConfirmed()
        );

        List<LeadResponse.ClaimedProDto> claimedPros = domain.getClaimedPros() != null
                ? domain.getClaimedPros().stream()
                .map(c -> new LeadResponse.ClaimedProDto(c.getProId(), c.getProName(), c.getClaimedAt()))
                .toList()
                : List.of();

        LeadResponse.ClientSummaryDto clientDto = new LeadResponse.ClientSummaryDto(
                domain.getClientId(),
                domain.getClientName(),
                includeContactDetails ? domain.getClientPhone() : maskPhone(domain.getClientPhone()),
                includeContactDetails ? domain.getClientEmail() : maskEmail(domain.getClientEmail()),
                domain.getClientCity(),
                domain.isClientIsVerified()
        );

        List<String> deliverables = parseStringList(domain.getPassportRequiredDeliverables());
        List<String> clarifications = parseStringList(domain.getPassportClarificationsRemaining());

        LeadResponse.LeadPassportDto passportDto = new LeadResponse.LeadPassportDto(
                domain.getPassportCompletenessScore(),
                domain.getPassportCoherenceStatus(),
                domain.isPassportContactVerified(),
                domain.isPassportIntentVerified(),
                deliverables,
                clarifications,
                domain.isPassportExtractedByAi(),
                domain.getPassportDuplicateRisk()
        );

        LeadResponse.MatchExplanationDto matchDto = null;
        if (domain.getMatchOverallScore() != null) {
            matchDto = new LeadResponse.MatchExplanationDto(
                    domain.getMatchOverallScore(),
                    domain.getMatchDeterministicFit(),
                    parseStringList(domain.getMatchReasons()),
                    parseStringList(domain.getMatchSemanticKeywords()),
                    domain.getMatchDistanceKm(),
                    domain.getMatchExpectedResponseTime()
            );
        }

        return new LeadResponse(
                domain.getId(),
                domain.getTitle(),
                domain.getCategoryId(),
                domain.getSubcategory(),
                domain.getDescription(),
                domain.getLocation(),
                budgetDto,
                domain.getTimeline(),
                domain.getLeadPrice(),
                domain.getMaxPros(),
                claimedPros,
                clientDto,
                domain.getStatus() != null ? domain.getStatus().getDbValue() : null,
                passportDto,
                matchDto,
                domain.getCreatedAt(),
                ownedByCurrentUser,
                domain.getWorkMode(),
                domain.getRequiredCapabilities(),
                domain.getPreferredCapabilities()
        );
    }

    private static List<String> parseStringList(String val) {
        if (val == null || val.isBlank()) return new ArrayList<>();
        return Arrays.stream(val.split(";"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    private static String maskPhone(String phone) {
        if (phone == null || phone.isBlank()) return phone;
        // Reveal only the last two digits. Keeping a prefix made the masked value
        // much easier to correlate with the real subscriber number.
        String digits = phone.replaceAll("\\D", "");
        if (digits.length() <= 2) return "••••";
        return "•••• ••• " + digits.substring(digits.length() - 2);
    }

    private static String maskEmail(String email) {
        if (email == null || email.isBlank()) return email;
        int at = email.indexOf('@');
        if (at <= 0) return "•••••";
        return email.charAt(0) + "•••••" + email.substring(at);
    }
}
