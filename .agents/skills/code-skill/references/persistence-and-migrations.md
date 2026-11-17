# Persistence, Concurrency, Time & Migrations

Concrete rules and examples for the persistence boundary. Paths are relative to the repository root.

---

## 1. Ownership

| Concern | Owner |
| :--- | :--- |
| Domain state and invariants | `backend/marketplace-domain` |
| Use-case transactions and orchestration | `backend/marketplace-application` (`UnitOfWork`) |
| JPA entities, Spring Data, adapters, SQL | `backend/marketplace-infrastructure` |
| HTTP DTOs and error mapping | `backend/marketplace-app` |
| Schema | `backend/marketplace-app/src/main/resources/db/migration` |

Spring, JPA, and Hibernate imports are forbidden in `domain` and `application`. `CleanArchitectureTest` fails the build if this is violated.

---

## 2. Insert-Only History

Append-only tables: `wallet_transactions`, `audit_logs`, `credit_grants`, `lead_claims`, `stripe_webhook_events`.

### 2.1 Port naming
```java
public interface WalletTransactionRepository {
    /** Appends an immutable ledger entry. Reusing an id must fail. */
    WalletTransaction append(WalletTransaction transaction);
}
```

### 2.2 Entity contract
```java
@Entity
@Table(name = "wallet_transactions")
public class WalletTransactionEntity implements Persistable<UUID> {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Override
    public boolean isNew() {
        // Never let a repeated id turn into an UPDATE.
        return true;
    }
}
```

Rules:
- `isNew()` returns `true` unconditionally for history entities.
- Do not expose `save()` on an append-only port.
- Do not add `@Version` to append-only tables; immutability is the guarantee.
- A duplicate id must fail at the database, not be silently ignored.
- Implement `Persistable` rather than injecting `EntityManager`: `BackendApplicationTests` excludes JPA autoconfiguration, and a `@PersistenceContext` field breaks that context.

---

## 3. Mutable Rows & Optimistic Locking

Migrations add `version BIGINT NOT NULL DEFAULT 0`, entities add `@Version`, and DTOs expose it.

```java
@Version
@Column(name = "version", nullable = false)
private Long version;
```

### 3.1 Update an existing row
```java
@Override
@Transactional
public Customer save(Customer customer) {
    CustomerEntity entity;
    if (customer.getId() == null || customer.getVersion() == null) {
        entity = EntityMapper.toEntity(customer);
    } else {
        entity = repository.findById(customer.getId())
                .map(existing -> {
                    EntityMapper.updateEntity(customer, existing);
                    return existing;
                })
                .orElseThrow(() -> new ResourceNotFoundException(...));
    }
    return EntityMapper.toDomain(repository.save(entity));
}
```

```java
public static void updateEntity(Customer domain, CustomerEntity entity) {
    entity.setName(domain.getName());
    entity.setEmail(domain.getEmail());
    entity.setPhone(domain.getPhone());
    // id, createdAt and version stay owned by JPA
}
```

### 3.2 Stale client versions
```java
if (updatedData.getVersion() != null
        && !updatedData.getVersion().equals(existing.getVersion())) {
    throw new ConflictException(
            "Customer was modified concurrently. Reload the customer and retry.");
}
```
`ConflictException` maps to HTTP 409. `ObjectOptimisticLockingFailureException` is also mapped to 409 for the load→save race.

Never rebuild a detached entity for an update. It loses `@Version`, and Hibernate treats it as a new row or performs a last-write-wins merge.

---

## 4. Native SQL

Use native SQL only when it expresses an invariant that JPQL cannot (atomic counters, guarded transitions).

```java
@Modifying(clearAutomatically = true, flushAutomatically = true)
@Query(value = "UPDATE leads SET current_pros = current_pros + 1, version = version + 1, "
        + "updated_at = :updatedAt, updated_by = :actor "
        + "WHERE id = :leadId AND current_pros < max_pros "
        + "AND status IN (:validatedStatus, :waitingStatus)", nativeQuery = true)
int reserveSlot(...);
```

Mandatory rules:
1. **Bind enums** from `getDbValue()`, never literals.
2. **Guard the state** in `WHERE`; a closed lead must not be resurrected.
3. **Bind `updated_at`** from `LocalDateTime.now(clock)`; never SQL `now()`.
4. **Bind `updated_by`** from `AuditActor.resolve(currentActorProvider)`.
5. `@Modifying(clearAutomatically = true, flushAutomatically = true)`.
6. Treat a `0` row count as "not applied", and translate it into a domain conflict.
7. Never concatenate request values into SQL.

---

## 5. Time

