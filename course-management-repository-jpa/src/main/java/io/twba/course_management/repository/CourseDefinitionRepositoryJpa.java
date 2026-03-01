package io.twba.course_management.repository;

import io.twba.course_management.repository.db.CourseDefinitionJpa;
import io.twba.course_management.repository.db.CourseDefinitionJpaHelper;
import io.twba.course_management.repository.db.CourseDurationJpa;
import io.twba.tk.core.AppendEvents;
import io.twba.tk.core.TenantId;
import io.twba.course_management.CourseDates;
import io.twba.course_management.CourseDefinition;
import io.twba.course_management.CourseDefinitionRepository;
import io.twba.course_management.CourseDescription;
import io.twba.course_management.CourseDuration;
import io.twba.course_management.CourseId;
import io.twba.course_management.CourseObjective;
import io.twba.course_management.CourseStatus;
import io.twba.course_management.CourseTitle;
import io.twba.course_management.PreRequirement;
import io.twba.course_management.TeacherId;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class CourseDefinitionRepositoryJpa implements CourseDefinitionRepository {

    private final CourseDefinitionJpaHelper helper;

    public CourseDefinitionRepositoryJpa(CourseDefinitionJpaHelper helper) {
        this.helper = helper;
    }

    @Override
    public boolean existsCourseDefinitionWith(TenantId tenantId, CourseTitle courseTitle) {
        return Objects.nonNull(helper.findCourseDefinitionJpaByTenantIdAndTitle(tenantId.value(), courseTitle.value()));
    }

    @AppendEvents
    @Override
    public CourseDefinition save(CourseDefinition courseDefinition) {
        if(Objects.isNull(courseDefinition)) {
            return null;
        }
        CourseDefinitionJpa courseDefinitionJpa = toJpa(courseDefinition);
        return toDomain(helper.save(courseDefinitionJpa));
    }

    @Override
    public Optional<CourseDefinition> findById(CourseId courseId, TenantId tenantId) {
        CourseDefinitionJpa jpa = helper.findCourseDefinitionJpaByIdAndTenantId(courseId.value(), tenantId.value());
        return Optional.ofNullable(toDomain(jpa));
    }

    @AppendEvents
    @Override
    public void delete(CourseDefinition courseDefinition) {
        helper.deleteById(courseDefinition.getId().value());
    }

    private CourseDefinition toDomain(CourseDefinitionJpa courseDefinitionJpa) {
        if(Objects.isNull(courseDefinitionJpa)) {
            return null;
        }
        return CourseDefinition.builder(new TenantId(courseDefinitionJpa.getTenantId()))
                .withCourseId(CourseId.of(courseDefinitionJpa.getId()))
                .withTenantId(TenantId.of(courseDefinitionJpa.getTenantId()))
                .withCourseDescription(CourseDescription.from(CourseTitle.of(courseDefinitionJpa.getTitle()), courseDefinitionJpa.getSummary(), courseDefinitionJpa.getDescription()))
                .withDuration(CourseDuration.of(courseDefinitionJpa.getDuration().getExpectedDurationMillis(), courseDefinitionJpa.getDuration().getNumberOfClasses()))
                .withCourseStatus(CourseStatus.valueOf(courseDefinitionJpa.getStatus()))
                .withCourseDates(CourseDates.of(courseDefinitionJpa.getPublicationDate(), courseDefinitionJpa.getOpeningDate()))
                .withTeacherId(TeacherId.from(courseDefinitionJpa.getTeacherId()))
                .withCourseObjective(CourseObjective.of(courseDefinitionJpa.getObjective()))
                .withPreRequirements(Objects.nonNull(courseDefinitionJpa.getPreRequirement())? Arrays.stream(courseDefinitionJpa.getPreRequirement().split("#", -1))
                        .map(PreRequirement::from).toList() : null)
                .withVersion(courseDefinitionJpa.getVersion())
                .instance();
    }

    private CourseDefinitionJpa toJpa(CourseDefinition courseDefinition) {
        CourseDefinitionJpa courseDefinitionJpa = new CourseDefinitionJpa();
        CourseDurationJpa courseDurationJpa = new CourseDurationJpa();
        courseDurationJpa.setExpectedDurationMillis(courseDefinition.getDuration().expectedDurationMillis());
        courseDurationJpa.setNumberOfClasses(courseDefinition.getDuration().numberOfClasses());
        courseDefinitionJpa.setId(courseDefinition.getId().value());
        courseDefinitionJpa.setDuration(courseDurationJpa);
        courseDefinitionJpa.setDescription(courseDefinition.getCourseDescription().description());
        courseDefinitionJpa.setStatus(courseDefinition.getCourseStatus().name());
        courseDefinitionJpa.setTitle(courseDefinition.getCourseDescription().title().value());
        courseDefinitionJpa.setTeacherId(courseDefinition.getTeacherId().value());
        courseDefinitionJpa.setTenantId(courseDefinition.getTenantId().value());
        courseDefinitionJpa.setObjective(courseDefinition.getCourseObjective().objectivesSummary());
        courseDefinitionJpa.setOpeningDate(courseDefinition.getCourseDates().openingDate());
        courseDefinitionJpa.setPublicationDate(courseDefinition.getCourseDates().publicationDate());
        courseDefinitionJpa.setPreRequirement(Objects.nonNull(courseDefinition.getPreRequirements())?courseDefinition.getPreRequirements()
                .stream().map(PreRequirement::requirement)
                .collect(Collectors.joining("#")):null);
        courseDefinitionJpa.setVersion(courseDefinition.getVersion());
        courseDefinitionJpa.setSummary(courseDefinition.getCourseDescription().summary());
        return courseDefinitionJpa;
    }
}
