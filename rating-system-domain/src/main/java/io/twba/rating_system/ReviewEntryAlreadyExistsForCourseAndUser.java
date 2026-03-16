package io.twba.rating_system;

public class ReviewEntryAlreadyExistsForCourseAndUser extends RuntimeException {

    ReviewEntryAlreadyExistsForCourseAndUser(CourseId courseId, EntryAuthor author) {
        super("ReviewEntry already exists for course " + courseId.id() + " and user " + author.userName() + ".");
    }
}
