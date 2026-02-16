package io.twba.course_management;

import io.twba.course_management.rest.CourseManagementController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ContextConfiguration(classes = {CourseManagementApp.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles({"postgres", "unsafe"})
public class DeleteCourseDefinitionApiTest {

    @Container
    public static PostgreSQLContainer<?> container = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("courses_db")
            .withUsername("sa")
            .withPassword("sa");

    @DynamicPropertySource
    public static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", container::getJdbcUrl);
        registry.add("spring.datasource.username", container::getUsername);
        registry.add("spring.datasource.password", container::getPassword);
        registry.add("spring.datasource.driver-class-name", container::getDriverClassName);
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void shouldDeleteExistingCourse() {
        String courseId = UUID.randomUUID().toString();
        createCourse(courseId);

        ResponseEntity<Void> response = restTemplate.exchange(
                "/twba/course/{courseId}",
                HttpMethod.DELETE,
                HttpEntity.EMPTY,
                Void.class,
                courseId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    public void shouldReturnErrorWhenDeletingNonExistentCourse() {
        String courseId = UUID.randomUUID().toString();

        ResponseEntity<Void> response = restTemplate.exchange(
                "/twba/course/{courseId}",
                HttpMethod.DELETE,
                HttpEntity.EMPTY,
                Void.class,
                courseId);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    private void createCourse(String courseId) {
        CourseManagementController.CreateCourseDefinitionRequest request = new CourseManagementController.CreateCourseDefinitionRequest();
        request.setId(courseId);
        request.setTitle("Course Title " + courseId);
        request.setSummary("Course Summary");
        request.setDescription("Course Description");
        request.setTeacherId(UUID.randomUUID().toString());
        request.setOpeningDate(Instant.now().plusSeconds(86400).toString());
        request.setPublicationDate(Instant.now().toString());
        request.setPreRequirement("Basic knowledge");
        request.setObjective("Learn something");
        request.setExpectedDurationHours(10);
        request.setNumberOfClasses(5);
        request.setStatus("ACTIVE");

        ResponseEntity<Void> createResponse = restTemplate.postForEntity("/twba/course", request, Void.class);
        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
    }
}
