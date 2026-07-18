package com.example.bulkwritedemo;

import com.example.bulkwritedemo.domain.InventoryLog;
import com.example.bulkwritedemo.domain.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final MongoOperations operations;

    @Override
    public void run(final String... args) {
        operations.dropCollection(Product.class);
        operations.dropCollection(InventoryLog.class);

        operations.insert(new Product("SKU-100", "Lightsaber", 5));
        operations.insert(new Product("SKU-999", "Discontinued Widget", 0));
    }
}
