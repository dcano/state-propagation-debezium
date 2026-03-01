package io.twba.rating_system.api;

import lombok.Data;

@Data
public class UpdateReviewEntryTitleRequest {

    private String userName;
    private String courseId;
    private String title;

}
