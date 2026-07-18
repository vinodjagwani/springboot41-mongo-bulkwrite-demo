package com.example.bulkwritedemo.repository;

import com.example.bulkwritedemo.domain.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ProductRepository extends MongoRepository<Product, String> {

    // Interface-based projection: Spring Data proxies this per row instead of
    // loading the full Product document. As of the 2026.0 (Spring Data
    // Commons 4.1) release, projection parameter/return types that aren't
    // explicitly marked with @ProjectedPayload log a deprecation warning
    // during a transition period (planned to become an error in 2026.1).
    List<ProductSummary> findSummaryByStockLessThan(int stock);
}
