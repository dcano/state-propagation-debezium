package com.twba.tk.cdc;

import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

public class MessageRelayReactive implements MessageRelay {

    private final OutboxReader outboxReader;
    private final MessagePublisher messagePublisher;

    public MessageRelayReactive(OutboxReader outboxReader,
                                MessagePublisher messagePublisher) {
        this.outboxReader = outboxReader;
        this.messagePublisher = messagePublisher;
    }

    @Override
    public void start() {
        Flux.from(outboxReader.scanMessages()).subscribeOn(Schedulers.newSingle("Message-Relay")).subscribe();
    }
}
