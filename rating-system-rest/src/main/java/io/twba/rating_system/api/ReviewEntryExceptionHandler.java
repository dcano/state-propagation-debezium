package io.twba.rating_system.api;

import io.twba.rating_system.ReviewEntryAlreadyExistsForCourseAndUser;
import io.twba.rating_system.ReviewEntryNotFoundForCourseAndUser;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ReviewEntryExceptionHandler {

    @ExceptionHandler(ReviewEntryNotFoundForCourseAndUser.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void handleReviewEntryNotFound() {
    }

    @ExceptionHandler(ReviewEntryAlreadyExistsForCourseAndUser.class)
    @ResponseStatus(HttpStatus.NOT_MODIFIED)
    public void handleReviewEntryAlreadyExists() {
    }

}
