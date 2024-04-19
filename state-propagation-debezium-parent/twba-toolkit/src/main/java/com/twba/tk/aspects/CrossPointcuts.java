package com.twba.tk.aspects;

import org.aspectj.lang.annotation.Pointcut;

public class CrossPointcuts {

    @Pointcut("@annotation(com.twba.tk.core.AppendEvents)")
    public void shouldAppendEvents() {}


}
