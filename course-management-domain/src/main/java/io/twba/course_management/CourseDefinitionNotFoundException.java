package io.twba.course_management;

public class CourseDefinitionNotFoundException extends RuntimeException {

    public CourseDefinitionNotFoundException(CourseId courseId) {
        super("Course definition with id " + courseId.value() + " not found");
    }
}