package com.twba.tk.command;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public interface CommandHandler<T extends DomainCommand> {

    void handle(T command);

    @SuppressWarnings("unchecked")
    default Class<T> handles(){
        Class<?> clazz = getClass();
        ParameterizedType parameterizedType = (ParameterizedType) clazz.getGenericInterfaces()[0];
        Type[] typeArguments = parameterizedType.getActualTypeArguments();
        return (Class<T>) typeArguments[0];
    }

}
