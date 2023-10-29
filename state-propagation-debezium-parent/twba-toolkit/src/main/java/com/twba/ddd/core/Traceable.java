package com.twba.ddd.core;

public interface Traceable {

    void setCorrelationId(CorrelationId correlationId);
    CorrelationId correlationId();

}
