package com.twba.tk.command;

public interface CommandBus {

    <T extends DomainCommand> void push(T command);


}
