package com.twba.course_management.repository.db;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Data
@Embeddable
public class CourseDurationJpa {

    private long expectedDurationMillis;
    private int numberOfClasses;

}
