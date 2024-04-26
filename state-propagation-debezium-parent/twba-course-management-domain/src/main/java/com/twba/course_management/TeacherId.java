package com.twba.course_management;

import java.util.Objects;

public record TeacherId(String value) {

    public TeacherId {
        if(Objects.isNull(value)) {
            throw new IllegalArgumentException("Teacher Id cannot be null");
        }
    }

    public static TeacherId from(String value) {
        return new TeacherId(value);
    }

}
