# Approved Design Patterns Playbook

This playbook provides architectural patterns, class diagrams, and guidelines tailored to the Intelligent Services Marketplace.

---

## 1. Strategy Pattern: Lead Matching Engine

### Motivation
Lead matching requires deterministic matching initially (category, geographic coverage, budget, availability) and will later incorporate semantic AI embeddings. The Strategy pattern decouples individual scoring rules from the distribution engine.

### Implementation Blueprint
```java
public interface MatchingStrategy {
    int getOrder();
    MatchEvaluation evaluate(Lead lead, ProfessionalProfile pro);
}

@Component
@Order(1)
public class CategoryFilterStrategy implements MatchingStrategy {
    @Override
    public MatchEvaluation evaluate(Lead lead, ProfessionalProfile pro) {
        boolean match = pro.getCategories().contains(lead.getCategory());
        return new MatchEvaluation(match, match ? 40 : 0, "Potrivire categorie: " + lead.getCategory());
    }
}

@Component
@Order(2)
public class GeographicCoverageStrategy implements MatchingStrategy {
    @Override
    public MatchEvaluation evaluate(Lead lead, ProfessionalProfile pro) {
        boolean inArea = pro.getCoverageArea().contains(lead.getLocation().getCity());
        return new MatchEvaluation(inArea, inArea ? 30 : 0, "În aria de acoperire");
    }
}
```

---

## 2. Adapter Pattern: LLM Integration

### Motivation
External AI models (OpenAI, Gemini, Anthropic) change API formats frequently. The Adapter pattern wraps the provider-specific SDK and translates unstructured prompt responses into the domain's strongly-typed `LeadDraftSpec`.

### Implementation Blueprint
```java
public interface LlmAssistantAdapter {
    LeadDraftSpec extractServiceRequest(String userMessage, ConversationHistory history);
}

@Component
@RequiredArgsConstructor
public class GeminiLlmAdapter implements LlmAssistantAdapter {
    private final GeminiApiClient geminiClient;
    private final ObjectMapper objectMapper;

    @Override
    public LeadDraftSpec extractServiceRequest(String userMessage, ConversationHistory history) {
        String rawJson = geminiClient.generateStructuredOutput(userMessage, LEAD_SCHEMA);
        return objectMapper.readValue(rawJson, LeadDraftSpec.class);
    }
}
```

---

## 3. Builder Pattern: Validated Entity & DTO Creation

### Motivation
Service requests and lead models have numerous fields, constraints, and validation rules. The Builder pattern guarantees that instances are fully valid before being published.

### Implementation Blueprint
```java
public record CreateLeadCommand(
    String title,
    String category,
    String subcategory,
    String description,
    String city,
    BigDecimal minBudget,
    BigDecimal maxBudget,
    String timeline
) {
    public CreateLeadCommand {
        Objects.requireNonNull(title, "Title cannot be null");
        Objects.requireNonNull(category, "Category cannot be null");
        if (minBudget != null && maxBudget != null && minBudget.compareTo(maxBudget) > 0) {
            throw new IllegalArgumentException("Minimum budget cannot exceed maximum budget");
        }
    }
}
```

---

## 4. Observer Pattern: Decoupled Domain Events

### Motivation
When a professional unlocks a lead, several independent actions must occur:
1. Credit balance decreases
2. Contact details unlock
3. Client is notified via WebSocket / email
4. PostHog analytics event is logged

Using Spring's `ApplicationEventPublisher` isolates these concerns.

### Implementation Blueprint
```java
// Event definition
public record LeadPurchasedEvent(
    UUID leadId,
    UUID professionalId,
    BigDecimal creditsPaid,
    Instant timestamp
) {}

// Publisher
@Service
@RequiredArgsConstructor
public class LeadPurchaseServiceImpl implements LeadPurchaseService {
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void purchase(UUID leadId, UUID proId) {
        // ... perform DB purchase ...
        eventPublisher.publishEvent(new LeadPurchasedEvent(leadId, proId, amount, Instant.now()));
    }
}

// Listeners
@Component
public class LeadNotificationListener {
    @EventListener
    public void onLeadPurchased(LeadPurchasedEvent event) {
        // Send WebSocket notification to client
    }
}

@Component
public class LeadAnalyticsListener {
    @EventListener
    public void onLeadPurchased(LeadPurchasedEvent event) {
        // Track PostHog conversion metric
    }
}
```
