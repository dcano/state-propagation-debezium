package io.twba.rating_system.api;

import io.twba.rating_system.api.mapper.RatingSystemRequestMappers;
import io.twba.tk.command.CommandBus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("twba")
public class RatingSystemController {

    private final RatingSystemRequestMappers requestMappers;
    private final CommandBus commandBus;

    @Autowired
    public RatingSystemController(RatingSystemRequestMappers requestMappers, CommandBus commandBus) {
        this.requestMappers = requestMappers;
        this.commandBus = commandBus;
    }

    @PostMapping(value = "/review-entry",
            path = "/review-entry",
            consumes = {"application/json"}
    )
    @ResponseStatus(code = HttpStatus.CREATED)
    public void createReviewEntry(@RequestBody CreateReviewEntryRequest request) {
        var command = requestMappers.mapRequestToCommand(request);
        commandBus.push(command);
    }

    @PatchMapping(value = "/review-entry/review",
            path = "/review-entry/review",
            consumes = {"application/json"}
    )
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void updateReview(@RequestBody UpdateReviewRequest request) {
        var command = requestMappers.mapRequestToCommand(request);
        commandBus.push(command);
    }

    @PatchMapping(value = "/review-entry/title",
            path = "/review-entry/title",
            consumes = {"application/json"}
    )
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void updateReviewEntryTitle(@RequestBody UpdateReviewEntryTitleRequest request) {
        var command = requestMappers.mapRequestToCommand(request);
        commandBus.push(command);
    }

}
