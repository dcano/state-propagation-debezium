package com.twba.course_management;

import com.twba.tk.core.MultiTenantEntity;
import com.twba.tk.core.TenantId;
import lombok.Getter;

import java.util.List;

@Getter
public class CourseDefinition extends MultiTenantEntity {

    private CourseId id;
    private CourseDescription courseDescription;
    private CourseObjective courseObjective;
    private List<PreRequirement> preRequirements;
    private CourseDuration duration;
    private TeacherId teacherId;
    private CourseDates courseDates;
    private CourseStatus courseStatus;

    private CourseDefinition(TenantId tenantId, long version, CourseId id, CourseDescription courseDescription, CourseObjective courseObjective, List<PreRequirement> preRequirements, CourseDuration duration, TeacherId teacherId, CourseDates courseDates, CourseStatus courseStatus) {
        super(tenantId, version);
        this.id = id;
        this.courseDescription = courseDescription;
        this.courseObjective = courseObjective;
        this.preRequirements = preRequirements;
        this.duration = duration;
        this.teacherId = teacherId;
        this.courseDates = courseDates;
        this.courseStatus = courseStatus;
    }

    @Override
    public String aggregateId() {
        return id.value();
    }

    public static CourseDefinitionBuilder builder(TenantId tenantId) {
        return new CourseDefinitionBuilder(tenantId);
    }

    public static class CourseDefinitionBuilder {
        private CourseId id;
        private CourseDescription courseDescription;
        private CourseObjective courseObjective;
        private List<PreRequirement> preRequirements;
        private CourseDuration duration;
        private TeacherId teacherId;
        private CourseDates courseDates;
        private CourseStatus courseStatus;
        private long version;
        private TenantId tenantId;

        CourseDefinitionBuilder(TenantId tenantId) {
            this.tenantId = tenantId;
        }

        public CourseDefinitionBuilder withCourseId(CourseId id) {
            this.id = id;
            return this;
        }
        public CourseDefinitionBuilder withCourseDescription(CourseDescription courseDescription) {
            this.courseDescription = courseDescription;
            return this;
        }
        public CourseDefinitionBuilder withCourseObjective(CourseObjective courseObjective) {
            this.courseObjective = courseObjective;
            return this;
        }
        public CourseDefinitionBuilder withPreRequirements(List<PreRequirement> preRequirements) {
            this.preRequirements = preRequirements;
            return this;
        }
        public CourseDefinitionBuilder withDuration(CourseDuration duration) {
            this.duration = duration;
            return this;
        }
        public CourseDefinitionBuilder withTeacherId(TeacherId teacherId) {
            this.teacherId = teacherId;
            return this;
        }
        public CourseDefinitionBuilder withCourseDates(CourseDates courseDates) {
            this.courseDates = courseDates;
            return this;
        }
        public CourseDefinitionBuilder withCourseStatus(CourseStatus courseStatus) {
            this.courseStatus = courseStatus;
            return this;
        }
        public CourseDefinitionBuilder withVersion(long version) {
            this.version = version;
            return this;
        }
        public CourseDefinitionBuilder withTenantId(TenantId tenantId) {
            this.tenantId = tenantId;
            return this;
        }

        public CourseDefinition createNew() {
            //new instance, generate domain event
            CourseDefinition courseDefinition = new CourseDefinition(tenantId, 0L, id, courseDescription, courseObjective, preRequirements, duration, teacherId, courseDates, CourseStatus.PENDING_TO_REVIEW);
            var courseDefinitionCreatedEvent = CourseDefinitionCreatedEvent.triggeredFrom(courseDefinition);
            courseDefinition.record(courseDefinitionCreatedEvent);
            return courseDefinition;
        }

        public CourseDefinition instance() {
            //existing instance
            return new CourseDefinition(tenantId, version, id, courseDescription, courseObjective, preRequirements, duration, teacherId, courseDates, courseStatus);
        }
    }

}
