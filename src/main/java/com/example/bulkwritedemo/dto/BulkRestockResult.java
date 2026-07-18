package com.example.bulkwritedemo.dto;

import org.springframework.data.mongodb.core.bulk.BulkWriteResult;

public record BulkRestockResult(long insertCount, long modifiedCount, long deleteCount, long upsertCount) {

    public static BulkRestockResult from(BulkWriteResult result) {
        return new BulkRestockResult(
                result.insertCount(), result.modifiedCount(), result.deleteCount(), result.upsertCount());
    }
}
