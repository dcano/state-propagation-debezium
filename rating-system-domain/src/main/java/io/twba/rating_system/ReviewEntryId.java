package io.twba.rating_system;

public record ReviewEntryId(String id) {

    static ReviewEntryId of(String id) {
        return new ReviewEntryId(id);
    }
}
