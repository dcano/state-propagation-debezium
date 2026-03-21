package io.twba.course_management;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public class UpdateCourseDescriptionCommandHandlerTest {

    @Mock
    private CourseDefinitionRepository courseDefinitionRepository;

    private UpdateCourseDescriptionCommandHandler commandHandler;

    @BeforeEach
    public void setUp() {
        commandHandler = new UpdateCourseDescriptionCommandHandler(courseDefinitionRepository);
    }

    @Test
    public void shouldUpdateCourseDescription() {
        UpdateCourseDescriptionCommand command = randomCommand();
        commandHandler.handle(command);
    }

    private UpdateCourseDescriptionCommand randomCommand() {
        return new UpdateCourseDescriptionCommand(
                UUID.randomUUID().toString(),
                "Course Title",
                "Course Summary",
                "Course Description"
        );
    }
}
