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
  - [GROUP BY fields in HAVING](#group-by-fields-in-having)
  - [Reserved words](#reserved-words)
- [Combining WHERE and HAVING](#combining-where-and-having)
- [Complete Examples](#complete-examples)
- [REST API Usage](#rest-api-usage)
- [Best Practices](#best-practices)
- [Limitations](#limitations)
- [Errors](#errors)
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
WHERE → GROUP BY → HAVING → SELECT → ORDER BY
```

SELECT projections are evaluated after HAVING, which is why a HAVING condition may reference a
SELECT alias — the alias is pre-built into an expression before the clause is compiled.

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

HAVING supports the RSQL comparison operators listed below:

| Operator | Description | Example |
|----------|-------------|---------|
| `==` | Equal to | `COUNT(*)==5` |
| `!=` or `=!` | Not equal to | `SUM(price)!=1000` |
| `=gt=` | Greater than | `AVG(quantity)=gt=10` |
| `=ge=` | Greater or equal | `COUNT(*)=ge=3` |
| `=lt=` | Less than | `MAX(price)=lt=1000` |
| `=le=` | Less or equal | `MIN(quantity)=le=5` |
| `=*` or `=like=` | Like | `MAX(code)=like='ABC%'` |
| `=!*`, `!=*` or `=nlike=` | Not like | `MAX(code)=nlike='ABC%'` |

Every operator above compares an expression with a **literal**. Putting an expression on *both* sides — two
aggregates, or two SELECT aliases — works with `==` and `!=` only; the four ordering operators `=gt=`,
`=ge=`, `=lt=` and `=le=` parse and then throw. See
[Limitations §4, Expression Comparison](#4-expression-comparison).

> **Note — HAVING `like` differs from WHERE `like`.** It does **not** map `*` to `%` (write `%` yourself)
> and does **not** wrap the expression in `lower(...)`, so matching is case-sensitive. Since 0.6.20
> the pattern is backslash-escaped and carries `ESCAPE '\'`, so a backslash matches literally - but `*`
> remains an ordinary character here.
>
> The WHERE-only case-sensitive operators `=clike=` / `=^*` and `=cnlike=` / `=!^*` / `!=^*` do **not**
> exist in HAVING and raise `SyntaxErrorException`. They are unnecessary here: HAVING's `=like=` is
> already case-sensitive.

### String literals

String, number, date, datetime and `TRUE`/`FALSE` literals are written exactly as in `WHERE` — the lexer is
shared. Pick a delimiter (`"`, `'` or `` ` ``) and escape it inside the value by **doubling it**; a backslash
has no special meaning and is passed through unchanged, so a value may also end with one:

```
MAX(code)=="say ""hi"""     -> value: say "hi"
MAX(code)=="C:\dir\"        -> value: C:\dir\
```

> **Changed in 0.6.19.** A backslash immediately before the active delimiter used to protect it and
> is now a syntax error; rewrite by doubling the delimiter. Conditions must also be separated by an explicit
> logical operator (`;` or `,`) — juxtaposed conditions used to be accepted, keeping only the last one.

> **Note — two WHERE literal forms are missing here.** Enum literals (`#ACTIVE#`) and named parameters
> (`:param`) are **not** accepted in HAVING; both raise `SyntaxErrorException`. Compare a grouped enum
> field with a quoted string instead (`status=='ACTIVE'`).

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

> **Note.** In HAVING, `COUNT(*)` compiles to `count(<root>.id)` — the identifier attribute is hard-coded
> to `id`. On an entity whose identifier is named something else this throws
> `IllegalArgumentException: Unknown property: id from entity <Entity>`; write `COUNT(theIdField)` there.

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

**COUNT(DIST field1, field2)** - parses in SELECT, **rejected in HAVING**. JPA Criteria's `countDistinct`
takes a single expression, so the multi-field form throws
`SyntaxErrorException: COUNT(DIST ...) supports only one field in HAVING clause. Found: 2 fields`.
Note that it does not count distinct *combinations* in SELECT either — it expands to one
`count(distinct ...)` selection per field, and if you attach an alias only the first field survives it.
Use one `COUNT(DIST field)` per field and filter each on its own alias:
```java
// ❌ Rejected - multi-field COUNT(DIST) in HAVING
HAVING: "COUNT(DIST type, size)=ge=5"

// ✅ Categories with at least 5 distinct types and 5 distinct sizes
SELECT: "category, COUNT(DIST type):typeCount, COUNT(DIST size):sizeCount"
HAVING: "typeCount=ge=5;sizeCount=ge=5"
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

> **Note.** `GRP` belongs in SELECT. In HAVING it is passed through unvalidated — a field that is not in
> GROUP BY escapes the library's own check and fails in the database instead
> (`SQLGrammarException: Column "…" must be in the GROUP BY list`). Write the bare field name in HAVING,
> which produces the clear `IllegalArgumentException` described under
> [GROUP BY fields in HAVING](#group-by-fields-in-having).

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
    "status==#ACTIVE#",
    "totalSales=gt=50000;avgPrice=bt=(100,500);productCount=ge=10",
    pageable
);
```

> **`getAggregateResult` vs `getAggregateResultAsPage`.** Both take the same four arguments
> `(selectString, filter, havingFilter, pageable)`, but they use the `Pageable` differently:
> `getAggregateResult` returns **all** matching groups — it reads only the `Sort` and ignores page number
> and page size — and it resolves that `Sort` against the entity root, so it **cannot** sort by a SELECT
> alias (`PropertyReferenceException: No property 'totalSales' found for type 'Product'`).
> `getAggregateResultAsPage` applies a real offset and limit, counts `totalElements` after HAVING, and
> resolves each sort property as SELECT alias → SELECT field path → entity path. Use it whenever you sort
> by an alias or need paging. Note that `Sort` is not a `Pageable`: wrap it as
> `PageRequest.of(0, 20, Sort.by("totalSales").descending())`.

### GROUP BY fields in HAVING

The GROUP BY list is derived from the SELECT string: **every SELECT element without an aggregate function
becomes a GROUP BY expression**. In `"productType.name:category, SUM(price):totalSales"` the GROUP BY list
is therefore `[productType.name]`.

A bare (non-aggregate) field in HAVING must be either a SELECT alias or one of those derived GROUP BY
fields. Anything else raises `java.lang.IllegalArgumentException` — not `SyntaxErrorException` — naming
both sets:

```
Field 'status' must be in GROUP BY or be a SELECT alias. Current GROUP BY fields: [productType.name].
Available SELECT aliases: [totalSales, category]
```

A grouped element can be named either way: the alias is looked up first, then the field path, so both
`category` and `productType.name` are accepted above. Comparing a grouped field with a literal is legal and
useful — it is the only way to filter a grouped enum column, since `#ENUM#` literals are not accepted in
HAVING:

```java
SELECT: "status:st, COUNT(*):productCount"
HAVING: "status=='ACTIVE';productCount=gt=1"
```

### Reserved words

The HAVING lexer turns these words into keywords, so none of them can be a referenced SELECT alias, a bare
field name, or the argument of an aggregate: `count`, `avg`, `sum`, `min`, `max`, `grp`, `all`, `dist`,
`and`, `or`, `null`, `true`, `false` (matching is case-insensitive). `COUNT(*):count` is accepted in SELECT,
but the HAVING condition `count=gt=5` then fails with
`SyntaxErrorException: Syntax error in HAVING clause at position 5: no viable alternative at input 'count='`,
and `SUM(count)` fails the same way. Only the **first** path segment is affected — `order.count=gt=1` parses,
because `.count` is a single token — and only an exact match, so `myCount` and `productCount` are fine.

```java
// ❌ Alias collides with a HAVING keyword
SELECT: "category, COUNT(*):count, AVG(price):avg"
HAVING: "count=gt=5;avg=lt=200"

// ✅ Rename the alias
SELECT: "category, COUNT(*):productCount, AVG(price):avgPrice"
HAVING: "productCount=gt=5;avgPrice=lt=200"
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

Page<Tuple> categories = queryService.getAggregateResultAsPage(
    "productType.name:category, SUM(price):totalSales, COUNT(*):productCount",
    "status==#ACTIVE#",  // WHERE - filters individual products
    "totalSales=gt=10000;productCount=ge=5",  // HAVING - filters grouped results
    PageRequest.of(0, 100, Sort.by("totalSales").descending())
);

for (Tuple row : categories) {
    String category = (String) row.get("category");
    BigDecimal sales = (BigDecimal) row.get("totalSales");
    Long count = (Long) row.get("productCount");

    System.out.printf("%s: %d products, $%s total%n",
        category, count, sales);
}
```

The alias `count` would have been a syntax error in HAVING (see [Reserved words](#reserved-words)), and
sorting by `totalSales` requires `getAggregateResultAsPage` rather than `getAggregateResult`.

### Performance Tip
- Use **WHERE** to reduce data before grouping
- Use **HAVING** only for aggregate conditions and for the fields you grouped by

A row filter on a field you did *not* group by is not merely slower in HAVING — it is rejected with
`IllegalArgumentException`, because such a field is neither a GROUP BY field nor a SELECT alias:

```java
// ❌ REJECTED - 'status' is not in GROUP BY and not a SELECT alias
SELECT: "productType.name:category, COUNT(*):productCount"
HAVING: "status=='ACTIVE';COUNT(*)=gt=5"

// ✅ GOOD - use WHERE for regular fields, HAVING for aggregates
WHERE:  "status=='ACTIVE'"
HAVING: "COUNT(*)=gt=5"
```

## Complete Examples

All four examples use `getAggregateResultAsPage`. Three of them build a sized `PageRequest` on the spot
(Examples 1, 2 and 3); Example 4 passes a caller-supplied `pageable` straight through. Two of them sort by a
SELECT alias — Example 1 on `totalRevenue`, Example 3 on `totalValue` — while Examples 2 and 4 pass no `Sort`
at all. `getAggregateResult` is the wrong method for all four: it ignores page number and page size, and it
would additionally reject those two alias sorts.

### Example 1: Top Selling Categories
```java
// Find categories with high sales volume
Page<Tuple> topCategories = queryService.getAggregateResultAsPage(
    "category.name:categoryName, " +
    "COUNT(*):productCount, " +
    "SUM(price):totalRevenue, " +
    "AVG(price):avgPrice",

    "status==#ACTIVE#;soldDate=ge=#2024-01-01#",  // WHERE

    "productCount=ge=10;totalRevenue=gt=100000",  // HAVING

    PageRequest.of(0, 10, Sort.by("totalRevenue").descending())
);
```

### Example 2: Quality Control Analysis
```java
// Find suppliers with quality issues
Page<Tuple> problemSuppliers = queryService.getAggregateResultAsPage(
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
Page<Tuple> slowMoving = queryService.getAggregateResultAsPage(
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
Page<Tuple> vipSegments = queryService.getAggregateResultAsPage(
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
public ResponseEntity<Page<Tuple>> getProductStats(
    @RequestParam(required = false) String filter,
    @RequestParam(required = false) String having,
    Pageable pageable
) {
    // aliases must avoid the HAVING keywords: count, avg, sum, min, max, grp, ...
    String selectString = "productType.name:category, " +
                         "COUNT(*):productCount, " +
                         "SUM(price):total, " +
                         "AVG(price):avgPrice";

    // getAggregateResultAsPage honours the request's page size and can sort by a SELECT alias;
    // getAggregateResult would return every group regardless of the page size
    Page<Tuple> stats = productService.getQueryService()
        .getAggregateResultAsPage(selectString, filter, having, pageable);

    return ResponseEntity.ok(stats);
}
```

A missing `having` parameter is not an error: a `null` or blank HAVING filter simply omits the clause.

### HTTP Request Examples

**Simple HAVING:**
```http
GET /api/product-stats?having=COUNT(*)=gt=5
```

**With WHERE and HAVING:**
```http
GET /api/product-stats?filter=status=='ACTIVE'&having=total=gt=10000
```

**Complex HAVING:**
```http
GET /api/product-stats?having=(productCount=ge=10,total=gt=50000);avgPrice=bt=(100,500)
```

**With Sorting:**
```http
GET /api/product-stats?having=productCount=gt=5&sort=total,desc&sort=category,asc
```

### URL Encoding

Remember to URL-encode special characters — `(`, `)`, `,`, `;` and `=`:

```http
# Before encoding
having=(COUNT(*)=gt=10,SUM(price)=gt=5000);AVG(price)=lt=200

# After encoding
having=%28COUNT%28*%29%3Dgt%3D10%2CSUM%28price%29%3Dgt%3D5000%29%3BAVG%28price%29%3Dlt%3D200
```

## Best Practices

### 1. Put Arithmetic Around Aggregates, Not Inside Them
Arithmetic inside an aggregate's argument does not parse in either grammar — `SUM(price*quantity)` is a
syntax error in SELECT and in HAVING alike (`no viable alternative at input 'SUM(price*'`). Arithmetic
between aggregates is supported, and the resulting alias is then usable in HAVING:

```java
// ❌ Syntax error - arithmetic inside the aggregate call
HAVING: "SUM(lineItems.price*lineItems.quantity)=gt=10000"

// ✅ Clear and maintainable - arithmetic around the aggregates
SELECT: "category, SUM(price) * 1.2:totalWithTax, SUM(debit) - SUM(credit):balance"
HAVING: "totalWithTax=gt=10000;balance=gt=0"
```

There is no rewrite of the ❌ line: a per-row product cannot be expressed inside an aggregate at all.
Persist the line total as a column and `SUM` that column instead.

A SELECT string containing `+`, `-`, `*` or `/` is only accepted by
`getAggregateResultWithExpressions(...)` and `getAggregateResultAsPageWithExpressions(...)`.
`getAggregateResult` and `getAggregateResultAsPage` reject it with `SyntaxErrorException`:

```
Arithmetic expressions with operators are not supported in this query type.
Use SelectExpressionVisitor for queries with arithmetic expressions.
```

```java
Page<Tuple> balances = queryService.getAggregateResultAsPageWithExpressions(
    "account.number:accountNumber, SUM(debit) - SUM(credit):balance",
    "year==2024",              // WHERE
    "balance=gt=0",            // HAVING - on the arithmetic alias
    PageRequest.of(0, 20, Sort.by("balance").descending())
);
```

### 2. WHERE Before HAVING
```java
// ❌ Rejected - 'status' is neither a GROUP BY field nor a SELECT alias
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
SELECT: "category, COUNT(*):productCount"
HAVING: "total=gt=1000"  // 'total' alias doesn't exist

// ✅ Works
SELECT: "category, COUNT(*):productCount, SUM(price):total"
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
HAVING can compare two expressions with `==` and `!=` only. The ordering operators `=gt=`, `=ge=`, `=lt=`
and `=le=` parse, but throw when the right-hand side is another expression rather than a literal — a
`ClassCastException` inside the compiler, surfaced as `SyntaxErrorException`:

```
Unexpected error while compiling HAVING clause 'SUM(debit)=gt=SUM(credit)': class
org.hibernate.query.sqm.function.SelfRenderingSqmAggregateFunction cannot be cast to class
java.lang.Comparable
```

```java
// ✅ Supported - equality between two expressions
HAVING: "SUM(debit)==SUM(credit)"

// ✅ Supported - and the same via aliases
SELECT: "SUM(debit):totalDebit, SUM(credit):totalCredit"
HAVING: "totalDebit!=totalCredit"

// ❌ Throws - ordering operator between two expressions
HAVING: "SUM(debit)=gt=SUM(credit)"
HAVING: "totalDebit=gt=totalCredit"
```

Compare against a literal instead, or compute the difference as an arithmetic SELECT expression and filter
on its alias — `SELECT: "SUM(debit) - SUM(credit):balance"` with `HAVING: "balance=gt=0"`, through
`getAggregateResultWithExpressions` / `getAggregateResultAsPageWithExpressions`.

### 5. DIST and ALL on Non-COUNT Aggregates
The grammar accepts `DIST` and `ALL` after `AVG`, `SUM`, `MIN`, `MAX` and `GRP`, but the HAVING visitor
never reads the modifier — it is silently ignored. Only `COUNT(DIST field)` actually applies DISTINCT:
```java
// ❌ Parses, but is identical to AVG(price) - no DISTINCT is applied
HAVING: "AVG(DIST price)=gt=100"
```

### 6. Depth Limits
Since 0.6.21 a HAVING clause may nest at most **100** levels of parentheses and produce a parse tree at
most **500** levels deep; over either limit the compiler raises `SyntaxErrorException`
(`HAVING clause is nested too deeply at position N …`, `HAVING clause is structured too deeply …`) rather
than crashing with `StackOverflowError`. An aggregate call's own parentheses count towards the nesting
limit, so 99 levels of grouping around `SUM(price)=gt=1` still parse and 100 do not.

No single condition count belongs to the tree-depth limit: the leaves spend the same 500-level budget as the
`;` and `,` nodes above them, so how long a flat chain may be depends on the shape of its conditions.
Measured on this branch, a `;`-chain accepts 492 conditions of `SUM(price)=gt=1`, 494 of `COUNT(*)=gt=1` and
495 of the bare-field `total=gt=1`. Read that as "a few hundred conditions" rather than as a number to design
against; a machine-generated clause anywhere near it should be split instead.

Both limits are global and shared with WHERE and SELECT, and both are settable
at runtime: `RsqlWhereTreeParser.setMaxNestingDepth(int)` / `setMaxTreeDepth(int)` — raising them past the
measured overflow point trades the exception back for a `StackOverflowError`.

## Errors

HAVING raises two distinct exception types, and a REST layer must be prepared for both — catching only
`SyntaxErrorException` still lets a bad field name escape as a 500:

| Exception | Raised for | Example message |
|-----------|------------|-----------------|
| `rsql.exceptions.SyntaxErrorException` | anything the grammar or the visitor rejects — bad syntax, depth limits, nested aggregates, multi-field `COUNT(DIST …)` | `Syntax error in HAVING clause at position 5: no viable alternative at input 'count='` |
| `java.lang.IllegalArgumentException` | a field that is neither a GROUP BY field nor a SELECT alias, or an unknown property | `Field 'status' must be in GROUP BY or be a SELECT alias. Current GROUP BY fields: [productType.name]. Available SELECT aliases: [totalSales, category]` |

A `null` or blank HAVING filter is **not** an error: the compiler returns no predicate and the clause is
simply omitted from the query.

## See Also

- [SELECT.md](SELECT.md) - Complete SELECT query documentation
- [README.md](README.md) - Library overview and quick start
- [API.md](API.md) - Full API reference
- [RsqlQueryServiceExpressionIT](rsql-filter-integration-tests/src/test/java/com/nomendi6/rsql/it/RsqlQueryServiceExpressionIT.java) -
  working examples on the API this page teaches: `getAggregateResultWithExpressions` and
  `getAggregateResultAsPageWithExpressions` called with a SELECT string, a WHERE filter and a HAVING filter
- [HavingClauseIT](rsql-filter-integration-tests/src/test/java/com/nomendi6/rsql/it/HavingClauseIT.java) -
  the same HAVING filter strings compiled through a **different, lower-level route**: every test calls
  `SimpleQueryExecutor.getAggregateQueryResult(entityClass, resultClass, List<AggregateField> selectFields,
  List<String> groupByFields, filter, having, pageable, rsqlContext, compiler)`, which takes the SELECT
  elements and the GROUP BY list already parsed instead of deriving them from a SELECT string. The HAVING
  syntax and its alias rules are identical; only the surrounding call differs. That route, and
  `AggregateQueryBuilder.createHavingPredicate(...)` alongside it, is documented in
  [API.md](API.md#simplequeryexecutor)
