package com.example.bulkwritedemo.web;

import com.example.bulkwritedemo.repository.ProductSummary;
import com.example.bulkwritedemo.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService service;

    @GetMapping("/low-stock")
    public List<ProductSummary> lowStock(@RequestParam(defaultValue = "10") final int threshold) {
        return service.lowStock(threshold);
    }
}
