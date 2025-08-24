package io.twba.tk.eventsource;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
public class EventStoreJdbcPostgresTest {

    public static final String DB_NAME = "test_db";
    public static final String DB_USERNAME = "sa";
    public static final String DB_PASSWORD = "sa";

    @Container
    public static PostgreSQLContainer<?> container = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName(DB_NAME)
            .withUsername(DB_USERNAME)
            .withPassword(DB_PASSWORD);


    @BeforeAll
    static void migrateDatabase() {
        // Ensure Flyway uses test resources SQL scripts under src/test/resources/db/migration
        Flyway.configure()
                .dataSource(container.getJdbcUrl(), container.getUsername(), container.getPassword())
                .locations("classpath:db/migration")
                .load()
                .migrate();
    }

    @Test
    void testRuntimeStartup() {
        assertTrue(true);
    }

}
