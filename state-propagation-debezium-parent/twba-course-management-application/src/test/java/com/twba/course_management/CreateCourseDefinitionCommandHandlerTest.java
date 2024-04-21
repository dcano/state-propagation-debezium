package com.twba.course_management;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CreateCourseDefinitionCommandHandlerTest {

    @Mock
    private CourseDefinitionRepository courseDefinitionRepository;
    @Captor
    ArgumentCaptor<CourseDefinition> courseDefinitionCaptor;

    private CreateCourseDefinitionCommandHandler commandHandler;

    @BeforeEach
    public void setUp() {
        commandHandler = new CreateCourseDefinitionCommandHandler(this.courseDefinitionRepository);
    }

    @Test
    public void shouldCreateCourseDefinitionFromCommand() {
        CreateCourseDefinitionCommand command = randomCommand();
        when(courseDefinitionRepository.existsCourseDefinitionWith(command.getTenantId(), command.getCourseDescription().title())).thenReturn(false);
        commandHandler.handle(command);
        verify(courseDefinitionRepository).save(courseDefinitionCaptor.capture());
        CourseDefinition actualCourseDefinition = courseDefinitionCaptor.getValue();
        assertAll("Expected CourseDefinition",
                () -> assertEquals(command.getCourseId(), actualCourseDefinition.getId()),
                () -> assertEquals(command.getCourseDescription(), actualCourseDefinition.getCourseDescription()));
    }

    private CreateCourseDefinitionCommand randomCommand() {
        return new CreateCourseDefinitionCommand(Instant.now(),
                Instant.now(),
                UUID.randomUUID().toString(),
                "Course Title",
                "Course Summary",
                "Course Description",
                "Course objectives summary",
                "CourseTeacher",
                Collections.singletonList("Course prerequirement 1"),
                Duration.ofHours(10).toMillis(),
                4,
                UUID.randomUUID().toString());
    }
}
