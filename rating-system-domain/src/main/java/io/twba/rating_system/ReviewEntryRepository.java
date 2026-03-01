package io.twba.rating_system;

import java.util.Optional;

interface ReviewEntryRepository {

    void save(ReviewEntry reviewEntry);
    Optional<ReviewEntry> retrieveReviewEntryFor(EntryAuthor author, CourseId courseId);
}
