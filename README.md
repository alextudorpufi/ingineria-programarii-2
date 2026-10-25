# Intelligent Services Marketplace

Software Engineering project: a web platform that connects clients with professionals through paid leads. Clients publish service requests for free; eligible professionals spend virtual credits to unlock contact details and start a conversation.

The project follows the [project proposal](https://docs.google.com/document/d/1k8P3i-QepIU9V51fEKPxdXBERiOJDkZDnbgApnwsyuU/edit), with implementation planned for weeks 3–14.

The [C4 Level 1 system context diagram](docs/system-context.md) shows the platform, its users, and its external integrations.
The [C4 Level 2 container diagram](docs/container-diagram.md) expands the platform into its web application, backend, and database.

## Team and proposed responsibilities

The proposal defines the project scope but does not assign individual roles. The following allocation is a proposed working plan. Each member will implement, test, document, and integrate their assigned features, collaborating wherever frontend and backend work meet.

| Team member | Main responsibility | Expected deliverables |
| --- | --- | --- |
| Tudor Alexandru | Backend foundation, security, transactions, and infrastructure | Spring Boot application, database migrations, authentication and authorization, credit purchases, WebSocket chat, Docker setup, and CI |
| Teodora Iacob | Client experience and request management | Client-facing Next.js pages, request forms, request APIs and validation, request status views, and the conversational AI interface |
| Sandu Theodor | Matching, AI integration, and dispute logic | Deterministic matching, lead distribution, LLM adapter and structured output validation, dispute APIs, and refund workflow |
| Baltag Bianca | Professional and administrator experience | Professional profiles and dashboards, lead purchase interface, credit history, chat interface, dispute screens, administration dashboard, and product analytics |

### Tudor Alexandru

- Set up the Spring Boot modular monolith and shared PostgreSQL/Flyway infrastructure; coordinate the initial data model with the other members.
- Implement registration, login, and role-based access for clients, professionals, and administrators using Spring Security.
- Implement the virtual credit balance, transaction history, and lead purchase operation. Ensure purchases are atomic, cannot overdraw the balance, and cannot charge twice for the same lead.
- Protect client contact details until a lead is purchased and enforce resource-level access checks.
- Implement WebSocket messaging, message persistence, conversation history APIs, and access limited to authorized participants.
- Prepare Docker-based local execution, GitHub Actions checks, and the final deployment setup.
- Write unit and integration tests for authentication, purchases, concurrent transactions, and chat authorization; document backend setup and API contracts.

### Teodora Iacob

- Set up the Next.js/React/TypeScript frontend foundation and shared navigation and form components with Bianca.
- Build registration/login screens and the client dashboard, integrating Tudor's authentication APIs.
- Implement request creation and management across the UI and backend: service category, description, location, budget, deadline, and optional attachments.
- Implement service category and request APIs, server-side validation, and request lifecycle/status handling in coordination with the matching and administration flows.
- Build the conversational request assistant interface using Sandu's AI APIs, including clarification questions and a screen to review and edit the generated request before publishing.
- Keep the traditional request form usable when the AI service is unavailable.
- Write tests for request validation and client journeys; document the client workflow and request API.

### Sandu Theodor

- Implement deterministic matching using service category, geographic coverage, budget, availability, and profile quality, with explicit scoring rules.
- Distribute validated leads to a configurable number of eligible professionals and expose the APIs needed by the professional dashboard.
- Build an isolated LLM adapter for a limited set of service categories. Extract request fields into structured JSON, validate the schema, and ask for missing information without inventing unknown values.
- Coordinate the AI-generated request format with Teodora's request module; require client confirmation before publication.
- Implement dispute submission and administrator decision APIs, keeping a history of decisions and evidence.
- Integrate approved refunds with Tudor's credit transaction module, ensuring refunds are recorded and cannot be applied twice.
- Write matching, AI validation, and dispute tests; prepare annotated scenarios to compare AI-assisted requests with traditional forms.

### Baltag Bianca

- Build professional profile setup and editing screens, and implement the profile APIs for services, locations, availability, experience, and contact information.
- Build the professional dashboard with eligible lead previews, purchase actions, unlocked contact details, balance, and transaction history using Sandu's and Tudor's APIs.
- Build the chat interface, message history, and new-message indicators using Tudor's WebSocket and conversation APIs.
- Build dispute submission and status screens, integrating Sandu's dispute and refund workflow.
- Build the administrator dashboard for user management, request review, disputes, and lead statistics; implement its moderation and reporting APIs with the relevant module owners.
- Instrument key product events in PostHog, such as registration, request publication, lead acceptance, conversation initiation, and dispute submission. Exclude contact details and conversation content from analytics events.
- Write tests for professional and administrator journeys; check usability and responsive layouts, and document these workflows.

## Shared responsibilities

- Agree on requirements, user stories, the data model, and API contracts before implementing dependent features.
- Review one another's pull requests and integrate work continuously into the shared application.
- Each member owns automated tests and documentation for their modules. Use JUnit and Testcontainers for backend behavior and Playwright for complete user journeys.
- Verify that unauthorized users cannot access contacts, conversations, or administrative operations.
- Prepare test data, the final demonstration, and project documentation together.
- A feature is complete when its interface, API, persistence, access checks, and relevant tests work together.

## Implementation plan

| Iteration | Weeks | Team deliverables |
| --- | --- | --- |
| 1 — Analysis and foundation | 3–4 | Everyone: requirements, user stories, architecture, and data model. Tudor: backend, database, Docker, and CI skeleton. Teodora and Bianca: frontend skeleton and main screens. Sandu: matching rules and AI interface design. |
| 2 — Users and requests | 5–6 | Tudor: authentication and roles. Teodora: client journey and request creation. Bianca: professional profiles. Sandu: matching inputs and request eligibility rules. |
| 3 — Matching and monetization | 7–8 | Sandu: deterministic matching and lead distribution. Tudor: atomic lead purchases and credit history. Bianca: lead dashboard and purchase UI. Teodora: client request tracking and integration. |
| 4 — Communication and disputes | 9–10 | Tudor: WebSocket chat and persistence. Bianca: chat, disputes, and administration screens. Sandu: dispute decisions and refund integration. Teodora: client chat integration and request review workflow. |
| 5 — AI and stabilization | 11–12 | Sandu: LLM prototype and validated JSON extraction. Teodora: conversational UI and request confirmation. Bianca: analytics and dashboard refinements. Everyone: integration tests and fixes. |
| 6 — Validation and delivery | 13–14 | Everyone: end-to-end validation, security checks, documentation, and demo. Sandu and Teodora: AI versus form evaluation. Tudor: deployment. Bianca: usability and analytics verification. |

## Scope and technology

The required MVP includes authentication, professional profiles, service requests, deterministic matching, virtual credit purchases, real-time chat, disputes, refunds, and administration. The semester AI objective is a limited conversational prototype that produces a validated request for the client to confirm.

Planned stack: **Next.js, React, TypeScript, Java, Spring Boot, Spring Security, PostgreSQL, Spring Data JPA, Flyway, WebSocket, Docker, GitHub Actions, and PostHog**, with an LLM accessed through a dedicated API adapter.

Semantic matching with embeddings/pgvector, mobile apps, audio/video calls, and real payment processing are future extensions. They are outside the required semester deliverables.

## Running with Docker and SonarQube

- **Application & Infrastructure**: Start PostgreSQL, Spring Boot, and Next.js using `docker compose up -d`.
- **SonarQube Static Analysis**: Start SonarQube and analyze both frontend and backend by running `./run-sonar-analysis.sh` or `docker compose run --rm sonar-analyzer`. See [SonarQube Setup Guide](docs/sonarqube-setup.md) for detailed instructions.
- **Backend Setup**: Refer to [Backend Setup Steps](docs/backend-setup-steps.md).
- **Frontend Setup**: Refer to [Frontend Setup Steps](docs/frontend-setup-steps.md).
