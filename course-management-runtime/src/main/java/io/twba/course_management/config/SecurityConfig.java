package io.twba.course_management.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!unsafe")
public class SecurityConfig {



}
