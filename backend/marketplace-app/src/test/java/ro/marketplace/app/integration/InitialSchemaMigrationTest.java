package ro.marketplace.app.integration;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The baseline migration is the only source of schema, so these assertions pin
 * the contract the consolidated V1..V11 chain used to provide.
 */
@Testcontainers(disabledWithoutDocker = true)
class InitialSchemaMigrationTest {

    private static final Set<String> EXPECTED_TABLES = Set.of(
            "audit_logs",
            "chat_messages",
            "credit_grants",
            "credit_purchases",
            "customers",
            "disputes",
            "flyway_schema_history",
            "lead_claims",
            "leads",
            "ratings",
            "service_categories",
            "service_subcategories",
            "stripe_webhook_events",
            "user_professional",
            "users",
            "wallet_transactions");

    @Container
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16-alpine");

    @Test
    void baselineCreatesFinalSchemaWithoutLegacyTablesOrColumns() throws Exception {
        migrate();

        try (Connection connection = open();
             var sql = connection.createStatement()) {
            assertThat(tables(sql)).isEqualTo(EXPECTED_TABLES);

            try (var rows = sql.executeQuery("""
                    SELECT count(*) FROM service_categories
                    """)) {
                rows.next();
                assertThat(rows.getInt(1)).isEqualTo(5);
            }

            try (var rows = sql.executeQuery("""
                    SELECT column_default FROM information_schema.columns
                    WHERE table_schema = 'public' AND table_name = 'users' AND column_name = 'role'
                    """)) {
                rows.next();
                assertThat(rows.getString(1)).contains("client");
            }

            try (var rows = sql.executeQuery("""
                    SELECT column_default FROM information_schema.columns
                    WHERE table_schema = 'public' AND table_name = 'customers' AND column_name = 'version'
                    """)) {
                rows.next();
                assertThat(rows.getString(1)).isEqualTo("0");
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

    @Test
    void baselineConstrainsLeadMatchingFields() throws Exception {
        migrate();

        try (Connection connection = open();
             var sql = connection.createStatement()) {
            sql.execute("""
                    INSERT INTO users(id,email,name) VALUES
                    ('00000000-0000-0000-0000-000000000001','client@example.com','Client');
                    INSERT INTO leads(id,title,category_id,subcategory,description,location,budget_min,budget_max,
                                      timeline,client_id,client_name,client_phone,client_email,client_city) VALUES
                    ('00000000-0000-0000-0000-000000000002','Existing website','it-software','Web','Keep this text',
                     'Iași',100,200,'Two weeks','00000000-0000-0000-0000-000000000001','Client','123',
                     'client@example.com','Iași');
                    """);

            try (var rows = sql.executeQuery("""
                    SELECT work_mode, required_capabilities::text, preferred_capabilities::text FROM leads
                    """)) {
                rows.next();
                assertThat(rows.getString(1)).isNull();
                assertThat(rows.getString(2)).isEqualTo("[]");
                assertThat(rows.getString(3)).isEqualTo("[]");
            }

            assertThatThrownBy(() -> sql.execute("UPDATE leads SET work_mode = 'UNKNOWN'"))
                    .isInstanceOf(SQLException.class);
            assertThatThrownBy(() -> sql.execute("UPDATE leads SET required_capabilities = '{}'::jsonb"))
                    .isInstanceOf(SQLException.class);
        }
    }

    private static void migrate() {
        Flyway.configure()
                .dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
                .load()
                .migrate();
    }

    private static Connection open() throws SQLException {
        return DriverManager.getConnection(
                POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
    }

    private static Set<String> tables(Statement sql) throws SQLException {
        Set<String> tables = new LinkedHashSet<>();
        try (var rows = sql.executeQuery("""
                SELECT table_name FROM information_schema.tables
                WHERE table_schema = 'public' AND table_type = 'BASE TABLE'
                """)) {
            while (rows.next()) {
                tables.add(rows.getString(1));
            }
        }
        return tables;
    }
}
