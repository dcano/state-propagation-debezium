package com.twba.course_management;

import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

public class CourseManagementAppPerformanceTest {

    private final static int SHIFT = 1000;

    //Perform http requests
    @Test
    public void shouldCreateCoursesForPerformanceVerification() {
        RestTemplate restTemplate = new RestTemplateBuilder().build();
        String apiUrl = "http://localhost:9095/twba/course";
        String requestJson = "{\n" +
                "    \"id\": \"{{myCourseId}}\",\n" +
                "    \"title\": \"{{courseTitle}}\",\n" +
                "    \"summary\": \"Course Summary 2\",\n" +
                "    \"description\": \"Course description2\",\n" +
                "    \"teacherId\": \"teacherId2\",\n" +
                "    \"openingDate\": \"2022-04-06T00:00:00.00Z\",\n" +
                "    \"publicationDate\": \"2022-04-03T00:00:00.00Z\",\n" +
                "    \"preRequirement\": \"Course pre requirement2\",\n" +
                "    \"objective\": \"Course objective2\",\n" +
                "    \"expectedDurationHours\": \"50\",\n" +
                "    \"numberOfClasses\": \"10\",\n" +
                "    \"status\": \"XX\"\n" +
                "}";

        for (int i = 0; i < 100; i++) {
            requestJson = requestJson.replace("{{myCourseId}}", "courseId__" + i + SHIFT);
            requestJson = requestJson.replace("{{courseTitle}}", "courseTitle__" + i + SHIFT);
            restTemplate.postForEntity(apiUrl, requestJson, String.class);
        }
    }
}
