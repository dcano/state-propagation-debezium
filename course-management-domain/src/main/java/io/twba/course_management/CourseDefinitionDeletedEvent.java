package io.twba.course_management;

import io.twba.tk.core.DomainEventPayload;
import io.twba.tk.core.TenantId;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class CourseDefinitionDeletedEvent extends DomainEventPayload {

    private final String courseId;

    public CourseDefinitionDeletedEvent(TenantId tenantId, CourseId courseId) {
        this(Instant.now(), UUID.randomUUID().toString(), tenantId, courseId);
    }

    public CourseDefinitionDeletedEvent(Instant occurredOn, String eventUid, TenantId tenantId, CourseId courseId) {
        super(occurredOn, eventUid, tenantId.value());
        this.courseId = courseId.value();
    }

    public static CourseDefinitionDeletedEvent triggeredFrom(CourseDefinition courseDefinition) {
        return new CourseDefinitionDeletedEvent(courseDefinition.getTenantId(), courseDefinition.getId());
    }

    @Override
    public String partitionKey() {
        return courseId;
    }
}