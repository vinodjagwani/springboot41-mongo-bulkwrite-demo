package com.example.bulkwritedemo.dto;

import jakarta.validation.Valid;

import java.util.List;

public record BulkRestockRequest(
        List<@Valid ProductRequest> newProducts,
        List<@Valid RestockLine> adjustments,
        List<String> discontinueSkus) {

    public BulkRestockRequest {
        newProducts = newProducts == null ? List.of() : List.copyOf(newProducts);
        adjustments = adjustments == null ? List.of() : List.copyOf(adjustments);
        discontinueSkus = discontinueSkus == null ? List.of() : List.copyOf(discontinueSkus);
    }
}
