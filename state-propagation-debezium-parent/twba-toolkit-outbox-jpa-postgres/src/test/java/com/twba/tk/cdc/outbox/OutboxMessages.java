package com.twba.tk.cdc.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twba.tk.cdc.OutboxMessage;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class OutboxMessages {

    static OutboxMessage randomOutboxMessage() throws JsonProcessingException {
        return new OutboxMessage(UUID.randomUUID().toString(), "", fakePayload(), "typeA", Instant.now().toEpochMilli(), "partitionA");
    }

    static String fakePayload() throws JsonProcessingException {
        Map<String, String> map = new HashMap<>();
        map.put("key", "value");

        ObjectMapper mapper = new ObjectMapper();
        return mapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(map);
    }

}
