package io.twba.tk.eventsource;

import io.twba.tk.core.DomainEventPayload;
import io.twba.tk.core.Entity;
import io.twba.tk.core.Event;

import java.util.List;

public interface EventSourced<T extends Entity> {

    T hydrateFrom(List<Event<DomainEventPayload>> events);

}
