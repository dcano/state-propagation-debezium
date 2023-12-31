package com.twba.tk.cdc.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.twba.tk.cdc.OutboxMessage;
import com.twba.tk.cdc.oubox.OutboxJpa;
import com.twba.tk.cdc.oubox.config.PersistenceConfig;
import com.twba.tk.cdc.oubox.jpa.OutboxMessageEntity;
import com.twba.tk.cdc.oubox.jpa.OutboxMessageRepositoryJpaHelper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.NoSuchElementException;

import static com.twba.tk.cdc.outbox.OutboxMessages.randomOutboxMessage;
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@ContextConfiguration(classes = {PersistenceConfig.class, TestAppConfig.class})
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class OutboxJpaIT {

    @Autowired
    public OutboxJpa outboxJpa;

    @Autowired
    public OutboxMessageRepositoryJpaHelper helper;

    @Container
    public static PostgreSQLContainer container = new PostgreSQLContainer("postgres:latest")
            .withDatabaseName("outbox_db")
            .withUsername("sa")
            .withPassword("sa");


    @DynamicPropertySource
    public static void overrideProperties(DynamicPropertyRegistry registry){
        registry.add("spring.datasource.url", container::getJdbcUrl);
        registry.add("spring.datasource.username", container::getUsername);
        registry.add("spring.datasource.password", container::getPassword);
        registry.add("spring.datasource.driver-class-name", container::getDriverClassName);
    }

    @Test
    public void shouldPersistsMessageInOutboxDatabase() throws JsonProcessingException {
        OutboxMessage expectedMessage = randomOutboxMessage();
        outboxJpa.appendMessage(expectedMessage);
        OutboxMessageEntity actualMessage = helper.findById(expectedMessage.uuid()).orElseThrow(NoSuchElementException::new);
        assertAll("Store outbox message in postgres database",
                () -> assertEquals(expectedMessage.epoch(), actualMessage.getEpoch()),
                () -> assertEquals(expectedMessage.uuid(), actualMessage.getUuid()),
                () -> assertNotNull(expectedMessage.payload(), actualMessage.getPayload()));
    }

}
