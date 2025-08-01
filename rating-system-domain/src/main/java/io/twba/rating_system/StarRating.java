package io.twba.rating_system;

record StarRating(Stars star, int numberOfVotes) {

    static StarRating of(Stars star, int numberOfVotes) {
        return new StarRating(star, numberOfVotes);
    }

}
