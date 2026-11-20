# SOLID, KISS & YAGNI in Practice

Concrete examples from the marketplace codebase. The goal is code that is easy to change, easy to test, and honest about what it does.

---

## 1. SOLID

### S — Single Responsibility

**Violation:** a controller that validates, queries, mutates credits, and writes audit rows.

**Correct:** the controller only translates HTTP.

```java
@RestController
public class LeadController {
    private final LeadService leadService;

    @PostMapping("/api/leads")
    public ResponseEntity<LeadResponse> create(@Valid @RequestBody CreateLeadRequest request) {
        Lead lead = leadService.createLead(LeadDtoMapper.toDomain(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(LeadDtoMapper.toResponse(lead));
    }
}
```

The use case owns the transaction; the adapter owns SQL; the domain model owns invariants.

| Class | One reason to change |
| :--- | :--- |
| `LeadController` | HTTP contract |
| `LeadServiceImpl` | Lead workflow |
| `LeadRepositoryAdapter` | Lead persistence |
| `EntityMapper` | Domain ↔ entity mapping |
| `GlobalExceptionHandler` | HTTP error mapping |

### O — Open/Closed

Extend through a new implementation of an existing port, not another branch in a central method.

```java
public interface StripePaymentGateway {
    StripeCheckoutSession createCheckoutSession(...);
    StripeWebhookEvent verifyWebhook(String payload, String signature);
}
```

A second provider is a new adapter; `CreditPaymentService` does not change.

Do **not** pre-build a strategy registry for a rule that has one implementation. YAGNI applies.

### L — Liskov Substitution

Implementations must honour the port's contract:
- nullable inputs are documented and handled;
- `Optional` means "may be absent", never "sometimes null";
- a failed insert throws, it does not silently return the existing row;
- `updateStatus` returns `false` when the guard did not match, not `true`.

### I — Interface Segregation

Ports are narrow and role-specific:

```java
public interface WalletTransactionRepository {
    WalletTransaction append(WalletTransaction transaction);
    PageResult<WalletTransaction> findPageByUserId(UUID userId, int page, int size);
    List<WalletTransaction> findByUserIdAndLeadId(UUID userId, UUID leadId);
}
```

Do not add `update`/`delete` to an append-only port just for symmetry.

### D — Dependency Inversion

Application services depend on ports and a clock, never on Spring:

```java
public LeadServiceImpl(LeadRepository leadRepository,
                       WalletService walletService,
                       UnitOfWork unitOfWork,
                       Clock clock) { ... }
```

Wiring lives in `ApplicationServiceConfig`; transactions are an injected `UnitOfWork`, not `@Transactional` in the use case.

---

## 2. KISS

- Prefer a guarded SQL update to a read-modify-write with retries.
- Prefer `PageRequests` to a hand-rolled `min/max` in every service.
- Prefer `AuditActor.resolve` to repeating truncation + fallback logic.
- Keep methods under ~40 lines; extract named private helpers.
- No reflection, dynamic proxies, custom annotation frameworks, or generic base classes for one use case.
- If a rule fits in the database (uniqueness, CHECK, FK, conditional update), put it there instead of orchestrating it in Java.

---

## 3. YAGNI

Do not add until a second concrete use case exists:

| Speculative addition | Why it waits |
| :--- | :--- |
| `MatchingStrategy` engine | Matching rules are columns today; no engine or second algorithm exists |
| Domain event publisher | No listener/bus is wired; audit + ledger cover current needs |
| Redis / cache layer | No measured latency problem |
| Generic `BaseEntity<T, ID>` | Each table has a different audit/locking shape |
| Soft-delete + multitenancy mixins | Not a requirement |
| Extra "just in case" columns | Schema is a contract; `V0` is deliberately minimal |
| Extra repository methods "for later" | Dead code and untested SQL |

The migration baseline demonstrates the rule: legacy profile tables and speculative matching columns were removed rather than carried forward.

---

## 4. Applying the Principles to Persistence

| Temptation | Better |
| :--- | :--- |
| Rebuild entity and `save()` | Load managed row, copy mutable fields, keep `@Version` |
| `save()` for a ledger | `append()` + `Persistable.isNew() == true` |
| `LocalDateTime.now()` | Injected UTC `Clock` |
| `UPDATE ... SET updated_at = now()` | Bind `:updatedAt` from the clock |
| `status = 'pro'` | `UserRole.PRO.getDbValue()` |
| `findAll()` | `PageRequests` + `PageResult` |
| `sort by createdAt` | `createdAt DESC, id DESC` |
| Catch and ignore a duplicate | Let the unique constraint fail; map to 409 |

---

## 5. Tests as Design Pressure

- If a service is hard to unit test, it depends on something it should not (static clock, concrete adapter, HTTP type).
- If SQL is hard to test, it is probably untested and should be covered by a Testcontainers test.
- If a mapping has no test, add one at the adapter boundary (bound parameters, enum values, timestamps).
- A bug fix without a regression test is incomplete.
