package com.twba.course_management.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.twba.tk.cdc.Outbox;
import com.twba.tk.cdc.OutboxProperties;
import com.twba.tk.command.CommandBus;
import com.twba.tk.command.CommandBusInProcess;
import com.twba.tk.command.CommandHandler;
import com.twba.tk.command.DomainCommand;
import com.twba.tk.core.DomainEventAppender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@EnableConfigurationProperties
@Configuration
@ComponentScan(basePackages = {
        "com.twba.tk.cdc.oubox",
        "com.twba.tk.aspects"
})
public class InfraConfig {

    @Bean
    public CommandBus commandBus(@Autowired List<CommandHandler<? extends DomainCommand>> handlers, @Autowired DomainEventAppender domainEventAppender) {
        return new CommandBusInProcess(handlers, domainEventAppender);
    }


    @ConfigurationProperties(prefix = "outbox")
    @Bean
    public OutboxProperties outboxProperties() {
        return new OutboxProperties();
    }

    @Bean
    public DomainEventAppender domainEventAppender(@Autowired Outbox outbox, @Autowired ObjectMapper objectMapper) {
        return new DomainEventAppender(outbox, objectMapper);
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper()
                .registerModule(new ParameterNamesModule())
                .registerModule(new Jdk8Module())
                .registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NON_PRIVATE);
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        return mapper;
    }


}
