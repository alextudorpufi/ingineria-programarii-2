# Salabot — robotul nostru salahor

Status: proposed implementation plan, aligned with sections 5 and 6 of the [project proposal](project-proposal.md), updated for the decision to combine structured geographic eligibility with semantic ranking. Professional pricing and dated availability are deferred.

Salabot helps a client describe the service they need, clarifies missing information, and prepares a request that the marketplace can match to eligible professionals. The LLM interprets language; Spring Boot validates the request and computes the matches.

## 1. First-version scope

Start with three categories: roof repair, interior painting, and website development. These cover on-site work and remote services without requiring a complete taxonomy for every trade.

Aim for two or three clarification rounds for ordinary requests. This is a usability target, not a rule that forces Salabot to recommend someone before it has enough information. Each round should ask one or two related questions. Clients can answer “I don't know,” edit their answers, or switch to the traditional form.

The first version supports Romanian conversations, structured request extraction, client confirmation, structured eligibility filtering, semantic ranking of eligible professionals, and factual match explanations. It excludes autonomous hiring, purchases, refunds, external browsing, model training, professional pricing and dated-availability matching, and long-term personal memory.

## 2. Client journey

1. The client describes the problem: “Vreau să schimb țigla de pe casă.”
2. Salabot asks the most useful missing questions: locality, approximate roof area, repair versus replacement, materials included, budget, and desired timing.
3. The backend validates each extracted field and identifies remaining gaps or conflicts.
4. The client sees an editable summary, including unknown values, and confirms it.
5. The backend computes eligible professionals and returns an initial shortlist with reasons. If none qualify, it explains which constraints prevented a match and asks whether the client wants to change them.
6. The client explicitly publishes the confirmed request. The marketplace validates and distributes the lead using its normal workflow.

Matching previews are read-only and do not publish requests, unlock contact details, or debit credits. Professionals still purchase eligible leads to contact clients, as defined in the proposal. Revalidate eligibility when distributing a lead because profile declarations and lead-acceptance settings may change after a preview.

## 3. Architecture

```mermaid
flowchart TD
    ui["Next.js<br/>Salabot conversation and request review"]
    orchestrator["Spring Boot<br/>Conversation state and validation"]
    model["LLM adapter<br/>Structured extraction"]
    matching["Matching service<br/>Eligibility, semantic ranking, explanations"]
    embeddings["Embedding adapter<br/>Versioned text representations"]
    db[("PostgreSQL<br/>Drafts, sessions, professional profiles")]

    ui -->|"Messages and confirmation"| orchestrator
    orchestrator -->|"Minimized conversation context"| model
    model -->|"Candidate fields"| orchestrator
    orchestrator -->|"Validated criteria"| matching
    matching -->|"Eligible profiles"| db
    matching -->|"Project and professional service text"| embeddings
    orchestrator -->|"Save and resume"| db
```

These are logical modules inside the existing Spring Boot backend, except for the frontend, database, and model provider. They are not separate microservices.

Proposed backend responsibilities:

| Module | Responsibility |
| --- | --- |
| `matching` | Shared structured eligibility, semantic and structured scoring, stable ordering, and reason codes for both forms and Salabot. |
| `matching.embeddings` | Embedding provider adapter, text preparation, model versions, and cached professional representations. |
| `salabot` | Conversation ownership, turn handling, extraction, clarification, draft revisions, and preview orchestration. |
| `salabot.llm` | Provider adapter, prompt and schema versions, timeouts, bounded retries, and response parsing. |
| Existing request module | Confirmed request persistence, publication, and lifecycle. |
| Existing lead and credit modules | Lead distribution, purchases, contact unlocking, and refunds. |

## 4. Define the data contract before prompting

Use the existing [professional profile](professional-profile-storage.md) as the foundation. Before matching, normalize the declarations into shared identifiers:

- Supported category and service IDs.
- Geographic coverage using locality/county IDs and an explicit scope: selected localities, whole county, or nationwide; retain `supportsOnsite` and `supportsRemote` for delivery mode.
- Category-specific capabilities, such as roof replacement or e-commerce development.
- Title, bio, service descriptions, and capability descriptions for semantic comparison.
- `onboardingStatus` and `acceptingLeads` for eligibility, plus a defined profile-completeness measure.

