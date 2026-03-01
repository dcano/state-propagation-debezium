package io.twba.rating_system.api.mapper;

import io.twba.rating_system.Stars;
import io.twba.rating_system.UpdateReviewCommand;
import io.twba.rating_system.api.UpdateReviewRequest;
import io.twba.tk.command.DomainCommand;
import io.twba.tk.rest.RequestMapper;
import org.springframework.stereotype.Component;

@Component
public class UpdateReviewRequestMapper implements RequestMapper<UpdateReviewRequest> {

    @Override
    public boolean maps(Class<?> requestClass) {
        return UpdateReviewRequest.class.isAssignableFrom(requestClass);
    }

    @Override
    public DomainCommand toCommand(UpdateReviewRequest request) {
        return new UpdateReviewCommand(
                request.getUserName(),
                request.getCourseId(),
                Stars.valueOf(request.getStars()),
                request.getComment());
    }
}
