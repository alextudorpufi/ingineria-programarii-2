# Codebase Inspection & Anti-Duplication Guide

Search before creating. This repository already has most of the primitives a feature needs; duplicate implementations drift and are rejected in review.

---

## 1. Inspection Protocol

### Step 1: Classify the responsibility

- Domain model, value object, or enum (`Lead`, `Credits`, `LeadStatus`)
- Use case / orchestration (`LeadServiceImpl`, `WalletServiceImpl`)
- Port (interface in `ro.marketplace.domain.port`)
- Persistence adapter, JPA entity, or Spring Data repository
- API controller, DTO, mapper, or exception mapping
- Frontend route, component, hook, service, or shared type
- Migration / schema change

### Step 2: Query the real module paths

```bash
# Backend: models, ports, services, adapters
find backend/marketplace-domain/src/main -name "*Lead*" -o -name "*Credit*"
find backend/marketplace-application/src/main -name "*Service*"
find backend/marketplace-infrastructure/src/main -name "*Adapter*" -o -name "*Entity*"
find backend/marketplace-app/src/main -name "*Controller*" -o -name "*Request*" -o -name "*Response*"

# Backend: concepts and helpers
git grep -n "PageRequests" backend
git grep -n "AuditActor" backend
git grep -n "getDbValue" backend
git grep -n "Clock" backend
git grep -n "reserveSlot\|markAssignedIfFull" backend

# Frontend
find frontend/components frontend/hooks -iname "*lead*" -o -iname "*card*"
find frontend/services frontend/types frontend/models -type f
git grep -n "formatCurrency\|formatDate" frontend
```

### Step 3: Evaluate

1. **Exact match:** import and use it.
2. **80% match:** extend or parameterize the existing abstraction with a backward-compatible change.
3. **Genuinely new responsibility:** create one class with one reason to change, in the correct module.

---

## 2. Duplication Traps in This Codebase

| Concept | Do not create | Reuse instead |
| :--- | :--- | :--- |
| Credit balance arithmetic | Raw `int` math in a controller/service | `Credits` (`plus`, `minus`, `isAtLeast`) + `WalletService` |
| Ledger entry | A second table or `UPDATE` on history | `wallet_transactions` via `WalletTransactionRepository.append` |
| Lead claim check | `SELECT` then `INSERT` without a constraint | `lead_claims` with `uq_lead_claims_lead_pro` + `reserveSlot` |
| Status strings | `"validat"`, `"pro"`, `"atribuit"` in Java | `LeadStatus.getDbValue()`, `UserRole.getDbValue()` |
| Timestamps | `LocalDateTime.now()` | Injected `Clock` (`LocalDateTime.now(clock)`) |
| Actor for audit columns | `"system"` hardcoded per call site | `AuditActor.resolve(CurrentActorProvider)` |
| Paging | `PageRequest.of(...)` with an arbitrary size | `PageRequests.of/limit` + `PageResult` |
| Sort order | `createdAt` only | `createdAt DESC, id DESC` |
| API error body | Ad-hoc maps / custom error classes | `ProblemDetail` via `GlobalExceptionHandler` |
| Enum → DB mapping | `switch` on `name()` | `getDbValue()` / `fromDbValue()` |
| Entity ↔ domain conversion | Copy-paste setters in adapters | `EntityMapper` |
| Frontend status union | A new literal union per component | shared `frontend/types` model |
| Currency/date formatting | Per-page formatter | shared `formatters` utility |
| Buttons/inputs | Native styled elements | `ui-skill` components and tokens |

---

## 3. Duplication Across Layers

- **Validation:** DTO annotations are the HTTP contract; domain invariants still live in the model/use case. Do not copy a domain rule into a controller, and do not duplicate the same rule in two services.
- **Enum values:** one Java enum, one database CHECK constraint, one frontend union. If you add a value, update all three in the same change.
- **Persistence rules:** one adapter owns a table. Two adapters writing the same table is a defect even if the SQL differs.
- **Migrations:** one baseline and append-only versions. Never re-create a table in a new migration because the first one drifted.

---

## 4. Review Questions

- Is there already a port or service with this responsibility?
- Does this logic exist in the domain model but was re-implemented in a controller or React component?
- Am I adding a second clock, paging helper, mapper, ledger, or audit path?
- Does a new enum value exist in the database CHECK, the Java enum, and the frontend type?
- Did I add a helper that a future developer will have to keep in sync with an existing one?
