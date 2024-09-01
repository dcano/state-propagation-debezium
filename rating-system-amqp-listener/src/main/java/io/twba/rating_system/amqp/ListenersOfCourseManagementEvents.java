package io.twba.rating_system.amqp;

import io.twba.tk.command.CommandBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ListenersOfCourseManagementEvents {

    private final static Logger log = LoggerFactory.getLogger(ListenersOfCourseManagementEvents.class);

    private final CommandBus commandBus;

    @Autowired
    public ListenersOfCourseManagementEvents(CommandBus commandBus) {
        this.commandBus = commandBus;
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "ratingSystemInboundQueue", durable = "true"),
            exchange = @Exchange(value = "__MR__course-management", ignoreDeclarationExceptions = "true", type = "topic"),
            key = "com.twba.course_management.coursedefinitioncreatedevent")
    )
    public void processOrder(String courseDefinitionJson) {
        log.info("Message received:::::{}", courseDefinitionJson);
    }

}
