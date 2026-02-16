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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class InitializeCourseReviewCommandHandlerTest {

    @Mock
    private CourseReviewRepository courseReviewRepository;
    @Captor
    ArgumentCaptor<CourseReview> courseReviewCaptor;

    private InitializeCourseReviewCommandHandler commandHandler;

    @BeforeEach
    public void setUp() {
        commandHandler = new InitializeCourseReviewCommandHandler(this.courseReviewRepository);
    }

    @Test
    public void shouldInitializeCourseReviewFromCommand() {
        InitializeCourseReviewCommand command = randomCommand();
        when(courseReviewRepository.existsCourseReviewForCourse(command.getCourseId())).thenReturn(false);
        commandHandler.handle(command);
        verify(courseReviewRepository).save(courseReviewCaptor.capture());
        CourseReview actualCourseReview = courseReviewCaptor.getValue();
        assertAll("Expected CourseReview",
                () -> assertEquals(command.getCourseId(), actualCourseReview.getCourseId()),
                () -> assertEquals(command.getTenantId(), actualCourseReview.getTenantId()));
    }

    @Test
    public void shouldNotInitializeCourseReviewWhenOneAlreadyExists() {
        InitializeCourseReviewCommand command = randomCommand();
        when(courseReviewRepository.existsCourseReviewForCourse(command.getCourseId())).thenReturn(true);
        commandHandler.handle(command);
        verify(courseReviewRepository, never()).save(any());
    }

    private InitializeCourseReviewCommand randomCommand() {
        return new InitializeCourseReviewCommand(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString());
    }
}
