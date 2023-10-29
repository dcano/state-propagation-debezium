package com.twba.ddd.cdc;

public record OutboxMessage(String uuid,
                            String metadata,
                            String payload,
                            String type,
                            long sequence,
                            String partitionKey,
                            int partition) {
}
