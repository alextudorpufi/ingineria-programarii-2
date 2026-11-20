---
name: code-skill
description: >-
  Mandatory software engineering guidelines and coding standards for all code generation,
  refactoring, and feature implementation in the Intelligent Services Marketplace.
  Covers the Java 21 / Spring Boot multi-module backend (domain, application,
  infrastructure, app), the Next.js frontend, Flyway migrations, persistence and
  concurrency rules, error/security conventions, and the testing and CI gates.
  Enforces a plan-first workflow, codebase discovery before writing code (DRY),
  SOLID, KISS, YAGNI, and cross-skill coordination.
---

# Code Engineering & Architectural Standards Skill

This skill governs how code is designed, written, and verified in this repository. Any AI agent or developer generating, modifying, or refactoring code must comply with it.

---

## 1. Non-Negotiable Rules

1. **Plan before writing.** Inspect, then state the design, then implement.
2. **Search before creating.** Reuse an existing class, port, mapper, value object, or component.
3. **Respect module boundaries.** `domain` and `application` must not import Spring, JPA, or outer layers; this is enforced by ArchUnit.
4. **Schema and code change together.** A JPA mapping change without a Flyway migration fails `ddl-auto: validate`.
5. **One clock.** Never call `LocalDateTime.now()`, `Instant.now()`, or SQL `now()` in production code; inject `Clock`.
6. **Explicit writes.** Insert-only history is append-only; updates use optimistic locking, never last-write-wins.
7. **Every behaviour gets a test.** Unit tests for logic; Testcontainers tests for SQL, migrations, and concurrency.
8. **No secrets in code, tests, examples, or logs.** Local secrets live in `.env`; CI scans with gitleaks.

---

## 2. Repository Map & Stack

### 2.1 Backend: Maven multi-module Clean Architecture

| Module | Contains | Must not depend on |
| :--- | :--- | :--- |
| `backend/marketplace-domain` | Domain models (`Lead`, `User`, `Credits`), enums, domain exceptions, repository/gateway **ports** | Spring, JPA, Hibernate, application, infrastructure, app |
| `backend/marketplace-application` | Use-case services, audit helpers, application exceptions | Spring, JPA, Hibernate, infrastructure, app |
| `backend/marketplace-infrastructure` | JPA entities, Spring Data repositories, adapters, `UnitOfWork` implementation, configuration, Stripe/security adapters | `marketplace-app` |
| `backend/marketplace-app` | Controllers, DTOs, mappers, security filters, `GlobalExceptionHandler`, Flyway migrations, `application.yaml` | `marketplace-infrastructure` |

Wiring order: `Controller → application service → domain port → infrastructure adapter → Spring Data → PostgreSQL`.

Application services are **plain classes** (no `@Service`); `ApplicationServiceConfig` in infrastructure registers them as `@Bean`s and owns the `Clock` bean.

`backend/marketplace-app/src/test/java/ro/marketplace/architecture/CleanArchitectureTest.java` enforces these rules. Never weaken an ArchUnit rule to make code compile.

### 2.2 Stack

| Area | Versions / choices |
| :--- | :--- |
| Backend | Java 21, Spring Boot 4.1.x, Maven (`backend/mvnw`) |
| Database | PostgreSQL 18 in `compose.yaml`; Testcontainers use `postgres:16-alpine` |
| Migrations | Flyway, `ddl-auto: validate`, single baseline `V0` |
| Frontend | Next.js 16 App Router, React 19, TypeScript 5, Tailwind 4 |
| CI | GitHub Actions: `backend-tests.yml`, `sonar.yml`, `secrets-check.yml` |

### 2.3 Frontend layout

`frontend/app` (routes), `components`, `hooks`, `services`, `models`, `types`, `theme`, `context`. Shared types live in `frontend/types`; do not redeclare a backend DTO shape in several files.

---

## 3. The Engineering Lifecycle

Every task follows these phases in order. Do not skip phase 2 or phase 8.

```
1. Requirements & contracts
2. Codebase discovery & anti-duplication
3. Written plan
4. Data model & migration design
5. Implementation
6. Persistence, time & concurrency review
7. Cross-skill coordination (ui-skill)
8. Tests, verification & quality gate
```

