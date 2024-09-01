package io.twba.course_management;

import io.twba.tk.core.DomainEventPayload;
import io.twba.tk.core.TenantId;
import lombok.Getter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
public class CourseDefinitionCreatedEvent extends DomainEventPayload {

    private final String courseId;
    private final CourseDescription courseDescription;
    private final CourseObjective courseObjective;
    private final List<PreRequirement> preRequirements;
    private final CourseDuration duration;
    private final String teacherId;
    private final CourseDates courseDates;
    private final String courseStatus;

    public CourseDefinitionCreatedEvent(TenantId tenantId, CourseId courseId, CourseDescription courseDescription, CourseObjective courseObjective, List<PreRequirement> preRequirements, CourseDuration duration, TeacherId teacherId, CourseDates courseDates, CourseStatus courseStatus) {
        this(Instant.now(), UUID.randomUUID().toString(), tenantId, courseId, courseDescription, courseObjective, preRequirements, duration, teacherId, courseDates, courseStatus);
    }

    public CourseDefinitionCreatedEvent(Instant occurredOn, String eventUid, TenantId tenantId, CourseId courseId, CourseDescription courseDescription, CourseObjective courseObjective, List<PreRequirement> preRequirements, CourseDuration duration, TeacherId teacherId, CourseDates courseDates, CourseStatus courseStatus) {
        super(occurredOn, eventUid, tenantId.value());
        this.courseId = courseId.value();
        this.courseDescription = courseDescription;
        this.courseObjective = courseObjective;
        this.preRequirements = preRequirements;
        this.duration = duration;
        this.teacherId = teacherId.value();
        this.courseDates = courseDates;
        this.courseStatus = courseStatus.name();
    }

    public static CourseDefinitionCreatedEvent triggeredFrom(CourseDefinition courseDefinition) {
        return new CourseDefinitionCreatedEvent(courseDefinition.getTenantId(),
                courseDefinition.getId(),
                courseDefinition.getCourseDescription(),
                courseDefinition.getCourseObjective(),
                courseDefinition.getPreRequirements(),
                courseDefinition.getDuration(),
                courseDefinition.getTeacherId(),
                courseDefinition.getCourseDates(),
                courseDefinition.getCourseStatus());
    }

    @Override
    public String partitionKey() {
        return courseId;
    }
}
