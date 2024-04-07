package com.twba.course_management;

import java.util.List;
import java.util.Objects;

public record CourseObjective(String objectivesSummary, List<CourseObjective> objectives) {

    public CourseObjective {
        if(Objects.isNull(objectives) || objectives.isEmpty()) {
            throw new IllegalArgumentException("The course must have at least one learning objective defined");
        }
    }

}
