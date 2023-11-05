package com.twba.tk.cdc.oubox.jpa;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Data
@Entity
@Table(schema="outbox_schema", name = "outbox",
        indexes = {
                @Index(name = "type", columnList = "type"),
                @Index(name = "epoch", columnList = "epoch")
        })
public class OutboxMessageEntity {

    @Id
    String uuid;
    @JdbcTypeCode(SqlTypes.JSON)
    String metadata;
    @JdbcTypeCode(SqlTypes.JSON)
    String payload;
    String type;
    long epoch;
    String partitionKey;
    int partition;
}
