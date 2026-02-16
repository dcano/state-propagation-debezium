package io.twba.course_management;

import io.twba.tk.core.TenantId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DeleteCourseDefinitionCommandHandlerTest {

    @Mock
    private CourseDefinitionRepository courseDefinitionRepository;
    @Captor
    ArgumentCaptor<CourseDefinition> courseDefinitionCaptor;

    private DeleteCourseDefinitionCommandHandler commandHandler;

    @BeforeEach
    public void setUp() {
        commandHandler = new DeleteCourseDefinitionCommandHandler(this.courseDefinitionRepository);
    }

    @Test
    public void shouldDeleteExistingCourseDefinition() {
        DeleteCourseDefinitionCommand command = randomCommand();
        CourseDefinition existingCourse = existingCourseDefinition(command.getCourseId(), command.getTenantId());
        when(courseDefinitionRepository.findById(command.getCourseId(), command.getTenantId()))
                .thenReturn(Optional.of(existingCourse));

        commandHandler.handle(command);

        verify(courseDefinitionRepository).delete(courseDefinitionCaptor.capture());
        CourseDefinition deletedCourse = courseDefinitionCaptor.getValue();
        assertAll("Deleted CourseDefinition",
                () -> assertEquals(command.getCourseId(), deletedCourse.getId()),
                () -> assertTrue(deletedCourse.hasEvents()),
                () -> assertInstanceOf(CourseDefinitionDeletedEvent.class, deletedCourse.getDomainEvents().getFirst().getPayload()));
    }

    @Test
    public void shouldThrowWhenCourseDefinitionNotFound() {
        DeleteCourseDefinitionCommand command = randomCommand();
        when(courseDefinitionRepository.findById(command.getCourseId(), command.getTenantId()))
                .thenReturn(Optional.empty());

        assertThrows(CourseDefinitionNotFoundException.class, () -> commandHandler.handle(command));
        verify(courseDefinitionRepository, never()).delete(any());
    }

    private DeleteCourseDefinitionCommand randomCommand() {
        return new DeleteCourseDefinitionCommand(UUID.randomUUID().toString(), UUID.randomUUID().toString());
    }

    private CourseDefinition existingCourseDefinition(CourseId courseId, TenantId tenantId) {
        return CourseDefinition.builder(tenantId)
                .withCourseId(courseId)
                .withCourseDescription(CourseDescription.from(CourseTitle.of("Title"), "Summary", "Description"))
                .withCourseObjective(CourseObjective.of("Objective"))
                .withPreRequirements(java.util.Collections.singletonList(PreRequirement.from("Req1")))
                .withDuration(CourseDuration.of(36000000, 10))
                .withTeacherId(TeacherId.from(UUID.randomUUID().toString()))
                .withCourseDates(CourseDates.of(java.time.Instant.now(), java.time.Instant.now()))
                .withCourseStatus(CourseStatus.ACTIVE)
                .withVersion(1)
                .instance();
    }
}
