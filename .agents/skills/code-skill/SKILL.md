---
name: code-skill
description: >-
  Mandatory software engineering guidelines and coding standards for all code generation,
  refactoring, and feature implementation across backend (Java/Spring Boot) and frontend (Next.js/React).
  Enforces a plan-first workflow, codebase discovery before writing code (zero duplication/DRY),
  SOLID principles, KISS, YAGNI, proper design patterns, and cross-skill coordination.
---

# Code Engineering & Architectural Standards Skill

This skill governs the software engineering standards, architectural patterns, and development workflow for the Intelligent Services Marketplace. Any AI agent or developer generating, modifying, or refactoring code in this repository must comply with these guidelines.

---

## 1. The 7-Phase Engineering Lifecycle

Every coding task—whether adding a single endpoint or building an entire module—must proceed through the following phases in order:

```
[Phase 1: Analyze Requirements]
              ↓
[Phase 2: Codebase Discovery & Anti-Duplication Check]
              ↓
[Phase 3: Formulate Written Plan & Contracts]
              ↓
[Phase 4: Data Modeling & Database Migrations]
              ↓
[Phase 5: Implementation (SOLID, KISS, YAGNI, Patterns)]
              ↓
[Phase 6: Cross-Skill Coordination (UI / SonarQube)]
              ↓
[Phase 7: Testing, Verification & Quality Gate]
```

### Phase 1: Requirement & Boundary Analysis
- Understand the complete user journey and technical constraints.
- Identify the role context: **Client**, **Professional**, or **Administrator**.
- Enforce transactional boundaries: Any operation involving credits, payment, or lead allocation must be strictly atomic.

### Phase 2: Codebase Discovery & Anti-Duplication (DRY)
Before declaring or writing any new file, class, method, function, or component:
1. **Search First**: Run search commands to find if an existing class, method, or helper already exists:
   ```bash
   # Check for existing Java classes, entities, DTOs:
   find backend/src -name "*Lead*" -o -name "*Transaction*"
   git grep -i "calculateScore" backend/src
   git grep -i "deductCredits" backend/src

   # Check for existing React components, models, hooks:
   find frontend/app -name "*card*" -o -name "*lead*"
   find mock-ui/src -name "*Lead*" -o -name "*Model*"
   ```
