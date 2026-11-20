package ro.marketplace.infrastructure.observability;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SqlAnalysisContextTest {

    private final SqlAnalysisContext context = new SqlAnalysisContext();

    @AfterEach
    void clearContext() {
        context.clear();
    }

    @Test
    void nestedInterceptionsShareOneSqlOperation() {
        assertThat(context.enter("Controller.method")).isTrue();
        assertThat(context.enter("Service.method")).isFalse();

        context.record("select * from users where id = ?", 12);

        context.exit(false);
        assertThat(context.statements()).hasSize(1);

        context.exit(true);
        assertThat(context.statements()).isEmpty();
    }

    @Test
    void statementSanitizerRedactsStringAndNumericLiterals() {
        String sanitized = SqlActivityAspect.sanitizeSql(
                "select * from users where email = 'private@example.com' and id = 123");

        assertThat(sanitized).doesNotContain("private@example.com").doesNotContain("123");
        assertThat(sanitized).contains("?");
    }
}
