package com.twba.course_management.repository.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseDefinitionJpaHelper extends JpaRepository<CourseDefinitionJpa, String> {

    CourseDefinitionJpa findCourseDefinitionJpaByTenantIdAndTitle(String tenantId, String title);

}
