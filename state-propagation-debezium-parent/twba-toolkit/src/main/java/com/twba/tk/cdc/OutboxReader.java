package com.twba.tk.cdc;


import org.reactivestreams.Publisher;

public interface OutboxReader {

    Publisher<Outbox> scanMessages();

}
