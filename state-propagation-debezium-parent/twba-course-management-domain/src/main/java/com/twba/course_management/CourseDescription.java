package com.twba.course_management;

import java.util.Objects;

public record CourseDescription(String title, String summary, String description) {

    public CourseDescription {
        if(Objects.isNull(title) || title.isEmpty()) {
            throw new IllegalArgumentException("Title cannot be null or empty");
        }
    }

}
