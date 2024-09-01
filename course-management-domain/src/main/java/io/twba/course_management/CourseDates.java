package io.twba.course_management;

import java.time.Instant;
import java.util.Objects;

public record CourseDates(Instant publicationDate, Instant openingDate) {

    public CourseDates {
        if(Objects.isNull(publicationDate)) {
            throw new IllegalArgumentException("Courses must have a valid publication date");
        }
    }

    public static CourseDates of(Instant publicationDate, Instant openingDate) {
        return new CourseDates(publicationDate, openingDate);
    }

}
