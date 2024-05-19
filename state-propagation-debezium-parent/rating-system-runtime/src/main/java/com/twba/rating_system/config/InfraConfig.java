package com.twba.rating_system.config;

import com.twba.tk.cdc.AmqpProperties;
import com.twba.tk.command.CommandBus;
import com.twba.tk.command.CommandBusInProcess;
import com.twba.tk.core.ApplicationProperties;
import com.twba.tk.core.TwbaTransactionManager;
import com.twba.tk.core.tx.TwbaTransactionManagerSpring;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Collections;

@EnableConfigurationProperties
@EnableRabbit
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
