package com.twba.tk.core;

public interface Traceable {

    void setCorrelationId(CorrelationId correlationId);
    CorrelationId correlationId();

}
