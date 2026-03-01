package io.twba.rating_system;

import io.twba.tk.command.DefaultDomainCommand;
import lombok.Getter;

@Getter
public class UpdateReviewEntryTitleCommand extends DefaultDomainCommand {

    private final EntryAuthor author;
    private final CourseId courseId;
    private final Title title;

    public UpdateReviewEntryTitleCommand(String userName, String courseId, String title) {
        this.author = EntryAuthor.of(userName);
        this.courseId = new CourseId(courseId);
        this.title = new Title(title);
    }
}
