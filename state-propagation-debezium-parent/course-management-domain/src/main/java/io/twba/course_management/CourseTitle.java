package io.twba.course_management;

public record CourseTitle(String value) {

    public static CourseTitle of(String value) {
        return new CourseTitle(value);
    }

}
