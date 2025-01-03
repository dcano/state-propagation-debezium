package io.twba.rating_system;

import java.util.Optional;

interface CourseManagementService {

    Optional<CourseData> retrieveCourse(String courseId);

}
