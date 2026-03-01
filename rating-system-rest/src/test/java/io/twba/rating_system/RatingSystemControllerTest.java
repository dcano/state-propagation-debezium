package io.twba.rating_system;

import io.twba.rating_system.api.ReviewEntryExceptionHandler;
import io.twba.rating_system.api.RatingSystemController;
import io.twba.rating_system.api.mapper.CreateReviewEntryRequestMapper;
import io.twba.rating_system.api.mapper.RatingSystemRequestMappers;
import io.twba.rating_system.api.mapper.UpdateReviewEntryTitleRequestMapper;
import io.twba.rating_system.api.mapper.UpdateReviewRequestMapper;
import io.twba.tk.command.CommandBus;
import io.twba.tk.command.DomainCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class RatingSystemControllerTest {

    @Mock
    private CommandBus commandBus;
    @Captor
    private ArgumentCaptor<DomainCommand> commandCaptor;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        var controller = new RatingSystemController(
                new RatingSystemRequestMappers(List.of(
                        new CreateReviewEntryRequestMapper(),
                        new UpdateReviewRequestMapper(),
                        new UpdateReviewEntryTitleRequestMapper())),
                commandBus);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ReviewEntryExceptionHandler())
                .build();
    }

    @Test
    void shouldDispatchCreateReviewEntryCommandFromRequest() throws Exception {
        mockMvc.perform(post("/twba/review-entry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "userName": "john.doe",
                                    "courseId": "course-123",
                                    "stars": "FOUR",
                                    "comment": "Great course!",
                                    "title": "My Review"
                                }
                                """))
                .andExpect(status().isCreated());

        verify(commandBus).push(commandCaptor.capture());
        CreateReviewEntryCommand command = assertInstanceOf(CreateReviewEntryCommand.class, commandCaptor.getValue());
        assertAll("Expected CreateReviewEntryCommand",
                () -> assertEquals("john.doe", command.getAuthor().userName()),
                () -> assertEquals("course-123", command.getCourseId().id()),
                () -> assertEquals(Stars.FOUR, command.getReview().stars()),
                () -> assertEquals("Great course!", command.getReview().comment()),
                () -> assertEquals("My Review", command.getTitle().value()));
    }

    @Test
    void shouldDispatchUpdateReviewCommandFromRequest() throws Exception {
        mockMvc.perform(patch("/twba/review-entry/review")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "userName": "john.doe",
                                    "courseId": "course-123",
                                    "stars": "FIVE",
                                    "comment": "Even better on reflection!"
                                }
                                """))
                .andExpect(status().isNoContent());

        verify(commandBus).push(commandCaptor.capture());
        UpdateReviewCommand command = assertInstanceOf(UpdateReviewCommand.class, commandCaptor.getValue());
        assertAll("Expected UpdateReviewCommand",
                () -> assertEquals("john.doe", command.getAuthor().userName()),
                () -> assertEquals("course-123", command.getCourseId().id()),
                () -> assertEquals(Stars.FIVE, command.getReview().stars()),
                () -> assertEquals("Even better on reflection!", command.getReview().comment()));
    }

    @Test
    void shouldDispatchUpdateReviewEntryTitleCommandFromRequest() throws Exception {
        mockMvc.perform(patch("/twba/review-entry/title")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "userName": "john.doe",
                                    "courseId": "course-123",
                                    "title": "Updated Title"
                                }
                                """))
                .andExpect(status().isNoContent());

        verify(commandBus).push(commandCaptor.capture());
        UpdateReviewEntryTitleCommand command = assertInstanceOf(UpdateReviewEntryTitleCommand.class, commandCaptor.getValue());
        assertAll("Expected UpdateReviewEntryTitleCommand",
                () -> assertEquals("john.doe", command.getAuthor().userName()),
                () -> assertEquals("course-123", command.getCourseId().id()),
                () -> assertEquals("Updated Title", command.getTitle().value()));
    }

    @Test
    void shouldReturn404WhenReviewEntryNotFound() throws Exception {
        doThrow(new ReviewEntryNotFoundForCourseAndUser(new CourseId("course-123"), EntryAuthor.of("john.doe")))
                .when(commandBus).push(any(DomainCommand.class));

        mockMvc.perform(patch("/twba/review-entry/review")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "userName": "john.doe",
                                    "courseId": "course-123",
                                    "stars": "THREE",
                                    "comment": "Decent"
                                }
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn304WhenReviewEntryAlreadyExists() throws Exception {
        doThrow(new ReviewEntryAlreadyExistsForCourseAndUser(new CourseId("course-123"), EntryAuthor.of("john.doe")))
                .when(commandBus).push(any(DomainCommand.class));

        mockMvc.perform(post("/twba/review-entry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "userName": "john.doe",
                                    "courseId": "course-123",
                                    "stars": "FOUR",
                                    "comment": "Great course!",
                                    "title": "My Review"
                                }
                                """))
                .andExpect(status().isNotModified());
    }
}
