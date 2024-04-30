package com.twba.tk.cdc;

import io.cloudevents.CloudEvent;

public class MessagePublisherRabbitMq implements MessagePublisher {

    private final RabbitMqCdcPublisherProperties publisherProperties;

    public MessagePublisherRabbitMq(MessageRelayProps messageRelayProps) {
        this.publisherProperties = new RabbitMqCdcPublisherProperties("__MR__" + messageRelayProps.getServiceName());
    }

    @Override
    public boolean publish(CloudEvent dispatchedMessage) {
        //TODO publish message
        return true;
    }

}
