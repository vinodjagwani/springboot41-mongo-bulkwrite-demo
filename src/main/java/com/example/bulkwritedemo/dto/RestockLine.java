package com.example.bulkwritedemo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RestockLine(@NotBlank String sku, @NotNull Integer delta, @NotBlank String reason) {
}
