package io.twba.course_management;

import io.twba.tk.command.DefaultDomainCommand;
import lombok.Getter;

@Getter
public class UpdateCourseDescriptionCommand extends DefaultDomainCommand {

    private final CourseId courseId;
    private final CourseDescription courseDescription;

    public UpdateCourseDescriptionCommand(String courseId, String title, String summary, String description) {
        this.courseId = new CourseId(courseId);
        this.courseDescription = new CourseDescription(new CourseTitle(title), summary, description);
    }

}
