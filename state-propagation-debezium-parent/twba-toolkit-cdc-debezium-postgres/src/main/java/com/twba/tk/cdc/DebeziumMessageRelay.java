package com.twba.tk.cdc;

import io.debezium.embedded.Connect;
import io.debezium.engine.DebeziumEngine;
import io.debezium.engine.RecordChangeEvent;
import io.debezium.engine.format.ChangeEventFormat;
import org.apache.kafka.connect.source.SourceRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class DebeziumMessageRelay implements MessageRelay {

    private static final Logger log = LoggerFactory.getLogger(DebeziumMessageRelay.class);

    private final Executor executor = Executors.newSingleThreadExecutor();
    private final MessagePublisher messagePublisher;
    private final DebeziumEngine<RecordChangeEvent<SourceRecord>> debeziumEngine;

    public DebeziumMessageRelay(MessagePublisher messagePublisher, DebeziumProperties debeziumProperties) {
        this.debeziumEngine = DebeziumEngine.create(ChangeEventFormat.of(Connect.class))
                .using(DebeziumConfigurationProvider.outboxConnectorConfig(debeziumProperties).asProperties())
                .notifying(this::handleChangeEvent)
                .build();
        this.messagePublisher = messagePublisher;
    }

    private void handleChangeEvent(RecordChangeEvent<SourceRecord> sourceRecordRecordChangeEvent) {
        SourceRecord sourceRecord = sourceRecordRecordChangeEvent.record();
        log.info("Key = '" + sourceRecord.key() + "' value = '" + sourceRecord.value() + "'");
        //TODO map to cloud events
        messagePublisher.publish(null);
    }

    @Override
    public void start() {
        this.executor.execute(debeziumEngine);
    }

    @Override
    public void stop() throws IOException {
        if (this.debeziumEngine != null) {
            this.debeziumEngine.close();
        }
    }

    @Override
    public void close() throws Exception {
        stop();
    }
}
