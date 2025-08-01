package io.twba.rating_system;

record AverageRating(float averageNumberOfStars, float averageRate, int totalNumberOfReviews) {

    static AverageRating initialize() {
        return new AverageRating(0, 0, 0);
    }

}
