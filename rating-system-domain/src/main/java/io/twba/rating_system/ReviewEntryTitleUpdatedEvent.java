package io.twba.rating_system;

import io.twba.tk.core.DomainEventPayload;
import lombok.AccessLevel;
import lombok.Getter;

import java.time.Instant;

@Getter(AccessLevel.PACKAGE)
class ReviewEntryTitleUpdatedEvent extends DomainEventPayload {

    private final String title;
    private final String reviewEntryId;
    private final Instant updatedAt;
    private final String courseId;

    ReviewEntryTitleUpdatedEvent(Title title, ReviewEntryId reviewEntryId, Instant updatedAt, CourseId courseId) {
        super();
        this.title = title.toString();
        this.reviewEntryId = reviewEntryId.id();
        this.updatedAt = updatedAt;
        this.courseId = courseId.id();
    }

    @Override
    public String partitionKey() {
        return courseId + ":" + reviewEntryId;
    }
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
