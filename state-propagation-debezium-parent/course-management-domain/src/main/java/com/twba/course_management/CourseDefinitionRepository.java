package com.twba.course_management;

import com.twba.tk.core.TenantId;

public interface CourseDefinitionRepository {

    boolean existsCourseDefinitionWith(TenantId tenantId, CourseTitle courseTitle);
    CourseDefinition save(CourseDefinition courseDefinition);

}
