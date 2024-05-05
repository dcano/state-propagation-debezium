package com.twba.tk.cdc;

import com.twba.tk.event.TwbaCloudEvent;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.v1.CloudEventBuilder;
import io.debezium.data.Envelope;
import io.debezium.embedded.Connect;
import io.debezium.engine.DebeziumEngine;
import io.debezium.engine.RecordChangeEvent;
import io.debezium.engine.format.ChangeEventFormat;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static io.debezium.data.Envelope.FieldName.*;

public class DebeziumMessageRelay implements MessageRelay {

    private static final Logger log = LoggerFactory.getLogger(DebeziumMessageRelay.class);

    private final Executor executor = Executors.newSingleThreadExecutor(r -> new Thread(r, "debezium-message-relay"));

    private final MessagePublisher messagePublisher;
    private final DebeziumEngine<RecordChangeEvent<SourceRecord>> debeziumEngine;

    public DebeziumMessageRelay(MessagePublisher messagePublisher, DebeziumProperties debeziumProperties) {
        this.debeziumEngine = DebeziumEngine.create(ChangeEventFormat.of(Connect.class))
                .using(DebeziumConfigurationProvider.outboxConnectorConfig(debeziumProperties).asProperties())
                .notifying(this::handleChangeEvent)
                .build();
        this.messagePublisher = messagePublisher;
    }

    private void handleChangeEvent(RecordChangeEvent<SourceRecord> sourceRecordRecordChangeEvent)  {
        SourceRecord sourceRecord = sourceRecordRecordChangeEvent.record();
        Struct sourceRecordChangeValue= (Struct) sourceRecord.value();
        log.info("Received record - Key = '" + sourceRecord.key() + "' value = '" + sourceRecord.value() + "'");
        final CloudEvent event;
        try {
            //TODO outbox table clean up
            Struct struct = (Struct) sourceRecordChangeValue.get(AFTER);
            String payload = (String)struct.get("payload");
            String uuid = (String)struct.get("uuid");
            String type = (String)struct.get("type");
            String tenantId = (String)struct.get("tenant_id");
            String aggregateId = (String)struct.get("aggregate_id");
            long epoch = (Long)struct.get("epoch");
            String partitionKey = (String)struct.get("partition_key");
            String source = (String)struct.get("source");
            String correlationId = (String)struct.get("correlation_id");

            event = new CloudEventBuilder()
                    .withId(uuid)
                    .withType(type)
                    .withSubject(aggregateId)
                    .withExtension(TwbaCloudEvent.CLOUD_EVENT_TENANT_ID, tenantId)
                    .withExtension(TwbaCloudEvent.CLOUD_EVENT_TIMESTAMP, epoch)
                    .withExtension(TwbaCloudEvent.CLOUD_EVENT_PARTITION_KEY, partitionKey)
                    .withExtension(TwbaCloudEvent.CLOUD_EVENT_CORRELATION_ID, correlationId)
                    .withExtension(TwbaCloudEvent.CLOUD_EVENT_GENERATING_APP_NAME, source)
                    .withSource(URI.create("https://thewhiteboardarchitect.com/" + source))
                    .withData("application/json",payload.getBytes("UTF-8"))
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
        log.info("Debezium CDC started");
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
