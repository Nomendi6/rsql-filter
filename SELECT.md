# RSQL Filter - SELECT Queries Guide

This document provides comprehensive documentation for SELECT query functionality in the RSQL Filter library.

## Table of Contents
- [Overview](#overview)
- [SELECT Syntax](#select-syntax)
  - [Basic Field Selection](#basic-field-selection)
  - [Field Aliases](#field-aliases)
  - [Navigation Properties](#navigation-properties)
  - [Wildcard Selection](#wildcard-selection)
  - [Arithmetic Expressions](#arithmetic-expressions)
- [Aggregate Queries](#aggregate-queries)
  - [Supported Aggregate Functions](#supported-aggregate-functions)
  - [GROUP BY Behavior](#group-by-behavior)
  - [Multiple Aggregates](#multiple-aggregates)
  - [COUNT DISTINCT](#count-distinct)
  - [Arithmetic with Aggregates](#arithmetic-with-aggregates)
- [RsqlQueryService Methods](#rsqlqueryservice-methods)
  - [getTupleWithSelect](#gettuplewithselect)
  - [getTupleAsPageWithSelect](#gettupleaspagewitchselect)
  - [getAggregateResult](#getaggregateresult)
  - [getLOVWithSelect](#getlovwithselect)
  - [getSelectResult](#getselectresult)
  - [getSelectResultAsPage](#getselectresultaspage)
- [Complete Examples](#complete-examples)
- [Migration from String[] API](#migration-from-string-api)
- [Converting Results to JSON](#converting-results-to-json)
- [Best Practices](#best-practices)
- [Limitations](#limitations)
- [See Also](#see-also)

## Overview

The SELECT functionality extends RSQL Filter to support flexible field selection, aliases, navigation properties, and aggregate queries. Instead of using `String[]` arrays for field selection, you can now use a more intuitive SELECT string syntax.

### Key Features

- **Field Aliases**: Rename fields in results using `:alias` syntax
- **Navigation Properties**: Access related entity fields using dot notation
- **Arithmetic Expressions**: Support for `+`, `-`, `*`, `/` operators with aggregate functions (in `getAggregateResult()` only)
- **Aggregate Functions**: COUNT, SUM, AVG, MIN, MAX with automatic GROUP BY
- **Type Safety**: Automatic LEFT JOIN creation for related entities
- **Backward Compatible**: Old `String[]` API still works

### Why SELECT Strings?

**Before (String[] API):**
```java
// Limited - no aliases
String[] fields = {"code", "name", "price"};
List<Tuple> results = queryService.getTuple("status==ACTIVE", pageable, fields);

// Result: [code, name, price] - no way to rename 
```

**After (SELECT String API):**
```java
// Flexible - aliases, navigation properties, readable
String selectString = "code:productCode, name, productType.name:typeName, price";
List<Tuple> results = queryService.getTupleWithSelect(selectString, "status==ACTIVE", pageable);

// Result: [productCode, name, typeName, price] - aliased and with related data
```

## SELECT Syntax

### Basic Field Selection

Select one or more fields from your entity:

```java
// Single field
"code"

// Multiple fields (comma-separated)
"code, name, price"

// All basic fields from entity
"*"
```

**Example:**
```java
List<Tuple> products = queryService.getTupleWithSelect(
    "code, name, price",
    "status==ACTIVE",
    PageRequest.of(0, 20)
);

// Access results
for (Tuple row : products) {
    String code = (String) row.get(0);
    String name = (String) row.get(1);
    BigDecimal price = (BigDecimal) row.get(2);
}
```

### Field Aliases

Rename fields in the result using the `:alias` syntax:

```java
// Syntax: fieldName:aliasName
"code:productCode"

// Multiple aliased fields
"code:productCode, name:productName, price:unitPrice"
```

**Example:**
```java
List<Tuple> products = queryService.getTupleWithSelect(
    "code:productCode, name:productName, price:unitPrice",
    "status==ACTIVE",
    PageRequest.of(0, 20)
);

// Access by alias
for (Tuple row : products) {
    String code = (String) row.get("productCode");
    String name = (String) row.get("productName");
    BigDecimal price = (BigDecimal) row.get("unitPrice");
}
```

**Why use aliases?**
- Make results more readable in REST APIs
- Avoid naming conflicts when joining multiple entities
- Match your DTO field names

### Navigation Properties

Access related entity fields using dot notation. The library automatically creates LEFT JOINs:

```java
// Access parent entity field
"productType.name"

// Multiple levels of nesting
"order.customer.country.name"

// Mix with regular fields
"code, name, productType.name, productType.code"
```

**Example:**
```java
// Automatically creates: LEFT JOIN product.productType
List<Tuple> products = queryService.getTupleWithSelect(
    "code, name, productType.name:typeName",
    "status==ACTIVE",
    PageRequest.of(0, 20)
);

for (Tuple row : products) {
    String code = (String) row.get(0);
    String name = (String) row.get(1);
    String typeName = (String) row.get("typeName"); // From related entity
}
```

**Supported relationships:**
- `@ManyToOne`
- `@OneToOne`
- `@Embedded`

**Not supported:**
- Collection relationships (`@OneToMany`, `@ManyToMany`) require custom JPQL

### Wildcard Selection

Select all basic fields from an entity:

```java
// Select all fields from root entity
"*"

// Select all fields from related entity
"productType.*"

// Mix with specific fields
"code, productType.*, price"
```

**Example:**
```java
// Selects all basic fields: id, code, name, price, status, createdDate, lastModifiedDate
List<Tuple> products = queryService.getTupleWithSelect(
    "*",
    "status==ACTIVE",
    PageRequest.of(0, 20)
);
```

**Note:** Wildcard (`*`) selects only **basic fields** (String, Integer, Date, etc.), not relationship fields.

### Arithmetic Expressions

Perform arithmetic calculations in **aggregate queries** using `+`, `-`, `*`, `/` operators with aggregate functions.

**Important:** Arithmetic expressions are currently supported **only in aggregate queries** via the `getAggregateResult()` method. They work with aggregate functions like SUM, AVG, COUNT, MIN, MAX.

```java
// Aggregate function arithmetic (SUPPORTED)
"SUM(debit) - SUM(credit):balance"
"SUM(price) * 1.2:totalWithTax"
"SUM(price) / COUNT(*):averagePrice"
"(SUM(price) - 100) * 2 / COUNT(*):metric"

// Simple field arithmetic (NOT SUPPORTED)
// "price + 10:adjustedPrice"              // ❌ Not supported in getTupleWithSelect
// "(price - discount) * quantity:total"   // ❌ Not supported in getTupleWithSelect
```

For simple field selection without arithmetic, use `getTupleWithSelect()` or `getSelectResult()`.

**Operator Precedence:**
1. Parentheses `()` (highest priority)
2. Multiplication `*` and Division `/`
3. Addition `+` and Subtraction `-` (lowest priority)

Example: `10 + 5 * 2` evaluates as `10 + (5 * 2) = 20`

**Supported Operands (in aggregate queries):**
- Aggregate functions: `SUM(field)`, `AVG(field)`, `COUNT(*)`, `MIN(field)`, `MAX(field)`
- Numeric literals: `10`, `1.2`, `0.5`
- Parenthesized expressions: `(SUM(price) - 100)`

**Not Supported:**
- ❌ Field references without aggregates: `price`, `quantity`, `discount`
- ❌ Arithmetic in non-aggregate queries (`getTupleWithSelect`, `getSelectResult`)

**Example - Calculate Balance:**
```java
// Debit minus credit
List<Tuple> balances = queryService.getAggregateResult(
    "account:accountName, SUM(debit) - SUM(credit):balance",
    "year==2024",
    PageRequest.of(0, 100, Sort.by("balance").descending())
);

for (Tuple row : balances) {
    String account = (String) row.get("accountName");
    BigDecimal balance = (BigDecimal) row.get("balance");

    System.out.printf("%s: $%s%n", account, balance);
}
```

**Example - Price with Tax:**
```java
// Add 20% tax to total
List<Tuple> totals = queryService.getAggregateResult(
    "productType.name:category, SUM(price):subtotal, SUM(price) * 1.2:totalWithTax",
    "status==ACTIVE",
    null
);

for (Tuple row : totals) {
    System.out.printf(
        "%s: Subtotal $%s, With Tax $%s%n",
        row.get("category"),
        row.get("subtotal"),
        row.get("totalWithTax")
    );
}
```

**Example - Complex Calculation:**
```java
// Adjusted average: (total - fixed_cost) * multiplier / count
List<Tuple> metrics = queryService.getAggregateResult(
    "category, (SUM(price) - 50) * 2 / COUNT(*):adjustedAverage",
    "",
    null
);

for (Tuple row : metrics) {
    String category = (String) row.get(0);
    BigDecimal metric = (BigDecimal) row.get("adjustedAverage");

    System.out.printf("%s: Adjusted Avg = $%s%n", category, metric);
}
```

**REST Endpoint Example:**
```java
@GetMapping("/api/financial/balances")
public ResponseEntity<List<Map<String, Object>>> getAccountBalances(
    @RequestParam(required = false, defaultValue = "2024") String year
) {
    List<Tuple> balances = accountService.getQueryService().getAggregateResult(
        "account.name:accountName, " +
        "SUM(debit):totalDebit, " +
        "SUM(credit):totalCredit, " +
        "SUM(debit) - SUM(credit):balance",
        "year==" + year,
        Sort.by("balance").descending()
    );

    return ResponseEntity.ok(TupleConverter.toMapList(balances));
}
```

**HTTP Request:**
```http
GET /api/financial/balances?year=2024
```

**Response:**
```json
[
  {
    "accountName": "Revenue",
    "totalDebit": 50000.00,
    "totalCredit": 75000.00,
    "balance": -25000.00
  },
  {
    "accountName": "Expenses",
    "totalDebit": 30000.00,
    "totalCredit": 15000.00,
    "balance": 15000.00
  }
]
```

## Aggregate Queries

Execute aggregate queries with automatic GROUP BY extraction.

### Supported Aggregate Functions

| Function | Description | Example |
|----------|-------------|---------|
| `COUNT(*)` | Count all rows | `COUNT(*)` |
| `COUNT(field)` | Count non-null values | `COUNT(price)` |
| `COUNT(DIST field)` | Count distinct values | `COUNT(DIST productType.name)` |
| `SUM(field)` | Sum numeric values | `SUM(price)` |
| `AVG(field)` | Average of values | `AVG(price)` |
| `MIN(field)` | Minimum value | `MIN(price)` |
| `MAX(field)` | Maximum value | `MAX(price)` |

### GROUP BY Behavior

The library **automatically extracts GROUP BY fields** from your SELECT string:

**Rule:** Any field **without** an aggregate function becomes a GROUP BY field.

```java
// SELECT string: "productType.name, COUNT(*)"
// SQL generated: SELECT productType.name, COUNT(*) FROM ... GROUP BY productType.name

// SELECT string: "category, status, COUNT(*), SUM(price)"
// SQL generated: SELECT category, status, COUNT(*), SUM(price) FROM ... GROUP BY category, status
```

**Example:**
```java
// Group by productType.name
List<Tuple> summary = queryService.getAggregateResult(
    "productType.name:type, COUNT(*):count, SUM(price):total",
    "status==ACTIVE",
    PageRequest.of(0, 100)
);

// Results grouped by type
for (Tuple row : summary) {
    String type = (String) row.get("type");
    Long count = (Long) row.get("count");
    BigDecimal total = (BigDecimal) row.get("total");

    System.out.println(type + ": " + count + " items, total " + total);
}
// Output:
// Electronics: 15 items, total 5430.00
// Books: 8 items, total 320.50
```

### Arithmetic with Aggregates

Combine aggregate functions with arithmetic operators for complex calculations:

```java
// Average price calculation using SUM/COUNT instead of AVG
List<Tuple> stats = queryService.getAggregateResult(
    "productType.name:type, SUM(price) / COUNT(*):customAverage",
    "status==ACTIVE",
    null
);

// Profit margin: (revenue - cost) / revenue
List<Tuple> margins = queryService.getAggregateResult(
    "category, " +
    "(SUM(revenue) - SUM(cost)) / SUM(revenue):profitMargin",
    "",
    null
);

// Multi-step calculation
List<Tuple> complex = queryService.getAggregateResult(
    "region, " +
    "(SUM(sales) - SUM(returns)) * 0.15 + 100:adjustedCommission",
    "year==2024",
    null
);
```

**Real-world Example - Financial Report:**
```java
@GetMapping("/api/reports/financial")
public ResponseEntity<List<Map<String, Object>>> getFinancialReport(
    @RequestParam String startDate,
    @RequestParam String endDate
) {
    String dateFilter = String.format(
        "transactionDate=ge=#%s#;transactionDate=le=#%s#",
        startDate, endDate
    );

    List<Tuple> report = transactionService.getQueryService().getAggregateResult(
        "account.category:category, " +
        "SUM(debit):totalDebit, " +
        "SUM(credit):totalCredit, " +
        "SUM(debit) - SUM(credit):netBalance, " +
        "(SUM(debit) - SUM(credit)) / SUM(debit):debitRatio",
        dateFilter,
        Sort.by("netBalance").descending()
    );

    return ResponseEntity.ok(TupleConverter.toMapList(report));
}
```

**Benefits:**
- ✅ Calculations performed at database level (faster)
- ✅ No need to post-process results in Java
- ✅ Automatically works with GROUP BY
- ✅ Can be used with HAVING filters (filter on calculated fields)

### Multiple Aggregates

Combine multiple aggregate functions in one query:

```java
List<Tuple> stats = queryService.getAggregateResult(
    "productType.name:type, " +
    "COUNT(*):count, " +
    "SUM(price):total, " +
    "AVG(price):average, " +
    "MIN(price):minPrice, " +
    "MAX(price):maxPrice",
    "",  // No filter - all records
    PageRequest.of(0, 100)
);

for (Tuple row : stats) {
    System.out.printf(
        "%s: count=%d, total=%s, avg=%s, min=%s, max=%s%n",
        row.get("type"),
        row.get("count"),
        row.get("total"),
        row.get("average"),
        row.get("minPrice"),
        row.get("maxPrice")
    );
}
```

### COUNT DISTINCT

Count unique values using `COUNT(DIST field)` syntax:

```java
// Count distinct product types
List<Tuple> result = queryService.getAggregateResult(
    "COUNT(DIST productType.name):distinctTypes",
    "status==ACTIVE",
    null
);

Long distinctTypes = (Long) result.get(0).get("distinctTypes");
System.out.println("Active products span " + distinctTypes + " different types");
```

**Multiple GROUP BY fields:**
```java
// Group by category AND status
List<Tuple> summary = queryService.getAggregateResult(
    "category:cat, status:st, COUNT(*):count",
    "",
    PageRequest.of(0, 100)
);

// Results grouped by (category, status) combinations
```

## RsqlQueryService Methods

### getTupleWithSelect

Returns a list of tuples with flexible SELECT string support.

**Important:** This method does **NOT** support arithmetic expressions. Use `getAggregateResult()` for queries with arithmetic operations.

**Signature:**
```java
public List<Tuple> getTupleWithSelect(
    String selectString,
    String filter,
    Pageable pageable
)
```

**Parameters:**
- `selectString` - SELECT clause (e.g., `"code:productCode, name, productType.name"`) - **without arithmetic expressions**
- `filter` - RSQL filter expression (e.g., `"status==ACTIVE"`)
- `pageable` - Pagination and sorting (can be `null`)

**Returns:** List of `Tuple` objects with selected fields

**Example:**
```java
List<Tuple> products = queryService.getTupleWithSelect(
    "code:productCode, name, productType.name:typeName, price",
    "status==ACTIVE;price=gt=100",
    PageRequest.of(0, 20, Sort.by("name").ascending())
);

for (Tuple row : products) {
    System.out.printf(
        "Product: %s - %s (%s) - $%s%n",
        row.get("productCode"),
        row.get(1),  // name - can access by index or alias
        row.get("typeName"),
        row.get(3)   // price
    );
}
```

**REST Controller Example:**
```java
@GetMapping("/api/products/select")
public ResponseEntity<List<Map<String, Object>>> getProducts(
    @RequestParam(required = false) String select,
    @RequestParam(required = false) String filter,
    Pageable pageable
) {
    List<Tuple> results = productService.getQueryService()
        .getTupleWithSelect(select, filter, pageable);

    // Convert tuples to maps for JSON response
    List<Map<String, Object>> response = results.stream()
        .map(tuple -> {
            Map<String, Object> map = new HashMap<>();
            tuple.getElements().forEach(element ->
                map.put(element.getAlias(), tuple.get(element.getAlias()))
            );
            return map;
        })
        .collect(Collectors.toList());

    return ResponseEntity.ok(response);
}
```

**HTTP Request:**
```http
GET /api/products/select?select=code:productCode,name,productType.name:type&filter=status==ACTIVE
```

### getTupleAsPageWithSelect

Paginated version of `getTupleWithSelect`.

**Signature:**
```java
public Page<Tuple> getTupleAsPageWithSelect(
    String selectString,
    String filter,
    Pageable pageable
)
```

**Example:**
```java
Pageable pageable = PageRequest.of(0, 20, Sort.by("name"));
Page<Tuple> page = queryService.getTupleAsPageWithSelect(
    "code, name, productType.name:type",
    "status==ACTIVE",
    pageable
);

System.out.println("Total elements: " + page.getTotalElements());
System.out.println("Total pages: " + page.getTotalPages());
System.out.println("Current page: " + page.getNumber());

for (Tuple row : page.getContent()) {
    System.out.printf("%s - %s (%s)%n",
        row.get(0), row.get(1), row.get("type")
    );
}
```

**REST Controller with Pagination:**
```java
@GetMapping("/api/products/select")
public ResponseEntity<Page<Map<String, Object>>> getProducts(
    @RequestParam String select,
    @RequestParam(required = false) String filter,
    Pageable pageable
) {
    Page<Tuple> tuplePage = productService.getQueryService()
        .getTupleAsPageWithSelect(select, filter, pageable);

    Page<Map<String, Object>> response = tuplePage.map(tuple -> {
        Map<String, Object> map = new HashMap<>();
        tuple.getElements().forEach(element ->
            map.put(element.getAlias(), tuple.get(element.getAlias()))
        );
        return map;
    });

    return ResponseEntity.ok(response);
}
```

### getAggregateResult

Execute aggregate queries with automatic GROUP BY and **arithmetic expressions**.

**Important:** This is the **only method** that supports arithmetic expressions in SELECT.

**Signature:**
```java
public List<Tuple> getAggregateResult(
    String selectString,
    String filter,
    Pageable pageable
)
```

**Parameters:**
- `selectString` - SELECT with aggregate functions and **arithmetic expressions** (e.g., `"category, SUM(price) * 1.2:totalWithTax"`)
- `filter` - RSQL filter applied **before** aggregation
- `pageable` - Sorting (pagination less useful with GROUP BY)

**Example - Sales by Category:**
```java
List<Tuple> sales = queryService.getAggregateResult(
    "productType.name:category, " +
    "COUNT(*):productCount, " +
    "SUM(price):totalValue, " +
    "AVG(price):avgPrice",
    "status==ACTIVE",
    Sort.by("totalValue").descending()
);

System.out.println("Sales Summary:");
for (Tuple row : sales) {
    System.out.printf(
        "  %s: %d products, Total: $%s, Avg: $%s%n",
        row.get("category"),
        row.get("productCount"),
        row.get("totalValue"),
        row.get("avgPrice")
    );
}
```

**Example - Multi-Dimensional Grouping:**
```java
// Group by category AND status
List<Tuple> breakdown = queryService.getAggregateResult(
    "productType.name:category, status, COUNT(*):count",
    "",  // All records
    null
);

System.out.println("Product Breakdown by Category and Status:");
for (Tuple row : breakdown) {
    System.out.printf(
        "  %s - %s: %d products%n",
        row.get("category"),
        row.get(1),  // status
        row.get("count")
    );
}
```

**REST Controller Example:**
```java
@GetMapping("/api/products/stats")
public ResponseEntity<List<Map<String, Object>>> getProductStats(
    @RequestParam(required = false) String filter
) {
    List<Tuple> results = productService.getQueryService().getAggregateResult(
        "productType.name:type, COUNT(*):count, SUM(price):total, AVG(price):avg",
        filter,
        Sort.by("count").descending()
    );

    List<Map<String, Object>> stats = results.stream()
        .map(tuple -> Map.of(
            "type", tuple.get("type"),
            "count", tuple.get("count"),
            "total", tuple.get("total"),
            "average", tuple.get("avg")
        ))
        .collect(Collectors.toList());

    return ResponseEntity.ok(stats);
}
```

### getLOVWithSelect

Get List of Values with flexible field selection (for dropdowns/autocomplete).

**Signature:**
```java
public List<LovDTO> getLOVWithSelect(
    String selectString,
    String filter,
    Pageable pageable
)
```

**SELECT string must map to LovDTO fields:**
- First field: `id` (Long)
- Second field (optional): `code` (String)
- Third field (optional): `name` (String)

**Example - Basic LOV:**
```java
List<LovDTO> categories = queryService.getLOVWithSelect(
    "id, code, name",
    "active==true",
    PageRequest.of(0, 100, Sort.by("name"))
);
```

**Example - LOV with Navigation Properties:**
```java
// Get products with their parent category info
List<LovDTO> products = queryService.getLOVWithSelect(
    "id, code, productType.name:name",  // Use parent name as product name
    "status==ACTIVE",
    PageRequest.of(0, 50)
);
```

**Example - Autocomplete Endpoint:**
```java
@GetMapping("/api/products/autocomplete")
public ResponseEntity<List<LovDTO>> autocomplete(
    @RequestParam String query
) {
    List<LovDTO> suggestions = productService.getQueryService()
        .getLOVWithSelect(
            "id, code, name",
            "name=like='*" + query + "*'",
            PageRequest.of(0, 10, Sort.by("name"))
        );

    return ResponseEntity.ok(suggestions);
}
```

**HTTP Request:**
```http
GET /api/products/autocomplete?query=phone
```

**Response:**
```json
[
  {"id": 123, "code": "P001", "name": "Smartphone X"},
  {"id": 124, "code": "P002", "name": "Phone Case"},
  {"id": 125, "code": "P003", "name": "Telephone Wire"}
]
```

### getSelectResult

Generic method for any result class with SELECT string support.

**Signature:**
```java
public <RESULT> List<RESULT> getSelectResult(
    Class<RESULT> resultClass,
    String selectString,
    String filter,
    Pageable pageable
)
```

**Parameters:**
- `resultClass` - Class to map results to (Tuple.class, custom DTO, etc.)
- `selectString` - SELECT fields matching result class constructor
- `filter` - RSQL filter
- `pageable` - Pagination and sorting

**Example with Tuple:**
```java
List<Tuple> products = queryService.getSelectResult(
    Tuple.class,
    "code, name, price",
    "status==ACTIVE",
    PageRequest.of(0, 20)
);
```

**Example with Custom DTO:**
```java
// DTO must have matching constructor
public record ProductSummaryDTO(String code, String name, BigDecimal price) {}

List<ProductSummaryDTO> summaries = queryService.getSelectResult(
    ProductSummaryDTO.class,
    "code, name, price",
    "status==ACTIVE;price=gt=100",
    PageRequest.of(0, 50)
);
```

**Why use this?**
- Type-safe results with custom DTOs
- More flexible than hardcoded getLOV methods
- Works with any result class that has appropriate constructor

### getSelectResultAsPage

Paginated version of `getSelectResult`.

**Signature:**
```java
public <RESULT> Page<RESULT> getSelectResultAsPage(
    Class<RESULT> resultClass,
    String selectString,
    String filter,
    Pageable pageable
)
```

**Example:**
```java
Page<ProductSummaryDTO> page = queryService.getSelectResultAsPage(
    ProductSummaryDTO.class,
    "code, name, productType.name:type, price",
    "status==ACTIVE",
    PageRequest.of(0, 20, Sort.by("name"))
);

System.out.println("Page " + (page.getNumber() + 1) + " of " + page.getTotalPages());
for (ProductSummaryDTO product : page.getContent()) {
    System.out.println(product);
}
```

## Complete Examples

### Example 1: Product Dashboard with Statistics

```java
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final ProductService productService;

    @GetMapping("/product-stats")
    public ResponseEntity<Map<String, Object>> getProductStatistics() {
        RsqlQueryService queryService = productService.getQueryService();

        // Overall statistics
        List<Tuple> overallStats = queryService.getAggregateResult(
            "COUNT(*):total, SUM(price):totalValue, AVG(price):avgPrice",
            "status==ACTIVE",
            null
        );

        // Statistics by category
        List<Tuple> byCategory = queryService.getAggregateResult(
            "productType.name:category, COUNT(*):count, SUM(price):total",
            "status==ACTIVE",
            Sort.by("total").descending()
        );

        // Top 10 most expensive products
        List<Tuple> topProducts = queryService.getTupleWithSelect(
            "code, name, productType.name:type, price",
            "status==ACTIVE",
            PageRequest.of(0, 10, Sort.by("price").descending())
        );

        Map<String, Object> response = new HashMap<>();
        response.put("overall", overallStats.get(0));
        response.put("byCategory", byCategory);
        response.put("topProducts", topProducts);

        return ResponseEntity.ok(response);
    }
}
```

### Example 2: Advanced Product Search

```java
@GetMapping("/api/products/search")
public ResponseEntity<Page<Map<String, Object>>> searchProducts(
    @RequestParam(required = false, defaultValue = "code, name, productType.name:type, price")
    String select,
    @RequestParam(required = false) String filter,
    @RequestParam(required = false) String search,
    Pageable pageable
) {
    // Combine search with filter
    String finalFilter = filter;
    if (search != null && !search.isEmpty()) {
        String searchFilter = "name=like='*" + search + "*',code=like='*" + search + "*'";
        finalFilter = (filter != null)
            ? "(" + searchFilter + ");" + filter
            : searchFilter;
    }

    Page<Tuple> tuplePage = productService.getQueryService()
        .getTupleAsPageWithSelect(select, finalFilter, pageable);

    Page<Map<String, Object>> response = tuplePage.map(tuple -> {
        Map<String, Object> map = new HashMap<>();
        tuple.getElements().forEach(element ->
            map.put(element.getAlias(), tuple.get(element.getAlias()))
        );
        return map;
    });

    return ResponseEntity.ok(response);
}
```

**HTTP Requests:**
```http
# Basic search
GET /api/products/search?search=phone&filter=status==ACTIVE

# Custom SELECT fields
GET /api/products/search?select=code:id,name:label,price&filter=price=gt=100

# With pagination and sorting
GET /api/products/search?search=laptop&page=0&size=20&sort=price,desc
```

### Example 3: Sales Report with Multiple Dimensions

```java
@GetMapping("/api/reports/sales")
public ResponseEntity<Map<String, Object>> getSalesReport(
    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
) {
    RsqlQueryService queryService = orderService.getQueryService();

    String dateFilter = String.format(
        "orderDate=ge=#%s#;orderDate=le=#%s#",
        startDate, endDate
    );

    // Sales by product type
    List<Tuple> byType = queryService.getAggregateResult(
        "product.productType.name:category, " +
        "COUNT(*):orderCount, " +
        "SUM(totalAmount):revenue",
        dateFilter,
        Sort.by("revenue").descending()
    );

    // Sales by status
    List<Tuple> byStatus = queryService.getAggregateResult(
        "status, COUNT(*):count, SUM(totalAmount):total",
        dateFilter,
        null
    );

    // Daily sales trend
    List<Tuple> dailyTrend = queryService.getAggregateResult(
        "DATE(orderDate):date, COUNT(*):orders, SUM(totalAmount):revenue",
        dateFilter,
        Sort.by("date").ascending()
    );

    return ResponseEntity.ok(Map.of(
        "byCategory", byType,
        "byStatus", byStatus,
        "dailyTrend", dailyTrend
    ));
}
```

## Migration from String[] API

### Old String[] API

```java
// Old way - limited functionality
String[] fields = {"code", "name", "price"};
List<Tuple> results = queryService.getTuple(
    "status==ACTIVE",
    PageRequest.of(0, 20),
    fields
);

// Problems:
// - No aliases
// - No navigation properties (can't access productType.name)
// - Less readable
```

### New SELECT String API

```java
// New way - full functionality
String selectString = "code:productCode, name, productType.name:typeName, price";
List<Tuple> results = queryService.getTupleWithSelect(
    selectString,
    "status==ACTIVE",
    PageRequest.of(0, 20)
);

// Benefits:
// + Aliases for clear field names
// + Navigation properties work
// + More readable and maintainable
```

### Backward Compatibility

**The old `String[]` API still works!** It now internally delegates to the new SELECT API:

```java
// This still works and will continue to work
String[] fields = {"code", "name", "price"};
List<Tuple> results = queryService.getTuple(
    "status==ACTIVE",
    PageRequest.of(0, 20),
    fields
);

// Internally converted to: "code, name, price"
```

### Migration Strategy

1. **Keep using old API** for simple cases without aliases/navigation
2. **Gradually migrate** to new API when you need:
   - Field aliases
   - Navigation properties
   - Aggregate functions
3. **New code** should use the new SELECT string API

### Quick Migration Guide

| Old API | New API | Benefit |
|---------|---------|---------|
| `getTuple(filter, pageable, String[])` | `getTupleWithSelect(selectString, filter, pageable)` | Aliases, navigation |
| `getLOV(filter, pageable, id, code, name)` | `getLOVWithSelect(selectString, filter, pageable)` | Flexible field selection |
| `getResultAsMap(filter, pageable, String...)` | `getSelectResult(Tuple.class, selectString, filter, pageable)` | Type safety, aliases |
| N/A - Not possible before | `getAggregateResult(selectString, filter, pageable)` | Aggregate queries! |

## Converting Results to JSON

When building REST APIs, you often need to convert `Tuple` or `List<Tuple>` results to JSON format. The `TupleConverter` utility class provides an easy way to convert Tuple results to JSON-friendly `Map` objects.

### TupleConverter Overview

`TupleConverter` is a utility class that converts JPA Tuple results to `Map<String, Object>`, which can be automatically serialized to JSON by Spring Boot.

**Location:** `rsql.helper.TupleConverter`

### Basic Usage

```java
import rsql.helper.TupleConverter;

// Convert single Tuple to Map
Map<String, Object> map = TupleConverter.toMap(tuple);

// Convert List<Tuple> to List<Map>
List<Map<String, Object>> mapList = TupleConverter.toMapList(tuples);
```

### REST Controller Example

```java
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final RsqlQueryService<Product, ProductDTO, ProductRepository, ProductMapper> queryService;

    /**
     * Get product summary as JSON
     * Example: GET /api/products/summary?filter=status==ACTIVE&sort=name,asc
     */
    @GetMapping("/summary")
    public ResponseEntity<List<Map<String, Object>>> getProductSummary(
        @RequestParam(required = false) String filter,
        Pageable pageable
    ) {
        // Execute SELECT query
        List<Tuple> tuples = queryService.getTupleWithSelect(
            "code:id, name, price, productType.name:category",
            filter,
            pageable
        );

        // Convert to JSON-friendly format
        List<Map<String, Object>> results = TupleConverter.toMapList(tuples);

        return ResponseEntity.ok(results);
    }

    /**
     * Get aggregate statistics as JSON
     * Example: GET /api/products/stats?filter=status==ACTIVE
     */
    @GetMapping("/stats")
    public ResponseEntity<List<Map<String, Object>>> getProductStats(
        @RequestParam(required = false) String filter
    ) {
        List<Tuple> stats = queryService.getAggregateResult(
            "productType.name:category, COUNT(*):count, AVG(price):avgPrice, SUM(price):total",
            filter,
            PageRequest.unpaged()
        );

        return ResponseEntity.ok(TupleConverter.toMapList(stats));
    }
}
```

### JSON Response Examples

**Product Summary Response:**
```json
[
  {
    "id": "P001",
    "name": "Laptop",
    "price": 1000.00,
    "category": "Electronics"
  },
  {
    "id": "P002",
    "name": "Mouse",
    "price": 25.00,
    "category": "Electronics"
  }
]
```

**Aggregate Stats Response:**
```json
[
  {
    "category": "Electronics",
    "count": 15,
    "avgPrice": 450.50,
    "total": 6757.50
  },
  {
    "category": "Books",
    "count": 8,
    "avgPrice": 35.75,
    "total": 286.00
  }
]
```

### How It Works

1. **Preserves Aliases**: Keys in the Map are the aliases from your SELECT string
2. **Preserves Field Order**: Uses `LinkedHashMap` to maintain field order
3. **Handles Null Values**: Null values are included in the Map with null as the value
4. **Automatic JSON Serialization**: Spring Boot's Jackson automatically converts Maps to JSON

### Benefits

✅ **Simple API**: Two static methods for all use cases
✅ **JSON-Ready**: Works seamlessly with Spring Boot's JSON serialization
✅ **Type-Safe**: Returns strongly-typed `Map<String, Object>`
✅ **Preserves Structure**: Maintains field order and handles nulls correctly
✅ **No Dependencies**: Pure JPA and Java standard library

### Alternative: Custom DTOs

Instead of using `TupleConverter`, you can also map results to custom DTOs:

```java
public record ProductSummaryDTO(String code, String name, BigDecimal price, String category) {}

List<ProductSummaryDTO> summaries = queryService.getSelectResult(
    ProductSummaryDTO.class,
    "code, name, price, productType.name",  // Order must match constructor
    "status==ACTIVE",
    pageable
);
```

**When to use TupleConverter vs Custom DTOs:**
- **Use TupleConverter** when you want flexible, dynamic queries without creating DTO classes
- **Use Custom DTOs** when you want type safety and IDE autocomplete in your code

## Best Practices

1. **Use aliases** for clarity in REST APIs:
   ```java
   "id:productId, code:productCode, productType.name:categoryName"
   ```

2. **Keep SELECT strings in constants** for reusability:
   ```java
   public class ProductQueries {
       public static final String PRODUCT_SUMMARY =
           "code, name, productType.name:type, price";
       public static final String PRODUCT_STATS =
           "productType.name:category, COUNT(*):count, SUM(price):total";
   }
   ```

3. **Document expected result structure** in REST APIs:
   ```java
   /**
    * GET /api/products
    * @param select - SELECT string (default: "code, name, price")
    * Response fields: {code, name, price}
    */
   ```

4. **Validate SELECT strings** from user input to prevent errors

5. **Use typed DTOs** with `getSelectResult()` for complex projections:
   ```java
   record ProductView(String code, String name, String type, BigDecimal price) {}

   List<ProductView> products = queryService.getSelectResult(
       ProductView.class,
       "code, name, productType.name, price",
       filter,
       pageable
   );
   ```

## Pagination with Aggregate Queries

The `getAggregateResultAsPage()` method provides full pagination support for aggregate queries with proper metadata and sorting.

### Basic Pagination

```java
Page<Tuple> page = queryService.getAggregateResultAsPage(
    "productType.name:category, SUM(price):total, COUNT(*):count",
    "status==ACTIVE",
    null,  // No HAVING filter
    PageRequest.of(0, 10)  // First page, 10 items
);

// Access pagination metadata
long totalElements = page.getTotalElements();  // Total number of groups
int totalPages = page.getTotalPages();
int currentPage = page.getNumber();
int pageSize = page.getSize();
List<Tuple> results = page.getContent();
```

### Sorting by Aliases

You can sort by SELECT aliases (including aggregate functions and arithmetic expressions):

```java
// Sort by aggregate field (descending)
Page<Tuple> page = queryService.getAggregateResultAsPage(
    "category, SUM(price):total, AVG(price):avg",
    "",
    null,
    PageRequest.of(0, 20, Sort.by("total").descending())
);

// Sort by arithmetic expression alias
Page<Tuple> salesPage = queryService.getAggregateResultAsPage(
    "category, SUM(price) * 1.2:totalWithTax",
    "",
    null,
    PageRequest.of(0, 10, Sort.by("totalWithTax").ascending())
);

// Sort by GROUP BY field using alias
Page<Tuple> catPage = queryService.getAggregateResultAsPage(
    "productType.name:category, COUNT(*):count",
    "",
    null,
    PageRequest.of(0, 10, Sort.by("category"))
);

// Sort by GROUP BY field using field path (without alias)
Page<Tuple> catPage2 = queryService.getAggregateResultAsPage(
    "productType.name:category, COUNT(*):count",
    "",
    null,
    PageRequest.of(0, 10, Sort.by("productType.name"))  // Use field path directly
);
```

### Sorting Precedence

When resolving sort properties in aggregate queries, the library checks in the following order:

1. **Alias** - First checks if the sort property matches a SELECT alias (e.g., `totalDebit`, `totalWithTax`)
2. **Field Path** - Then checks if it matches a field path from a SELECT expression (e.g., `account.code`, `productType.name`)
3. **Entity Property** - Finally falls back to entity property path resolution (creates new JOIN if needed)

**Example:**
```java
// SELECT with aliases and field paths
String selectString = "account.code:accountCode, SUM(debit):totalDebit, SUM(credit):totalCredit";

// All three sorting approaches work:

// 1. Sort by alias
Sort.by("accountCode").ascending()          // Uses alias from SELECT

// 2. Sort by field path
Sort.by("account.code").ascending()         // Uses existing field path from SELECT

// 3. Sort by aggregate alias
Sort.by("totalDebit").descending()          // Uses aggregate function alias
```

**Benefits:**
- **No duplicate JOINs** - Uses existing JOINs from SELECT clause
- **Flexibility** - Sort by either alias or original field path
- **Backward Compatible** - Existing code with aliases continues to work

### Pagination with HAVING Filter

```java
// Only groups with total > 1000, paginated
Page<Tuple> page = queryService.getAggregateResultAsPage(
    "category, SUM(price):total, COUNT(*):count",
    "status==ACTIVE",
    "total=gt=1000;count=ge=5",  // HAVING filter
    PageRequest.of(0, 10, Sort.by("total").descending())
);

// totalElements will be the count AFTER HAVING filter is applied
```

### Count Behavior

The `Page.getTotalElements()` behaves differently based on the query type:

1. **No GROUP BY:** Returns total number of matching rows
   ```java
   // Returns count of individual products
   Page<Tuple> page = queryService.getAggregateResultAsPage(
       "SUM(price):total",  // No GROUP BY
       "status==ACTIVE",
       null,
       PageRequest.of(0, 10)
   );
   ```

2. **With GROUP BY:** Returns total number of groups
   ```java
   // Returns count of unique categories (number of groups)
   Page<Tuple> page = queryService.getAggregateResultAsPage(
       "category, SUM(price):total",  // GROUP BY category
       "",
       null,
       PageRequest.of(0, 10)
   );
   ```

3. **With HAVING:** Returns number of groups **after** HAVING filter
   ```java
   // Returns count of categories with total > 1000
   Page<Tuple> page = queryService.getAggregateResultAsPage(
       "category, SUM(price):total",
       "",
       "total=gt=1000",  // HAVING filter
       PageRequest.of(0, 10)
   );
   ```

### REST Controller Example

```java
@GetMapping("/api/sales-by-category")
public Page<Tuple> getSalesByCategory(
    @RequestParam(required = false) String filter,
    @RequestParam(required = false) String having,
    Pageable pageable
) {
    return queryService.getAggregateResultAsPage(
        "productType.name:category, SUM(price):total, COUNT(*):count",
        filter != null ? filter : "",
        having,
        pageable
    );
}
```

**REST API usage:**
```http
GET /api/sales-by-category?page=0&size=10&sort=total,desc
GET /api/sales-by-category?filter=status==ACTIVE&having=total=gt=1000&page=0&size=20
GET /api/sales-by-category?having=count=ge=5&sort=category,asc&page=1&size=10
```

### Key Features

- ✅ Sort by SELECT **aliases** (aggregate fields, arithmetic expressions)
- ✅ Sort by entity properties (GROUP BY fields)
- ✅ Proper `totalElements` calculation for GROUP BY queries
- ✅ Correct count with HAVING filters (counts filtered groups)
- ✅ Full `Page<T>` metadata (`totalPages`, `hasNext`, `hasPrevious`, etc.)

### Important Notes

1. **Sorting:** You can sort by:
   - **Alias** - Use the alias from SELECT (e.g., `Sort.by("totalDebit")`)
   - **Field Path** - Use the original field path (e.g., `Sort.by("account.code")`)
   - Both approaches use the same JPA Expression from SELECT (no duplicate JOINs)
2. **Count with HAVING:** The count reflects groups **after** HAVING filter is applied
3. **Performance:** For large datasets with GROUP BY + HAVING, the count query executes the full aggregation to get accurate totals

## Limitations

1. **Arithmetic expressions** only work in aggregate queries (`getAggregateResult()`) - not supported in `getTupleWithSelect()` or `getSelectResult()`
2. **Field-level arithmetic** without aggregates not supported (e.g., `price * 1.2` in non-aggregate query)
3. **Collection relationships** (`@OneToMany`, `@ManyToMany`) require custom JPQL
4. **Aggregate functions** cannot be nested (e.g., `SUM(AVG(field))` not supported)
5. **String operations** in arithmetic not supported (only numeric operations)
6. **Result class constructor** must match SELECT field order and types

## See Also

- [README.md](README.md) - General RSQL Filter documentation
- [API.md](API.md) - Complete API reference
- [CLAUDE.md](CLAUDE.md) - Development guide