Category IDs are currently validated. The professional editor now selects validated Romanian county codes in `coverageAreas`, including București separately from Ilfov; each selection declares whole-county coverage. Existing free-text coverage remains until explicitly replaced and must not be treated as normalized eligibility. Locality-only coverage and shared service/capability identifiers remain planned prerequisites for finer matching. Services and capabilities are still self-declared text. Preserve original text, ask for confirmation of ambiguous mappings, and distinguish declared capabilities from verified qualifications. Profile completeness must not be presented as verified work quality.

Do not add rates, `availableFrom`, `availableUntil`, or `availabilityConfirmedAt` to this matching scope. Client budget and desired dates may be collected as project context, but current professional profiles cannot establish price or schedule compatibility. `acceptingLeads` expresses willingness to receive leads; it does not confirm availability for a particular date.

Use one request schema for the traditional form and Salabot. An illustrative draft is:

```json
{
  "categoryId": "roofing",
  "serviceIds": ["roof_tile_replacement"],
  "requiredCapabilities": [],
  "preferredCapabilities": [],
  "description": "Înlocuirea țiglei de pe acoperișul casei",
  "location": {
    "countyId": null,
    "localityId": null,
    "localityText": "Iași"
  },
  "workMode": "ONSITE",
  "budget": {
    "min": null,
    "max": null,
    "currency": "RON",
    "basis": "project",
    "scope": null
  },
  "desiredStartDate": null,
  "deadline": null,
  "details": {
    "areaSquareMetres": null,
    "materialsIncluded": null
  }
}
```

IDs above are examples; use IDs from the application catalog. The backend resolves locality text to a known locality, and asks for clarification if ambiguous. Category-specific detail schemas constrain allowed keys and types. Validate dates relative to an explicit conversation date and timezone; ask when phrases such as “next Friday” are ambiguous.

### Current lead contract

`POST /api/leads` now accepts `workMode` (`ONSITE` or `REMOTE`, required for new
leads), `requiredCapabilities`, and `preferredCapabilities`. Both capability lists
are optional, default to empty, and allow up to 50 nonblank descriptions of at most
255 characters each. The application trims and deduplicates entries without case
sensitivity; required capabilities take precedence over duplicates in the preferred
list. These are descriptive declarations, not validated catalog IDs or proof of a
professional's qualifications. Shared capability identifiers remain future work.

The same fields are returned in lead detail and list responses, retained by the
frontend lead model, editable in the project review panel, and displayed in lead
details. Migration V5 stores capabilities in JSONB arrays and leaves existing work
modes null rather than guessing. Older leads therefore display an unspecified work
mode. New lead creation requires an explicit choice, even through non-HTTP service
callers. No country field is introduced; the marketplace operates in Romania.

The existing lead `location` remains free text. Resolving it to the professional
county coverage catalog remains a geographic matching task; a county is a județ,
not a country. The conversational extraction and matching services are still planned;
the current keyword-based chat demonstration does not constitute LLM extraction.

Missing values stay `null`. An unmentioned capability is unknown, not automatically false. Keep field evidence separately: source message ID and exact supporting text, plus whether a value was user-provided, inferred, or confirmed. Evidence supports review but does not by itself prove an extraction correct.

For the initial version, preview requires a known category, a meaningful description, an agreed work mode, and a resolved locality for on-site services. Each category defines any additional required fields. Budget and timing can remain unknown. If the client makes either mandatory, retain that requirement and mark compatibility as requiring professional confirmation; do not label the shortlist as satisfying it.

## 5. Build structured eligibility and semantic ranking

Use database eligibility filtering followed by structured and semantic scoring. Semantic similarity ranks eligible professionals; it cannot override geographic or mandatory-capability requirements. Build a reproducible structured baseline first, then include semantic ranking in the first-version delivery and evaluation.

Hard filters:

- Account is active, professional onboarding is `COMPLETE`, and `acceptingLeads` is true.
- Professional offers the required category and mandatory capabilities.
- For on-site requests, the professional supports on-site work and structured coverage includes the project locality. For remote requests, the professional supports remote work; local coverage is not required.

