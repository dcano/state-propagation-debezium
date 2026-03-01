package io.twba.rating_system;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import io.twba.tk.core.DomainEventPayload;

import java.io.IOException;
import java.lang.reflect.Field;
import java.time.Instant;

abstract class ReviewEntryEventDeserializer<T extends DomainEventPayload> extends StdDeserializer<T> {

    private static final Field OCCURRED_ON_FIELD;
    private static final Field EVENT_UID_FIELD;
    private static final Field TENANT_ID_FIELD;

    static {
        try {
            OCCURRED_ON_FIELD = DomainEventPayload.class.getDeclaredField("occurredOn");
            OCCURRED_ON_FIELD.setAccessible(true);
            EVENT_UID_FIELD = DomainEventPayload.class.getDeclaredField("eventUid");
            EVENT_UID_FIELD.setAccessible(true);
            TENANT_ID_FIELD = DomainEventPayload.class.getDeclaredField("tenantId");
            TENANT_ID_FIELD.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    protected ReviewEntryEventDeserializer(Class<T> eventClass) {
        super(eventClass);
    }

    protected void restoreBaseFields(T event, JsonNode node, ObjectMapper mapper) throws IOException {
        try {
            JsonNode occurredOnNode = node.get("occurredOn");
            if (occurredOnNode != null && !occurredOnNode.isNull()) {
                OCCURRED_ON_FIELD.set(event, mapper.treeToValue(occurredOnNode, Instant.class));
            }
            JsonNode eventUidNode = node.get("eventUid");
            if (eventUidNode != null && !eventUidNode.isNull()) {
                EVENT_UID_FIELD.set(event, eventUidNode.asText());
            }
            JsonNode tenantIdNode = node.get("tenantId");
            if (tenantIdNode != null && !tenantIdNode.isNull()) {
                TENANT_ID_FIELD.set(event, tenantIdNode.asText());
            }
        } catch (IllegalAccessException e) {
            throw new IOException("Failed to restore base event fields", e);
        }
    }
}
