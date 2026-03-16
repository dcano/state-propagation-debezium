package io.twba.rating_system;

import java.util.Arrays;
import java.util.List;

record RatingSummary(List<StarRating> stars) {

    static RatingSummary initialize() {
        return new RatingSummary(Arrays.asList(
                StarRating.of(Stars.NONE, 0),
                StarRating.of(Stars.ONE, 0),
                StarRating.of(Stars.TWO, 0),
                StarRating.of(Stars.THREE, 0),
                StarRating.of(Stars.FOUR, 0),
                StarRating.of(Stars.FIVE, 0)
        ));
    }

    void rateZero() {
        StarRating current = stars.getFirst();
        stars.set(0, StarRating.of(Stars.NONE, current.numberOfVotes()+1));
    }

    void rateOne() {
        StarRating current = stars.get(1);
        stars.set(1, StarRating.of(Stars.ONE, current.numberOfVotes()+1));
    }

    void rateTwo() {
        StarRating current = stars.get(1);
        stars.set(2, StarRating.of(Stars.TWO, current.numberOfVotes()+1));
    }

    void rateThree() {
        StarRating current = stars.get(1);
        stars.set(3, StarRating.of(Stars.THREE, current.numberOfVotes()+1));
    }

    void rateFour() {
        StarRating current = stars.get(1);
        stars.set(4, StarRating.of(Stars.FOUR, current.numberOfVotes()+1));
    }

    void rateFive() {
        StarRating current = stars.get(4);
        stars.set(5, StarRating.of(Stars.FIVE, current.numberOfVotes()+1));
    }

}
