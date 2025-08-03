package io.twba.rating_system;

interface CourseReviewRepository {

    void save(CourseReview review);
    CourseReview retrieveForCourse(CourseId courseId);
}
