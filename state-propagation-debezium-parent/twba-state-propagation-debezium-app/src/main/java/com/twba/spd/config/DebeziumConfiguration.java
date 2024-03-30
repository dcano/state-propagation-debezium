package com.twba.spd.config;

import com.twba.tk.cdc.DebeziumProperties;
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

}