### Phase 1: Requirements & Boundaries
- Identify the acting role: **Client**, **Professional**, or **Administrator**.
- Define the API contract first: method, path, request/response record, validation, error codes.
- Mark any credit, payment, claim, or status operation as a **transactional boundary**.

### Phase 2: Discovery & Anti-Duplication
Search the real module paths before creating anything:

```bash
# Domain models, ports, services
find backend/marketplace-domain/src/main -name "*Lead*" -o -name "*Credit*"
find backend/marketplace-application/src/main -name "*Service*"
find backend/marketplace-infrastructure/src/main -name "*Adapter*" -o -name "*Entity*"

# Methods and concepts
git grep -n "deductCredits" backend
git grep -n "PageRequests" backend
git grep -n "getDbValue" backend

# Frontend components, hooks, types
find frontend/components frontend/hooks -iname "*lead*" -o -iname "*card*"
find frontend/types frontend/models -type f
```

Reuse or extend an existing abstraction when it covers ~80% of the need. Never create a second credit calculator, status mapper, pagination helper, or formatter. Read `references/anti-duplication-checklist.md`.

### Phase 3: Written Plan
State before coding:
- files to add/change and which module owns each responsibility;
- new ports and their insert/update semantics;
- migration filename and the exact constraints/indexes;
- test strategy (unit vs Testcontainers) and the commands you will run.

### Phase 4: Data Model & Migration
Design the schema with `V0`/`V1` policy in Section 9. Add CHECKs for enum vocabularies, FKs with explicit `ON DELETE`, and indexes for every FK and frequent filter/sort column.

### Phase 5: Implementation
Follow Sections 4–8. Put business rules in domain/application, SQL in infrastructure adapters, protocol details in controllers.

### Phase 6: Persistence, Time & Concurrency
Re-read Section 7 before committing. Verify insert-only boundaries, optimistic locking, bound timestamps, bounded paging, and actor attribution.

### Phase 7: Cross-Skill Coordination
- Any UI or styling change must activate **`ui-skill`**.
- Use design tokens only; no arbitrary hex colours or inline overrides.
- `'use client'` only when state, effects, or event handlers are required.

### Phase 8: Tests & Quality Gate
Run the commands in Section 11. Do not report a task complete while tests, `ddl-auto: validate`, or the build are failing.

---

## 4. Module Boundaries & SOLID

### 4.1 SRP by layer

| Layer | Single responsibility | Forbidden |
| :--- | :--- | :--- |
| Controller | HTTP binding, validation, `ProblemDetail`, pagination headers | Queries, business rules, repository access |
| Application service | One use case / sub-domain; orchestration and invariants | Spring annotations, JPA, SQL, HTTP types |
| Domain model | Invariants and state transitions | Persistence, framework code, `java.util.concurrent` |
| Repository adapter | Mapping + persistence mechanics | Business decisions |
| JPA entity | Table mapping only | Domain logic |
| React component | One visual responsibility | Duplicated formatters, inline design tokens |

### 4.2 Ports & DIP
- Port interfaces live only in `ro.marketplace.domain.port`; ArchUnit enforces this.
- Name ports by intent, not by storage:
  - `create(...)` / `append(...)` = insert-only.
  - `save(...)` = insert-or-update, and only where optimistic locking is implemented.
  - Focused lifecycle methods (`reserveSlot`, `markAssignedIfFull`, `updateStatus`) instead of a generic `save` for state machines.
- Constructor injection only; `private final` fields; explicit `Objects.requireNonNull` in application/infrastructure constructors. Never field-inject.

### 4.3 Transactions
- Application code uses `UnitOfWork.executeInTransaction(...)`; it must not import `@Transactional`.
- `SpringUnitOfWorkAdapter` runs `PROPAGATION_REQUIRED` + `READ_COMMITTED`, so nested use cases join the caller's transaction.
- Infrastructure adapters mark reads `@Transactional(readOnly = true)`.
- A claim + wallet deduction + ledger + audit entry must commit or roll back together.

