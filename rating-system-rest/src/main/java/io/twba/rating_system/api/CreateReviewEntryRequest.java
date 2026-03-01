package io.twba.rating_system.api;

import lombok.Data;

@Data
public class CreateReviewEntryRequest {

    private String userName;
    private String courseId;
    private String stars;
    private String comment;
    private String title;

}
