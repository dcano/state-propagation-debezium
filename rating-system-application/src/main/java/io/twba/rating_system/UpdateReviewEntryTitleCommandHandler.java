package io.twba.rating_system;

import io.twba.tk.command.CommandHandler;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named
class UpdateReviewEntryTitleCommandHandler implements CommandHandler<UpdateReviewEntryTitleCommand> {

    private final ReviewEntryRepository reviewEntryRepository;

    @Inject
    UpdateReviewEntryTitleCommandHandler(ReviewEntryRepository reviewEntryRepository) {
        this.reviewEntryRepository = reviewEntryRepository;
    }

    @Override
    public void handle(UpdateReviewEntryTitleCommand command) {
        ReviewEntry reviewEntry = reviewEntryRepository
                .retrieveReviewEntryFor(command.getAuthor(), command.getCourseId())
                .orElseThrow(() -> new ReviewEntryNotFoundForCourseAndUser(command.getCourseId(), command.getAuthor()));
        reviewEntry.updateTitle(command.getTitle());
        reviewEntryRepository.save(reviewEntry);
    }
}
