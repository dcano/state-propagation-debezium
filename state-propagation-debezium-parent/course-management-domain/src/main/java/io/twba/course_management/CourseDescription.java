package io.twba.course_management;

import java.util.Objects;

public record CourseDescription(CourseTitle title, String summary, String description) {

    public CourseDescription {
        if(Objects.isNull(title)) {
            throw new IllegalArgumentException("Title cannot be null or empty");
        }
    }

    public static CourseDescription from(CourseTitle title, String summary, String description) {
        return new CourseDescription(title, summary, description);
    }

}
