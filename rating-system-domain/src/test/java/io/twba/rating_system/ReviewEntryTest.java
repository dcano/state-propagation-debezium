package io.twba.rating_system;

import io.twba.tk.core.Event;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewEntryTest {

    @Mock
    private ReviewEntryCreationService reviewEntryCreationService;

    private EntryAuthor author;
    private CourseId courseId;
    private Review review;
    private Title title;

    @BeforeEach
    void setUp() {
        author = EntryAuthor.of("john.doe");
        courseId = new CourseId(UUID.randomUUID().toString());
        review = new Review(Stars.FOUR, "Great course!");
        title = new Title("My Review");
    }

    @Test
    void shouldCreateNewReviewEntryWithCorrectState() {
        when(reviewEntryCreationService.existsForAuthorAndCourse(author, courseId)).thenReturn(false);

        ReviewEntry entry = ReviewEntry.createNew(author, review, courseId, title, reviewEntryCreationService);

        assertAll("Expected ReviewEntry state",
                () -> assertEquals(author, entry.getAuthor()),
                () -> assertEquals(review, entry.getReview()),
                () -> assertEquals(courseId, entry.getCourseId()),
                () -> assertEquals(title, entry.getTitle()),
                () -> assertNotNull(entry.getReviewEntryId()),
                () -> assertNotNull(entry.getEntryCreationTime()),
                () -> assertNotNull(entry.getEntryUpdateTime()));
    }

    @Test
    void shouldRecordReviewEntryCreatedEventOnCreation() {
        when(reviewEntryCreationService.existsForAuthorAndCourse(author, courseId)).thenReturn(false);

        ReviewEntry entry = ReviewEntry.createNew(author, review, courseId, title, reviewEntryCreationService);

        assertEquals(1, entry.getDomainEvents().size());
        ReviewEntryCreatedEvent event = assertInstanceOf(
                ReviewEntryCreatedEvent.class,
                entry.getDomainEvents().get(0).getPayload());
        assertAll("Expected ReviewEntryCreatedEvent payload",
                () -> assertEquals(author.userName(), event.getAuthor()),
                () -> assertEquals(courseId.id(), event.getCourseId()),
                () -> assertEquals(review, event.getReview()),
                () -> assertEquals(title.value(), event.getTitle()));
    }

    @Test
    void shouldThrowWhenReviewEntryAlreadyExistsForAuthorAndCourse() {
        when(reviewEntryCreationService.existsForAuthorAndCourse(author, courseId)).thenReturn(true);

        assertThrows(ReviewEntryAlreadyExistsForCourseAndUser.class,
                () -> ReviewEntry.createNew(author, review, courseId, title, reviewEntryCreationService));
    }

    @Test
    void shouldExposeCompositeAggregateId() {
        when(reviewEntryCreationService.existsForAuthorAndCourse(author, courseId)).thenReturn(false);

        ReviewEntry entry = ReviewEntry.createNew(author, review, courseId, title, reviewEntryCreationService);

        assertEquals(courseId.id() + ":" + author.userName(), entry.aggregateId());
    }

    @Test
    void shouldUpdateTitleAndRecordEventWhenTitleChanges() {
        ReviewEntry entry = existingEntry();
        Title newTitle = new Title("Updated Title");

        entry.updateTitle(newTitle);

        assertEquals(newTitle, entry.getTitle());
        assertEquals(1, entry.getDomainEvents().size());
        ReviewEntryTitleUpdatedEvent event = assertInstanceOf(
                ReviewEntryTitleUpdatedEvent.class,
                entry.getDomainEvents().get(0).getPayload());
        assertAll("Expected ReviewEntryTitleUpdatedEvent payload",
                () -> assertEquals(newTitle.value(), event.getTitle()),
                () -> assertEquals(courseId.id(), event.getCourseId()));
    }

    @Test
    void shouldNotRecordEventWhenTitleIsUnchanged() {
        ReviewEntry entry = existingEntry();

        entry.updateTitle(title);

        assertEquals(title, entry.getTitle());
        assertTrue(entry.getDomainEvents().isEmpty());
    }

    @Test
    void shouldUpdateReviewAndRecordEventWhenReviewChanges() {
        ReviewEntry entry = existingEntry();
        Review newReview = new Review(Stars.FIVE, "Even better on reflection!");

        entry.updateReview(newReview);

        assertEquals(newReview, entry.getReview());
        assertEquals(1, entry.getDomainEvents().size());
        ReviewUpdatedEvent event = assertInstanceOf(
                ReviewUpdatedEvent.class,
                entry.getDomainEvents().get(0).getPayload());
        assertAll("Expected ReviewUpdatedEvent payload",
                () -> assertEquals(newReview.stars(), event.getReview().stars()),
                () -> assertEquals(newReview.comment(), event.getReview().comment()),
                () -> assertEquals(courseId.id(), event.getCourseId()));
    }

    @Test
    void shouldNotRecordEventWhenReviewIsUnchanged() {
        ReviewEntry entry = existingEntry();

        entry.updateReview(review);

        assertEquals(review, entry.getReview());
        assertTrue(entry.getDomainEvents().isEmpty());
    }

    private ReviewEntry existingEntry() {
        ReviewEntryCreatedEvent createdEvent = new ReviewEntryCreatedEvent(
                new ReviewEntryId(UUID.randomUUID().toString()),
                Instant.now(),
                Instant.now(),
                author,
                review,
                courseId,
                title);
        return ReviewEntry.from(List.of(new Event<>(createdEvent)));
    }
}
