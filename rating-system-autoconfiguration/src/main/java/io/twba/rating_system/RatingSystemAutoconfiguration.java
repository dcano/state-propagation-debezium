package io.twba.rating_system;

import io.twba.tk.eventsource.EventStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RatingSystemAutoconfiguration {

    @ConditionalOnMissingBean
    @Bean
    CourseReviewRepository courseReviewRepository(@Autowired EventStore eventStore) {
        return new CourseReviewRepositoryEventSourced(eventStore);
    }

}
