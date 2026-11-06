# Professional profile storage

`user_professional` replaces `user_categories`, `user_services_offered` and
`user_coverage_areas`. Its primary key, `user_id`, references `users.id`; deletion
of an account removes its professional profile. Identity, contacts, authentication,
credits and existing rating data remain in `users`.

## Fields

| Fields | Meaning |
| --- | --- |
| `categories`, `services_offered` | JSONB arrays preserving the existing multi-value profile data. |
| `coverage_areas` | JSONB array of Romanian county codes, such as `RO-IS`; `RO-B` covers București separately from `RO-IF` (Ilfov). Legacy draft text is preserved until explicitly replaced. |
| `capabilities` | JSONB array for structured capability identifiers. |
| `supports_onsite`, `supports_remote` | Nullable delivery-mode declarations; null means unknown. |
| `years_experience` | Optional nonnegative experience declaration. |
| `onboarding_status`, `accepting_leads` | Profiles start as `DRAFT`, not accepting leads. Accepting leads requires `COMPLETE`. |
| Audit columns and `version` | Persistence audit and optimistic locking metadata. |

Migration V3 copies all existing collection values, verifies that every value was
preserved, then drops the old tables in the same Flyway transaction. It does not
guess catalog identifiers from legacy text, infer currency for `users.hourly_rate`,
or approve the synthetic profiles. Migration V4 removes the deferred pricing and
availability columns introduced by V3; applied migration files remain immutable. The development seed scripts create synthetic complete profiles with catalog category IDs,
Romanian county codes, capabilities and work modes, without overwriting profiles already
present. Migrated legacy profiles retain their original draft state and values.

JSONB keeps this foundation in one profile table, at the cost of not providing
foreign-key enforcement for individual identifiers inside arrays. The editing service
validates category IDs against `service_categories` before completing onboarding.
Service names and capabilities remain self-declared text, not verified catalog identifiers.
Completed profile updates require coverage IDs from the shared `RomanianCounty` catalog.
`GET /api/locations/counties` exposes the 41 counties plus București, with Romanian
labels sorted alphabetically. The editor provides searchable, scrollable multi-selection.
County abbreviations follow the [Romanian county coding list](https://legislatie.just.ro/Public/DetaliiDocumentAfis/305779), prefixed with `RO-` for the stored identifiers.
Selecting a county declares coverage of its entire territory; locality-only and radius-based
coverage are not implemented. Selecting all 42 entries expresses nationwide coverage.
Remote-only profiles may leave coverage empty.

Existing free-text coverage is never automatically expanded from a city to a whole county.
It remains visible in the editor and can be retained in drafts. Selecting a county replaces
all legacy text entries with the explicit county selections; one action also removes all
legacy entries for remote-only profiles. Changes persist only when saved. Legacy text must
be replaced or removed before the next completed save. Existing rows are not
rewritten; matching must check valid county IDs rather than assume an older `COMPLETE`
profile has normalized coverage. No schema migration is needed because the existing JSONB
array stores the codes. County coverage is self-declared, not independently verified.

The existing authentication profile response continues to expose `categories`,
`servicesOffered` and `coverageAreas`, now projected from the professional profile.
Professional registration creates a draft profile; client registration does not.

## Editing API and UI

Professionals edit their own profile in the existing profile page. Both
`GET /api/professional-profile/me` and `PUT /api/professional-profile/me` require
a professional role; identity comes from the JWT, never a submitted account ID.

The response/request includes `name`, `companyName`, `title`, `phone`, `bio`,
`categories`, `servicesOffered`, `coverageAreas`, `capabilities`, nullable
`supportsOnsite`/`supportsRemote`, nullable `yearsExperience`, `acceptingLeads`,
`onboardingStatus`, and a required optimistic-lock `version`.

The page supports saving a draft, completing onboarding and discarding unsaved
changes. Completion requires at least one catalog category, one declared service,
a supported work mode, valid county codes for any coverage entries, and at least one
county for on-site work. Experience and capabilities
are optional. A draft cannot accept leads. Completion means required fields have
been filled, not that identity, qualifications or coverage have been verified.

Updates modify a whitelist of editable fields on managed entities. Credentials,
account role, verification badges, ratings, credit balance and other system-owned
fields cannot be changed by this API. A stale version returns 409; malformed input
returns 400 and incomplete/invalid business data returns 422. The frontend retains
unsaved input after errors and offers an explicit reload for conflicts.

The matching engine and actual lead distribution remain separate work.
