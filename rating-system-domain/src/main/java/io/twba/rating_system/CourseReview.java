package io.twba.rating_system;

import io.twba.tk.core.DomainEventPayload;
import io.twba.tk.core.Entity;
import io.twba.tk.core.Event;
import io.twba.tk.core.TenantId;
import io.twba.tk.eventsource.EventSourced;
import lombok.AccessLevel;
import lombok.Getter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;

@Getter(AccessLevel.PACKAGE)
class CourseReview extends Entity implements EventSourced<CourseReview> {
    private ReviewId reviewId;
    private RatingSummary ratingSummary;
    private CourseId courseId;
    private AverageRating averageRating;
    private TenantId tenantId;

    private CourseReview(ReviewId reviewId, RatingSummary ratingSummary, CourseId courseId, AverageRating averageRating, TenantId tenantId, Long version) {
        super(version);
        this.reviewId = reviewId;
        this.ratingSummary = ratingSummary;
        this.courseId = courseId;
        this.averageRating = averageRating;
        this.tenantId = tenantId;
    }

    private CourseReview(Long version) {
        super(version);
    }

    @Override
    public String aggregateId() {
        return courseId.id();
    }

    static CourseReview initializeCourseReview(CourseId courseId, TenantId tenantId) {
        CourseReview courseReview = new CourseReview(ReviewId.of(UUID.randomUUID().toString()),
                RatingSummary.initialize(),
                courseId,
                AverageRating.initialize(),
                tenantId,
                null);

        courseReview.record(new CourseReviewInitializedEvent(Instant.now(),
                UUID.randomUUID().toString(),
                tenantId,
                courseReview.ratingSummary,
                courseReview.averageRating,
                courseReview.courseId,
                courseReview.reviewId));

        return courseReview;
    }

    static CourseReview from(List<Event<DomainEventPayload>> events) {
        return new CourseReview((long)events.size()).hydrateFrom(events);
    }

    @Override
    public CourseReview hydrateFrom(List<Event<DomainEventPayload>> events) {

        return events.stream().reduce(new CourseReview((long) events.size()), (courseReview, domainEventPayloadEvent) -> {

            if (domainEventPayloadEvent.getPayload() instanceof CourseReviewInitializedEvent courseReviewInitializedEvent) {
                courseReview.courseId = new CourseId(courseReviewInitializedEvent.getCourseId());
                courseReview.reviewId = new ReviewId(courseReviewInitializedEvent.getReviewId());
                courseReview.ratingSummary = new RatingSummary(courseReviewInitializedEvent.getStarsRating());
                courseReview.averageRating = new AverageRating(courseReviewInitializedEvent.getAverageNumberOfStars(), courseReviewInitializedEvent.getAverageRate(), courseReviewInitializedEvent.getTotalNumberOfReviews());
                courseReview.tenantId = new TenantId(courseReviewInitializedEvent.getTenantId());
            }

            return courseReview;
        }, (courseReview, courseReview2) -> courseReview2);
    }
}
