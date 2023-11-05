package com.twba.tk.cdc;

public interface MessagePublisher {

    boolean publish(OutboxMessage outboxMessage);

}
