package io.twba.rating_system;

import io.twba.tk.command.CommandHandler;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named
class InitializeCourseReviewCommandHandler implements CommandHandler<InitializeCourseReviewCommand> {

    private final CourseReviewRepository courseReviewRepository;

    @Inject
    InitializeCourseReviewCommandHandler(CourseReviewRepository courseReviewRepository) {
        this.courseReviewRepository = courseReviewRepository;
    }

    @Override
    public void handle(InitializeCourseReviewCommand command) {
        if (!courseReviewRepository.existsCourseReviewForCourse(command.getCourseId())) {
            courseReviewRepository.save(CourseReview.initializeCourseReview(command.getCourseId(), command.getTenantId()));
        }
    }
}
