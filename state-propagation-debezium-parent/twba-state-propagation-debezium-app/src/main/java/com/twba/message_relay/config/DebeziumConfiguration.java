package com.twba.message_relay.config;

import com.twba.tk.cdc.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DebeziumConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "debezium")
    public DebeziumProperties debeziumProperties() {
        return new DebeziumProperties();
    }

    @Bean
    public MessagePublisher messagePublisher(@Autowired MessageRelayProps messageRelayProps, @Autowired RabbitTemplate rabbitTemplate) {
        return new MessagePublisherRabbitMq(messageRelayProps, rabbitTemplate);
    }

    @Bean
    public MessageRelay debeziumMessageRelay(@Autowired DebeziumProperties debeziumProperties,
                                             @Autowired MessagePublisher messagePublisher) {
        return new DebeziumMessageRelay(messagePublisher, debeziumProperties);
    }

    @Bean
    @ConfigurationProperties(prefix = "cdc")
    public MessageRelayProps messageRelayProps() {
        return new MessageRelayProps();
    }
}
