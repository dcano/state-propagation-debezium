package com.twba.ddd.cdc;


import org.reactivestreams.Publisher;

public interface OutboxReader {

    Publisher<Outbox> scanMessages();

}
