package com.twba.tk.aspects;

import com.twba.tk.core.DomainEventAppender;
import com.twba.tk.core.Entity;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.aspectj.lang.JoinPoint;

@Named
public class DomainEventAppenderConcern {

    private final DomainEventAppender domainEventAppender;

    @Inject
    public DomainEventAppenderConcern(DomainEventAppender domainEventAppender) {
        this.domainEventAppender = domainEventAppender;
    }

    public void appendEventsToBuffer(JoinPoint jp) {
        if(Entity.class.isAssignableFrom(jp.getArgs()[0].getClass())) {
            Entity entity = (Entity)jp.getArgs()[0];
            domainEventAppender.append(entity.getDomainEvents());
        }
    }


}