package com.twba.tk.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twba.tk.cdc.Outbox;
import com.twba.tk.cdc.OutboxMessage;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import static java.util.Objects.isNull;

@Named
public class DomainEventAppender {

    private final ThreadLocal<List<Event<? extends DomainEventPayload>>> eventsToPublish = new ThreadLocal<>();
    private final Outbox outbox;
    private final ObjectMapper objectMapper;

    @Inject
    public DomainEventAppender(Outbox outbox, ObjectMapper objectMapper) {
        this.outbox = outbox;
        this.objectMapper = objectMapper;
    }


    public void append(List<Event<? extends DomainEventPayload>> events) {
        //add the event to the buffer, later this event will be published to other bounded contexts
        if(isNull(eventsToPublish.get())) {
            resetBuffer();
        }
        //ensure event is not already in buffer
        events.stream().filter(this::notInBuffer).forEach(event -> eventsToPublish.get().add(event));
    }

    public void publishToOutbox() {
        if(Objects.nonNull(eventsToPublish.get().stream()) && !eventsToPublish.get().isEmpty()){
            eventsToPublish.get().stream().map(this::toOutboxMessage).forEach(outbox::appendMessage);
        }
        resetBuffer();
    }

    private OutboxMessage toOutboxMessage(Event<? extends DomainEventPayload> event) {
        try {
            String header = objectMapper.writeValueAsString(event.header());
            String payload = objectMapper.writeValueAsString(event.getPayload());
            return new OutboxMessage(event.getId(), header, payload, event.eventType(), Instant.now().toEpochMilli(), event.partitionKey());
        }
        catch (JsonProcessingException e) {
            throw new UnableToSerializeEventException(event.getPayload().getClass(), e);
        }
    }

    private void resetBuffer(){
        eventsToPublish.remove();
        eventsToPublish.set(new ArrayList<>());
    }

    private boolean notInBuffer(Event domainEvent) {
        return !eventsToPublish.get().contains(domainEvent);
    }

}
