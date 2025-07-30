package io.twba.rating_system;

public record EntryAuthor(String userName) {

    static EntryAuthor of(String userName) {
        return new EntryAuthor(userName);
    }

}
