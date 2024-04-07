package com.twba.course_management;

import java.util.Objects;

public record TeacherId(String value) {

    public TeacherId {
        if(Objects.isNull(value)) {
            throw new IllegalArgumentException("Teacher Id cannot be null");
        }
    }

}
