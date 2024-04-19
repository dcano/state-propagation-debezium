package com.twba.tk.command.decorator;

import com.twba.tk.command.CommandHandler;
import com.twba.tk.command.DomainCommand;
import com.twba.tk.core.DomainEventAppender;
import jakarta.transaction.Transactional;

public class TransactionalCommandHandlerDecorator implements CommandHandler<DomainCommand> {

    @SuppressWarnings("rawtypes")
    private final CommandHandler commandHandler;
    private final DomainEventAppender domainEventAppender;

    public TransactionalCommandHandlerDecorator(CommandHandler<? extends DomainCommand> commandHandler,
            DomainEventAppender domainEventAppender) {
        this.commandHandler = commandHandler;
        this.domainEventAppender = domainEventAppender;
    }

    @Transactional
    @SuppressWarnings("unchecked")
    @Override
    public void handle(DomainCommand command) {
        commandHandler.handle(command);
        domainEventAppender.publishToOutbox();
    }
}
