package com.twba.tk.cdc;


import io.cloudevents.CloudEvent;
import org.apache.commons.lang3.RandomStringUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

@Testcontainers
@ExtendWith(MockitoExtension.class)
public class DebeziumMessageRelayTest {

    private static final Logger log = LoggerFactory.getLogger(DebeziumMessageRelayTest.class);

    private DebeziumMessageRelay debeziumMessageRelay;
    private static Map<String, String> debeziumOffsetStorageProps;

    private static Map<String, String> debeziumCustomProps;

    @TempDir
    private static Path offsetPath;

    @Mock
    public MessagePublisher messagePublisher;

    @Captor
    ArgumentCaptor<CloudEvent> eventDispatchedCaptor;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("test_db")
            .withUsername("postgres")
            .withPassword("postgres")
            .withCommand("postgres -c wal_level=logical");


    @BeforeAll
    static void bootstrap() throws IOException {

        final Path offsetStorageTempFile = Files.createFile(offsetPath.resolve("offsets_.dat"));
        final Path historyTempFile = Files.createFile(offsetPath.resolve("history_.dat"));
        debeziumOffsetStorageProps = new HashMap<>();
        debeziumOffsetStorageProps.put("offset.storage.file.filename", offsetStorageTempFile.toString());

        debeziumOffsetStorageProps.put("database.history", "io.debezium.relational.history.FileDatabaseHistory");
        debeziumOffsetStorageProps.put("database.history.file.filename", historyTempFile.toString());

        log.info("Offset storage file location ["+ debeziumOffsetStorageProps.get("offset.storage.file.filename")+"]");


        debeziumCustomProps = new HashMap<>();
        debeziumCustomProps.put("topic.prefix", "embedded-debezium");
        debeziumCustomProps.put("debezium.source.plugin.name", "pgoutput");
        debeziumCustomProps.put("plugin.name", "pgoutput");
    }

    @BeforeEach
    void setUp() {
        DebeziumProperties.SourceDatabaseProperties sourceDatabaseProperties = getSourceDatabaseProperties();
        DebeziumProperties.OffsetProperties offsetProperties = getOffsetProperties();
        DebeziumProperties debeziumProperties = new DebeziumProperties();
        debeziumProperties.setConnectorClass("io.debezium.connector.postgresql.PostgresConnector");
        debeziumProperties.setSourceDatabaseProperties(sourceDatabaseProperties);
        debeziumProperties.setOffsetStorage(offsetProperties);
        debeziumProperties.setCustomProps(debeziumCustomProps);
        initialize();
        debeziumMessageRelay = new DebeziumMessageRelay(messagePublisher, debeziumProperties);
    }

    @Test
    public void shouldInitializeTestRuntime() throws IOException {
        assertTrue( true);
        debeziumMessageRelay.start();
        debeziumMessageRelay.stop();
    }

    @Test
    public void whenAddingRecordsThenCDC() throws InterruptedException, IOException {
        debeziumMessageRelay.start();
        log.info("Adding record");
        Map<String, String> record = addRandomRecord(); //TODO assert stuff against the "record"
        log.info("Record added, waiting");
        Thread.sleep(3000);
        verify(messagePublisher).publish(eventDispatchedCaptor.capture());
        CloudEvent dispatchedMessage = eventDispatchedCaptor.getValue();
        assertNotNull(dispatchedMessage);
    }

    private Map<String, String> addRandomRecord() {
        Map<String, String> record = new HashMap<>();
        record.put("uuid", UUID.randomUUID().toString());
        record.put("payload", RandomStringUtils.randomAlphabetic(10));
        record.put("type", RandomStringUtils.randomAlphabetic(10));
        record.put("tenant_id", RandomStringUtils.randomAlphabetic(10));
        record.put("aggregate_id", RandomStringUtils.randomAlphabetic(10));
        record.put("epoch", String.valueOf(Instant.now().toEpochMilli()));
        record.put("partition_key", RandomStringUtils.randomAlphabetic(10));
        record.put("partition", "1");
        record.put("source", RandomStringUtils.randomAlphabetic(10));
        record.put("correlation_id", RandomStringUtils.randomAlphabetic(10));
        try (Connection conn = getConnection(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
             PreparedStatement pst = conn.prepareStatement("insert into test_schema.test_table(uuid, payload, type, tenant_id, aggregate_id, epoch, partition_key, partition, source, correlation_id) values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
            pst.setString(1, record.get("uuid"));
            pst.setString(2, record.get("payload"));
            pst.setString(3, record.get("type"));
            pst.setString(4, record.get("tenant_id"));
            pst.setString(5, record.get("aggregate_id"));
            pst.setLong(6, Long.parseLong(record.get("epoch")));
            pst.setString(7, record.get("partition_key"));
            pst.setInt(8, Integer.parseInt(record.get("partition")));
            pst.setString(9, record.get("source"));
            pst.setString(10, record.get("correlation_id"));
            pst.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return record;
    }

    private void initialize() {
        try (Connection conn = getConnection(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
            PreparedStatement pstmt = conn.prepareStatement(
                    """
                    create schema if not exists test_schema;
                    create table if not exists test_schema.test_table (
                        uuid varchar not null,
                        payload varchar not null,
                        type varchar not null,
                        tenant_id varchar not null,
                        aggregate_id varchar not null,
                        epoch int8 not null,
                        partition_key varchar(1000),
                        partition int4 not null,
                        source varchar not null,
                        correlation_id varchar not null,
                        primary key (uuid)
                    )
                    """
            )) {
            pstmt.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    static Connection getConnection(String url, String username, String password) {
        try {
            return DriverManager.getConnection(url, username, password);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    private static DebeziumProperties.SourceDatabaseProperties getSourceDatabaseProperties() {
        DebeziumProperties.SourceDatabaseProperties sourceDatabaseProperties = new DebeziumProperties.SourceDatabaseProperties();
        sourceDatabaseProperties.setPort(postgres.getMappedPort(5432));
        sourceDatabaseProperties.setHostname(postgres.getHost());
        sourceDatabaseProperties.setUser(postgres.getUsername());
        sourceDatabaseProperties.setPassword(postgres.getPassword());
        sourceDatabaseProperties.setDbName(postgres.getDatabaseName());
        sourceDatabaseProperties.setServerId(UUID.randomUUID().toString());
        sourceDatabaseProperties.setServerName("debezium-embedded-test");
        sourceDatabaseProperties.setOutboxTable("test_schema.test_table");
        return sourceDatabaseProperties;
    }

    @NotNull
    private static DebeziumProperties.OffsetProperties getOffsetProperties() {
        DebeziumProperties.OffsetProperties offsetProperties = new DebeziumProperties.OffsetProperties();
        offsetProperties.setType("org.apache.kafka.connect.storage.FileOffsetBackingStore");
        offsetProperties.setFlushInterval(5000);
        offsetProperties.setOffsetProps(debeziumOffsetStorageProps);
        return offsetProperties;
    }


}
