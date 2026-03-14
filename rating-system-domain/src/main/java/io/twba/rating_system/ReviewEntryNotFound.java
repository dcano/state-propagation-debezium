package io.twba.rating_system;

public class ReviewEntryNotFound extends RuntimeException {

    ReviewEntryNotFound(CourseId courseId) {
        super("ReviewEntry not found for course " + courseId.id() + ".");
    }
}
