package ro.marketplace.matching.example;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

/**
 * <strong>Future pattern — not implemented in the codebase.</strong>
 *
 * Matching today is deterministic and driven by the lead's stored work mode and
 * capability arrays. Do not extract this strategy engine until at least two real
 * algorithms and a caller that selects between them exist.
 *
 * <p>Conforms to SOLID (SRP, OCP, DIP) and DRY principles.</p>
 */
public class MatchingStrategyExample {

    public record LeadRequest(
        String id,
        String category,
        String city,
        BigDecimal clientBudget,
        String workMode,                 // 'ONSITE' | 'REMOTE' | null (unspecified)
        Set<String> requiredCapabilities
    ) {}

    public record ProfessionalCandidate(
        String id,
        String name,
        Set<String> categories,
        Set<String> coverageCities,
        Set<String> capabilities,
        Set<String> supportedWorkModes,
        BigDecimal minimumBudgetRequirement,
        int averageResponseMinutes
    ) {}

    public record StrategyScore(
        boolean eligible,
        int scoreWeight,
        String explanation
    ) {}

    public interface MatchingRuleStrategy {
        StrategyScore evaluate(LeadRequest lead, ProfessionalCandidate candidate);
    }

    public static class CategoryMatchStrategy implements MatchingRuleStrategy {
        @Override
        public StrategyScore evaluate(LeadRequest lead, ProfessionalCandidate candidate) {
            boolean matches = candidate.categories().contains(lead.category());
            return new StrategyScore(
                matches,
                matches ? 40 : 0,
                matches ? "Categorie compatibilă: " + lead.category() : "Categorie incompatibilă"
            );
        }
    }

    public static class WorkModeStrategy implements MatchingRuleStrategy {
        @Override
        public StrategyScore evaluate(LeadRequest lead, ProfessionalCandidate candidate) {
            // null work mode means "unspecified": do not infer it from category or location.
            if (lead.workMode() == null) {
                return new StrategyScore(true, 0, "Mod de lucru nespecificat");
            }
            boolean matches = candidate.supportedWorkModes().contains(lead.workMode());
            return new StrategyScore(
                matches,
                matches ? 20 : 0,
                matches ? "Mod de lucru compatibil: " + lead.workMode() : "Mod de lucru incompatibil"
            );
        }
    }

    public static class CapabilityFitStrategy implements MatchingRuleStrategy {
        @Override
        public StrategyScore evaluate(LeadRequest lead, ProfessionalCandidate candidate) {
            boolean fits = candidate.capabilities().containsAll(lead.requiredCapabilities());
            return new StrategyScore(
                fits,
                fits ? 20 : 0,
                fits ? "Toate capabilitățile obligatorii sunt acoperite" : "Capabilități obligatorii lipsă"
            );
        }
    }

    public static class GeographicCoverageStrategy implements MatchingRuleStrategy {
        @Override
        public StrategyScore evaluate(LeadRequest lead, ProfessionalCandidate candidate) {
            boolean inCoverage = candidate.coverageCities().contains(lead.city());
            return new StrategyScore(
                inCoverage,
                inCoverage ? 30 : 0,
                inCoverage ? "Locație în aria de acoperire: " + lead.city() : "Locație în afara ariei de acoperire"
            );
        }
    }

    public static class BudgetFitStrategy implements MatchingRuleStrategy {
        @Override
        public StrategyScore evaluate(LeadRequest lead, ProfessionalCandidate candidate) {
            boolean fits = lead.clientBudget() == null || 
                           lead.clientBudget().compareTo(candidate.minimumBudgetRequirement()) >= 0;
            return new StrategyScore(
                fits,
                fits ? 20 : 0,
                fits ? "Buget client compatibil cu tarifele minime" : "Buget sub pragul minim al profesionistului"
            );
        }
    }

    public record MatchResult(
        boolean matched,
        int totalScore,
        List<String> explanations
    ) {}

    public static class DeterministicMatchingEngine {
        private final List<MatchingRuleStrategy> strategies;

        public DeterministicMatchingEngine(List<MatchingRuleStrategy> strategies) {
            this.strategies = strategies;
        }

        public MatchResult match(LeadRequest lead, ProfessionalCandidate candidate) {
            int score = 0;
            var explanations = new java.util.ArrayList<String>();

            for (MatchingRuleStrategy strategy : strategies) {
                StrategyScore res = strategy.evaluate(lead, candidate);
                if (!res.eligible()) {
                    return new MatchResult(false, 0, List.of("Lead neeligibil: " + res.explanation()));
                }
                score += res.scoreWeight();
                explanations.add(res.explanation());
            }

            return new MatchResult(true, score, explanations);
        }
    }
}
