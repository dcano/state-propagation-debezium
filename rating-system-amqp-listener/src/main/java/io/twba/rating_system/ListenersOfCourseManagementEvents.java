package io.twba.rating_system;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
class ListenersOfCourseManagementEvents {

    private final static Logger LOGGER = LoggerFactory.getLogger(ListenersOfCourseManagementEvents.class);

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "ratingSystemInboundQueue", durable = "true"),
            exchange = @Exchange(value = "__MR__course-management", ignoreDeclarationExceptions = "true", type = "topic"),
            key = "io.twba.course_management.coursedefinitioncreatedevent")
    )
    public void processOrder(String courseDefinitionJson) {
        LOGGER.info("Message received:::::{}", courseDefinitionJson);
    }

}