- One bean: `Clock.systemUTC()` in `ApplicationServiceConfig`.
- Application and adapters: `LocalDateTime.now(clock)`.
- JPA auditing: `JpaAuditConfig.auditingDateTimeProvider(Clock)`.
- JDBC: `spring.jpa.properties.hibernate.jdbc.time_zone: UTC`.
- Tests: `Clock.fixed(Instant.parse("2026-01-01T10:00:00Z"), ZoneOffset.UTC)`.

Forbidden in production code:
```java
LocalDateTime.now();     // JVM default zone
Instant.now();           // untestable
now()                    // SQL, inside an application update
CURRENT_TIMESTAMP        // same
```

---

## 6. Actor Attribution

`AuditActor.resolve(CurrentActorProvider)`:
- prefers the authenticated principal;
- truncates to 100 characters (the column width);
- falls back to `"system"` for background jobs.

Use it for `created_by`/`updated_by` in native updates and for JPA auditing. Never write `null` into those columns.

---

## 7. Enums

- Every persisted enum: `getDbValue()` + `fromDbValue()` via `DbEnumValue.parse`.
- Database CHECK constraint lists exactly those values.
- Read tolerantly (case/space) but write canonically.
- Do not compare Java enum names to database strings; do not add ad-hoc `"validat".equals(...)`.

---

## 8. Paging

```java
PageRequests.of(page, size, sort);          // clamps page >= 0, size in [1, 100]
PageRequests.limit(requested, fallback);    // caps a bare limit
```

- Maximum page size: 100. Default limit: 50.
- Every paged query sorts with a unique tie-breaker:
  `Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"))`.
- Return `PageResult<T>`; expose `X-Total-Count`, `X-Page`, `X-Page-Size`.
- Avoid `findAll()` on unbounded, user-facing datasets.

---

## 9. Transactions

- Application: `unitOfWork.executeInTransaction(() -> ...)`.
- Infrastructure: `SpringUnitOfWorkAdapter` uses `PROPAGATION_REQUIRED`, `READ_COMMITTED`.
- Reads: `@Transactional(readOnly = true)` on adapter methods.
- A use case that charges credits, records a claim, writes the ledger, and writes an audit entry must run in one transaction.
- Nested services must join the caller's transaction, not open a new one.

---

## 10. Migrations

### 10.1 Current policy
- `V0__initial_schema.sql` is the single baseline.
- Next migration: `V1__<description>.sql`.
- Never edit `V0` in a feature branch; ship changes as new migrations.
- A deliberate re-consolidation must replay the chain on an empty database and produce an identical `pg_dump --schema-only`.

### 10.2 Conventions
- Explicit names: `chk_*`, `uq_*`, `idx_*`, `fk_*` where the default would be unclear.
- `CHECK` for every enum column.
- `FOREIGN KEY` with explicit `ON DELETE`; index every FK.
- `NOT NULL` plus neutral defaults for trust/verification fields.
- `TIMESTAMP WITHOUT TIME ZONE`, written in UTC.
- Deterministic seeds with `ON CONFLICT (id) DO NOTHING`.
- No data backfills in the baseline.

### 10.3 Verification
```bash
docker compose down --volumes
docker compose up -d db backend
docker exec -it marketplace-db psql -U "$DB_USER" -d "$DB_NAME" \
  -c "SELECT version, description, success FROM flyway_schema_history;"
docker exec -it marketplace-db psql -U "$DB_USER" -d "$DB_NAME" \
  -c "SELECT count(*) FROM service_categories;"
```

Expected: one history row (`0`, success), 5 categories, backend `healthy` (Hibernate validation passed).

For a new migration, also run the Testcontainers migration test against an empty database.

---

## 11. Anti-Patterns

| Anti-pattern | Why it is wrong | Correct approach |
| :--- | :--- | :--- |
| `save()` on a rebuilt entity for an existing row | Loses `@Version`, becomes upsert | Load managed row + copy fields |
| `ON CONFLICT DO UPDATE` for a ledger row | Rewrites financial history | Append-only `Persistable.isNew() = true` |
| `updated_at = now()` in native SQL | Bypasses the app clock, breaks tests | Bind `LocalDateTime.now(clock)` |
| `status = 'validat'` in Java | Drifts from the enum/CHECK | `LeadStatus.VALIDAT.getDbValue()` |
| `findAll()` for an API list | Unbounded response, unstable pages | `PageRequests` + `PageResult` |
| Sort by `createdAt` only | Duplicate/skipped rows across pages | Add `id` tie-breaker |
| `LocalDateTime.now()` in a service | Zone-dependent, untestable | Injected UTC `Clock` |
| Editing an applied migration | Breaks every existing database | New migration version |
| Adding a JPA field without a migration | `ddl-auto: validate` fails at startup | Migration in the same PR |
