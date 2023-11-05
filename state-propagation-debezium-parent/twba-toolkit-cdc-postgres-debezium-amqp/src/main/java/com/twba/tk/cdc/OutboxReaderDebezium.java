package com.twba.tk.cdc;

import org.reactivestreams.Publisher;

public class OutboxReaderDebezium implements OutboxReader {

    @Override
    public Publisher<Outbox> scanMessages() {
        return null;
    }
}
