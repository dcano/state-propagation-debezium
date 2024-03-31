package com.twba.tk.cdc;

import io.cloudevents.CloudEvent;
import io.cloudevents.core.v1.CloudEventBuilder;
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
import java.io.UnsupportedEncodingException;
import java.net.URI;
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
                .using(new DebeziumEngine.ConnectorCallback() {
                    @Override
                    public void connectorStarted() {
                        DebeziumEngine.ConnectorCallback.super.connectorStarted();
                        log.info("Debezium CDC started");
                    }
                })
                .build();
        this.messagePublisher = messagePublisher;
    }

    private void handleChangeEvent(RecordChangeEvent<SourceRecord> sourceRecordRecordChangeEvent)  {
        SourceRecord sourceRecord = sourceRecordRecordChangeEvent.record();
        log.info("Key = '" + sourceRecord.key() + "' value = '" + sourceRecord.value() + "'");
        final CloudEvent event;
        try {
            event = new CloudEventBuilder()
                    .withId("000")
                    .withType("example.demo")
                    .withSource(URI.create("http://thewhiteboardarchitect.com/state-propagation/test"))
                    .withData("application/json","{'key': 'value`}".getBytes("UTF-8"))
                    .build();
            messagePublisher.publish(event);
        }
        catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
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
