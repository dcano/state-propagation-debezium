package io.twba.rating_system;

record ReviewId(String id) {

    static ReviewId of(String id) {
        return new ReviewId(id);
    }
}
