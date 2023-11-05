package com.twba.tk.cdc;

public record OutboxMessage(String uuid,
                            String metadata,
                            String payload,
                            String type,
                            long epoch,
                            String partitionKey) {
}
