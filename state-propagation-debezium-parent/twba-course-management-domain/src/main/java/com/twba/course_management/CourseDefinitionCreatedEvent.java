package com.twba.course_management;

import com.twba.tk.core.DomainEventPayload;
import com.twba.tk.core.TenantId;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class CourseDefinitionCreatedEvent extends DomainEventPayload {

    private final String id;
    private final CourseDescription courseDescription;
    private final CourseObjective courseObjective;
    private final List<PreRequirement> preRequirements;
    private final CourseDuration duration;
    private final String teacherId;
    private final CourseDates courseDates;
    private final String courseStatus;

    public CourseDefinitionCreatedEvent(TenantId tenantId, CourseId id, CourseDescription courseDescription, CourseObjective courseObjective, List<PreRequirement> preRequirements, CourseDuration duration, TeacherId teacherId, CourseDates courseDates, CourseStatus courseStatus) {
        this(Instant.now(), UUID.randomUUID().toString(), tenantId, id, courseDescription, courseObjective, preRequirements, duration, teacherId, courseDates, courseStatus);
    }

    public CourseDefinitionCreatedEvent(Instant occurredOn, String eventUid, TenantId tenantId, CourseId id, CourseDescription courseDescription, CourseObjective courseObjective, List<PreRequirement> preRequirements, CourseDuration duration, TeacherId teacherId, CourseDates courseDates, CourseStatus courseStatus) {
        super(occurredOn, eventUid, tenantId.getId());
        this.id = id.value();
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
}
