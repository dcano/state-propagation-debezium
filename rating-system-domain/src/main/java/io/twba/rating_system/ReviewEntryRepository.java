package io.twba.rating_system;

interface ReviewEntryRepository {

    void save(ReviewEntry reviewEntry);
    ReviewEntry retrieveEntry(ReviewEntryId reviewEntryId);
}
