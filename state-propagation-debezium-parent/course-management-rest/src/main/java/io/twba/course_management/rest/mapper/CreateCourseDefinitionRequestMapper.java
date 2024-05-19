package io.twba.course_management.rest.mapper;

import io.twba.course_management.CreateCourseDefinitionCommand;
import io.twba.course_management.rest.CourseManagementController;
import io.twba.tk.command.DomainCommand;
import io.twba.tk.rest.RequestMapper;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;

@Component
public class CreateCourseDefinitionRequestMapper implements RequestMapper<CourseManagementController.CreateCourseDefinitionRequest> {

    public static final String TENANT_ID = "LOCAL_TENANT"; //TODO get from security token

    @Override
    public boolean maps(Class<?> requestClass) {
        return CourseManagementController.CreateCourseDefinitionRequest.class.isAssignableFrom(requestClass);
    }

    @Override
    public DomainCommand toCommand(CourseManagementController.CreateCourseDefinitionRequest request) {
        return new CreateCourseDefinitionCommand(
                Instant.parse(request.getPublicationDate()),
                Instant.parse(request.getOpeningDate()),
                request.getId(),
                request.getTitle(),
                request.getSummary(),
                request.getDescription(),
                request.getObjective(),
                request.getTeacherId(),
                Collections.singletonList(request.getPreRequirement()),
                Duration.ofHours(request.getExpectedDurationHours()).toMillis(),
                request.getNumberOfClasses(),
                TENANT_ID);
    }
}
