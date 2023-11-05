package com.twba.tk.cdc.outbox;

import com.twba.tk.cdc.OutboxProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestAppConfig {

    @Bean
    public OutboxProperties outboxProperties() {
        OutboxProperties outboxProperties = new OutboxProperties();
        outboxProperties.setNumPartitions(2);
        return outboxProperties;
    }

}
