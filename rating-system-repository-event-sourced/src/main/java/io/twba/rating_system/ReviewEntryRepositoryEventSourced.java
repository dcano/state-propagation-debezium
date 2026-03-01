package io.twba.rating_system;

import io.twba.tk.eventsource.EventStore;

import java.util.Optional;

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
    public Optional<ReviewEntry> retrieveReviewEntryFor(EntryAuthor author, CourseId courseId) {
        return Optional.ofNullable(ReviewEntry.from(eventStore.retrieveEventsFor(ReviewEntry.class.getSimpleName(), courseId.id() + ":" + author.userName())));
    }
}
