package io.twba.rating_system;

import io.twba.tk.command.DefaultDomainCommand;
import lombok.Getter;

@Getter
public class CreateReviewEntryCommand extends DefaultDomainCommand {

    private final EntryAuthor author;
    private final Review review;
    private final CourseId courseId;
    private final Title title;

    public CreateReviewEntryCommand(String userName, Stars stars, String comment, String courseId, String title) {
        this.author = EntryAuthor.of(userName);
        this.review = new Review(stars, comment);
        this.courseId = new CourseId(courseId);
        this.title = new Title(title);
    }
}
