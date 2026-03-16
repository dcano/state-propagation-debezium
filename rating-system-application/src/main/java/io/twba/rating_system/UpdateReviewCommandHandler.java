package io.twba.rating_system;

import io.twba.tk.command.CommandHandler;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named
class UpdateReviewCommandHandler implements CommandHandler<UpdateReviewCommand> {

    private final ReviewEntryRepository reviewEntryRepository;

    @Inject
    UpdateReviewCommandHandler(ReviewEntryRepository reviewEntryRepository) {
        this.reviewEntryRepository = reviewEntryRepository;
    }

    @Override
    public void handle(UpdateReviewCommand command) {
        ReviewEntry reviewEntry = reviewEntryRepository
                .retrieveReviewEntryFor(command.getAuthor(), command.getCourseId())
                .orElseThrow(() -> new ReviewEntryNotFoundForCourseAndUser(command.getCourseId(), command.getAuthor()));
        reviewEntry.updateReview(command.getReview());
        reviewEntryRepository.save(reviewEntry);
    }
}
