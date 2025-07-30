package io.twba.rating_system;

import io.twba.tk.core.Entity;

import java.time.Instant;
import java.util.UUID;

class ReviewEntry extends Entity {

    private ReviewEntryId reviewEntryId;
    private Instant entryCreationTime;
    private Instant entryUpdateTime;
    private EntryAuthor author;
    private Review review;
    private CourseId courseId;

    private ReviewEntry(ReviewEntryId reviewEntryId,
                        Instant entryCreationTime,
                        Instant entryUpdateTime,
                        EntryAuthor entryAuthor,
                        Review review,
                        CourseId courseId,
                        Long version) {
        super(version);
    }

    private ReviewEntry() {
        super(null);
    }

    @Override
    public String aggregateId() {
        return reviewEntryId.id();
    }

    static ReviewEntry createNew(EntryAuthor author, Review review, CourseId courseId) {
        ReviewEntry reviewEntry = new ReviewEntry(ReviewEntryId.of(UUID.randomUUID().toString()), Instant.now(), Instant.now(), author, review, courseId, null);
        return reviewEntry;
    }
}
