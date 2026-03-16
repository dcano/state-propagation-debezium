package io.twba.rating_system;

import io.twba.tk.core.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Getter(AccessLevel.PACKAGE)
@Setter(AccessLevel.PACKAGE)
class ReviewEntry extends Entity {

    private ReviewEntryId reviewEntryId;
    private Instant entryCreationTime;
    private Instant entryUpdateTime;
    private EntryAuthor author;
    private Review review;
    private CourseId courseId;
    private Title title;

    ReviewEntry(Long version) {
        super(version);
    }

    private ReviewEntry(ReviewEntryId reviewEntryId,
                        Instant entryCreationTime,
                        Instant entryUpdateTime,
                        EntryAuthor entryAuthor,
                        Review review,
                        CourseId courseId,
                        Title title,
                        Long version) {
        super(version);
        this.reviewEntryId = reviewEntryId;
        this.entryCreationTime = entryCreationTime;
        this.entryUpdateTime = entryUpdateTime;
        this.author = entryAuthor;
        this.review = review;
        this.courseId = courseId;
        this.title = title;
    }


    @Override
    public String aggregateId() {
        return courseId.id() + ":" + author.userName();
    }

    void updateTitle(Title title) {
        if(!this.title.equals(title)) {
            this.title = title;
            this.record(new ReviewEntryTitleUpdatedEvent(title, reviewEntryId, Instant.now(), courseId));
        }
    }

    void updateReview(Review review) {
        if(!Objects.equals(this.review, review)) {
            this.review = review;
            this.record(new ReviewUpdatedEvent(review, reviewEntryId, Instant.now(), courseId));
        }
    }

    static ReviewEntry createNew(EntryAuthor author,
                                 Review review,
                                 CourseId courseId,
                                 Title title,
                                 ReviewEntryCreationService reviewEntryCreationService) {

        if(reviewEntryCreationService.existsForAuthorAndCourse(author, courseId)) {
            throw new ReviewEntryAlreadyExistsForCourseAndUser(courseId, author);
        }

        ReviewEntry reviewEntry = new ReviewEntry(ReviewEntryId.of(UUID.randomUUID().toString()),
                Instant.now(),
                Instant.now(),
                author,
                review,
                courseId,
                title,
                null);

        reviewEntry.record(new ReviewEntryCreatedEvent(reviewEntry.getReviewEntryId(),
                reviewEntry.entryCreationTime,
                reviewEntry.getEntryUpdateTime(),
                reviewEntry.author,
                reviewEntry.getReview(),
                reviewEntry.getCourseId(),
                reviewEntry.getTitle()));
        return reviewEntry;
    }

    static ReviewEntry instance(ReviewEntryId reviewEntryId, Instant entryCreationTime, Instant entryUpdateTime, EntryAuthor author, Review review, CourseId courseId, Title title, Long version) {
        return new ReviewEntry(reviewEntryId, entryCreationTime, entryUpdateTime, author, review, courseId, title, version);
    }
}
