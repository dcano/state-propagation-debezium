package io.twba.rating_system.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.twba.tk.cdc.Outbox;
import io.twba.tk.cdc.OutboxMessage;
import io.twba.tk.command.CommandBus;
import io.twba.tk.command.CommandBusInProcess;
import io.twba.tk.core.ApplicationProperties;
import io.twba.tk.core.DomainEventAppender;
import io.twba.tk.core.ExternalService;
import io.twba.tk.core.TwbaTransactionManager;
import io.twba.tk.core.tx.TwbaTransactionManagerSpring;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@EnableConfigurationProperties
@EnableRabbit
@ComponentScan(basePackages = {
        "io.twba.tk.aspects"
})
@Configuration
public class InfraConfig {

    @Bean
    public TwbaTransactionManager twbaTransactionManager(@Autowired PlatformTransactionManager platformTransactionManager) {
        return new TwbaTransactionManagerSpring(platformTransactionManager);
    }

    @Bean
    public CommandBus commandBus(TwbaTransactionManager transactionManager) {
        return new CommandBusInProcess(Collections.emptyList(), null, transactionManager);
    }

    @ConfigurationProperties(prefix = "twba.application")
    @Bean
    public ApplicationProperties applicationProperties() {
        return new ApplicationProperties();
    }

    @Bean
    public DomainEventAppender domainEventAppender(@Autowired ObjectMapper objectMapper,
                                                   @Autowired ApplicationProperties applicationProperties) {
        return new DomainEventAppender(new Outbox() {

            private static final Logger LOGGER = LoggerFactory.getLogger(DomainEventAppender.class);

            @Override
            public void appendMessage(OutboxMessage outboxMessage) {
                LOGGER.info("Message appended to outbox {}", outboxMessage.uuid());
            }

            @Override
            public int partitionFor(String partitionKey) {
                return 0;
            }
        }, objectMapper, applicationProperties);
    }

    @ConfigurationProperties(prefix = "twba.external-services")
    @Bean
    public List<ExternalService> externalServices() {
        return new ArrayList<>();
    }

    /*
    @ConfigurationProperties(prefix = "spring.rabbitmq")
    @Bean
    public AmqpProperties amqpProperties() {
        return new AmqpProperties();
    }

    @Bean
    public CachingConnectionFactory cachingConnectionFactory(@Autowired AmqpProperties amqpProperties) {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setHost(amqpProperties.getHost());
        connectionFactory.setPort(amqpProperties.getPort());
        connectionFactory.setUsername(amqpProperties.getUsername());
        connectionFactory.setPassword(amqpProperties.getPassword());
        return connectionFactory;
    }*/

}
