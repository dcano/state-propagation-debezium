package io.twba.rating_system;

import io.twba.tk.core.Event;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UpdateReviewCommandHandlerTest {

    @Mock
    private ReviewEntryRepository reviewEntryRepository;
    @Captor
    ArgumentCaptor<ReviewEntry> reviewEntryCaptor;

    private UpdateReviewCommandHandler commandHandler;

    @BeforeEach
    public void setUp() {
        commandHandler = new UpdateReviewCommandHandler(reviewEntryRepository);
    }

    @Test
    public void shouldUpdateReviewOfReviewEntry() {
        UpdateReviewCommand command = randomCommand(Stars.THREE, "Updated comment");
        ReviewEntry existingEntry = existingReviewEntry(command.getAuthor(), command.getCourseId(), Stars.ONE, "Old comment");
        when(reviewEntryRepository.retrieveReviewEntryFor(command.getAuthor(), command.getCourseId()))
                .thenReturn(Optional.of(existingEntry));

        commandHandler.handle(command);

        verify(reviewEntryRepository).save(reviewEntryCaptor.capture());
        assertEquals(command.getReview(), reviewEntryCaptor.getValue().getReview());
    }

    @Test
    public void shouldThrowWhenReviewEntryNotFound() {
        UpdateReviewCommand command = randomCommand(Stars.FIVE, "Great!");
        when(reviewEntryRepository.retrieveReviewEntryFor(any(), any())).thenReturn(Optional.empty());

        assertThrows(ReviewEntryNotFoundForCourseAndUser.class, () -> commandHandler.handle(command));
    }

    private UpdateReviewCommand randomCommand(Stars stars, String comment) {
        return new UpdateReviewCommand(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                stars,
                comment);
    }

    private ReviewEntry existingReviewEntry(EntryAuthor author, CourseId courseId, Stars stars, String comment) {
        ReviewEntryCreatedEvent createdEvent = new ReviewEntryCreatedEvent(
                new ReviewEntryId(UUID.randomUUID().toString()),
                Instant.now(),
                Instant.now(),
                author,
                new Review(stars, comment),
                courseId,
                new Title("Some Title"));
        return ReviewEntry.from(List.of(new Event<>(createdEvent)));
    }
}
