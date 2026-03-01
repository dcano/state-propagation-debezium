package io.twba.rating_system.api.mapper;

import io.twba.tk.command.DomainCommand;
import io.twba.tk.rest.RequestMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@SuppressWarnings("unchecked")
@Component
public class RatingSystemRequestMappers {

    private final List<RequestMapper> mappers;

    @Autowired
    public RatingSystemRequestMappers(List<RequestMapper> mappers) {
        this.mappers = mappers;
    }

    public DomainCommand mapRequestToCommand(final Object request) {
        return mappers.stream()
                .filter(m -> m.maps(request.getClass()))
                .findFirst()
                .map(mapper -> mapper.toCommand(request))
                .orElseThrow();
    }
}
