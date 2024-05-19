package com.twba.course_management.repository.config;

import com.twba.course_management.CourseDefinitionRepository;
import com.twba.course_management.repository.CourseDefinitionRepositoryJpa;
import com.twba.course_management.repository.db.CourseDefinitionJpaHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ComponentScan(basePackages = {
        "com.twba.course_management"
})
@EntityScan(basePackages = {
        "com.twba.course_management"
})
@EnableJpaRepositories(basePackages = {
        "com.twba.course_management"
})
public class PersistenceConfig {

    @Bean
    public CourseDefinitionRepository studentRepository(@Autowired CourseDefinitionJpaHelper courseDefinitionJpaHelper) {
        return new CourseDefinitionRepositoryJpa(courseDefinitionJpaHelper);
    }

}
