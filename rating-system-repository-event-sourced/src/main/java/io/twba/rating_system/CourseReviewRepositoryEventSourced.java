package io.twba.rating_system;

import io.twba.tk.core.DomainEventPayload;
import io.twba.tk.core.Event;
import io.twba.tk.eventsource.EventStore;

import java.util.List;

class CourseReviewRepositoryEventSourced implements CourseReviewRepository {

    private final EventStore eventStore;

    CourseReviewRepositoryEventSourced(EventStore eventStore) {
        this.eventStore = eventStore;
    }

    @Override
    public void save(CourseReview review) {
        eventStore.appendEventEvents(review.getDomainEvents());
    }

    @Override
    public CourseReview retrieveForCourse(CourseId courseId) {
        List<Event<DomainEventPayload>> events = eventStore.retrieveEventsFor(CourseReview.class.getSimpleName(), courseId.id());

        return null;
    }
}
