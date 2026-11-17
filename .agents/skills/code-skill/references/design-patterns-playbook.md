# Approved Design Patterns Playbook

Patterns are tools, not goals. Apply one only when the current requirement needs it. Each entry states whether it is already used in this repository or reserved for a future, concrete need.

---

## 1. Ports & Adapters (in use)

**Requirement:** keep business rules independent of Spring, JPA, Stripe, and HTTP.

```text
Controller
   ↓
Application service  →  domain port (interface)
                              ↓
                     infrastructure adapter
                              ↓
                     Spring Data / Stripe SDK
```

**Real examples**
- `ro.marketplace.domain.port.LeadRepository` → `LeadRepositoryAdapter`
- `ro.marketplace.domain.port.UnitOfWork` → `SpringUnitOfWorkAdapter`
- `ro.marketplace.domain.port.StripePaymentGateway` → `StripePaymentGatewayAdapter`
- `ro.marketplace.domain.port.CurrentActorProvider` → security adapter

**Rules**
- Ports live only in `ro.marketplace.domain.port`.
- Application services are plain classes registered by `ApplicationServiceConfig`.
- Adapters own framework types; ports expose domain types only.
- Do not create an adapter for an in-process helper that has no external boundary.

---

## 2. Repository + Explicit Write Semantics (in use)

**Requirement:** persistence operations must say what they mean.

```java
public interface LeadRepository {
    Lead create(Lead lead);                      // insert only
    Optional<Lead> findById(UUID id);            // read
    boolean reserveSlot(UUID leadId);            // guarded update
    boolean updateStatus(UUID leadId, LeadStatus expected, LeadStatus next);
}

public interface WalletTransactionRepository {
    WalletTransaction append(WalletTransaction transaction); // immutable history
}
```

**Why not one `save()`:** a generic save hides insert vs update, loses `@Version` when the entity is rebuilt, and turns ledgers into mutable tables.

---

## 3. Value Object (in use)

**Requirement:** a balance must never be negative or silently overflow.

```java
public record Credits(int value) {
    public Credits {
        if (value < 0) throw new IllegalArgumentException("Credits must not be negative");
    }
    public Credits plus(Credits other) { return of(Math.addExact(value, other.value)); }
    public Credits minus(Credits other) {
        if (value < other.value) throw new IllegalArgumentException("Insufficient credits");
        return of(value - other.value);
    }
}
```

Use value objects for validated scalars: `Credits`, IDs, money, phone numbers. Do not pass raw `int`/`String` through several layers when a type can carry the invariant.

---

## 4. Unit of Work (in use)

**Requirement:** a claim, wallet deduction, ledger row, and audit entry must commit atomically without Spring leaking into use cases.

```java
unitOfWork.executeInTransaction(() -> {
    LeadClaim claim = leadRepository.createClaim(...);
    walletService.deductCredits(...);
    auditService.recordEvent(...);
    return claim;
});
```

`SpringUnitOfWorkAdapter` runs `PROPAGATION_REQUIRED` + `READ_COMMITTED`, so nested use cases join the same transaction.

---

## 5. Guarded Native Update (in use)

**Requirement:** exactly one concurrent transaction may take the last slot, and a closed lead must not reopen.

```sql
UPDATE leads
   SET current_pros = current_pros + 1,
       version = version + 1,
       updated_at = :updatedAt,
       updated_by = :actor
 WHERE id = :leadId
   AND current_pros < max_pros
   AND status IN (:validated, :waiting)
```

The affected-row count is the contract: `0` means the precondition no longer holds.

---

## 6. Builder / Factory (in use, sparingly)

Domain models with many fields use Lombok builders (`Lead.builder()`, `Customer.builder()`), and records are used for immutable commands/results.

Use a builder when construction is genuinely multi-field; do not add one to a two-field value object. Validate in the compact constructor or a `validate()` method, not in the controller.

---

## 7. Strategy (future — not implemented)

**Reserved for:** a matching engine with multiple interchangeable scoring rules.

There is no `MatchingStrategy` in the codebase today. Leads already carry `work_mode`, `required_capabilities`, and `preferred_capabilities`, but scoring is deterministic and not yet extracted. Do not introduce the interface until there are at least two real strategies and a caller that selects between them.

When that happens:

```java
public interface MatchingRule {
    boolean isApplicable(Lead lead, ProfessionalProfile pro);
    MatchScore evaluate(Lead lead, ProfessionalProfile pro);
}
```

Register implementations behind the domain port; keep the rules free of Spring.

---

## 8. Observer / Domain Events (future — not implemented)

**Reserved for:** decoupling notifications/analytics from the purchase transaction.

No `ApplicationEventPublisher` is wired today. Do not add an event bus for a single listener. When two independent consumers exist (e.g. notification + analytics), publish after the transaction commits to avoid notifying on a rolled-back purchase.

---

## 9. External Adapter: Stripe (in use)

`StripePaymentGatewayAdapter` isolates the SDK:
- checkout session creation;
- webhook signature verification;
- mapping provider exceptions to `StripeProviderException` / `StripeWebhookVerificationException`.

Application code never imports Stripe SDK types. Webhook handling is idempotent through `stripe_webhook_events` and the purchase's unique idempotency key.

---

## 10. Choosing a Pattern

| Need | Pattern | Status |
| :--- | :--- | :--- |
| Isolate framework/SDK/HTTP | Ports & adapters | In use |
| Explicit insert vs guarded update | Repository with intent methods | In use |
| Enforce a scalar invariant | Value object | In use |
| Multi-step atomic operation | Unit of Work | In use |
| Concurrency-safe state transition | Guarded native update | In use |
| Many interchangeable scoring rules | Strategy | Future |
| Multiple independent side effects | Observer / events | Future |
| Complex object construction | Builder | In use |

If a pattern is not in the "in use" column, adding it requires a concrete requirement and a test — not a feeling that it might be useful.
