package io.twba.rating_system;

import io.twba.tk.core.DomainEventPayload;
import io.twba.tk.core.Event;
import io.twba.tk.eventsource.EventSourced;
import io.twba.tk.eventsource.EventStore;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

class ReviewEntryRepositoryEventSourced implements ReviewEntryRepository, EventSourced<ReviewEntry> {

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
        return hydrateFrom(eventStore.retrieveEventsFor(ReviewEntry.class.getSimpleName(), courseId.id() + ":" + author.userName()));
    }

    @Override
    public Optional<ReviewEntry> hydrateFrom(List<Event<DomainEventPayload>> events) {

        if(Objects.isNull(events) || events.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(events.stream().reduce(new ReviewEntry((long) events.size()), (reviewEntry, domainEventPayloadEvent) -> {
            if (domainEventPayloadEvent.getPayload() instanceof ReviewEntryCreatedEvent event) {
                reviewEntry.setCourseId(new CourseId(event.getCourseId()));
                reviewEntry.setReviewEntryId(new ReviewEntryId(event.getReviewEntryId()));
                reviewEntry.setTitle(new Title(event.getTitle()));
                reviewEntry.setAuthor(new EntryAuthor(event.getAuthor()));
                reviewEntry.setEntryCreationTime(event.getCreatedTime());
                reviewEntry.setEntryUpdateTime(event.getUpdatedTime());
                reviewEntry.setReview(event.getReview());
            } else if(domainEventPayloadEvent.getPayload() instanceof ReviewEntryTitleUpdatedEvent event) {
                reviewEntry.setTitle(new Title(event.getTitle()));
                reviewEntry.setEntryUpdateTime(event.getUpdatedAt());
            } else if(domainEventPayloadEvent.getPayload() instanceof ReviewUpdatedEvent event) {
                reviewEntry.setReview(new Review(event.getReview().stars(), event.getReview().comment()));
                reviewEntry.setEntryUpdateTime(event.getUpdatedAt());
            }
            return  reviewEntry;
        }, (courseReview, courseReview2) -> courseReview2));
    }
}