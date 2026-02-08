package io.twba.rating_system;

import io.twba.tk.eventsource.EventStore;

class ReviewEntryRepositoryEventSourced implements ReviewEntryRepository {

    private final EventStore eventStore;

    ReviewEntryRepositoryEventSourced(EventStore eventStore) {
        this.eventStore = eventStore;
    }

    @Override
    public void save(ReviewEntry reviewEntry) {
        eventStore.appendEvents(reviewEntry.getDomainEvents());
    }

    @Override
    public ReviewEntry retrieveEntry(ReviewEntryId reviewEntryId) {
        return ReviewEntry.from(eventStore.retrieveEventsFor(ReviewEntry.class.getSimpleName(), reviewEntryId.id()));
    }
}
