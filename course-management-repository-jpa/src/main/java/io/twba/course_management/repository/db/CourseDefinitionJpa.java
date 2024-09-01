package io.twba.course_management.repository.db;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Data
@Entity
@Table(schema="courses_context", name = "course_definition",
        indexes = {
                @Index(name = "tenant_course_id", columnList = "tenantId, id"),
                @Index(name = "tenant_status", columnList = "tenantId, status")
        })
public class CourseDefinitionJpa {

    @Id
    private String id;
    private String status;
    private String teacherId;
    private String tenantId;
    private String title;
    private String objective;
    private String summary;
    private String description;
    private String preRequirement;
    private Instant publicationDate;
    private Instant openingDate;
    @Embedded
    private CourseDurationJpa duration;

    @Version
    private Long version;

}
