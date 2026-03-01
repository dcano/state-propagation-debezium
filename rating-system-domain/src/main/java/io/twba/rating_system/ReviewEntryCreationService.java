package io.twba.rating_system;

import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named
class ReviewEntryCreationService {

    private final ReviewEntryRepository reviewEntryRepository;

    @Inject
    ReviewEntryCreationService(ReviewEntryRepository reviewEntryRepository) {
        this.reviewEntryRepository = reviewEntryRepository;
    }

    boolean existsForAuthorAndCourse(EntryAuthor entryAuthor, CourseId courseId) {
        return reviewEntryRepository.retrieveReviewEntryFor(entryAuthor, courseId).isPresent();
    }

}
