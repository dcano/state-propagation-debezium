package com.twba.tk.command.decorator;

import com.twba.tk.command.CommandHandler;
import com.twba.tk.command.DomainCommand;
import jakarta.transaction.Transactional;

public class TransactionalCommandHandlerDecorator implements CommandHandler<DomainCommand> {

    @SuppressWarnings("rawtypes")
    private final CommandHandler commandHandler;

    public TransactionalCommandHandlerDecorator(CommandHandler<? extends DomainCommand> commandHandler) {
        this.commandHandler = commandHandler;
    }

    @Transactional
    @SuppressWarnings("unchecked")
    @Override
    public void handle(DomainCommand command) {
        commandHandler.handle(command);
    }
}
