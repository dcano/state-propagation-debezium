package io.twba.rating_system;

interface CourseReviewRepository {

    boolean existsCourseReviewForCourse(CourseId courseId);
    void save(CourseReview review);
    CourseReview retrieveForCourse(CourseId courseId);
}
