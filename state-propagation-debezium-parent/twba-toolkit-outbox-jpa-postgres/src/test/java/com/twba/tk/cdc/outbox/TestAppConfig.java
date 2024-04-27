package com.twba.tk.cdc.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twba.tk.aspects.DomainEventAppenderConcern;
import com.twba.tk.cdc.OutboxProperties;
import com.twba.tk.core.DomainEventAppender;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestAppConfig {

    @Bean
    public OutboxProperties outboxProperties() {
        OutboxProperties outboxProperties = new OutboxProperties();
        outboxProperties.setNumPartitions(2);
        return outboxProperties;
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

}
