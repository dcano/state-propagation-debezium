package io.twba.rating_system;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CreateReviewEntryCommandHandlerTest {

    @Mock
    private ReviewEntryRepository reviewEntryRepository;
    @Mock
    private ReviewEntryCreationService reviewEntryCreationService;
    @Captor
    ArgumentCaptor<ReviewEntry> reviewEntryCaptor;

    private CreateReviewEntryCommandHandler commandHandler;

    @BeforeEach
    public void setUp() {
        commandHandler = new CreateReviewEntryCommandHandler(reviewEntryRepository, reviewEntryCreationService);
    }

    @Test
    public void shouldCreateReviewEntryFromCommand() {
        CreateReviewEntryCommand command = randomCommand();
        when(reviewEntryCreationService.existsForAuthorAndCourse(any(), any())).thenReturn(false);

        commandHandler.handle(command);

        verify(reviewEntryRepository).save(reviewEntryCaptor.capture());
        ReviewEntry actual = reviewEntryCaptor.getValue();
        assertAll("Expected ReviewEntry",
                () -> assertEquals(command.getAuthor(), actual.getAuthor()),
                () -> assertEquals(command.getReview(), actual.getReview()),
                () -> assertEquals(command.getCourseId(), actual.getCourseId()),
                () -> assertEquals(command.getTitle(), actual.getTitle()));
    }

    @Test
    public void shouldThrowWhenReviewEntryAlreadyExistsForAuthorAndCourse() {
        CreateReviewEntryCommand command = randomCommand();
        when(reviewEntryCreationService.existsForAuthorAndCourse(any(), any())).thenReturn(true);

        org.junit.jupiter.api.Assertions.assertThrows(
                ReviewEntryAlreadyExistsForCourseAndUser.class,
                () -> commandHandler.handle(command));
    }

    private CreateReviewEntryCommand randomCommand() {
        return new CreateReviewEntryCommand(
                UUID.randomUUID().toString(),
                Stars.FOUR,
                "Great course",
                UUID.randomUUID().toString(),
                "My Review Title");
    }
}