### 4.4 Other principles
- **OCP:** add a new branch behind an interface, not another `if` in a central switch.
- **LSP:** implementations honour nullability, error, and side-effect contracts.
- **ISP:** small role-specific ports; do not add unused methods "for symmetry".
- **KISS:** plain methods over reflection, dynamic proxies, or custom frameworks. Keep methods under ~40 lines.
- **YAGNI:** no speculative columns, unused repository methods, caches, event buses, matching strategies, or abstraction layers. This repository has no matching engine or domain-event publisher today; do not add one until a concrete requirement exists.

---

## 5. Domain Invariants

### 5.1 Credits & ledger
- Balances are `Credits` value objects: non-negative, overflow-checked (`plus` uses `Math.addExact`), never raw `int` arithmetic in domain code.
- Mutations go through `WalletService`; amounts must be **positive** and bounded (`MAX_CREDITS_PER_OPERATION`).
- `users.credits_balance` is the authoritative balance.
- `wallet_transactions` is an **append-only ledger**: every mutation writes one row with `balance_after`, amount sign, type, actor, and clock timestamp. Never `UPDATE`/`DELETE` it.
- Insufficient funds is a `ConflictException` (409), not a 400.

### 5.2 Lead lifecycle, claims & idempotency
- There is no `pro_wallet` or `claimed_pros` table. Purchases debit `users.credits_balance` and append to `wallet_transactions`; claims live in `lead_claims` with `UNIQUE (lead_id, pro_id)`.
- Reserve a slot atomically with `LeadRepository.reserveSlot` (`current_pros < max_pros` in the `WHERE`).
- Never resurrect a closed lead: `updateStatus(leadId, expectedStatus, newStatus)` must match the expected status in SQL, and `markAssignedIfFull` only transitions from claimable states.
- A professional cannot be charged twice for the same lead; check/claim uniqueness at the database level.
- Stripe checkout is idempotent: require `Idempotency-Key`, enforce `UNIQUE (user_id, idempotency_key)`, and deduplicate webhooks via the `stripe_webhook_events` primary key.

### 5.3 Privacy
- Mask client phone/email on unclaimed leads.
- Full contact data only for the lead's client, an administrator, or a professional holding a claim.
- Never log secrets, tokens, passwords, full contact data, or raw webhook payloads.

### 5.4 Matching inputs
Use the real lead columns: `work_mode` (`ONSITE`/`REMOTE`), `required_capabilities`, `preferred_capabilities`, `location`, budgets, and category.
- `work_mode` may be `NULL` for legacy/unspecified leads; never infer it.
- Capability arrays are JSONB arrays with `jsonb_array_length(...) <= 50`.
- Any scoring must be deterministic and explainable.

---

## 6. Time, Auditing & Enums

### 6.1 One clock
- `ApplicationServiceConfig.clock()` returns `Clock.systemUTC()`; inject it everywhere.
- Use `LocalDateTime.now(clock)` for timestamps. Tests use `Clock.fixed(Instant.parse("..."), ZoneOffset.UTC)`.
- JPA auditing uses the same clock through `JpaAuditConfig.auditingDateTimeProvider`.
- `spring.jpa.properties.hibernate.jdbc.time_zone: UTC` keeps JDBC aligned.
- Native SQL must bind a timestamp parameter; `now()`/`CURRENT_TIMESTAMP` in application writes is a defect.

### 6.2 Actor attribution
- `AuditActor.resolve(CurrentActorProvider)` produces the bounded `created_by`/`updated_by` value: authenticated principal, truncated to 100 characters, else `"system"`.
- Native bulk updates bypass JPA auditing and **must** set `updated_by` from `AuditActor` themselves.
- `audit_logs` is append-only; actions come from `AuditAction`.

### 6.3 Enums
- Domain enums expose `getDbValue()` and `fromDbValue()` (via `DbEnumValue.parse`).
- Persist and query with `getDbValue()`; never hardcode `'validat'`, `'pro'`, `'atribuit'` in Java.
- Database CHECK constraints must list the same values as the enum.
- Frontend status strings must match the API representation, not the Java constant name.

