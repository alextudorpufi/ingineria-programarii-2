package ro.marketplace.infrastructure.observability;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

@Aspect
@Component
public class SqlActivityAspect {

    private static final Logger LOG = LoggerFactory.getLogger(SqlActivityAspect.class);
    private static final Pattern STRING_LITERAL =
            Pattern.compile("'(?:''|[^'])*'");
    private static final Pattern POSTGRES_CAST_LITERAL =
            Pattern.compile("\\b(?:-)?\\d+(?:\\.\\d+)?(?:[Ee][+-]?\\d+)?(?:::[\\w.]+)?");
    private static final int MAX_SQL_TEXT_LENGTH = 500;

    private final SqlAnalysisContext context;
    private final boolean enabled;
    private final long slowThresholdMs;
    private final boolean logStatements;

    public SqlActivityAspect(SqlAnalysisContext context,
                             @Value("${app.sql-analysis.enabled:false}") boolean enabled,
                             @Value("${app.sql-analysis.slow-threshold-ms:250}") long slowThresholdMs,
                             @Value("${app.sql-analysis.log-statements:false}") boolean logStatements) {
        this.context = context;
        this.enabled = enabled;
        this.slowThresholdMs = Math.max(0, slowThresholdMs);
        this.logStatements = logStatements;
    }

    @Around("@within(org.springframework.web.bind.annotation.RestController) || " +
            "within(ro.marketplace.application.service..*)")
    public Object analyze(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!enabled) {
            return joinPoint.proceed();
        }

        String operationName = joinPoint.getSignature().toShortString();
        boolean outerOperation = context.enter(operationName);
        long startedAt = System.nanoTime();
        try {
            return joinPoint.proceed();
        } finally {
            long durationMs = (System.nanoTime() - startedAt) / 1_000_000;
            if (outerOperation) {
                report(operationName, durationMs);
                context.exit(true);
            } else {
                context.exit(false);
            }
        }
    }

    private void report(String operationName, long operationDurationMs) {
        List<SqlStatement> statements = context.statements();
        LOG.info("SQL activity: operation={} sqlCount={} operationDurationMs={}",
                operationName, statements.size(), operationDurationMs);

        for (SqlStatement statement : statements) {
            String safeSql = sanitizeSql(statement.sql());
            if (statement.durationMs() >= slowThresholdMs) {
                LOG.warn("Slow SQL: operation={} durationMs={} sql={}",
                        operationName, statement.durationMs(), safeSql);
            } else if (logStatements) {
                LOG.info("SQL statement: operation={} durationMs={} sql={}",
                        operationName, statement.durationMs(), safeSql);
            }
        }
    }

    static String sanitizeSql(String sql) {
        if (sql == null || sql.isBlank()) {
            return "<empty>";
        }
        String sanitized = STRING_LITERAL.matcher(sql).replaceAll("?")
                .replaceAll("--[^\\r\\n]*", "")
                .replaceAll("/\\*.*?\\*/", "");
        sanitized = POSTGRES_CAST_LITERAL.matcher(sanitized).replaceAll("?");
        sanitized = sanitized.replaceAll("\\s+", " ").trim();
        if (sanitized.length() > MAX_SQL_TEXT_LENGTH) {
            return sanitized.substring(0, MAX_SQL_TEXT_LENGTH) + "...";
        }
        return sanitized;
    }
}
