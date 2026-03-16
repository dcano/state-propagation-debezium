package io.twba.rating_system;

import io.twba.tk.command.DefaultDomainCommand;
import io.twba.tk.core.TenantId;
import lombok.Getter;

@Getter
public class InitializeCourseReviewCommand extends DefaultDomainCommand {

    private final CourseId courseId;
    private final TenantId tenantId;

    public InitializeCourseReviewCommand(String courseId, String tenantId) {
        this.courseId = new CourseId(courseId);
        this.tenantId = new TenantId(tenantId);
    }
}