---

## 7. Persistence & Data Integrity

### 7.1 Insert-only vs update
| Intent | API | Mechanism |
| :--- | :--- | :--- |
| New aggregate | `create(...)` | `springData.save(newEntity)`; no lookup |
| Immutable history row | `append(...)` | Entity implements `Persistable<UUID>` with `isNew() == true` |
| Existing mutable row | focused update | Load managed entity, copy mutable fields, let `@Version` check |

- Append-only tables: `wallet_transactions`, `audit_logs`, `credit_grants`, `lead_claims`, `stripe_webhook_events`. A repeated id must fail, never rewrite history.
- Do not rebuild a detached entity and `save()` it for an update: the rebuilt instance loses `@Version` and becomes an insert/last-write-wins.
- Use `EntityMapper.updateEntity(domain, managedEntity)` to copy only mutable fields; never overwrite id, `createdAt`, or `version`.
- No `ON CONFLICT DO UPDATE` upserts and no "insert-or-ignore" that hides duplicate writes.

### 7.2 Optimistic locking
- Mutable tables have `version BIGINT NOT NULL DEFAULT 0`; entity fields use `@Version`.
- Map `version` through domain ↔ entity ↔ response DTO.
- `Update...Request` accepts an optional `version`; a mismatch is rejected with `ConflictException` (409) before saving.
- The database check remains the safety net for the load→save race; map it to 409 via `ObjectOptimisticLockingFailureException`.
- `CustomerResponse` exposes `version`; other mutable resources follow the same pattern when they gain concurrent edits.

### 7.3 Native SQL
When JPQL cannot express an atomic operation:
- Bind enum values via `getDbValue()`; never string-concatenate user input.
- Put state guards in the `WHERE` clause (`status IN (...)`, `current_pros < max_pros`, `version = ...`).
- Write `updated_at = :updatedAt` bound from the injected `Clock`; write `updated_by = :actor` from `AuditActor`.
- Annotate with `@Modifying(clearAutomatically = true, flushAutomatically = true)`.
- Return the affected-row count and treat `0` as a conflict/no-op, not success.
- Test the bound parameters with a fixed clock.

### 7.4 Paging
- Use `PageRequests.of(page, size, sort)` / `PageRequests.limit(requested, fallback)`; maximum page size is 100, default 50.
- Return `PageResult<T>` from ports and set `X-Total-Count`, `X-Page`, `X-Page-Size` via `PageHeaders`.
- Every paged query needs a **stable** sort: at minimum `(createdAt DESC, id DESC)`. Without a tie-breaker, offset pages duplicate or skip rows.
- Never expose an unbounded `findAll()` on a user-facing list.

### 7.5 Entities
- Keep JPA annotations in infrastructure entities, never in domain models.
- `@Column(nullable = false)` must match the migration.
- Prefer explicit `@Table(name = ...)` and column names.
- Do not add `@Data`/`@EqualsAndHashCode` to entities; use `@Getter/@Setter` and explicit constructors/builders.

---

## 8. API, Errors & Security

### 8.1 API
- DTOs are Java `record`s; map explicitly in `marketplace-app/.../mapper`.
- Validate with Jakarta annotations (`@NotBlank`, `@Email`, `@Size`, `@Positive`, `@Min`).
- Return `ResponseEntity`/`ProblemDetail`; never leak entities from controllers.
- Controllers must not import repositories (ArchUnit rule); go through an application service.
- Keep request/response field names stable; add fields rather than renaming.

### 8.2 Error mapping (`GlobalExceptionHandler`)
| Exception | Status | `type` suffix |
| :--- | :--- | :--- |
| `ValidationException` | 422 | `validation` |
| `DomainException` | 400 | `domain-violation` |
| `ConflictException`, `DataIntegrityViolationException`, `ObjectOptimisticLockingFailureException` | 409 | `conflict` |
| `ResourceNotFoundException` | 404 | `not-found` |
| `AccessDeniedException` | 403 | `forbidden` |
| `BadCredentialsException` | 401 | `unauthorized` |
| Payload too large | 413 | `payload-too-large` |
| Stripe misconfiguration / provider failure | 503 / 502 | `payment-*` |

