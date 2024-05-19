package io.twba.course_management.repository.config;

import io.twba.course_management.CourseDefinitionRepository;
import io.twba.course_management.repository.CourseDefinitionRepositoryJpa;
import io.twba.course_management.repository.db.CourseDefinitionJpaHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ComponentScan(basePackages = {
        "io.twba.course_management"
})
@EntityScan(basePackages = {
        "io.twba.course_management"
})
@EnableJpaRepositories(basePackages = {
        "io.twba.course_management"
})
public class PersistenceConfig {

    @Bean
    public CourseDefinitionRepository studentRepository(@Autowired CourseDefinitionJpaHelper courseDefinitionJpaHelper) {
        return new CourseDefinitionRepositoryJpa(courseDefinitionJpaHelper);
    }

}
