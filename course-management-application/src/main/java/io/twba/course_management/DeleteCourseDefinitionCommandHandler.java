package io.twba.course_management;

import io.twba.tk.command.CommandHandler;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named
public class DeleteCourseDefinitionCommandHandler implements CommandHandler<DeleteCourseDefinitionCommand> {

    private final CourseDefinitionRepository courseDefinitionRepository;

    @Inject
    public DeleteCourseDefinitionCommandHandler(CourseDefinitionRepository courseDefinitionRepository) {
        this.courseDefinitionRepository = courseDefinitionRepository;
    }

    @Override
    public void handle(DeleteCourseDefinitionCommand command) {
        CourseDefinition courseDefinition = courseDefinitionRepository.findById(command.getCourseId(), command.getTenantId())
                .orElseThrow(() -> new CourseDefinitionNotFoundException(command.getCourseId()));
        courseDefinition.delete();
        courseDefinitionRepository.delete(courseDefinition);
    }
}
