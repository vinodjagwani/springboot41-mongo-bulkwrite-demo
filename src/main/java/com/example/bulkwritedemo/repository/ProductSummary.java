package com.example.bulkwritedemo.repository;

import org.springframework.data.web.ProjectedPayload;

@ProjectedPayload
public interface ProductSummary {

    String getSku();

    String getName();

    int getStock();
}
