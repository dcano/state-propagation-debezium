package io.twba.course_management.rest;

import io.twba.course_management.CourseView;
import io.twba.course_management.rest.mapper.CourseManagementRequestMappers;
import io.twba.tk.command.CommandBus;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("twba")
public class CourseManagementController {

    private final CourseManagementRequestMappers requestMappers;
    private final CommandBus commandBus;

    @Autowired
    public CourseManagementController(CourseManagementRequestMappers requestMappers, CommandBus commandBus) {
        this.requestMappers = requestMappers;
        this.commandBus = commandBus;
    }

    @PostMapping(value = "/course",
            path = "/course",
            consumes = {"application/json"}
    )
    @ResponseStatus(code = HttpStatus.CREATED)
    public void createCourse(@RequestBody CreateCourseDefinitionRequest request) {
        var command = requestMappers.mapRequestToCommand(request);
        commandBus.push(command);
    }


    @PreAuthorize("hasAuthority('READ')")
    @GetMapping(value = "/course/{courseId}")
    public CourseView retrieveCourse(@PathVariable String courseId) {
        return new CourseView(courseId);
    }

    @Data
    public static class CreateCourseDefinitionRequest {

        private String id;
        private String title;
        private String summary;
        private String description;
        private String teacherId;
        private String openingDate;
        private String publicationDate;
        private String preRequirement;
        private String objective;
        private long expectedDurationHours;
        private int numberOfClasses;
        private String status;



    }

}
