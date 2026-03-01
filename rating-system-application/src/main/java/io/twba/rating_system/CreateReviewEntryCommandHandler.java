package io.twba.rating_system;

import io.twba.tk.command.CommandHandler;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named
class CreateReviewEntryCommandHandler implements CommandHandler<CreateReviewEntryCommand> {

    private final ReviewEntryRepository reviewEntryRepository;
    private final ReviewEntryCreationService reviewEntryCreationService;

    @Inject
    CreateReviewEntryCommandHandler(ReviewEntryRepository reviewEntryRepository,
                                    ReviewEntryCreationService reviewEntryCreationService) {
        this.reviewEntryRepository = reviewEntryRepository;
        this.reviewEntryCreationService = reviewEntryCreationService;
    }

    @Override
    public void handle(CreateReviewEntryCommand command) {
        ReviewEntry reviewEntry = ReviewEntry.createNew(
                command.getAuthor(),
                command.getReview(),
                command.getCourseId(),
                command.getTitle(),
                reviewEntryCreationService);
        reviewEntryRepository.save(reviewEntry);
    }
}
