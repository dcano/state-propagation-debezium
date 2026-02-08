package io.twba.rating_system;

import io.twba.tk.eventsource.EventStore;

class CourseReviewRepositoryEventSourced implements CourseReviewRepository {

    private final EventStore eventStore;

    CourseReviewRepositoryEventSourced(EventStore eventStore) {
        this.eventStore = eventStore;
    }

    @Override
    public void save(CourseReview review) {
        eventStore.appendEvents(review.getDomainEvents());
    }

    @Override
    public CourseReview retrieveForCourse(CourseId courseId) {
        return CourseReview.from(eventStore.retrieveEventsFor(CourseReview.class.getSimpleName(), courseId.id()));
    }
}
