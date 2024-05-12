package com.twba.tk.command.decorator;

import com.twba.tk.command.CommandHandler;
import com.twba.tk.command.DomainCommand;
import com.twba.tk.core.TwbaTransactionManager;

public class TransactionalCommandHandlerDecorator implements CommandHandler<DomainCommand> {

    @SuppressWarnings("rawtypes")
    private final CommandHandler commandHandler;
    private final TwbaTransactionManager transactionManager;

    public TransactionalCommandHandlerDecorator(CommandHandler<? extends DomainCommand> commandHandler,
                                                TwbaTransactionManager transactionManager) {
        this.commandHandler = commandHandler;
        this.transactionManager = transactionManager;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void handle(DomainCommand command) {
        try {
            transactionManager.begin();
            commandHandler.handle(command);
            transactionManager.commit();
        }
        catch(Exception e) {
            transactionManager.rollback();
            throw e;
        }

    }
}
