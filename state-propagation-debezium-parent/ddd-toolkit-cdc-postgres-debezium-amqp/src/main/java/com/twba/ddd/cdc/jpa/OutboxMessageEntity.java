package com.twba.ddd.cdc.jpa;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Data
@Entity
@Table(schema="outbox_schema", name = "outbox",
        indexes = {
                @Index(name = "type", columnList = "type"),
                @Index(name = "sequence", columnList = "sequence")
        })
public class OutboxMessageEntity {

    @Id
    String uuid;
    @JdbcTypeCode(SqlTypes.JSON)
    String metadata;
    @JdbcTypeCode(SqlTypes.JSON)
    String payload;
    String type;
    long sequence;
    String partitionKey;
    int partition;
}
