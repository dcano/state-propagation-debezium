package com.twba.tk.command;

import com.twba.tk.core.DomainEventAppender;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Collections;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CommandBusInProcessTest {

    @Mock
    public DomainEventAppender domainEventAppender;

    @Mock
    public CommandHandler<MyCommand> commandHandler;

    @Test
    public void shouldExecuteCommands() {
        when(commandHandler.handles()).thenReturn(MyCommand.class);
        MyCommand command = new MyCommand();
        CommandBusInProcess commandBusInProcess = new CommandBusInProcess(Collections.singletonList(commandHandler), domainEventAppender);
        commandBusInProcess.push(command);
        verify(commandHandler).handle(command);
        verify(domainEventAppender).publishToOutbox();
    }


    public static class MyCommand implements DomainCommand {

        @Override
        public String commandUid() {
            return "testCommandId";
        }

        @Override
        public Instant occurredOn() {
            return Instant.MAX;
        }

        @Override
        public String commandName() {
            return "testCommandName";
        }
    }

}
