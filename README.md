# Multi-Collection Bulk Write REST API (Spring Boot 4.1 / Spring Data 2026.0)

Enterprise-layered REST API demonstrating `MongoOperations.bulkWrite(...)` —
one request that mixes insert/update/remove across the `products` and
`inventory_logs` collections in a single call.

## Verified end-to-end, not just compiled
- Ran against a real MongoDB 8.0 replica-set container (`docker run mongo:8.0 --replSet rs0`).
- Hit every endpoint with `curl`, independently cross-checked results with `mongosh`
  (not just trusting the app's own response body).
- Decompiled the actual resolved 5.1.0 jar with `javap` to get real method
  signatures — the official release-notes code sample omits imports and got
  the package one level wrong (`core.bulk`, not `core`).

## Real bug found and fixed during verification
`spring.data.mongodb.*` (the property prefix that has worked since Spring
Boot 1.x) is **dead** as of Spring Boot 4.0 — it's an error-level-deprecated
alias that nothing binds to. It fails **silently**: no startup error, app
boots fine, `MongoOperations` just falls back to `mongodb://localhost/test`
and writes there while `bulkwrite_demo` stays empty. Confirmed by extracting
`META-INF/spring-configuration-metadata.json` from `spring-boot-mongodb-4.1.0.jar`:

```
spring.data.mongodb.uri -> {"level": "error", "replacement": "spring.mongodb.uri", "since": "4.0.0"}
```

The real prefix is **`spring.mongodb.*`** (module moved to
`org.springframework.boot.mongodb.autoconfigure`). This project's
[application.yml](src/main/resources/application.yml) uses the corrected prefix.
If you're upgrading an existing app to Boot 4.x, grep your config for
`spring.data.mongodb.` — it will silently point at the wrong database.

## Scope
Only two endpoints, on purpose: the multi-collection bulk write API, and a
`@ProjectedPayload` projection query. No generic CRUD.

## Package layout
```
domain/      Product, InventoryLog — @Document entities
repository/  ProductRepository (MongoRepository) + ProductSummary (@ProjectedPayload projection)
dto/         Java records — ProductRequest, RestockLine, BulkRestockRequest, BulkRestockResult
service/     ProductService (lowStock only), BulkRestockService (sealed interface + pattern-matching switch -> Bulk API)
web/         ProductController (GET low-stock only), BulkRestockController
exception/   ApiExceptionHandler (RFC 7807 ProblemDetail, validation errors only)
```

## JDK 21+ features used
- Records for every DTO (`ProductRequest`, `ProductResponse`, `RestockLine`,
  `BulkRestockRequest` w/ compact constructor, `BulkRestockResult`).
- Sealed interface + record patterns: `RestockOperation` (`NewProduct` /
  `AdjustStock` / `Discontinue`) consumed via an exhaustive pattern-matching
  `switch` in `BulkRestockService` (no `default` branch needed).
- Virtual threads: `spring.threads.virtual.enabled: true` (backed by the real
  `org.springframework.boot.thread.Threading` enum, confirmed present in the
  resolved `spring-boot-4.1.0.jar`).

## `@ProjectedPayload`
`org.springframework.data.web.ProjectedPayload` is a pre-existing marker
annotation for **interface-based** projections — it doesn't apply to records
(record/DTO projections don't go through the JDK-proxy path the annotation
gates). `ProductSummary` in `repository/` is annotated with it and returned
directly from `GET /api/products/low-stock` — Spring serializes the proxy
via Jackson without any extra mapping code.

## Requirements
- Java 21+
- MongoDB 8.0+ server, running as a replica set (cross-collection bulkWrite
  needs 8.0+ **and** a replica set — a bare standalone `mongod` won't accept
  the underlying multi-namespace command).

## Run
```
docker run -d --name mongo -p 27017:27017 mongo:8.0 --replSet rs0
docker exec mongo mongosh --eval 'rs.initiate()'
mvn spring-boot:run
```

## Endpoints (both exercised live against a real Mongo 8.0 replica set)

| Method | Path | Notes |
|---|---|---|
| GET | `/api/products/low-stock?threshold=N` | `@ProjectedPayload` projection |
| POST | `/api/bulk/restock` | **the feature** — one `bulkWrite` across 2 collections |

`POST /api/bulk/restock` example:
```json
{
  "newProducts": [{"sku": "SKU-200", "name": "Blaster", "stock": 12}],
  "adjustments": [{"sku": "SKU-100", "delta": 3, "reason": "restock"}],
  "discontinueSkus": ["SKU-999"]
}
```
Response: `{"insertCount":2,"modifiedCount":1,"deleteCount":1,"upsertCount":0}`
(insert lands in both `products` and `inventory_logs` — that's the "2").

## Still unverified
- `@Version` / optimistic-locking behavior on the new bulk API is undocumented
  and not exercised here (the old `BulkOperations` explicitly didn't support it).
