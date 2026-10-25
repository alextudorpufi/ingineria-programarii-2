# SOLID, KISS, and YAGNI Practical Guide

This guide provides concrete, stack-specific examples of how to apply and avoid violating SOLID, KISS, and YAGNI in the Intelligent Services Marketplace.

---

## 1. SOLID Principles in Practice

### S - Single Responsibility Principle (SRP)

**Anti-Pattern (Violation):**
```java
// BAD: Controller doing validation, DB queries, payment logic, and email sending
@RestController
public class BadLeadController {
    @PostMapping("/leads/buy")
    public ResponseEntity<?> buyLead(@RequestBody BuyLeadRequest req) {
        // 1. Manually checks balance in DB
        // 2. Decrements credits
        // 3. Unmasks contact details
        // 4. Sends email notification
        // 5. Writes to audit log
    }
}
```

**Clean Pattern (Adherence):**
```java
// GOOD: Controller delegates to specialized service
@RestController
@RequiredArgsConstructor
public class LeadPurchaseController {
    private final LeadPurchaseService purchaseService;

    @PostMapping("/api/leads/{id}/purchase")
    public ResponseEntity<LeadDetailsResponse> purchaseLead(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        LeadDetailsResponse response = purchaseService.purchaseLead(id, currentUser.getId());
        return ResponseEntity.ok(response);
    }
}
```

---

### O - Open/Closed Principle (OCP)

**Anti-Pattern (Violation):**
```java
// BAD: Giant switch statement that must be modified for every new matching rule
public class BadMatchingEngine {
    public int scoreMatch(Lead lead, Professional pro, String ruleType) {
        switch (ruleType) {
            case "GEO": return calculateGeo(lead, pro);
            case "CATEGORY": return calculateCategory(lead, pro);
            case "AI_SEMANTIC": return calculateAi(lead, pro);
            default: throw new IllegalArgumentException();
        }
    }
}
```

**Clean Pattern (Adherence):**
```java
// GOOD: Open for new strategies via Strategy interface
public interface MatchingRule {
    boolean isApplicable(Lead lead, Professional pro);
    MatchScore evaluate(Lead lead, Professional pro);
}

@Component
public class GeoProximityRule implements MatchingRule { ... }

@Component
public class CategoryMatchRule implements MatchingRule { ... }
```

---

### L - Liskov Substitution Principle (LSP)

Subtypes must behave predictably according to the contract of their base types without throwing unexpected exceptions.

```java
// GOOD: Base contract is respected by all implementations
public interface WalletService {
    WalletBalance getBalance(UUID userId);
    TransactionResult transact(UUID userId, BigDecimal amount, TransactionType type);
}
```

---

### I - Interface Segregation Principle (ISP)

**Anti-Pattern (Violation):**
```java
// BAD: Fat interface forcing clients to implement methods they don't use
public interface SuperMarketplaceManager {
    void createLead(Lead lead);
    void purchaseLead(UUID leadId, UUID proId);
    void resolveDispute(UUID disputeId, String decision); // Pros shouldn't have this
    void configureAiPrompt(String prompt); // Clients shouldn't have this
}
```

**Clean Pattern (Adherence):**
```java
// GOOD: Role-segregated contracts
public interface LeadManagementService {
    Lead createLead(CreateLeadCommand command);
}

public interface LeadPurchaseService {
    LeadDetailsResponse purchaseLead(UUID leadId, UUID proId);
}

public interface DisputeResolutionService {
    void resolveDispute(UUID disputeId, DisputeDecision decision);
}
```

---

### D - Dependency Inversion Principle (DIP)

Always inject interfaces or abstractions rather than concrete classes.

```java
// GOOD: Injected via constructor using interface
@Service
@RequiredArgsConstructor
public class LeadMatchingServiceImpl implements LeadMatchingService {
    private final List<MatchingRule> activeRules;
    private final ProfessionalRepository proRepository;
    private final ApplicationEventPublisher eventPublisher;
}
```

---

## 2. KISS (Keep It Simple, Stupid)

### Rules of Thumb:
1. Prefer standard Java Streams and standard collections over third-party helper libraries when straightforward.
2. Keep methods under 40 lines.
3. Don't write 5 levels of inheritance; use flat composition.
4. Prefer plain SQL or standard Spring Data method queries (`findByCategoryIdAndCity`) over complex Criteria queries unless dynamically constructed.

---

## 3. YAGNI (You Aren't Gonna Need It)

### What to Avoid:
1. **Speculative Database Columns**: Do not add `bonus_points_tier_3`, `crypto_wallet_address`, or `referral_discount_code` if the story only calls for virtual credit purchases.
2. **Premature Caching**: Do not wire Redis caches for leads before standard DB queries show measurable latency.
3. **Over-abstracted Generic Classes**: Avoid `BaseEntityWithAuditingAndVersioningAndSoftDeleteAndMultitenancy<T, ID>` when a simple entity with Flyway table is sufficient.
