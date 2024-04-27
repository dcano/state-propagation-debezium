package com.twba.course_management.repository;

import com.twba.course_management.CourseDefinition;
import org.junit.jupiter.api.Test;

import com.twba.course_management.CourseDefinitionRepository;

import static com.twba.course_management.repository.CourseDefinitions.randomNewCourseDefinition;
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


}
