package io.twba.course_management.rest.mapper;

import io.twba.tk.command.DomainCommand;
import io.twba.tk.rest.RequestMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@SuppressWarnings("unchecked")
@Component
public class CourseManagementRequestMappers {

    private final List<RequestMapper> mappers;

    @Autowired
    public CourseManagementRequestMappers(List<RequestMapper> mappers) {
        this.mappers = mappers;
    }

    public DomainCommand mapRequestToCommand(final Object request) {
        return mappers.stream().filter(m -> m.maps(request.getClass())).findFirst().map(requestMapper -> {
            return requestMapper.toCommand(request);
        }).orElseThrow();
    }
}
