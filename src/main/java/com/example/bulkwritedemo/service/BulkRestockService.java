package com.example.bulkwritedemo.service;

import com.example.bulkwritedemo.domain.InventoryLog;
import com.example.bulkwritedemo.domain.Product;
import com.example.bulkwritedemo.dto.BulkRestockRequest;
import com.example.bulkwritedemo.dto.BulkRestockResult;
import com.example.bulkwritedemo.dto.ProductRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.bulk.Bulk;
import org.springframework.data.mongodb.core.bulk.BulkWriteOptions;
import org.springframework.data.mongodb.core.bulk.BulkWriteResult;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

/**
 * Translates a {@link BulkRestockRequest} into one MongoOperations.bulkWrite(...)
 * call spanning the "products" and "inventory_logs" collections (Spring Data
 * MongoDB 5.1 / Spring Data 2026.0 Multi-Collection Bulk Write API).
 */
@Service
@RequiredArgsConstructor
public class BulkRestockService {

    private final MongoOperations operations;

    public BulkRestockResult apply(final BulkRestockRequest request) {
        final List<RestockOperation> plan = toOperations(request);
        final BulkOperationGroups groups = groupByCollection(plan);
        final Bulk bulk = buildBulk(groups);
        final BulkWriteResult result = operations.bulkWrite(bulk, BulkWriteOptions.ordered());
        return BulkRestockResult.from(result);
    }

    private List<RestockOperation> toOperations(final BulkRestockRequest request) {
        final List<RestockOperation> ops = new ArrayList<>();
        request.newProducts().forEach(p -> ops.add(new RestockOperation.NewProduct(p)));
        request.adjustments().forEach(a -> ops.add(new RestockOperation.AdjustStock(a.sku(), a.delta(), a.reason())));
        request.discontinueSkus().forEach(sku -> ops.add(new RestockOperation.Discontinue(sku)));
        return ops;
    }

    private BulkOperationGroups groupByCollection(final List<RestockOperation> plan) {
        final List<Consumer<Bulk.BulkSpec>> productOps = new ArrayList<>();
        final List<Consumer<Bulk.BulkSpec>> inventoryLogOps = new ArrayList<>();

        for (final RestockOperation op : plan) {
            switch (op) {
                case RestockOperation.NewProduct newProduct -> productOps.add(insertProductOp(newProduct));
                case RestockOperation.AdjustStock adjustStock -> {
                    productOps.add(updateStockOp(adjustStock));
                    inventoryLogOps.add(insertLogOp(adjustStock));
                }
                case RestockOperation.Discontinue discontinue -> productOps.add(removeProductOp(discontinue));
            }
        }

        return new BulkOperationGroups(productOps, inventoryLogOps);
    }

    private Consumer<Bulk.BulkSpec> insertProductOp(final RestockOperation.NewProduct op) {
        final ProductRequest product = op.product();
        return spec -> spec.insert(new Product(product.sku(), product.name(), product.stock()));
    }

    private Consumer<Bulk.BulkSpec> updateStockOp(final RestockOperation.AdjustStock op) {
        return spec -> spec.updateOne(query(where("sku").is(op.sku())), new Update().inc("stock", op.delta()));
    }

    private Consumer<Bulk.BulkSpec> insertLogOp(final RestockOperation.AdjustStock op) {
        return spec -> spec.insert(new InventoryLog(op.sku(), op.delta(), op.reason(), Instant.now()));
    }

    private Consumer<Bulk.BulkSpec> removeProductOp(final RestockOperation.Discontinue op) {
        return spec -> spec.remove(query(where("sku").is(op.sku())));
    }

    private Bulk buildBulk(final BulkOperationGroups groups) {
        return Bulk.create(builder -> {
            addCollection(builder, Product.class, groups.productOps());
            addCollection(builder, InventoryLog.class, groups.inventoryLogOps());
        });
    }

    private <T> void addCollection(
            final Bulk.BulkBuilder builder, final Class<T> collectionType, final List<Consumer<Bulk.BulkSpec>> ops) {
        if (!ops.isEmpty()) {
            builder.inCollection(collectionType, spec -> ops.forEach(op -> op.accept(spec)));
        }
    }

    private record BulkOperationGroups(
            List<Consumer<Bulk.BulkSpec>> productOps, List<Consumer<Bulk.BulkSpec>> inventoryLogOps) {
    }
}
