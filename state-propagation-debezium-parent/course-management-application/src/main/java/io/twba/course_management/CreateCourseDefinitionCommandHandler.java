package io.twba.course_management;

import io.twba.tk.command.CommandHandler;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named
public class CreateCourseDefinitionCommandHandler implements CommandHandler<CreateCourseDefinitionCommand> {

    private final CourseDefinitionRepository courseDefinitionRepository;

    @Inject
    public CreateCourseDefinitionCommandHandler(CourseDefinitionRepository courseDefinitionRepository) {
        this.courseDefinitionRepository = courseDefinitionRepository;
    }

    @Override
    public void handle(CreateCourseDefinitionCommand command) {
        if(!courseDefinitionRepository.existsCourseDefinitionWith(command.getTenantId(), command.getCourseDescription().title())) {
            courseDefinitionRepository.save(CourseDefinition.builder(command.getTenantId())
                    .withCourseDates(command.getCourseDates())
                    .withCourseDescription(command.getCourseDescription())
                    .withCourseObjective(command.getCourseObjective())
                    .withDuration(command.getCourseDuration())
                    .withTeacherId(command.getTeacherId())
                    .withPreRequirements(command.getPreRequirements())
                    .withCourseId(command.getCourseId())
                    .createNew());
        }
        else {
            throw new IllegalStateException("Course definition with value " + command.getCourseDescription().title() + " already exists");
        }
    }
}
