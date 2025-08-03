package io.twba.tk.eventsource;

import io.twba.tk.core.DomainEventPayload;
import io.twba.tk.core.Event;

import java.util.List;

public interface EventStore {

    void appendEventEvents(List<Event<? extends DomainEventPayload>> event);
    List<Event<DomainEventPayload>> retrieveEventsFor(String aggregateType, String aggregateId);
}
