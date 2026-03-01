package io.twba.rating_system.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import io.twba.rating_system.ReviewEntryEventsModule;
import io.twba.tk.cdc.Outbox;
import io.twba.tk.cdc.OutboxMessage;
import io.twba.tk.command.CommandBus;
import io.twba.tk.command.CommandBusInProcess;
import io.twba.tk.command.CommandHandler;
import io.twba.tk.command.DomainCommand;
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
import java.util.List;

@EnableConfigurationProperties
@EnableRabbit
@ComponentScan(basePackages = {
        "io.twba.tk.aspects",
        "io.twba.tk.autoconfigure"
})
@Configuration
public class InfraConfig {

    @Bean
    public TwbaTransactionManager twbaTransactionManager(@Autowired PlatformTransactionManager platformTransactionManager) {
        return new TwbaTransactionManagerSpring(platformTransactionManager);
    }

    @Bean
    public CommandBus commandBus(@Autowired List<CommandHandler<? extends DomainCommand>> handlers,
                                 @Autowired TwbaTransactionManager transactionManager) {
        return new CommandBusInProcess(handlers, transactionManager);
    }

    @ConfigurationProperties(prefix = "twba.application")
    @Bean
    public ApplicationProperties applicationProperties() {
        return new ApplicationProperties();
    }


    @ConfigurationProperties(prefix = "twba.external-services")
    @Bean
    public List<ExternalService> externalServices() {
        return new ArrayList<>();
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper()
                .registerModule(new ParameterNamesModule())
                .registerModule(new Jdk8Module())
                .registerModule(new JavaTimeModule())
                .registerModule(new ReviewEntryEventsModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NON_PRIVATE);
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        return mapper;
    }

}
