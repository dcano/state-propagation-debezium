package io.twba.course_management;

public record CourseDuration(long expectedDurationMillis, int numberOfClasses) {

    public static CourseDuration of(long expectedDurationMillis, int numberOfClasses) {
        return new CourseDuration(expectedDurationMillis, numberOfClasses);
    }

}
