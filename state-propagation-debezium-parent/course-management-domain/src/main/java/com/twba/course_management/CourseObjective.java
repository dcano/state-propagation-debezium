package com.twba.course_management;

public record CourseObjective(String objectivesSummary) {

    public static CourseObjective of(String objectivesSummary) {
        return new CourseObjective(objectivesSummary);
    }

}