For mandatory constraints, unknown professional data does not prove compatibility. Exclude profiles with unresolved mandatory capabilities or coverage from confirmed matches, or show them separately as requiring verification. Budget and timing requirements always need professional confirmation in this scope. Never silently weaken constraints to produce results.

Compare embeddings of the project description and relevant category-specific details with professional title, bio, services, and capabilities. Exclude contact information and authentication data. Cache professional embeddings and refresh them when their source fields change. Use the same versioned text preparation and embedding model for both sides. Select the model through Romanian-language evaluation; no particular provider or vector database is required by this plan.

For eligible profiles, start with this proposed score:

| Signal | Weight |
| --- | --- |
| Semantic relevance of project and professional service text | 60% |
| Coverage of additional services and preferred capabilities | 30% |
| Profile completeness | 10% |

Define each signal on a 0–1 scale with documented rules before implementation, including the transformation from embedding similarity to a ranking signal. Calculate over applicable signals with available comparable data, and return evidence coverage alongside the score. Unknown data earns no claim of compatibility; low-coverage results should be visibly marked. Treat weights as a hypothesis to evaluate, not established product truth. Price and schedule are not scoring signals.

Use a stable professional ID as the final tie-breaker. Store the scoring version, embedding model and text-preparation versions, and source profile revisions. Evaluate against a fixed profile and embedding snapshot: identical criteria, representations, data, and rules must yield identical ordering.

Return professional IDs and factual reason codes such as `SERVICE_SUPPORTED`, `AREA_COVERED`, and `REMOTE_SUPPORTED`. Render explanations from templates. Semantic similarity may support a relevance label but does not prove a specific capability, qualification, or courier integration. Do not ask the LLM to invent reasons or select a different ordering. A score is a ranking aid, not a probability of a successful contract.

## 6. Add conversational extraction

On each turn, send the model the relevant conversation, current draft, allowed category/service definitions, extraction schema, and missing-field list. Do not send the full professional catalog, CVs, contact details, or unrelated conversations.

Require structured output, parse it into typed objects, then validate catalog IDs, value ranges, field relationships, evidence, and authorization in Java. Schema-valid output can still misunderstand what the client meant.

Begin with structured extraction rather than a general agent loop. The backend merges validated changes, computes missing fields, and selects the next clarification from category templates. Corrections replace earlier draft values and invalidate confirmation of the changed draft. Preserve explicit uncertainty and ask about contradictions.

If tool calling is added, expose a small read-only tool such as `preview_matches(criteria)`. A tool call is a model-generated request to execute a function; the backend still validates the arguments, authorizes access, and decides whether to execute it. Never expose arbitrary SQL or credit-changing operations to the model.

Low temperature may reduce variation but does not guarantee deterministic extraction. Determinism belongs in the matching service, not in an assumption about repeated LLM responses.

## 7. Minimal conversation workflow

Use an explicit backend state machine:

```text
COLLECTING → REVIEW → CONFIRMED → PREVIEWED → PUBLISHED
```

Clients can return to editing from review or preview. Any edit invalidates the confirmed revision and previous preview. Publication requires the latest client-confirmed revision and the normal request validation rules.

Store session owner, messages, draft revision, confirmation revision, timestamps, and model/prompt/schema versions in PostgreSQL. Limit context size and apply an agreed retention policy. Reject stale concurrent updates and duplicate message submissions using revision checks and idempotency keys.

Start with ordinary HTTP request/response calls; streaming can follow later. Suggested API contracts:

| Endpoint | Purpose |
| --- | --- |
| `POST /api/salabot/sessions` | Create an authenticated client session. |
| `POST /api/salabot/sessions/{id}/messages` | Submit a turn and return the updated draft or clarification. |
| `GET /api/salabot/sessions/{id}` | Resume an owned conversation. |
| `POST /api/salabot/sessions/{id}/confirm` | Confirm a specific draft revision. |
| `POST /api/salabot/sessions/{id}/preview` | Compute read-only matches for that revision. |

Publication should reuse the request module rather than duplicate its business logic in Salabot. Check session ownership on every operation.

## 8. Model hosting and suggested tools

