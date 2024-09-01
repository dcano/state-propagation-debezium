package io.twba.course_management.repository;

import io.twba.tk.core.TenantId;
import io.twba.course_management.*;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.UUID;

public class CourseDefinitions {

    static CourseDefinition randomNewCourseDefinition() {
        return CourseDefinition.builder(TenantId.of(UUID.randomUUID().toString()))
                .withCourseId(CourseId.of(UUID.randomUUID().toString()))
                .withCourseDates(CourseDates.of(Instant.now(), Instant.now()))
                .withTeacherId(TeacherId.from(UUID.randomUUID().toString()))
                .withPreRequirements(Collections.singletonList(PreRequirement.from("Req1")))
                .withDuration(CourseDuration.of(Duration.ofDays(5).toMillis(), 10))
                .withCourseDescription(CourseDescription.from(CourseTitle.of("Course title"), "Course Summary", "Course Description"))
                .withCourseObjective(CourseObjective.of("Course objective"))
                .createNew();
    }

}
