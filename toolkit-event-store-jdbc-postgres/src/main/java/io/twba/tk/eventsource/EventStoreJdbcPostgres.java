package io.twba.tk.eventsource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.twba.tk.core.DomainEventPayload;
import io.twba.tk.core.Event;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

class EventStoreJdbcPostgres implements EventStore {

    private static final String INSERT_EVENT_SQL = """
        INSERT INTO event_sourcing_schema.event_store (
            uuid, aggregate_type, aggregate_id, type, payload,
            tenant_id, event_epoch, event_stream_version, system_epoch, event_class_name
        ) VALUES (?, ?, ?, ?, ?::jsonb, ?, ?, ?, ?, ?)
        """;

    private static final String SELECT_EVENTS_SQL = """
        SELECT type, payload
        FROM event_sourcing_schema.event_store
        WHERE aggregate_type = ? AND aggregate_id = ?
        ORDER BY event_stream_version ASC
        """;

    private final DataSource dataSource;
    private final ObjectMapper objectMapper;

    public EventStoreJdbcPostgres(DataSource dataSource, ObjectMapper objectMapper) {
        this.dataSource = dataSource;
        this.objectMapper = objectMapper;
    }

    @Override
    public void appendEventEvents(List<Event<? extends DomainEventPayload>> events) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(INSERT_EVENT_SQL)) {

            for (Event<? extends DomainEventPayload> event : events) {
                ps.setString(1, event.getId());
                ps.setString(2, event.getAggregateType());
                ps.setString(3, event.getAggregateId());
                ps.setString(4, event.eventType());
                ps.setString(5, objectMapper.writeValueAsString(event.getPayload()));
                ps.setString(6, event.getTenantId());
                ps.setLong(7, event.getPayload().getOccurredOn().toEpochMilli());
                ps.setLong(8, event.getEventStreamVersion());
                ps.setLong(9, Instant.now().toEpochMilli());
                ps.setString(10, event.getPayload().getClass().getName());
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException | JsonProcessingException e) {
            throw new EventStoreException("Failed to append events to the event store", e);
        }
    }

    @Override
    public List<Event<DomainEventPayload>> retrieveEventsFor(String aggregateType, String aggregateId) {
        List<Event<DomainEventPayload>> events = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(SELECT_EVENTS_SQL)) {

            ps.setString(1, aggregateType);
            ps.setString(2, aggregateId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    events.add(reconstituteEvent(rs.getString("event_class_name"), rs.getString("payload"), rs.getString("type")));
                }
            }
        } catch (SQLException e) {
            throw new EventStoreException("Failed to retrieve events for aggregate: " + aggregateId, e);
        }
        return events;
    }

    private Event<DomainEventPayload> reconstituteEvent(String eventClassName, String payloadJson, String eventType) {
        try {
            Class<?> payloadClass = Class.forName(eventClassName);
            DomainEventPayload payload = (DomainEventPayload) objectMapper.readValue(payloadJson, payloadClass);

            return new Event<>(payload, eventType);
        } catch (JsonProcessingException | ClassNotFoundException | ClassCastException e) {
            throw new EventStoreException("Failed to deserialize event of type: " + eventClassName, e);
        }
    }
}
