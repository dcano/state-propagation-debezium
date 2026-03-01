package io.twba.rating_system;

public class ReviewEntryNotFoundForCourseAndUser extends RuntimeException {

    ReviewEntryNotFoundForCourseAndUser(CourseId courseId, EntryAuthor author) {
        super("ReviewEntry not found for course " + courseId.id() + " and user " + author.userName() + ".");
    }
}
