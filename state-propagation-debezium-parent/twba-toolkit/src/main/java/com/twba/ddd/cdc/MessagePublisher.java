package com.twba.ddd.cdc;

public interface MessagePublisher {

    boolean publish(OutboxMessage outboxMessage);

}
