package io.twba.rating_system;

import io.twba.tk.core.DomainEventPayload;
import io.twba.tk.core.TenantAware;
import io.twba.tk.core.TenantId;
import lombok.Getter;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Getter
class CourseReviewInitializedEvent extends DomainEventPayload implements TenantAware {

    private final List<StarRating> starsRating;
    private final float averageNumberOfStars;
    private final float averageRate;
    private final int totalNumberOfReviews;
    private final String courseId;
    private final String reviewId;

    public CourseReviewInitializedEvent(Instant occurredOn,
                                        String eventUid,
                                        TenantId tenantId,
                                        RatingSummary ratingSummary,
                                        AverageRating averageRating,
                                        CourseId courseId,
                                        ReviewId reviewId) {
        super(occurredOn, eventUid, tenantId.value());
        this.starsRating = ratingSummary.stars();
        this.averageNumberOfStars = averageRating.averageNumberOfStars();
        this.averageRate = averageRating.averageRate();
        this.totalNumberOfReviews = averageRating.totalNumberOfReviews();
        this.courseId = courseId.id();
        this.reviewId = reviewId.id();
    }

    @Override
    public String partitionKey() {
        return "";
    }

    @Override
    public TenantId tenantId() {
        return TenantId.of(super.getTenantId());
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
