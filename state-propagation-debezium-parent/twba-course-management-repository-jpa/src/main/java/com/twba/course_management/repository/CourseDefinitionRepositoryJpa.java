package com.twba.course_management.repository;

import com.twba.course_management.CourseDefinition;
import com.twba.course_management.CourseDefinitionRepository;
import com.twba.course_management.CourseTitle;
import com.twba.course_management.repository.db.CourseDefinitionJpaHelper;
import com.twba.tk.core.TenantId;

public class CourseDefinitionRepositoryJpa implements CourseDefinitionRepository {

    private final CourseDefinitionJpaHelper helper;

    public CourseDefinitionRepositoryJpa(CourseDefinitionJpaHelper helper) {
        this.helper = helper;
    }

    @Override
    public boolean existsCourseDefinitionWith(TenantId tenantId, CourseTitle courseTitle) {
        return false;
    }

    @Override
    public CourseDefinition save(CourseDefinition courseDefinition) {
        return null;
    }
}
