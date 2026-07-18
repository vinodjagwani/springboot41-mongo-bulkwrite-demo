package com.example.bulkwritedemo.web;

import com.example.bulkwritedemo.dto.BulkRestockRequest;
import com.example.bulkwritedemo.dto.BulkRestockResult;
import com.example.bulkwritedemo.service.BulkRestockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * One request here triggers one MongoOperations.bulkWrite(...) call mixing
 * insert/update/remove across the "products" and "inventory_logs" collections.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/bulk")
public class BulkRestockController {

    private final BulkRestockService service;

    @PostMapping("/restock")
    public BulkRestockResult restock(@Valid @RequestBody final BulkRestockRequest request) {
        return service.apply(request);
    }
}
