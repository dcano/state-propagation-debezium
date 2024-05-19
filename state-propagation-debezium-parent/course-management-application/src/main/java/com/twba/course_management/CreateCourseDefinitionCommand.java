package com.twba.course_management;

import com.twba.tk.command.DefaultDomainCommand;
import com.twba.tk.core.TenantId;
import lombok.Getter;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

@Getter
public class CreateCourseDefinitionCommand extends DefaultDomainCommand {

    private final CourseDates courseDates;
    private final CourseId courseId;
    private final CourseDescription courseDescription;
    private final CourseObjective courseObjective;
    private final TeacherId teacherId;
    private final List<PreRequirement> preRequirements;
    private final CourseDuration courseDuration;
    private final TenantId tenantId;


    public CreateCourseDefinitionCommand(Instant publicationDate,
                                         Instant openingDate,
                                         String courseId,
                                         String title,
                                         String summary,
                                         String description,
                                         String objectivesSummary,
                                         String teacherId,
                                         List<String> requirements,
                                         long expectedDuration,
                                         int numberOfClasses,
                                         String tenantId) {
        this.courseDates = new CourseDates(publicationDate, openingDate);
        this.courseId = new CourseId(courseId);
        this.courseDescription = new CourseDescription(new CourseTitle(title), summary, description);
        this.courseObjective = new CourseObjective(objectivesSummary);
        this.teacherId = new TeacherId(teacherId);
        this.preRequirements = requirements.stream().map(PreRequirement::new).toList();
        this.courseDuration = new CourseDuration(expectedDuration, numberOfClasses);
        this.tenantId = new TenantId(tenantId);
    }

}
