package io.twba.course_management;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@EnableAspectJAutoProxy
@SpringBootApplication
public class CourseManagementApp {

    public static void main(String[] args) {
        SpringApplication.run(CourseManagementApp.class, args);
    }

}
