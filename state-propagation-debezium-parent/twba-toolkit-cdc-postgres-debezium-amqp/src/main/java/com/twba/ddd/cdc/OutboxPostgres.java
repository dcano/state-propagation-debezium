package com.twba.ddd.cdc;

import com.twba.ddd.cdc.jpa.OutboxMessageEntity;
import com.twba.ddd.cdc.jpa.OutboxMessageRepositoryJpaHelper;
import org.apache.commons.codec.digest.MurmurHash3;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OutboxPostgres implements Outbox {

    private final OutboxProperties outboxProperties;
    private final OutboxMessageRepositoryJpaHelper helper;

    @Autowired
    public OutboxPostgres(OutboxProperties outboxProperties, OutboxMessageRepositoryJpaHelper helper) {
        this.outboxProperties = outboxProperties;
        this.helper = helper;
    }

    @Override
    public void appendMessage(OutboxMessage outboxMessage) {
        helper.save(toJpa(outboxMessage));
    }

    @Override
    public int partitionFor(String partitionKey) {
        return MurmurHash3.hash32x86(partitionKey.getBytes()) % outboxProperties.getNumPartitions();
    }

    private static OutboxMessageEntity toJpa(OutboxMessage outboxMessage) {
        OutboxMessageEntity outboxMessageEntity = new OutboxMessageEntity();
        outboxMessageEntity.setMetadata(outboxMessage.metadata());
        outboxMessageEntity.setUuid(outboxMessage.uuid());
        outboxMessageEntity.setType(outboxMessage.type());
        outboxMessageEntity.setPartitionKey(outboxMessage.partitionKey());
        outboxMessageEntity.setPayload(outboxMessage.payload());
        outboxMessageEntity.setPartition(outboxMessage.partition());
        return outboxMessageEntity;
    }
}
