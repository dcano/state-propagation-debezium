package io.twba.rating_system.api.mapper;

import io.twba.rating_system.CreateReviewEntryCommand;
import io.twba.rating_system.Stars;
import io.twba.rating_system.api.CreateReviewEntryRequest;
import io.twba.tk.command.DomainCommand;
import io.twba.tk.rest.RequestMapper;
import org.springframework.stereotype.Component;

@Component
public class CreateReviewEntryRequestMapper implements RequestMapper<CreateReviewEntryRequest> {

    static final String TENANT_ID = "LOCAL_TENANT"; //TODO get from security token

    @Override
    public boolean maps(Class<?> requestClass) {
        return CreateReviewEntryRequest.class.isAssignableFrom(requestClass);
    }

    @Override
    public DomainCommand toCommand(CreateReviewEntryRequest request) {
        return new CreateReviewEntryCommand(
                request.getUserName(),
                Stars.valueOf(request.getStars()),
                request.getComment(),
                request.getCourseId(),
                request.getTitle());
    }
}
