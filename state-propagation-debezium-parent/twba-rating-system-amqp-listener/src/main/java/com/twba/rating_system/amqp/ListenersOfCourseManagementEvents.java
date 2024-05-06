package com.twba.rating_system.amqp;

import com.twba.tk.command.CommandBus;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ListenersOfCourseManagementEvents {

    private final CommandBus commandBus;

    @Autowired
    public ListenersOfCourseManagementEvents(CommandBus commandBus) {
        this.commandBus = commandBus;
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "ratingSystemInboundQueue", durable = "true"),
            exchange = @Exchange(value = "__MR__course-management", ignoreDeclarationExceptions = "true"),
            key = "com.twba.course_management.coursedefinitioncreatedevent")
    )
    public void processOrder(String courseDefinitionJson) {
        //TODO create command for initialize rating for course
        //TODO call command bus
    }

}
