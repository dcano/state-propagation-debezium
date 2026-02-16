package io.twba.course_management;

import io.twba.tk.command.DefaultDomainCommand;
import io.twba.tk.core.TenantId;
import lombok.Getter;

@Getter
public class DeleteCourseDefinitionCommand extends DefaultDomainCommand {

    private final CourseId courseId;
    private final TenantId tenantId;

    public DeleteCourseDefinitionCommand(String courseId, String tenantId) {
        this.courseId = new CourseId(courseId);
        this.tenantId = new TenantId(tenantId);
    }
}
