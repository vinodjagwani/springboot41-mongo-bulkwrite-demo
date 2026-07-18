package com.example.bulkwritedemo.service;

import com.example.bulkwritedemo.repository.ProductRepository;
import com.example.bulkwritedemo.repository.ProductSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository repository;

    /** Low-stock projection — exercises the @ProjectedPayload-annotated {@link ProductSummary}. */
    public List<ProductSummary> lowStock(final int threshold) {
        return repository.findSummaryByStockLessThan(threshold);
    }
}
