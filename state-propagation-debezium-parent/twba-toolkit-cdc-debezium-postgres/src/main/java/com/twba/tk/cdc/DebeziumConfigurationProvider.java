package com.twba.tk.cdc;

import io.debezium.config.Configuration;

public class DebeziumConfigurationProvider {

    public static io.debezium.config.Configuration outboxConnectorConfig(DebeziumProperties properties) {
        return withCustomProps(withStorageProps(io.debezium.config.Configuration.create()
                .with("name", "outbox-connector")
                .with("connector.class", properties.getConnectorClass())
                .with("offset.storage", properties.getOffsetStorage().getType()), properties.getOffsetStorage())
                .with("offset.flush.interval.ms", properties.getOffsetStorage().getFlushInterval())
                .with("database.hostname", properties.getSourceDatabaseProperties().getHostname())
                .with("database.port", properties.getSourceDatabaseProperties().getPort())
                .with("database.user", properties.getSourceDatabaseProperties().getUser())
                .with("database.password", properties.getSourceDatabaseProperties().getPassword())
                .with("database.dbname", properties.getSourceDatabaseProperties().getDbName()), properties)
                .with("database.include.list", properties.getSourceDatabaseProperties().getDbName())
                .with("database.server.id", properties.getSourceDatabaseProperties().getServerId())
                .with("database.server.name", properties.getSourceDatabaseProperties().getServerName())
                .with("include.schema.changes", "false")
                .with("schema.include.list", properties.getSourceDatabaseProperties().getOutboxSchema())
                .with("table.include.list", properties.getSourceDatabaseProperties().getOutboxTable())
                //.with("topic.prefix", "embedded-debezium") //This should be a custom property
                //.with("debezium.source.plugin.name", "pgoutput") //This should be a custom property
                .build();
    }

    private static Configuration.Builder withStorageProps(Configuration.Builder debeziumConfigBuilder, DebeziumProperties.OffsetProperties offsetProperties) {
        offsetProperties.getOffsetProps().forEach(debeziumConfigBuilder::with);
        return debeziumConfigBuilder;
    }
    private static Configuration.Builder withCustomProps(Configuration.Builder debeziumConfigBuilder, DebeziumProperties debeziumProperties) {
        debeziumProperties.getCustomProps().forEach(debeziumConfigBuilder::with);
        return debeziumConfigBuilder;
    }

}
