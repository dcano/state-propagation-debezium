package io.twba.course_management.repository.db;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
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
