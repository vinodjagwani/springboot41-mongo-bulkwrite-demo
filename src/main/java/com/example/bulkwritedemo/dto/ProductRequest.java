package com.example.bulkwritedemo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

public record ProductRequest(
        @NotBlank String sku,
        @NotBlank String name,
        @PositiveOrZero int stock) {
}
