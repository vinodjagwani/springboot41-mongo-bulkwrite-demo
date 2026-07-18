package com.example.bulkwritedemo.service;

import com.example.bulkwritedemo.dto.ProductRequest;

sealed interface RestockOperation permits RestockOperation.NewProduct, RestockOperation.AdjustStock, RestockOperation.Discontinue {

    record NewProduct(ProductRequest product) implements RestockOperation {
    }

    record AdjustStock(String sku, int delta, String reason) implements RestockOperation {
    }

    record Discontinue(String sku) implements RestockOperation {
    }
}
