package com.example.bulkwritedemo.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Document("products")
public class Product {

    @Id
    private String id;
    private String sku;
    private String name;
    private int stock;

    public Product(final String sku, final String name, final int stock) {
        this(null, sku, name, stock);
    }
}
