package com.twba.ddd.cdc;

import org.reactivestreams.Publisher;

public interface Outbox {

    void appendMessage(OutboxMessage outboxMessage);
    int partitionFor(String partitionKey);
}
