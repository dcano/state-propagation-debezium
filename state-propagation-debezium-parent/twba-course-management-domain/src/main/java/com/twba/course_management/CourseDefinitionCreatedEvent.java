package com.twba.course_management;

import com.twba.tk.core.DomainEventPayload;
import com.twba.tk.core.TenantId;

import java.time.Instant;
import java.util.UUID;

public class CourseDefinitionCreatedEvent extends DomainEventPayload {

    public CourseDefinitionCreatedEvent(TenantId tenantId) {
        this(Instant.now(), UUID.randomUUID().toString(), tenantId);
    }

    public CourseDefinitionCreatedEvent(Instant occurredOn, String eventUid, TenantId tenantId) {
        super(occurredOn, eventUid, tenantId.getId());
    }
}
