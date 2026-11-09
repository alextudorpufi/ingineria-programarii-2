# Pre-push verification — 2026-11-06

Status: runtime tests, builds and seed checks pass; **SonarQube quality gates still fail**.
No commit or push was performed. The application database was not seeded or reset.

## Checks completed

- Backend: 105 tests, zero failures/errors/skips, including Flyway migration,
  professional profile persistence/authorization, optimistic locking and lead matching fields.
- Frontend: ESLint and production webpack build passed after the final changes.
- Seeds: fresh 5,000 and 100,000 rows per main entity; 5k rerun; 5k → 100k;
  100k rerun; 100k → 5k. Full-row hashes verify reruns preserve existing data,
  including explicitly edited credentials, credit balances and profile versions.
- Seed assertions: exact counts, category references, county codes, canonical roles,
  complete profiles, lead work modes/capabilities and claim counts. PostgreSQL
  enforced the schema's foreign keys/check constraints throughout.
- Both seeds were tested against migration SQL V1–V5 in disposable PostgreSQL 18
  databases. Backend integration tests separately exercise Flyway.
- `git diff --check` passed. `.env` and `.scannerwork` are ignored. Applied
  migration files were not edited during this verification.
- Runtime searches found no `PROFESSIONAL` alias or removed availability fields;
  the alias remains only in negative authorization tests.
- Both Sonar scans completed and processed. New duplicated lines: **0.0%** each.

## Corrections made during verification

- Replaced duplicated seed bodies with one parameterized generator and two size wrappers.
- Fixed 100,000-category ID truncation and conflicting demo administrator identities.
- Removed reseed updates that reset credentials, edited identity fields and balances.
- Fresh seeds now supply normalized county IDs and professional/lead matching fields.
  Existing rows remain untouched; rerunning does not retrofit legacy rows.
- Shared JWT role parsing between controller identity and Spring authorities.
- Removed duplicate exception literals and simplified AI matching draft initialization.
  The final scan confirms the two findings introduced today are resolved.
- Consolidated scanner Dockerfiles, fixed the JDK image and server health check,
  repaired Maven reactor analysis, and included the complete frontend source tree.
- Added the repeatable isolated `scripts/verify_seeds.py` check.

## Remaining quality blockers

Both projects report 0% new-code coverage because no coverage reports are imported.
Passing tests do not prove the 80% coverage threshold is met. Configure coverage
collection/import and address any measured shortfall before claiming a green gate.

Frontend hotspot review is 0% (two outstanding reviews): recursive Docker context
copy and the random fallback rating ID in `models/RatingProtocolModel.ts`. These
are review findings, not automatically confirmed vulnerabilities. Their Sonar
statuses were not changed or waived.

Existing critical code-smell findings remain: 11 backend and 10 frontend, all
created before this verification. The gates currently pass reliability, security,
maintainability ratings and duplication, but this does not mean there are no critical
code smells. Details from the final scan:

### marketplace-backend

- `marketplace-app/src/main/java/ro/marketplace/app/security/SecurityConfig.java:72` — Define a constant instead of duplicating this literal "ADMIN" 3 times.
- `marketplace-app/src/test/java/ro/marketplace/BackendApplicationTests.java:67` — Add at least one assertion to this test case.
- `marketplace-application/src/main/java/ro/marketplace/application/service/CustomerServiceImpl.java:46` — Define a constant instead of duplicating this literal "id must not be null" 3 times.
- `marketplace-application/src/main/java/ro/marketplace/application/service/LeadServiceImpl.java:53` — Refactor this method to reduce its Cognitive Complexity from 19 to the 15 allowed.
- `marketplace-application/src/main/java/ro/marketplace/application/service/WalletServiceImpl.java:49` — Define a constant instead of duplicating this literal "User not found with id: " 3 times.
- `marketplace-application/src/main/java/ro/marketplace/application/service/WalletServiceImpl.java:58` — Define a constant instead of duplicating this literal " credits" 3 times.
- `marketplace-application/src/main/java/ro/marketplace/application/service/WalletServiceImpl.java:66` — Define a constant instead of duplicating this literal "amount" 3 times.
- `marketplace-application/src/main/java/ro/marketplace/application/service/WalletServiceImpl.java:67` — Define a constant instead of duplicating this literal "balanceBefore" 3 times.
- `marketplace-application/src/main/java/ro/marketplace/application/service/WalletServiceImpl.java:68` — Define a constant instead of duplicating this literal "balanceAfter" 3 times.
- `marketplace-application/src/main/java/ro/marketplace/application/service/WalletServiceImpl.java:69` — Define a constant instead of duplicating this literal "transactionId" 3 times.
- `marketplace-infrastructure/src/main/java/ro/marketplace/infrastructure/persistence/adapter/LeadRepositoryAdapter.java:138` — Define a constant instead of duplicating this literal "leadId must not be null" 5 times.

### marketplace-frontend

- `components/common/ProfileView.tsx:27` — Refactor this function to reduce its Cognitive Complexity from 21 to the 15 allowed.
- `components/views/AnalyticsView.tsx:8` — Refactor this function to reduce its Cognitive Complexity from 22 to the 15 allowed.
- `components/views/Header.tsx:20` — Refactor this function to reduce its Cognitive Complexity from 33 to the 15 allowed.
- `components/views/LeadFeed.tsx:42` — Refactor this function to reduce its Cognitive Complexity from 21 to the 15 allowed.
- `components/views/LeadFeed.tsx:276` — Refactor this function to reduce its Cognitive Complexity from 22 to the 15 allowed.
- `components/views/WalletModal.tsx:80` — Refactor this function to reduce its Cognitive Complexity from 20 to the 15 allowed.
- `context/AppContext.tsx:186` — Refactor this function to reduce its Cognitive Complexity from 26 to the 15 allowed.
- `context/AppContext.tsx:361` — Refactor this function to reduce its Cognitive Complexity from 21 to the 15 allowed.
- `models/LeadModel.ts:107` — Refactor this function to reduce its Cognitive Complexity from 31 to the 15 allowed.
- `models/ProfileModel.ts:66` — Refactor this function to reduce its Cognitive Complexity from 27 to the 15 allowed.

## Repeat the checks

Use Java 21 and a running Docker daemon:

```sh
(cd backend && ./mvnw test)
(cd frontend && npm run lint && npm run build -- --webpack)
python3 scripts/verify_seeds.py
./run-sonar-analysis.sh --both
git diff --check
```

Review the processed [backend](http://localhost:9000/dashboard?id=marketplace-backend)
and [frontend](http://localhost:9000/dashboard?id=marketplace-frontend) gates after scanning.
Do not interpret scanner upload success as a passing quality gate.

Seed wallet/history rows remain synthetic UI/load fixtures, not a reconciled ledger.
The profile/lead contract is validated here; the real LLM and matching engine are still
separate implementation work.
