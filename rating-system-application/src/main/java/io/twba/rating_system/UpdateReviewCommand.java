package io.twba.rating_system;

import io.twba.tk.command.DefaultDomainCommand;
import lombok.Getter;

@Getter
public class UpdateReviewCommand extends DefaultDomainCommand {

    private final EntryAuthor author;
    private final CourseId courseId;
    private final Review review;

    public UpdateReviewCommand(String userName, String courseId, Stars stars, String comment) {
        this.author = EntryAuthor.of(userName);
        this.courseId = new CourseId(courseId);
        this.review = new Review(stars, comment);
    }
}
