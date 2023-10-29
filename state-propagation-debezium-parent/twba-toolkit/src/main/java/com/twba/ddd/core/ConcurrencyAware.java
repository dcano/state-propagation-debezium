package com.twba.ddd.core;

public interface ConcurrencyAware {

    Long getVersion();
    boolean isStaleWith(ConcurrencyAware existingEntity);


}
