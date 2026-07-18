package com.example.bulkwritedemo.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Document("inventory_logs")
public class InventoryLog {

    @Id
    private String id;
    private String sku;
    private int change;
    private String reason;
    private Instant timestamp;

    public InventoryLog(final String sku, final int change, final String reason, final Instant timestamp) {
        this(null, sku, change, reason, timestamp);
    }
}
