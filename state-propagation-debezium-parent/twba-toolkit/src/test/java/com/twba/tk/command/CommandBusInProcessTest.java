package com.twba.tk.command;

import com.twba.tk.core.DomainEventAppender;
import lombok.Getter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
        MyCommand command = new MyCommand("testProp");
        when(commandHandler.handles()).thenReturn(command.commandName());
        CommandBusInProcess commandBusInProcess = new CommandBusInProcess(Collections.singletonList(commandHandler), domainEventAppender);
        commandBusInProcess.push(command);
        verify(commandHandler).handle(command);
        verify(domainEventAppender).publishToOutbox();
    }


    @Getter
    public static class MyCommand extends DefaultDomainCommand {

        private final String propertyTest;

        public MyCommand(String propertyTest) {
            this.propertyTest = propertyTest;
        }
    }

}