2. **Reuse & Extend**: If an existing function or class does 80% of what is needed, extend or parameterize it. Never write duplicate classes, parallel validation routines, or redundant formatters.
3. Consult the detailed [Anti-Duplication Protocol Guide](file:///Users/theo/Documents/ingineria-programarii-2/.agents/skills/code-skill/references/anti-duplication-checklist.md).

### Phase 3: Formulate Written Plan & Contracts
- Define the explicit API contracts (JSON request/response DTOs) and TypeScript interfaces before writing business logic.
- Specify HTTP verbs, paths, validation constraints, and error codes (`400 Bad Request`, `402 Payment Required`, `404 Not Found`, `409 Conflict`).

### Phase 4: Data Modeling & Schema Migrations
- All PostgreSQL database schema changes must be versioned Flyway migrations in `backend/src/main/resources/db/migration/V<timestamp_or_version>__<description>.sql`.
- Never modify existing, already-applied migration files.
- Ensure proper constraints: `NOT NULL`, `FOREIGN KEY ... ON DELETE CASCADE/RESTRICT`, `CHECK`, and appropriate `INDEX` on foreign keys and frequently queried columns (e.g. `category_id`, `pro_id`, `created_at`).

### Phase 5: Implementation (SOLID, KISS, YAGNI, Patterns)
Follow the architectural principles detailed in Section 2 and Section 3 below.

### Phase 6: Cross-Skill Coordination
- When touching user interfaces or styles, activate and follow **`ui-skill`**.
- Maintain design token consistency; never write arbitrary hex colors or inline style overrides.

### Phase 7: Verification & Quality Gates
- Run unit/integration tests: `./mvnw test` (Backend) and `npm run lint && npm run build` (Frontend).
- Run SonarQube static code analysis: `./run-sonar-analysis.sh` and ensure zero new blocker or critical issues.

---

## 2. Core Architectural Principles

### 2.1 SOLID Principles with Project-Specific Rules

#### Single Responsibility Principle (SRP)
- **Controllers** only handle HTTP protocol translation, parameter validation, and response serialization. No business rules or queries in controllers.
- **Services** encapsulate a single business sub-domain (e.g., `LeadPurchaseService` is separated from `LeadMatchingService`).
- **Repositories** exclusively handle database queries and persistence.
- **React Components** separate presentational markup from stateful data fetching and complex business logic.

#### Open/Closed Principle (OCP)
- Core workflows must be open for extension without modifying base code.
- Example: Matching algorithms implement the `LeadMatchingStrategy` interface. Adding a new matching rule (e.g., AI semantic embeddings) creates a new strategy class rather than modifying the deterministic rule engine.

#### Liskov Substitution Principle (LSP)
- Implementations of interfaces must honor all pre-conditions and post-conditions.
- Never throw `UnsupportedOperationException` or return unexpected `null` values from interface implementations.

#### Interface Segregation Principle (ISP)
- Favor small, role-tailored interfaces over monolithic "god" interfaces.
- Separate `LeadReaderService` from `LeadPurchaseService` if clients only need read operations.

#### Dependency Inversion Principle (DIP)
- Depend on abstractions (interfaces), never on concrete implementations.
- Always use constructor-based dependency injection in Spring Boot with `private final` fields.
- Avoid field injection (`@Autowired` on fields is strictly prohibited).

---

### 2.2 KISS (Keep It Simple, Stupid)
- Choose straightforward, idiomatic solutions over clever, convoluted abstractions.
- Avoid creating custom wrapper frameworks, unnecessary reflection, or dynamic proxying when a plain method call suffices.
- If a method exceeds 40 lines, break it down into clean, descriptive private helper methods.

---

### 2.3 YAGNI (You Aren't Gonna Need It)
- Implement only the functionality required by the immediate requirement.
- **Forbidden**: Speculative database columns "just in case", generic unused repository methods, premature Redis/caching layers before performance profiling, and empty abstraction layers.
- Defer generalized architectures until there are at least two concrete, existing use cases.

---

## 3. Approved Design Patterns Playbook

Refer to the full playbook with implementations in:
[design-patterns-playbook.md](file:///Users/theo/Documents/ingineria-programarii-2/.agents/skills/code-skill/references/design-patterns-playbook.md)

| Pattern | Marketplace Use Case | Example Structure |
| :--- | :--- | :--- |
| **Strategy** | Lead matching algorithms & dispute refund resolution. | `MatchingStrategy` interface with `DeterministicMatchingStrategy` and `ProximityMatchingStrategy`. |
| **Adapter** | Isolating external LLMs or third-party payment/notification services. | `LlmProviderAdapter` translating raw model outputs into structured, validated JSON DTOs. |
| **Builder / Factory** | Complex lead requests, credit transactions, and audit records. | `ServiceRequest.builder()...build()` ensuring validity upon creation. |
| **Observer / Pub-Sub** | Real-time chat events, lead purchase notifications, and PostHog analytics. | Spring `ApplicationEventPublisher` publishing `LeadPurchasedEvent` consumed by WebSocket and notification listeners. |
| **Repository** | Database query abstraction with Spring Data JPA. | `LeadRepository extends JpaRepository<LeadEntity, UUID>`. |
| **Interceptor / Filter** | Authentication token validation, security roles, and API auditing. | Spring Security `SecurityFilterChain` and Next.js middleware. |

---

## 4. Critical Domain Rules & Concurrency

### 4.1 Atomic Lead Purchases (Credit & Claim Integrity)
A professional purchasing a lead is a critical financial operation:
1. **Transaction Isolation**: Must be wrapped in `@Transactional(isolation = Isolation.READ_COMMITTED)`.
2. **Overdraft Prevention**: Must verify `balance >= leadPrice` and deduct credits atomically in SQL (`UPDATE pro_wallet SET balance = balance - :amount WHERE id = :proId AND balance >= :amount`).
3. **Claim Limits**: Default max of 4-6 claimed professionals per lead. Enforce claim limits with optimistic locking (`@Version`) or database check constraints.
4. **Idempotency**: A professional cannot purchase or be charged for the same lead twice. Check `claimed_pros` table for uniqueness on `(lead_id, pro_id)`.

### 4.2 Lead Contact Privacy & Masking
- Un-purchased leads returned by the API must have client phone and email masked (e.g. `+40 7•• ••• •••` and `c•••••@gmail.com`).
- Full contact details must only be serialized if the requesting user is the lead author (Client), an administrator, or an authorized professional who has unlocked the lead.

### 4.3 Deterministic Matching & Lead Distribution
- Matching rules must be deterministic and explainable:
  - Service Category & Subcategory match (hard requirement)
  - Geographic Coverage / Distance (e.g. within 25 km or specified county)
  - Budget compatibility (client budget meets pro minimums)
  - Availability & response rate score
- Every match must produce an explainable breakdown (`MatchExplanation` DTO) indicating why the professional was selected.

---

## 5. Technology Stack Standards

### 5.1 Backend (Java 21 / Spring Boot 4)
- **DTOs & Immutability**: Use Java `record` for all DTOs and API requests/responses.
- **Validation**: Use Jakarta Validation annotations (`@NotNull`, `@NotBlank`, `@Positive`, `@Size`).
- **Error Responses**: Return standardized `ProblemDetail` (RFC 7807) from `@RestControllerAdvice`.
- **Database Schema**: Every migration must be tested with rollback considerations and Flyway naming `V<n>__<description>.sql`.

### 5.2 Frontend (Next.js 16 / React 19 / TypeScript 5)
- **Strict Typing**: No `any`. Declare shared models in `types/index.ts`.
- **Component Architecture**: Server components by default; `'use client'` only where state or event listeners are required.
- **Styling**: Strictly use `ui-skill` design tokens and Tailwind classes.

---

## 6. Code Review Checklist Before Completion

Before submitting code, verify:
- [ ] **Plan First**: Was the architecture planned before writing code?
- [ ] **Anti-Duplication**: Did I search the codebase and reuse existing functions/classes?
- [ ] **SOLID**: Does each class have a single, well-defined responsibility?
- [ ] **KISS & YAGNI**: Is the code simple, without speculative or unused abstractions?
- [ ] **Financial Integrity**: Are all credit and balance changes atomic and non-overdrawable?
- [ ] **Privacy**: Are client contact details masked until unlocked?
- [ ] **Tests & Quality**: Do all unit tests pass, and does SonarQube pass without critical bugs?
