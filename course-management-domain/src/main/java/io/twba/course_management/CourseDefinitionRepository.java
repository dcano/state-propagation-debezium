package io.twba.course_management;

import io.twba.tk.core.TenantId;

public interface CourseDefinitionRepository {

    boolean existsCourseDefinitionWith(TenantId tenantId, CourseTitle courseTitle);
    CourseDefinition save(CourseDefinition courseDefinition);

}