Use `https://api.marketplace.ro/errors/<suffix>` as the `type`. Do not echo untrusted input values in error messages.

### 8.3 Security
- Secrets come from environment variables; there is no development fallback for `JWT_SECRET`. `.env` is local-only and git-ignored.
- The `prod` profile must supply `DB_PASSWORD`, `JWT_SECRET`, and `CORS_ALLOWED_ORIGINS`.
- Keep rate limits (`login`, `register`, `refresh`) and the global request-size limit enabled.
- Stripe webhooks: verify the signature **before** parsing, enforce the CIDR allowlist, and process each event id once.
- Never trust role or ownership claims from the request body; derive them from the authenticated principal.
- No `System.out`, no secrets in logs, no stack traces in HTTP responses.

---

## 9. Migrations

### 9.1 Location and versions
```
backend/marketplace-app/src/main/resources/db/migration/
└── V0__initial_schema.sql      # single baseline (replaced V1..V11)
```
- `V0` is the complete pre-release baseline: final schema, constraints, indexes, and the 5 seeded `service_categories`.
- **The next migration is `V1__<description>.sql`.** Never reuse a version number.
- The app is unreleased, but `V0` is now shared: do not rewrite it in a feature branch. Schema changes ship as new migrations.
- A deliberate re-consolidation is a separate, reviewed refactor: replay the chain on an empty database, diff `pg_dump --schema-only`, replace the files, and reset local volumes.

### 9.2 Rules
- Flyway owns the schema; `ddl-auto: validate` must pass. An entity change without a migration is a bug.
- Never modify a migration that has been merged or applied anywhere.
- Use `CREATE TABLE` / `ALTER TABLE` with explicit constraint names (`chk_`, `uq_`, `fk_`, `idx_`).
- Add CHECK constraints for every enum vocabulary, matching the Java enum's `getDbValue()`.
- Add FKs with explicit `ON DELETE CASCADE/RESTRICT/SET NULL`, plus indexes on FK columns.
- Use neutral defaults: `users.role = 'client'`, `rating = 0.00`, `trust_score = 0.00`, passport defaults `FALSE`/`0`/`Nedeterminat`, `client_is_verified = FALSE`.
- Timestamps are `TIMESTAMP WITHOUT TIME ZONE`, written in UTC by the application.
- Seed data must be deterministic and conflict-safe (`ON CONFLICT (id) DO NOTHING`).
- No data-migration blocks and no legacy tables in the baseline; a fresh database never creates them.
- Every new migration gets a Testcontainers test proving it applies to an empty database.

### 9.3 Local reset
Rewriting migration history makes existing Flyway databases invalid (`V0` would be out of order). Reset once:

```bash
docker compose down --volumes        # destroys postgres_data + sonar volumes + maven cache
docker compose up -d db backend      # Flyway applies V0; healthcheck proves ddl-auto validate
```

Verify:
```bash
docker exec -it marketplace-db psql -U "$DB_USER" -d "$DB_NAME" \
  -c "SELECT version, description, success FROM flyway_schema_history;"
```

The history must contain exactly one row, version `0`. Then check `service_categories` has 5 rows and the backend is `healthy`.

---

## 10. Testing & Quality Gates

### 10.1 Test layers
| Layer | Tool | What it proves |
| :--- | :--- | :--- |
| Domain/application unit | JUnit 5 + Mockito | Invariants, orchestration, error mapping |
| Adapter unit | Mockito | Bound parameters, enum values, clock timestamps, paging sort |
| Integration | Testcontainers PostgreSQL | Real SQL, constraints, transactions, concurrency |
| Migration | Testcontainers + Flyway | `V0`/new migrations produce the expected schema and seeds |
| Architecture | ArchUnit | Module boundaries and port placement |

