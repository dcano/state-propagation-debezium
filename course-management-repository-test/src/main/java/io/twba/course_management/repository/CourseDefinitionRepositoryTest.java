package io.twba.course_management.repository;

import io.twba.course_management.CourseDefinition;
import io.twba.course_management.CourseDefinitionRepository;
import org.junit.jupiter.api.Test;

import static io.twba.course_management.repository.CourseDefinitions.randomNewCourseDefinition;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;


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
    public void shouldSaveExistingCourseDefinition() {
        CourseDefinition expected = randomNewCourseDefinition();
        CourseDefinition actualTemp = courseDefinitionRepository.save(expected);
        CourseDefinition actual = courseDefinitionRepository.save(actualTemp);

        assertAll("Update Existing Course Definition",
                () -> assertEquals(actualTemp.getVersion()+1, actual.getVersion(), "should match versions"),
                () -> assertEquals(expected.getId(), actual.getId(), "should match ids"));
    }


}
