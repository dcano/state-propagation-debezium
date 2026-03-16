package io.twba.rating_system;

import io.twba.tk.core.DomainEventPayload;
import io.twba.tk.core.Event;
import io.twba.tk.eventsource.EventStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReviewEntryRepositoryEventSourcedTest {

    @Mock
    private EventStore eventStore;
    @Mock
    private ReviewEntryCreationService reviewEntryCreationService;
    @Captor
    ArgumentCaptor<List<Event<? extends DomainEventPayload>>> eventsCaptor;

    private ReviewEntryRepositoryEventSourced repository;

    @BeforeEach
    void setUp() {
        repository = new ReviewEntryRepositoryEventSourced(eventStore);
    }

    @Test
    void shouldSaveReviewEntryByAppendingDomainEvents() {
        ReviewEntry reviewEntry = newReviewEntry(
                EntryAuthor.of("user1"),
                new CourseId(UUID.randomUUID().toString()));

        repository.save(reviewEntry);

        verify(eventStore).appendEvents(eventsCaptor.capture());
        assertEquals(reviewEntry.getDomainEvents(), eventsCaptor.getValue());
    }

    @Test
    void shouldRetrieveReviewEntryByAuthorAndCourse() {
        EntryAuthor author = EntryAuthor.of("user1");
        CourseId courseId = new CourseId(UUID.randomUUID().toString());
        String expectedAggregateId = courseId.id() + ":" + author.userName();
        Review review = new Review(Stars.FOUR, "Great course!");
        Title title = new Title("My Review");

        when(eventStore.retrieveEventsFor(ReviewEntry.class.getSimpleName(), expectedAggregateId))
                .thenReturn(reviewEntryCreatedEvents(author, courseId, review, title));

        Optional<ReviewEntry> result = repository.retrieveReviewEntryFor(author, courseId);

        assertAll("Expected ReviewEntry",
                () -> assertTrue(result.isPresent()),
                () -> assertEquals(author, result.get().getAuthor()),
                () -> assertEquals(courseId, result.get().getCourseId()),
                () -> assertEquals(review, result.get().getReview()),
                () -> assertEquals(title, result.get().getTitle()));
    }

    @Test
    void shouldQueryEventStoreWithAggregateTypeAndCompositeAggregateId() {
        EntryAuthor author = EntryAuthor.of("user2");
        CourseId courseId = new CourseId(UUID.randomUUID().toString());
        String expectedAggregateId = courseId.id() + ":" + author.userName();

        when(eventStore.retrieveEventsFor(ReviewEntry.class.getSimpleName(), expectedAggregateId))
                .thenReturn(reviewEntryCreatedEvents(author, courseId, new Review(Stars.TWO, "Decent"), new Title("Ok")));

        repository.retrieveReviewEntryFor(author, courseId);

        verify(eventStore).retrieveEventsFor("ReviewEntry", expectedAggregateId);
    }

    private ReviewEntry newReviewEntry(EntryAuthor author, CourseId courseId) {
        when(reviewEntryCreationService.existsForAuthorAndCourse(author, courseId)).thenReturn(false);
        return ReviewEntry.createNew(author, new Review(Stars.FIVE, "Excellent!"), courseId, new Title("My Title"), reviewEntryCreationService);
    }

    private List<Event<DomainEventPayload>> reviewEntryCreatedEvents(EntryAuthor author, CourseId courseId, Review review, Title title) {
        ReviewEntryCreatedEvent payload = new ReviewEntryCreatedEvent(
                new ReviewEntryId(UUID.randomUUID().toString()),
                Instant.now(),
                Instant.now(),
                author,
                review,
                courseId,
                title);
        return List.of(new Event<>(payload));
    }
}
