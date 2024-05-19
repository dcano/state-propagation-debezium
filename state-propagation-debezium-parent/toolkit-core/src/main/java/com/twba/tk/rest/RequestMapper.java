package com.twba.tk.rest;

import com.twba.tk.command.DomainCommand;

public interface RequestMapper<REQUEST> {

    boolean maps(Class<?> requestClass);
    DomainCommand toCommand(REQUEST request);

}
