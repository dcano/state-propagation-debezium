package com.twba.tk.core;

public interface TwbaTransactionManager {

    void begin();
    void commit();
    void rollback();

}
