package io.twba.rating_system;

record CourseData(CourseId courseId) {
    public static CourseData empty() {
        return new CourseData(new CourseId(""));
    }
}
