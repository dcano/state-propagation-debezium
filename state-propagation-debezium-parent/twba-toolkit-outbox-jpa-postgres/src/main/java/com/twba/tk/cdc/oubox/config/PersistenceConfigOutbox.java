package com.twba.tk.cdc.oubox.config;

import com.twba.tk.cdc.Outbox;
import com.twba.tk.cdc.OutboxProperties;
import com.twba.tk.cdc.oubox.OutboxJpa;
import com.twba.tk.cdc.oubox.jpa.OutboxMessageRepositoryJpaHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ComponentScan(basePackages = {
        "com.twba.tk"
})
@EntityScan(basePackages = {
        "com.twba.tk"
})
@EnableJpaRepositories(basePackages = {
        "com.twba.tk"
})
public class PersistenceConfigOutbox {

    public Outbox outbox(@Autowired OutboxMessageRepositoryJpaHelper helper,
                         @Autowired OutboxProperties outboxProperties) {
        return new OutboxJpa(outboxProperties, helper);
    }

}
