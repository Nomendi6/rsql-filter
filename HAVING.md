# RSQL Filter - HAVING Clause Guide

This document provides comprehensive documentation for HAVING clause functionality in the RSQL Filter library.

## Table of Contents
- [Overview](#overview)
- [HAVING Syntax](#having-syntax)
  - [Comparison Operators](#comparison-operators)
  - [Logical Operators](#logical-operators)
  - [Special Operators](#special-operators)
- [Supported Aggregate Functions](#supported-aggregate-functions)
  - [COUNT Function](#count-function)
  - [SUM, AVG, MIN, MAX Functions](#sum-avg-min-max-functions)
  - [GRP Function](#grp-function)
- [Working with Aliases](#working-with-aliases)
- [Combining WHERE and HAVING](#combining-where-and-having)
- [Complete Examples](#complete-examples)
- [REST API Usage](#rest-api-usage)
- [Best Practices](#best-practices)
- [Limitations](#limitations)
- [See Also](#see-also)

## Overview

The HAVING clause is used to filter results of aggregate queries based on aggregate function results. It works similarly to WHERE, but operates on grouped/aggregated data rather than individual rows.

### Key Differences: WHERE vs HAVING

| Aspect | WHERE | HAVING |
|--------|-------|--------|
| **When Applied** | Before aggregation | After aggregation |
| **Filters** | Individual rows | Grouped results |
| **Can Use** | Regular fields | Aggregate functions + GROUP BY fields |
| **Example** | `price=gt=100` | `SUM(price)=gt=1000` |

### Execution Order

```
SELECT → WHERE → GROUP BY → HAVING → ORDER BY
```

### Why Use HAVING?

HAVING allows you to filter aggregated results, which is impossible with WHERE alone:

```java
// ❌ This won't work - WHERE can't use aggregate functions
"COUNT(*) > 5"  // ERROR

// ✅ This works - HAVING can filter aggregated results
HAVING: "COUNT(*)=gt=5"
```

## HAVING Syntax

### Comparison Operators

HAVING supports all standard RSQL comparison operators:

| Operator | Description | Example |
|----------|-------------|---------|
| `==` | Equal to | `COUNT(*)==5` |
| `!=` or `=!` | Not equal to | `SUM(price)!=1000` |
| `=gt=` | Greater than | `AVG(quantity)=gt=10` |
| `=ge=` | Greater or equal | `COUNT(*)=ge=3` |
| `=lt=` | Less than | `MAX(price)=lt=1000` |
| `=le=` | Less or equal | `MIN(quantity)=le=5` |

**Examples:**
```java
// Products by category with at least 5 items
HAVING: "COUNT(*)=ge=5"

// Categories with total sales over $10,000
HAVING: "SUM(price)=gt=10000"

// Product types with average price under $100
HAVING: "AVG(price)=lt=100"
```

### Logical Operators

Combine multiple HAVING conditions:

| Operator | Description | Example |
|----------|-------------|---------|
| `;` or `AND` | Logical AND | `COUNT(*)=gt=5;SUM(price)=gt=1000` |
| `,` or `OR` | Logical OR | `COUNT(*)==1,AVG(price)=gt=500` |
| `( )` | Grouping | `(COUNT(*)=gt=10,SUM(price)=gt=5000);AVG(price)=lt=200` |

**Examples:**
```java
// AND condition (semicolon syntax)
HAVING: "SUM(price)=gt=1000;COUNT(*)=ge=3"

// AND condition (keyword syntax)
HAVING: "SUM(price)=gt=1000 AND COUNT(*)=ge=3"

// OR condition (comma syntax)
HAVING: "COUNT(*)==1,AVG(price)=gt=500"

// OR condition (keyword syntax)
HAVING: "COUNT(*)==1 OR AVG(price)=gt=500"

// Complex with parentheses
HAVING: "(SUM(price)=gt=5000,COUNT(*)=gt=100);AVG(price)=le=200"
```

### Special Operators

#### BETWEEN
```java
// Average price between 50 and 150
HAVING: "AVG(price)=bt=(50,150)"

// Count between 1 and 10
HAVING: "COUNT(*)=bt=(1,10)"
```

#### NOT BETWEEN
```java
// Total price NOT between 100 and 500
HAVING: "SUM(price)=nbt=(100,500)"
```

#### IN
```java
// Count in specific values
HAVING: "COUNT(*)=in=(1,2,5,10)"
```

#### NOT IN
```java
// Sum not in specific values
HAVING: "SUM(quantity)=nin=(100,200,300)"
```

#### NULL, TRUE, FALSE
```java
// Count of descriptions is null
HAVING: "COUNT(description)==NULL"

// Boolean aggregate result
HAVING: "MAX(active)==TRUE"
```

## Supported Aggregate Functions

### COUNT Function

**COUNT(*)** - Count all rows in group:
```java
// Categories with more than 10 products
SELECT: "category:cat, COUNT(*):total"
HAVING: "COUNT(*)=gt=10"
```

**COUNT(field)** - Count non-null values:
```java
// Categories where at least 5 products have descriptions
SELECT: "category, COUNT(description):withDesc"
HAVING: "COUNT(description)=ge=5"
```

**COUNT(DIST field)** - Count distinct values:
```java
// Categories with products from at least 3 different suppliers
SELECT: "category, COUNT(DIST supplier.id):supplierCount"
HAVING: "COUNT(DIST supplier.id)=ge=3"
```

**COUNT(DIST field1, field2)** - Count distinct combinations:
```java
// Categories with at least 5 unique type-size combinations
SELECT: "category, COUNT(DIST type, size):combinations"
HAVING: "COUNT(DIST type, size)=ge=5"
```

### SUM, AVG, MIN, MAX Functions

**SUM(field)** - Sum of values:
```java
// Categories with total sales over $50,000
SELECT: "category, SUM(price):totalSales"
HAVING: "SUM(price)=gt=50000"
```

**AVG(field)** - Average of values:
```java
// Categories with average price between $100-$500
SELECT: "category, AVG(price):avgPrice"
HAVING: "AVG(price)=bt=(100,500)"
```

**MIN(field)** - Minimum value:
```java
// Categories where cheapest product is at least $50
SELECT: "category, MIN(price):minPrice"
HAVING: "MIN(price)=ge=50"
```

**MAX(field)** - Maximum value:
```java
// Categories where most expensive product is under $1000
SELECT: "category, MAX(price):maxPrice"
HAVING: "MAX(price)=lt=1000"
```

### GRP Function

**GRP(field)** - Explicit GROUP BY field (no aggregation):
```java
SELECT: "GRP(category):cat, COUNT(*):total"
HAVING: "COUNT(*)=gt=5"
```

## Working with Aliases

You can reference SELECT aliases in HAVING instead of repeating aggregate functions:

### Using Aggregate Functions Directly
```java
SELECT: "category, SUM(price):totalSales, COUNT(*):productCount"
HAVING: "SUM(price)=gt=10000;COUNT(*)=ge=5"
```

### Using Aliases (Recommended)
```java
SELECT: "category, SUM(price):totalSales, COUNT(*):productCount"
HAVING: "totalSales=gt=10000;productCount=ge=5"
```

### Benefits of Using Aliases
- **Cleaner syntax** - shorter, more readable
- **Consistency** - same names in SELECT and HAVING
- **Maintainability** - change aggregate function in one place

**Example:**
```java
List<Tuple> stats = queryService.getAggregateResult(
    "productType.name:category, " +
    "SUM(price):totalSales, " +
    "AVG(price):avgPrice, " +
    "COUNT(*):productCount",
    "status==ACTIVE",
    "totalSales=gt=50000;avgPrice=bt=(100,500);productCount=ge=10",
    pageable
);
```

## Combining WHERE and HAVING

WHERE and HAVING work together to provide powerful filtering:

### Execution Flow
```
1. WHERE filters individual rows
2. GROUP BY groups the filtered rows
3. Aggregate functions calculate group results
4. HAVING filters the grouped results
```

### Example: Sales Analysis

```java
// Find product categories where:
// - Products are ACTIVE (WHERE)
// - Category has total sales > $10,000 (HAVING)
// - Category has at least 5 products (HAVING)

List<Tuple> categories = queryService.getAggregateResult(
    "productType.name:category, SUM(price):totalSales, COUNT(*):count",
    "status==ACTIVE",  // WHERE - filters individual products
    "totalSales=gt=10000;count=ge=5",  // HAVING - filters grouped results
    PageRequest.of(0, 100, Sort.by("totalSales").descending())
);

for (Tuple row : categories) {
    String category = (String) row.get("category");
    BigDecimal sales = (BigDecimal) row.get("totalSales");
    Long count = (Long) row.get("count");

    System.out.printf("%s: %d products, $%s total%n",
        category, count, sales);
}
```

### Performance Tip
- Use **WHERE** to reduce data before grouping
- Use **HAVING** only for aggregate conditions
- Don't use HAVING for non-aggregate conditions

```java
// ❌ BAD - filtering non-aggregate field in HAVING
HAVING: "status=='ACTIVE';COUNT(*)=gt=5"

// ✅ GOOD - use WHERE for regular fields, HAVING for aggregates
WHERE:  "status=='ACTIVE'"
HAVING: "COUNT(*)=gt=5"
```

## Complete Examples

### Example 1: Top Selling Categories
```java
// Find categories with high sales volume
List<Tuple> topCategories = queryService.getAggregateResult(
    "category.name:categoryName, " +
    "COUNT(*):productCount, " +
    "SUM(price):totalRevenue, " +
    "AVG(price):avgPrice",

    "status==ACTIVE;soldDate=ge=#2024-01-01#",  // WHERE

    "productCount=ge=10;totalRevenue=gt=100000",  // HAVING

    PageRequest.of(0, 10, Sort.by("totalRevenue").descending())
);
```

### Example 2: Quality Control Analysis
```java
// Find suppliers with quality issues
List<Tuple> problemSuppliers = queryService.getAggregateResult(
    "supplier.name:supplierName, " +
    "COUNT(*):totalProducts, " +
    "COUNT(DIST productType):productTypes, " +
    "AVG(defectRate):avgDefectRate",

    "active==true",  // WHERE

    "(totalProducts=gt=100;avgDefectRate=gt=5),productTypes=lt=3",  // HAVING

    PageRequest.of(0, 20)
);
```

### Example 3: Inventory Optimization
```java
// Find slow-moving product categories
List<Tuple> slowMoving = queryService.getAggregateResult(
    "category:cat, " +
    "AVG(monthsSinceLastSale):avgMonthsIdle, " +
    "SUM(inventoryValue):totalValue, " +
    "COUNT(*):itemCount",

    "warehouse.location=='US-WEST'",  // WHERE

    "avgMonthsIdle=gt=6;totalValue=gt=10000;itemCount=ge=5",  // HAVING

    PageRequest.of(0, 50, Sort.by("totalValue").descending())
);
```

### Example 4: Customer Segmentation
```java
// Find high-value customer segments
List<Tuple> vipSegments = queryService.getAggregateResult(
    "customerSegment:segment, " +
    "COUNT(DIST customer.id):customerCount, " +
    "SUM(orderTotal):totalSpent, " +
    "AVG(orderTotal):avgOrderValue",

    "orderDate=ge=#2024-01-01#;status==#COMPLETED#",  // WHERE

    "(customerCount=gt=100;totalSpent=gt=1000000),avgOrderValue=gt=500",  // HAVING

    pageable
);
```

## REST API Usage

### Basic REST Endpoint

```java
@GetMapping("/api/product-stats")
public ResponseEntity<List<Tuple>> getProductStats(
    @RequestParam(required = false) String filter,
    @RequestParam(required = false) String having,
    Pageable pageable
) {
    String selectString = "productType.name:category, " +
                         "COUNT(*):count, " +
                         "SUM(price):total, " +
                         "AVG(price):avg";

    List<Tuple> stats = productService.getQueryService()
        .getAggregateResult(selectString, filter, having, pageable);

    return ResponseEntity.ok(stats);
}
```

### HTTP Request Examples

**Simple HAVING:**
```http
GET /api/product-stats?having=COUNT(*)=gt=5
```

**With WHERE and HAVING:**
```http
GET /api/product-stats?filter=status==ACTIVE&having=total=gt=10000
```

**Complex HAVING:**
```http
GET /api/product-stats?having=(count=ge=10,total=gt=50000);avg=bt=(100,500)
```

**With Sorting:**
```http
GET /api/product-stats?having=count=gt=5&sort=total,desc&sort=category,asc
```

### URL Encoding

Remember to URL-encode special characters:

```http
# Before encoding
having=(COUNT(*)>10,SUM(price)>5000);AVG(price)<200

# After encoding
having=%28COUNT%28%2A%29%3E10%2CSUM%28price%29%3E5000%29%3BAVG%28price%29%3C200
```

## Best Practices

### 1. Use Aliases for Readability
```java
// ❌ Hard to read
HAVING: "SUM(lineItems.price*lineItems.quantity)=gt=10000"

// ✅ Clear and maintainable
SELECT: "SUM(lineItems.price*lineItems.quantity):totalRevenue"
HAVING: "totalRevenue=gt=10000"
```

### 2. WHERE Before HAVING
```java
// ❌ Less efficient
HAVING: "status=='ACTIVE';COUNT(*)=gt=5"

// ✅ Filter early with WHERE
WHERE:  "status=='ACTIVE'"
HAVING: "COUNT(*)=gt=5"
```

### 3. Use Appropriate Aggregates
```java
// For counting distinct values
SELECT: "category, COUNT(DIST supplier.id):uniqueSuppliers"
HAVING: "uniqueSuppliers=ge=3"

// For totals
SELECT: "category, SUM(quantity):totalQty"
HAVING: "totalQty=gt=1000"
```

### 4. Combine Related Conditions
```java
// ❌ Multiple similar conditions
HAVING: "COUNT(*)=gt=5,COUNT(*)=lt=100"

// ✅ Use BETWEEN
HAVING: "COUNT(*)=bt=(5,100)"
```

### 5. Test Performance
- Monitor query execution time
- Add database indexes on GROUP BY fields
- Consider materialized views for complex aggregations

## Limitations

### 1. Alias Scope
Aliases must be defined in SELECT to be used in HAVING:
```java
// ❌ Won't work - alias not in SELECT
SELECT: "category, COUNT(*):count"
HAVING: "total=gt=1000"  // 'total' alias doesn't exist

// ✅ Works
SELECT: "category, COUNT(*):count, SUM(price):total"
HAVING: "total=gt=1000"
```

### 2. Nested Aggregates Not Supported
```java
// ❌ Not supported
HAVING: "SUM(AVG(price))=gt=100"

// ✅ Alternative approach
SELECT: "category, AVG(price):avgPrice"
HAVING: "avgPrice=gt=100"
```

### 3. Subqueries in HAVING
Currently, HAVING doesn't support subqueries:
```java
// ❌ Not supported
HAVING: "COUNT(*)>(SELECT COUNT(*) FROM ...)"
```

### 4. Expression Comparison
HAVING supports comparing two aggregate expressions:
```java
// ✅ Supported
HAVING: "SUM(debit)=gt=SUM(credit)"

// ✅ Supported
SELECT: "SUM(debit):totalDebit, SUM(credit):totalCredit"
HAVING: "totalDebit=gt=totalCredit"
```

## See Also

- [SELECT.md](SELECT.md) - Complete SELECT query documentation
- [README.md](README.md) - Library overview and quick start
- [API.md](API.md) - Full API reference
- [Integration Tests](rsql-filter-integration-tests/src/test/java/com/nomendi6/rsql/it/HavingClauseIT.java) - Working examples
