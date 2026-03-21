package io.twba.course_management;

import io.twba.tk.command.CommandHandler;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named
public class UpdateCourseDescriptionCommandHandler implements CommandHandler<UpdateCourseDescriptionCommand> {

    private final CourseDefinitionRepository courseDefinitionRepository;

    @Inject
    public UpdateCourseDescriptionCommandHandler(CourseDefinitionRepository courseDefinitionRepository) {
        this.courseDefinitionRepository = courseDefinitionRepository;
    }

    @Override
    public void handle(UpdateCourseDescriptionCommand command) {

    }
}
