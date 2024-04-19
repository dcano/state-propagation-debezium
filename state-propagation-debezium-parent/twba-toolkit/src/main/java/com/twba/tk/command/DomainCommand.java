package com.twba.tk.command;

import java.time.Instant;

public interface  DomainCommand {
    String commandUid();
    Instant occurredOn();
    String commandName();
}
