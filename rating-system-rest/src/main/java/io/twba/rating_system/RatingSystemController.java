package io.twba.rating_system;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("twba")
public class RatingSystemController {

    private final CourseManagementService courseManagementService;

    @Autowired
    public RatingSystemController(CourseManagementService courseManagementService) {
        this.courseManagementService = courseManagementService;
    }

    @GetMapping(value = "/rating/{courseId}")
    public RateView retrieveCourse(@PathVariable("courseId") String courseId) {
        CourseData courseData = courseManagementService.retrieveCourse(courseId).orElse(CourseData.empty());
        return new RateView(courseData.courseId());
    }

}
