package com.twba.tk.cdc;

import io.cloudevents.CloudEvent;

public class MessagePublisherRabbitMq implements MessagePublisher {

    @Override
    public boolean publish(CloudEvent dispatchedMessage) {
        return true;
    }

}
