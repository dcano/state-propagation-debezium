package io.twba.rating_system;

import io.twba.tk.core.DomainEventPayload;
import io.twba.tk.core.Entity;
import io.twba.tk.core.Event;
import io.twba.tk.eventsource.EventSourced;
import lombok.AccessLevel;
import lombok.Getter;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Getter(AccessLevel.PACKAGE)
class ReviewEntry extends Entity implements EventSourced<ReviewEntry> {

    private ReviewEntryId reviewEntryId;
    private Instant entryCreationTime;
    private Instant entryUpdateTime;
    private EntryAuthor author;
    private Review review;
    private CourseId courseId;
    private Title title;

    private ReviewEntry(Long version) {
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

    static ReviewEntry from(List<Event<DomainEventPayload>> events) {
        return new ReviewEntry((long)events.size()).hydrateFrom(events).orElseThrow(() -> new RuntimeException("Failed to hydrate review entry"));
    }

    static ReviewEntry createNew(EntryAuthor author, Review review, CourseId courseId, Title title, ReviewEntryCreationService reviewEntryCreationService) {

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

    @Override
    public Optional<ReviewEntry> hydrateFrom(List<Event<DomainEventPayload>> events) {

        if(Objects.isNull(events) || events.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(events.stream().reduce(new ReviewEntry((long) events.size()), (reviewEntry, domainEventPayloadEvent) -> {

            if (domainEventPayloadEvent.getPayload() instanceof ReviewEntryCreatedEvent event) {
                reviewEntry.courseId = new CourseId(event.getCourseId());
                reviewEntry.reviewEntryId = new ReviewEntryId(event.getReviewEntryId());
                reviewEntry.title = new Title(event.getTitle());
                reviewEntry.author = new EntryAuthor(event.getAuthor());
                reviewEntry.entryCreationTime = event.getCreatedTime();
                reviewEntry.entryUpdateTime = event.getUpdatedTime();
                reviewEntry.review = event.getReview();
            } else if(domainEventPayloadEvent.getPayload() instanceof ReviewEntryTitleUpdatedEvent event) {
                reviewEntry.title = new Title(event.getTitle());
                reviewEntry.entryUpdateTime = event.getUpdatedAt();
            } else if(domainEventPayloadEvent.getPayload() instanceof ReviewUpdatedEvent event) {
                reviewEntry.review = new Review(event.getReview().stars(), event.getReview().comment());
                reviewEntry.entryUpdateTime = event.getUpdatedAt();
            }

            return  reviewEntry;
        }, (courseReview, courseReview2) -> courseReview2));
    }
}
