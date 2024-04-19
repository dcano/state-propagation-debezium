package com.twba.tk.command.decorator;

import com.twba.tk.command.CommandHandler;
import com.twba.tk.command.DomainCommand;
import com.twba.tk.core.DomainEventAppender;

public class PublishBufferedEventsCommandHandlerDecorator implements CommandHandler<DomainCommand> {

    @SuppressWarnings("rawtypes")
    private final CommandHandler commandHandler;
    private final DomainEventAppender domainEventAppender;

    public PublishBufferedEventsCommandHandlerDecorator(CommandHandler<? extends DomainCommand> commandHandler,
                                                        DomainEventAppender domainEventAppender) {
        this.commandHandler = commandHandler;
        this.domainEventAppender = domainEventAppender;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void handle(DomainCommand command) {
        commandHandler.handle(command);
        domainEventAppender.publishToOutbox();
    }

}
