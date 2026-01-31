package io.twba.rating_system;

import io.twba.tk.core.DomainEventPayload;
import lombok.AccessLevel;
import lombok.Getter;

import java.time.Instant;

@Getter(AccessLevel.PACKAGE)
class ReviewEntryCreatedEvent extends DomainEventPayload {

    private final String title;
    private final Review review;
    private final Instant createdTime;
    private final Instant updatedTime;
    private final String author;
    private final String courseId;
    private final String reviewEntryId;

    ReviewEntryCreatedEvent(ReviewEntryId reviewEntryId,
                            Instant entryCreationTime,
                            Instant entryUpdateTime,
                            EntryAuthor entryAuthor,
                            Review review,
                            CourseId courseId,
                            Title title) {
        super();
        this.reviewEntryId = reviewEntryId.id();
        this.author = entryAuthor.userName();
        this.courseId = courseId.id();
        this.createdTime = entryCreationTime;
        this.updatedTime = entryUpdateTime;
        this.review = review;
        this.title = title.toString();
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
