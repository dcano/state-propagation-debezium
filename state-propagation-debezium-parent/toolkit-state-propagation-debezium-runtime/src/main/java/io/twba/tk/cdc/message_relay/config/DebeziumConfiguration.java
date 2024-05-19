package io.twba.tk.cdc.message_relay.config;

import io.twba.tk.cdc.*;
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
    public MessagePublisher messagePublisher(@Autowired RabbitTemplate rabbitTemplate) {
        return new MessagePublisherRabbitMq(rabbitTemplate);
    }

    @Bean
    public MessageRelay debeziumMessageRelay(@Autowired DebeziumProperties debeziumProperties,
                                             @Autowired CdcRecordChangeConsumer cdcRecordChangeConsumer) {
        return new DebeziumMessageRelay(debeziumProperties, cdcRecordChangeConsumer);
    }

    @Bean
    public CdcRecordChangeConsumer cdcRecordChangeConsumer(@Autowired MessagePublisher messagePublisher) {
        return new DebeziumRecordChangeConsumerRabbitMQ(messagePublisher);
    }
}
