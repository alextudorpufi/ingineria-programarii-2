# SQL Statement Analysis

## Purpose

The SQL Statement Analysis component associates JDBC activity with an intercepted application operation and reports:

- the operation name;
- the number of SQL statements executed;
- the total operation duration; and
- statements that meet or exceed the configured slow-query threshold.

It is implemented for Spring Data JPA/Hibernate and PostgreSQL JDBC. It uses a JDBC `DataSource` proxy for execution counts and durations, plus Spring AOP around REST controller methods and application service implementations.

## Enable and configure

The component is disabled by default. Development/test profiles enable it because `!prod` in `application.yaml` activates the development defaults.

| YAML property | Environment variable | Default | Meaning |
| --- | --- | --- | --- |
| `app.sql-analysis.enabled` | `SQL_ANALYSIS_ENABLED` | `false` | Enables the AOP context and `DataSource` proxy |
| `app.sql-analysis.slow-threshold-ms` | `SQL_SLOW_THRESHOLD_MS` | `250` | Marks a statement as slow at or above this duration |
| `app.sql-analysis.log-statements` | `SQL_LOG_STATEMENTS` | `false` | Logs non-slow statement text in development |

Disable or override it explicitly outside development:

```yaml
app:
  sql-analysis:
    enabled: false
    slow-threshold-ms: 250
    log-statements: false
```

The equivalent environment setup is:

```text
SQL_ANALYSIS_ENABLED=true
SQL_SLOW_THRESHOLD_MS=250
SQL_LOG_STATEMENTS=true
```

For production, leave `SQL_ANALYSIS_ENABLED=false` unless temporary diagnosis is intentionally required.

## Output

The normal summary is logged at `INFO`:

```text
SQL activity: operation=LeadController.method sqlCount=3 operationDurationMs=42
```

Statements at or above the threshold are logged at `WARN`:

```text
Slow SQL: operation=LeadController.method durationMs=310 sql=select ... from leads ...
```

The summary reports one operation for nested controller/service interception. Nested service calls do not create additional reports or count the same JDBC executions again.

## Privacy and Flyway behavior

- Prepared-statement bind values are not collected.
- Logged statement text has SQL string literals and numeric literals redacted where possible and is truncated to 500 characters.
- Keep `log-statements=false` outside development.
- Do not log credentials, tokens, bind values, or application parameter objects.
- Flyway migrations execute before or outside an intercepted application operation, and the component records only SQL performed while an operation context is active. Migration scripts require no changes.

## Limitations

- The proxy reports JDBC execution duration supplied by `datasource-proxy`.
- SQL text for static statements containing embedded literals may be sanitized only approximately; the placeholder/literal redaction and 500-character limit limit exposure but do not guarantee removal of every sensitive value.
- Hibernate can execute batched statements; the `DataSource` proxy may report each query entry in the batch according to its listener behavior.
- Self-invocation inside a Spring bean can bypass AOP if the call is not going through a proxy. Controller-to-service calls and Spring-proxied service calls are covered.