| Option | Plan |
| --- | --- |
| Fireworks-hosted model | First cloud candidate. Evaluate a model that supports the required structured output or tool interface; keep API credentials on the backend. Fireworks documents [tool calling](https://docs.fireworks.ai/guides/function-calling). |
| Local model through LM Studio | Optional experiment using the same evaluation dataset. LM Studio exposes [local HTTP APIs](https://lmstudio.ai/docs/developer/core/server). Select an exact Qwen or other model and quantization only after measuring Romanian extraction quality and speed on available hardware. |
| Jev | Candidate for bounded category selection or routing experiments. Its announcement describes typed decisions and gives up string generation, so it is not a drop-in conversational text generator. Validate decision accuracy separately from type correctness. See [TypeSafe's announcement](https://typesafe.ai/blog/introducing-system-one-models-and-jev). |
| Pi Durable | Defer evaluation until there is a demonstrated need for durable multi-step agent execution. The initial workflow can use Spring Boot and PostgreSQL session state. Adding a harness is a separate architecture decision. |

Define a provider-neutral extraction interface and separate model configuration from domain logic. Do not assume provider-compatible APIs have identical schema or tool behavior; test each adapter.

Cloud inference requires no local inference GPU. The conversation's 24 GB Mac example is anecdotal, not a production sizing estimate. For local hosting, measure model/quantization memory, context length, concurrent sessions, latency, and throughput before specifying hardware. Do not purchase hardware for the prototype before comparing it with hosted inference.

## 9. Evaluation: 50 profiles and 100 cases

Create 50 synthetic professional profiles across the three categories. Build 100 manually annotated request cases with canonical criteria, acceptable eligible profiles, ranking judgments, Romanian conversation variants, expected missing fields, and expected no-match cases.

Include vague descriptions, missing budgets, spelling errors, no diacritics, remote versus local work, ambiguous city/county names, coverage boundaries, semantic paraphrases, similar wording with missing mandatory capabilities, corrections, conflicting dates, unsupported services, and instructions attempting to override the workflow. Verify that budget and dates remain project context and never become unsupported compatibility claims.

Test in three layers:

1. Matching tests: canonical criteria against a frozen profile and embedding dataset must satisfy independently annotated eligibility rules and ranking judgments. Compare semantic ranking with the structured baseline and report top-3 relevance against human judgments. This validates the algorithm itself.
2. Extraction tests: compare conversation-derived criteria against the annotated fields, including correct preservation of unknowns and corrections.
3. End-to-end tests: compare matches produced from canonical criteria with those produced from extracted criteria. Matching the baseline proves extraction consistency, not that the baseline is the best recommender.

Use 60 development cases, 20 validation cases, and 20 held-out final cases. Keep paraphrases of the same underlying case in the same split. Repeat model runs to measure variability and preserve the held-out split for the final comparison.

Proposed acceptance targets, to revise before final evaluation:

| Measure | Initial target |
| --- | --- |
| Mandatory-constraint violations in returned matches | Zero in the test suite |
| Matching repeatability on fixed inputs and data | Identical ordered results |
| Exact category extraction | At least 95% |
| Correct handling of annotated missing budget/date values | No invented values in the test suite |
| Valid output after at most one repair attempt | At least 99% |
| Top-3 overlap with canonical-criteria output | At least 90% averaged across cases with matches |
| Ordinary-case clarification rounds | Median no more than three |

For top-3 overlap, divide shared IDs by the canonical shortlist size, up to three. Evaluate empty-result cases separately. Also report field-level accuracy, retrieval precision/recall against annotated eligibility, p50/p95 response latency, token usage, and cost per completed request. These are proposed targets, not measured results or guarantees.

Compare exact model versions under the same prompts, schemas, profile snapshot, and cases. Record first-attempt failures as well as repaired results; do not hide retries in the accuracy report.

Record the embedding configuration separately from the conversational extraction model. Evaluate semantic ranking on held-out relevance judgments and report whether it improves the structured baseline; extraction consistency alone does not establish semantic matching quality.

## 10. Failure handling and boundaries

- On provider timeout, rate limiting, or invalid output, preserve the draft and offer retry or the traditional form. Set per-call deadlines, per-session turn/token limits, and at most one output-repair attempt.
- If embeddings are unavailable, preserve all eligibility filters and use the structured baseline with an explicit fallback indicator. Do not mix incompatible embedding versions or present fallback results as semantically ranked.
- Do not execute instructions embedded in client text or professional descriptions as system instructions. Model output cannot bypass catalog validation, session ownership, publication confirmation, or credit rules.
- Never invent professionals, qualifications, certifications, availability, or prices. Read displayed facts from persisted profiles and mark unverified claims appropriately.
- Keep client contacts locked until the existing lead purchase rules allow disclosure. Match previews reveal only approved public profile data.
- Track model latency, failures, clarification rounds, confirmation, and publication as metadata. Do not send conversation content or contact details to PostHog, and avoid raw prompts in routine application logs.

## 11. Implementation schedule — weeks 5–14

Tudor Alexandru is the sole implementation owner for all six stages, including Salabot's matching logic, backend, frontend, tests, evaluation, and documentation. This allocation supersedes the shared Salabot ownership proposed earlier; the README's broader marketplace allocation is unchanged.

| Stage | Weeks | Implementation work | Completion condition |
| --- | --- | --- | --- |
| 1. Data contract | 5 | Define schemas for the three categories, shared service/capability IDs, structured locality/county coverage, professional profile inputs, API contracts, and scoring rules. Define embedding source fields and versioning. Create initial synthetic profiles and annotated cases. Reserve development, validation, and held-out splits. | Versioned contracts and sample fixtures are ready; required fields, unknown values, and hard constraints have explicit rules. Pricing and dated availability are excluded from professional matching. |
| 2. Matching baseline and semantic ranking | 6–7 | Implement eligibility filtering, a structured scoring baseline, an embedding adapter and cached professional representations, combined ranking, stable tie-breaking, reason codes, a read-only preview endpoint, and a minimal form and shortlist UI. Add eligibility, semantic relevance, repeatability, and embedding-fallback tests. | A request entered through the form produces reproducible, geographically eligible matches with semantic ranking and factual explanations; no-match and structured-fallback cases are handled explicitly. |
| 3. Conversation backend | 8–9 | Implement owned sessions, persistence and migrations, draft revisions, the state machine, and a provider-neutral LLM adapter. Add structured extraction, backend validation, clarification selection, timeouts, bounded retries, and correction handling. | API-level conversations produce validated drafts; sessions resume correctly, stale updates are rejected, and provider failures preserve the draft. |
| 4. Client journey | 10–11 | Build the Romanian conversation UI, editable review, revision confirmation, match preview, and form fallback. Integrate publication through the existing request module and enforce access rules. | A client can describe a need, clarify it, edit and confirm the request, preview matches, and explicitly publish it. Corrections invalidate earlier confirmation and previews. |
| 5. Evaluation | 12–13 | Complete the 50-profile/100-case dataset. Compare extraction and embedding configurations using the development and validation splits; measure extraction accuracy, geographic eligibility, semantic ranking quality against the structured baseline, variability, latency, and cost. Freeze the chosen configurations before running the held-out evaluation. | A reproducible report compares results with canonical criteria and human relevance judgments, records failures and retries, and assesses the acceptance targets. Final held-out results are reported without tuning against them. |
| 6. Demo and decision | 14 | Fix release-blocking defects, finalize setup and architecture notes, record the provider decision, and prepare repeatable demonstrations of successful matching, corrections, no matches, and provider failure. | The prototype and evaluation report are ready to present, with documented limitations, a selected provider configuration, and an operational form fallback. |

Build test fixtures throughout stages 1–4 rather than postponing them until evaluation. Use synthetic profiles and contract-based adapters if marketplace profile or request APIs are not ready, then replace those adapters with real integrations in stage 4. Authentication, profile data, and request publication remain integration dependencies; fixtures do not count as a completed marketplace integration.

Protect week 14 for stabilization and delivery. Keep the three-category scope fixed, use one hosted extraction provider initially, and treat local-model comparisons, streaming, and an agent harness as optional follow-up work if the core milestones are complete. Structured geographic eligibility and semantic ranking are part of the first-version scope. Professional pricing and dated availability remain deferred. Model training and hardware procurement are not prerequisites for this schedule.
