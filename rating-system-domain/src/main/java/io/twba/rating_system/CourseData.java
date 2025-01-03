package io.twba.rating_system;

import com.fasterxml.jackson.annotation.JsonProperty;

record CourseData(@JsonProperty("courseId") String courseId) {
    public static CourseData empty() {
        return new CourseData("");
    }
}
