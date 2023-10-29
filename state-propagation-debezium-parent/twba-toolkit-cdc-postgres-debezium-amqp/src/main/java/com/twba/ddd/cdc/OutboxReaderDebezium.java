package com.twba.ddd.cdc;

import org.reactivestreams.Publisher;

public class OutboxReaderDebezium implements OutboxReader {

    @Override
    public Publisher<Outbox> scanMessages() {
        return null;
    }
}
