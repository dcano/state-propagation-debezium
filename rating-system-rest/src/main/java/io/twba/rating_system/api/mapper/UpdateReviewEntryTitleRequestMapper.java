package io.twba.rating_system.api.mapper;

import io.twba.rating_system.UpdateReviewEntryTitleCommand;
import io.twba.rating_system.api.UpdateReviewEntryTitleRequest;
import io.twba.tk.command.DomainCommand;
import io.twba.tk.rest.RequestMapper;
import org.springframework.stereotype.Component;

@Component
public class UpdateReviewEntryTitleRequestMapper implements RequestMapper<UpdateReviewEntryTitleRequest> {

    @Override
    public boolean maps(Class<?> requestClass) {
        return UpdateReviewEntryTitleRequest.class.isAssignableFrom(requestClass);
    }

    @Override
    public DomainCommand toCommand(UpdateReviewEntryTitleRequest request) {
        return new UpdateReviewEntryTitleCommand(
                request.getUserName(),
                request.getCourseId(),
                request.getTitle());
    }
}
