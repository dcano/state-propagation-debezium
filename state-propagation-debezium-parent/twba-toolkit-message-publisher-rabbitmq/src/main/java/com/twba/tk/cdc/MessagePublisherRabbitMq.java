package com.twba.tk.cdc;

import io.cloudevents.CloudEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.Objects;

public class MessagePublisherRabbitMq implements MessagePublisher {

    private final RabbitMqCdcPublisherProperties publisherProperties;
    private final RabbitTemplate rabbitTemplate;

    public MessagePublisherRabbitMq(MessageRelayProps messageRelayProps, RabbitTemplate rabbitTemplate) {
        this.publisherProperties = new RabbitMqCdcPublisherProperties("__MR__" + messageRelayProps.getServiceName());
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public boolean publish(CloudEvent dispatchedMessage) {
        //TODO proper handling of ACK
        //TODO retries and DLQ
        rabbitTemplate.convertAndSend(publisherProperties.getExchange(), dispatchedMessage.getType(), Objects.nonNull(dispatchedMessage.getData())?dispatchedMessage.getData().toBytes():new byte[0]);
        return true;
    }

}
