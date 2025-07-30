package io.twba.rating_system;

import io.twba.tk.core.Entity;

class CourseReview  extends Entity {
    private ReviewId reviewId;
    private RatingSummary starsSummary;
    private CourseId courseId;
    private AverageRating averageRating;
    private int totalNumberOfReviews;

    public CourseReview(Long version) {
        super(version);
    }

    @Override
    public String aggregateId() {
        return courseId.id();
    }

    static CourseReview initializeCourseReview(CourseId courseId) {
        CourseReview courseReview = new CourseReview(null);
        // create CourseReviewCreatedEvent and record it
        // then save
        courseReview.courseId = courseId;
        return courseReview;
    }

}
