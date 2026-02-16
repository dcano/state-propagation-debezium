package io.twba.course_management;

import io.twba.tk.core.TenantId;

import java.util.Optional;

public interface CourseDefinitionRepository {

    boolean existsCourseDefinitionWith(TenantId tenantId, CourseTitle courseTitle);
    CourseDefinition save(CourseDefinition courseDefinition);
    Optional<CourseDefinition> findById(CourseId courseId, TenantId tenantId);
    void delete(CourseDefinition courseDefinition);

}
