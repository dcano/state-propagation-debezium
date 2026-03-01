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
public class UpdateReviewEntryTitleCommandHandlerTest {

    @Mock
    private ReviewEntryRepository reviewEntryRepository;
    @Captor
    ArgumentCaptor<ReviewEntry> reviewEntryCaptor;

    private UpdateReviewEntryTitleCommandHandler commandHandler;

    @BeforeEach
    public void setUp() {
        commandHandler = new UpdateReviewEntryTitleCommandHandler(reviewEntryRepository);
    }

    @Test
    public void shouldUpdateTitleOfReviewEntry() {
        UpdateReviewEntryTitleCommand command = randomCommand("New Title");
        ReviewEntry existingEntry = existingReviewEntry(command.getAuthor(), command.getCourseId(), "Old Title");
        when(reviewEntryRepository.retrieveReviewEntryFor(command.getAuthor(), command.getCourseId()))
                .thenReturn(Optional.of(existingEntry));

        commandHandler.handle(command);

        verify(reviewEntryRepository).save(reviewEntryCaptor.capture());
        assertEquals(command.getTitle(), reviewEntryCaptor.getValue().getTitle());
    }

    @Test
    public void shouldThrowWhenReviewEntryNotFound() {
        UpdateReviewEntryTitleCommand command = randomCommand("Any Title");
        when(reviewEntryRepository.retrieveReviewEntryFor(any(), any())).thenReturn(Optional.empty());

        assertThrows(ReviewEntryNotFoundForCourseAndUser.class, () -> commandHandler.handle(command));
    }

    private UpdateReviewEntryTitleCommand randomCommand(String title) {
        return new UpdateReviewEntryTitleCommand(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                title);
    }

    private ReviewEntry existingReviewEntry(EntryAuthor author, CourseId courseId, String title) {
        ReviewEntryCreatedEvent createdEvent = new ReviewEntryCreatedEvent(
                new ReviewEntryId(UUID.randomUUID().toString()),
                Instant.now(),
                Instant.now(),
                author,
                new Review(Stars.TWO, "Some comment"),
                courseId,
                new Title(title));
        return ReviewEntry.from(List.of(new Event<>(createdEvent)));
    }
}
