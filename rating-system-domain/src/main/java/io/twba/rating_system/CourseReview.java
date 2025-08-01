package io.twba.rating_system;

import io.twba.tk.core.Entity;
import io.twba.tk.core.TenantId;
import lombok.AccessLevel;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter(AccessLevel.PACKAGE)
class CourseReview  extends Entity {
    private ReviewId reviewId;
    private RatingSummary ratingSummary;
    private CourseId courseId;
    private AverageRating averageRating;
    private TenantId tenantId;

    private CourseReview(ReviewId reviewId, RatingSummary ratingSummary, CourseId courseId, AverageRating averageRating, TenantId tenantId, Long version) {
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

}
