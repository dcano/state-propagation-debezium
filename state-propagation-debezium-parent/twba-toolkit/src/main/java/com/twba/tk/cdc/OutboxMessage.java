package com.twba.tk.cdc;

public record OutboxMessage(String uuid,
                            String header,
                            String payload,
                            String type,
                            long epoch,
                            String partitionKey) {
}
