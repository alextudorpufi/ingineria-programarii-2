package ro.marketplace.app.integration;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.sql.DriverManager;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers(disabledWithoutDocker = true)
class LeadMatchingMigrationTest {
    @Container
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16-alpine");

    @Test
    void migrationPreservesExistingLeadsAndConstrainsNewFields() throws Exception {
        Flyway.configure().dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
                .target("4").load().migrate();
        try (var connection = DriverManager.getConnection(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
             var sql = connection.createStatement()) {
            sql.execute("""
                    INSERT INTO users(id,email,name,role) VALUES
                    ('00000000-0000-0000-0000-000000000001','client@example.com','Client','client');
                    INSERT INTO leads(id,title,category_id,subcategory,description,location,budget_min,budget_max,
                                      timeline,client_id,client_name,client_phone,client_email,client_city) VALUES
                    ('00000000-0000-0000-0000-000000000002','Existing website','it-software','Web','Keep this text',
                     'Iași',100,200,'Two weeks','00000000-0000-0000-0000-000000000001','Client','123',
                     'client@example.com','Iași');
                    """);
            Flyway.configure().dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
                    .load().migrate();
            try (var rows = sql.executeQuery("SELECT description,work_mode,required_capabilities::text,preferred_capabilities::text FROM leads")) {
                assertThat(rows.next()).isTrue();
                assertThat(rows.getString(1)).isEqualTo("Keep this text");
                assertThat(rows.getString(2)).isNull();
                assertThat(rows.getString(3)).isEqualTo("[]");
                assertThat(rows.getString(4)).isEqualTo("[]");
            }
            assertThatThrownBy(() -> sql.execute("UPDATE leads SET work_mode = 'UNKNOWN'"))
                    .isInstanceOf(SQLException.class);
            assertThatThrownBy(() -> sql.execute("UPDATE leads SET required_capabilities = '{}'::jsonb"))
                    .isInstanceOf(SQLException.class);
        }
    }
}
