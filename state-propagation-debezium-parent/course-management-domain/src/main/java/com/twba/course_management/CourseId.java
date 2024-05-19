package com.twba.course_management;

import java.util.Objects;

public record CourseId(String value) {

    public CourseId {
        if(Objects.isNull(value)) {
            throw new IllegalArgumentException("Course Id cannot be null");
        }
    }

    public static CourseId of(String value) {
        return new CourseId(value);
    }

}
