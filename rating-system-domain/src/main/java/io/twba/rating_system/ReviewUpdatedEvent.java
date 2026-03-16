package io.twba.rating_system;

import io.twba.tk.core.DomainEventPayload;
import lombok.AccessLevel;
import lombok.Getter;

import java.time.Instant;

@Getter(AccessLevel.PACKAGE)
public class ReviewUpdatedEvent extends DomainEventPayload {


    private final Review review;
    private final String reviewEntryId;
    private final Instant updatedAt;
    private final String courseId;

    ReviewUpdatedEvent(Review review, ReviewEntryId reviewEntryId, Instant now, CourseId courseId) {

        this.review = new Review(review.stars(), review.comment());
        this.reviewEntryId = reviewEntryId.id();
        this.updatedAt = now;
        this.courseId = courseId.id();

    }

    @Override
    public String partitionKey() {
        return courseId + ":" + reviewEntryId;
    }
}