### 10.2 Conventions
- Time-sensitive tests use a fixed `Clock` and assert the exact bound `updated_at`.
- Testcontainers classes: `@Testcontainers(disabledWithoutDocker = true)`, `@Container @ServiceConnection static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16-alpine")`.
- Use `org.testcontainers.postgresql.PostgreSQLContainer` (Testcontainers 2.x / Boot 4), not the legacy `org.testcontainers.containers.PostgreSQLContainer`.
- Never make a unit test depend on Docker; never hide a broken integration test by removing `disabledWithoutDocker`.
- When a migration is added or consolidated, update `InitialSchemaMigrationTest` so it pins the final contract (table set, seeds, defaults, key CHECKs).

### 10.3 Commands
```bash
# Backend, with Docker available (integration + migration suites run)
cd backend && ./mvnw -B clean test

# Same, inside a container with the Docker socket
docker run --rm \
  -v "$PWD/backend":/app -v marketplace-m2:/root/.m2 \
  -v /var/run/docker.sock:/var/run/docker.sock \
  -e TESTCONTAINERS_HOST_OVERRIDE=host.docker.internal \
  -w /app eclipse-temurin:21-jdk-alpine \
  sh -c "chmod +x mvnw && ./mvnw -B clean test"

# Focused integration test
docker run --rm -v "$PWD/backend":/app -v marketplace-m2:/root/.m2 \
  -v /var/run/docker.sock:/var/run/docker.sock \
  -e TESTCONTAINERS_HOST_OVERRIDE=host.docker.internal \
  -w /app eclipse-temurin:21-jdk-alpine \
  sh -c "chmod +x mvnw && ./mvnw -B -pl marketplace-app -am \
    -Dtest=InitialSchemaMigrationTest -Dsurefire.failIfNoSpecifiedTests=false test"

# Container image
docker compose build backend

# Frontend
cd frontend && npm run lint && npm run build
```
Use `clean` whenever migrations or resources changed: stale `target/classes` can replay deleted migrations.

### 10.4 CI
- `.github/workflows/backend-tests.yml` runs `./mvnw -B verify` on JDK 21 Ubuntu with Docker, so Testcontainers suites execute.
- `.github/workflows/sonar.yml` runs analysis; zero new blocker/critical issues.
- `.github/workflows/secrets-check.yml` runs gitleaks; never commit `.env`, tokens, or keys.
- Current baseline: **241 tests, 0 failures, 0 skipped** with Docker.

---

## 11. Completion Checklist

Before reporting a task complete:

- [ ] **Plan & discovery:** searched the codebase, reused existing abstractions, no duplicate logic.
- [ ] **Boundaries:** domain/application are framework-free; controllers do not touch repositories; dependencies follow the module table.
- [ ] **Schema:** every entity/mapping change has a Flyway migration; `ddl-auto: validate` passes; `V0` was not edited in a feature branch.
- [ ] **Writes:** insert-only history uses `create`/`append`; mutable updates use `@Version` and a managed row; stale versions return 409.
- [ ] **Native SQL:** enum values bound via `getDbValue()`, state guards in `WHERE`, `updated_by`/`updated_at` bound from `AuditActor` and `Clock`.
- [ ] **Time:** no `now()`/`LocalDateTime.now()`/`Instant.now()` in production paths; the injected UTC clock is used.
- [ ] **Paging:** bounded by `PageRequests`, stable sort with an `id` tie-breaker, `PageResult` + headers.
- [ ] **Credits:** positive amounts, `Credits` semantics, append-only ledger, atomic balance update, no overdraft.
- [ ] **Privacy & security:** masked contacts, no secrets in code/logs, signature and idempotency checks intact.
- [ ] **Tests:** unit + integration as appropriate; migration test updated; full suite green; `docker compose build backend` succeeds.
- [ ] **Frontend:** `ui-skill` tokens only, no `any`, lint and build pass.
- [ ] **Docs:** skill/reference docs updated if a convention changed.

---

## 12. References

- [Anti-duplication checklist](references/anti-duplication-checklist.md)
- [Persistence & migrations rules](references/persistence-and-migrations.md)
- [SOLID / KISS / YAGNI guide](references/solid-kiss-yagni-guide.md)
- [Design patterns playbook](references/design-patterns-playbook.md)
