package io.twba.course_management.repository;

import io.twba.course_management.CourseDefinition;
import io.twba.course_management.CourseDefinitionRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static io.twba.course_management.repository.CourseDefinitions.randomNewCourseDefinition;
import static org.junit.jupiter.api.Assertions.*;


public class CourseDefinitionRepositoryTest {

    CourseDefinitionRepository courseDefinitionRepository;

    @Test
    public void shouldSaveANewlyCreatedCourseDefinition() {
        CourseDefinition expected = randomNewCourseDefinition();
        CourseDefinition actual = courseDefinitionRepository.save(expected);

        //TODO add more assertions
        assertAll("Create Course Definition",
                () -> assertEquals(0L, actual.getVersion(), "should match versions"),
                () -> assertEquals(expected.getId(), actual.getId(), "should match ids")
        );

    }

    @Test
    public void shouldDeleteExistingCourseDefinition() {
        CourseDefinition expected = randomNewCourseDefinition();
        CourseDefinition saved = courseDefinitionRepository.save(expected);

        saved.delete();
        courseDefinitionRepository.delete(saved);

        Optional<CourseDefinition> result = courseDefinitionRepository.findById(expected.getId(), expected.getTenantId());
        assertTrue(result.isEmpty(), "should not find deleted course definition");
    }

    @Test
    public void shouldFindExistingCourseDefinitionById() {
        CourseDefinition expected = randomNewCourseDefinition();
        CourseDefinition saved = courseDefinitionRepository.save(expected);

        Optional<CourseDefinition> result = courseDefinitionRepository.findById(saved.getId(), saved.getTenantId());
        assertTrue(result.isPresent(), "should find existing course definition");
        assertEquals(saved.getId(), result.get().getId(), "should match ids");
    }

    @Test
    public void shouldReturnEmptyWhenCourseDefinitionNotFound() {
        Optional<CourseDefinition> result = courseDefinitionRepository.findById(
                io.twba.course_management.CourseId.of("non-existent"),
                io.twba.tk.core.TenantId.of("non-existent"));
        assertTrue(result.isEmpty(), "should return empty for non-existent course");
    }

    @Test
    public void shouldSaveExistingCourseDefinition() {
        CourseDefinition expected = randomNewCourseDefinition();
        CourseDefinition actualTemp = courseDefinitionRepository.save(expected);
        CourseDefinition actual = courseDefinitionRepository.save(actualTemp);

        assertAll("Update Existing Course Definition",
                () -> assertEquals(actualTemp.getVersion()+1, actual.getVersion(), "should match versions"),
                () -> assertEquals(expected.getId(), actual.getId(), "should match ids"));
    }


}
