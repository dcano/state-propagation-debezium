package io.twba.rating_system;

import io.twba.tk.core.ExternalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

@Component
class CourseManagementServiceSpring implements CourseManagementService {

    private static final String COURSE_MANAGEMENT_SERVICE = "course-management";

    private final RestTemplate restTemplate;
    private final String courseManagementServiceBaseUrl;

    @Autowired
    CourseManagementServiceSpring(RestTemplate restTemplate, List<ExternalService> externalServices) {
        this.restTemplate = restTemplate;
        courseManagementServiceBaseUrl = externalServices.stream()
                .filter(es -> es.getServiceName().equals(COURSE_MANAGEMENT_SERVICE))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Wrong external services configuration, missing \"course management\" service configuration"))
                .getServiceUrl();
    }

    @Override
    public Optional<CourseData> retrieveCourse(String courseId) {
        var getCourseUrl = courseManagementServiceBaseUrl + "/" + courseId;
        ResponseEntity<CourseData> response = restTemplate.getForEntity(getCourseUrl, CourseData.class);
        return Optional.ofNullable(response.getBody());

    }
}
