package com.twba.message_relay.config;

import com.twba.tk.cdc.MessageRelayProps;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile(value={"dev"})
public class RabbitMqTopologyConfiguration {

    @Bean
    TopicExchange exchange(@Autowired MessageRelayProps messageRelayProps) {
        return new TopicExchange("__MR__" + messageRelayProps.getServiceName());
    }

}
