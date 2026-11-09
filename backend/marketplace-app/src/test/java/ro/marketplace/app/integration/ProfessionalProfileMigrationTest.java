package ro.marketplace.app.integration;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.sql.DriverManager;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers(disabledWithoutDocker = true)
class ProfessionalProfileMigrationTest {
    @Container
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16-alpine");

    @Test
    void migrationPreservesEveryLegacyValueAndCreatesEmptyProfessionalProfiles() throws Exception {
        Flyway.configure().dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
                .target("2").load().migrate();
        try (var connection = DriverManager.getConnection(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
             var sql = connection.createStatement()) {
            sql.execute("""
                    INSERT INTO users(id,email,name,role) VALUES
                    ('00000000-0000-0000-0000-000000000001','pro@example.com','Pro','pro'),
                    ('00000000-0000-0000-0000-000000000002','empty@example.com','Empty','pro'),
                    ('00000000-0000-0000-0000-000000000003','client@example.com','Client','client');
                    INSERT INTO user_categories VALUES
                    ('00000000-0000-0000-0000-000000000001','IT'),
                    ('00000000-0000-0000-0000-000000000001','Marketing');
                    INSERT INTO user_services_offered VALUES
                    ('00000000-0000-0000-0000-000000000001','Website');
                    INSERT INTO user_coverage_areas VALUES
                    ('00000000-0000-0000-0000-000000000001','Iași');
                    """);
            Flyway.configure().dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
                    .load().migrate();
            try (var rows = sql.executeQuery("""
                    SELECT categories::text, services_offered::text, coverage_areas::text,
                           onboarding_status, accepting_leads, supports_remote
                    FROM user_professional ORDER BY user_id
                    """)) {
                assertThat(rows.next()).isTrue();
                assertThat(rows.getString(1)).isEqualTo("[\"IT\", \"Marketing\"]");
                assertThat(rows.getString(2)).isEqualTo("[\"Website\"]");
                assertThat(rows.getString(3)).isEqualTo("[\"Iași\"]");
                assertThat(rows.getString(4)).isEqualTo("DRAFT");
                assertThat(rows.getBoolean(5)).isFalse();
                assertThat(rows.getObject(6)).isNull();
                assertThat(rows.next()).isTrue();
                assertThat(rows.getString(1)).isEqualTo("[]");
                assertThat(rows.next()).isFalse();
            }
            try (var rows = sql.executeQuery("SELECT to_regclass('user_categories'), to_regclass('user_services_offered'), to_regclass('user_coverage_areas')")) {
                rows.next();
                assertThat(rows.getObject(1)).isNull();
                assertThat(rows.getObject(2)).isNull();
                assertThat(rows.getObject(3)).isNull();
            }
            try (var rows = sql.executeQuery("""
                    SELECT count(*) FROM information_schema.columns
                    WHERE table_schema = 'public' AND table_name = 'user_professional'
                    AND column_name IN ('rates', 'available_from', 'available_until', 'availability_confirmed_at')
                    """)) {
                rows.next();
                assertThat(rows.getInt(1)).isZero();
            }
        }
    }
}
