# RSQL Filter API Documentation

This document provides detailed information about all the methods available in the RSQL Filter library.

## Table of Contents
- [RsqlQueryService](#rsqlqueryservice)
  - [Constructor Methods](#constructor-methods)
  - [Basic Query Methods](#basic-query-methods)
  - [Paginated Query Methods](#paginated-query-methods)
  - [LOV (List of Values) Methods](#lov-list-of-values-methods)
  - [SELECT Query Methods](#select-query-methods)
  - [JPQL Query Methods](#jpql-query-methods)
  - [Utility Methods](#utility-methods)
- [RsqlCompiler](#rsqlcompiler)
  - [Compilation Methods](#compilation-methods)
  - [Parameter Binding Methods](#parameter-binding-methods)
  - [Helper Methods](#helper-methods)

## RsqlQueryService

The `RsqlQueryService` is the main service class for executing RSQL queries. It's a generic class that works with entities, DTOs, repositories, and mappers.

### Constructor Methods

#### Basic Constructor
```java
public RsqlQueryService(
    REPOS repository,
    MAPPER mapper,
    EntityManager entityManager,
    Class<ENTITY> entityClass
)
```
Creates a new RsqlQueryService instance.

**Parameters:**
- `repository` - JPA repository that extends `JpaRepository` and `JpaSpecificationExecutor`
- `mapper` - Entity to DTO mapper
- `entityManager` - JPA EntityManager
- `entityClass` - Class of the entity

#### Constructor with Custom JPQL
```java
public RsqlQueryService(
    REPOS repository,
    MAPPER mapper,
    EntityManager entityManager,
    Class<ENTITY> entityClass,
    String jpqlSelectAllFromEntity,
    String jpqlSelectCountFromEntity
)
```
Creates a new RsqlQueryService with custom JPQL queries for complex scenarios.

**Parameters:**
- All parameters from basic constructor plus:
- `jpqlSelectAllFromEntity` - Custom JPQL SELECT query
- `jpqlSelectCountFromEntity` - Custom JPQL COUNT query

### Basic Query Methods

#### findByFilter
```java
public List<ENTITY_DTO> findByFilter(String filter)
```
Returns a list of DTOs matching the RSQL filter.

**Parameters:**
- `filter` - RSQL filter expression (e.g., "name=='John';age=gt=25")

**Returns:** List of DTOs matching the filter

**Example:**
```java
List<ProductDTO> products = queryService.findByFilter("price=gt=100;category.name=='Electronics'");
```

#### findEntitiesByFilter
```java
public List<ENTITY> findEntitiesByFilter(String filter)
```
Returns a list of entities (not DTOs) matching the RSQL filter.

**Parameters:**
- `filter` - RSQL filter expression

**Returns:** List of entities matching the filter

#### findByFilterAndSort
```java
public List<ENTITY_DTO> findByFilterAndSort(String filter, Pageable sortOrder)
```
Returns a sorted list of DTOs matching the RSQL filter.

**Parameters:**
- `filter` - RSQL filter expression
- `sortOrder` - Spring Data Pageable for sorting (only sort is used)

**Returns:** Sorted list of DTOs

**Example:**
```java
Pageable sort = PageRequest.of(0, 10, Sort.by("name").ascending());
List<ProductDTO> products = queryService.findByFilterAndSort("active==true", sort);
```

#### findEntitiesByFilterAndSort
```java
public List<ENTITY> findEntitiesByFilterAndSort(String filter, Pageable sortOrder)
```
Returns a sorted list of entities matching the RSQL filter.

### Paginated Query Methods

#### findByFilter (Paginated)
```java
public Page<ENTITY_DTO> findByFilter(String filter, Pageable page)
```
Returns a paginated result of DTOs matching the RSQL filter.

**Parameters:**
- `filter` - RSQL filter expression
- `page` - Spring Data Pageable for pagination and sorting

**Returns:** Page of DTOs

**Example:**
```java
Pageable pageable = PageRequest.of(0, 20, Sort.by("createdDate").descending());
Page<OrderDTO> orders = queryService.findByFilter("status==#PENDING#", pageable);
```

#### findEntitiesByFilter (Paginated)
```java
public Page<ENTITY> findEntitiesByFilter(String filter, Pageable page)
```
Returns a paginated result of entities matching the RSQL filter.

#### countByFilter
```java
public long countByFilter(String filter)
```
Returns the count of entities matching the RSQL filter.

**Parameters:**
- `filter` - RSQL filter expression

**Returns:** Number of matching entities

**Example:**
```java
long activeUsers = queryService.countByFilter("active==true;registrationDate=ge=#2024-01-01#");
```

### LOV (List of Values) Methods

#### getLOV (Custom Fields)
```java
public List<LovDTO> getLOV(
    String filter,
    Pageable pageable,
    String idField,
    String codeField,
    String nameField
)
```
Returns a list of values for dropdowns/autocomplete with custom field selection.

**Parameters:**
- `filter` - RSQL filter expression
- `pageable` - Pagination and sorting
- `idField` - Name of the ID field (required)
- `codeField` - Name of the code field (optional, can be null)
- `nameField` - Name of the name field (optional, can be null)

**Returns:** List of LovDTO objects

**Example:**
```java
// Get id and name only
List<LovDTO> categories = queryService.getLOV(
    "active==true", 
    PageRequest.of(0, 100),
    "id", null, "name"
);
```

#### getLOV (Standard Fields)
```java
public List<LovDTO> getLOV(String filter, Pageable pageable)
```
Returns a list of values with standard fields (id, code, name).

**Parameters:**
- `filter` - RSQL filter expression
- `pageable` - Pagination and sorting

**Example:**
```java
List<LovDTO> products = queryService.getLOV(
    "name=like='*phone*'", 
    PageRequest.of(0, 10)
);
```

#### getLOVwithIdAndName
```java
public List<LovDTO> getLOVwithIdAndName(String filter, Pageable pageable)
```
Returns a list of values with only id and name fields.

### SELECT Query Methods

The SELECT query methods provide powerful field selection capabilities with aliases, navigation properties, and aggregate functions. For complete SELECT syntax and detailed examples, see [SELECT.md](SELECT.md).

#### getTupleWithSelect
```java
public List<Tuple> getTupleWithSelect(
    String selectString,
    String filter,
    Pageable pageable
)
```
Executes a SELECT query with field selection and returns results as Tuples.

**Parameters:**
- `selectString` - SELECT expression (e.g., "code:id, name, productType.name:type")
- `filter` - RSQL filter expression
- `pageable` - Pagination and sorting

**Returns:** List of Tuples with selected fields

**Example:**
```java
List<Tuple> products = queryService.getTupleWithSelect(
    "code:productCode, name, productType.name:typeName, price",
    "status==ACTIVE",
    PageRequest.of(0, 20, Sort.by("name"))
);

for (Tuple row : products) {
    String code = (String) row.get("productCode");
    String name = (String) row.get("name");
    String type = (String) row.get("typeName");
    BigDecimal price = (BigDecimal) row.get("price");
}
```

#### getTupleAsPageWithSelect
```java
public Page<Tuple> getTupleAsPageWithSelect(
    String selectString,
    String filter,
    Pageable pageable
)
```
Executes a paginated SELECT query with field selection.

**Parameters:**
- `selectString` - SELECT expression
- `filter` - RSQL filter expression
- `pageable` - Pagination and sorting

**Returns:** Page of Tuples with selected fields

**Example:**
```java
Page<Tuple> page = queryService.getTupleAsPageWithSelect(
    "id, name, price",
    "price=gt=100",
    PageRequest.of(0, 20)
);

long total = page.getTotalElements();
List<Tuple> results = page.getContent();
```

#### getAggregateResult (with HAVING)
```java
public List<Tuple> getAggregateResult(
    String selectString,
    String filter,
    String havingFilter,
    Pageable pageable
)
```
Executes an aggregate query with automatic GROUP BY generation and HAVING clause filtering.

**Parameters:**
- `selectString` - SELECT expression with aggregate functions (e.g., "category:cat, COUNT(*):count, SUM(price):total")
- `filter` - RSQL WHERE filter expression (applied before aggregation)
- `havingFilter` - RSQL HAVING filter expression (applied after aggregation)
- `pageable` - Pagination and sorting

**Returns:** List of Tuples with aggregated results

**HAVING Filter Syntax:**
- Can reference SELECT aliases: `"totalSales=gt=10000;productCount=ge=5"`
- Can use aggregate functions directly: `"SUM(price)=gt=50000;COUNT(*)=ge=5"`
- Supports all RSQL operators: `==`, `!=`, `=gt=`, `=ge=`, `=lt=`, `=le=`, `=bt=`, `=in=`
- Supports logical operators: `;` (AND), `,` (OR), parentheses for grouping

For complete HAVING syntax documentation, see [HAVING.md](HAVING.md).

#### getAggregateResult (without HAVING - backward compatible)
```java
public List<Tuple> getAggregateResult(
    String selectString,
    String filter,
    Pageable pageable
)
```
Executes an aggregate query with automatic GROUP BY generation (no HAVING clause).

**Parameters:**
- `selectString` - SELECT expression with aggregate functions
- `filter` - RSQL filter expression
- `pageable` - Pagination and sorting

**Returns:** List of Tuples with aggregated results

**Note:** This is a backward-compatible version. Use the version with `havingFilter` parameter for filtering aggregated results.

**Supported aggregate functions:**
- `COUNT(*)` - Count all rows
- `COUNT(field)` - Count non-null values
- `COUNT(DIST field)` - Count distinct values
- `SUM(field)` - Sum of numeric field
- `AVG(field)` - Average of numeric field
- `MIN(field)` - Minimum value
- `MAX(field)` - Maximum value

**Example without HAVING:**
```java
// Sales statistics by product type (all categories)
List<Tuple> stats = queryService.getAggregateResult(
    "productType.name:category, COUNT(*):count, SUM(price):total, AVG(price):avg",
    "status==ACTIVE",
    PageRequest.of(0, 100, Sort.by("category"))
);
```

**Example with HAVING:**
```java
// Top performing categories (total sales > $50,000 AND at least 10 products)
List<Tuple> topCategories = queryService.getAggregateResult(
    "productType.name:category, COUNT(*):count, SUM(price):total, AVG(price):avg",
    "status==ACTIVE",  // WHERE filter
    "total=gt=50000;count=ge=10",  // HAVING filter using aliases
    PageRequest.of(0, 100, Sort.by("total").descending())
);

for (Tuple row : topCategories) {
    String category = (String) row.get("category");
    Long count = (Long) row.get("count");
    BigDecimal total = (BigDecimal) row.get("total");
    Double avg = (Double) row.get("avg");

    System.out.printf("%s: %d items, total $%s, avg $%.2f%n",
        category, count, total, avg);
}
```

**Example with HAVING using aggregate functions:**
```java
// Categories where average price is between $100-$500
List<Tuple> midRangeCategories = queryService.getAggregateResult(
    "productType.name:category, AVG(price):avgPrice, COUNT(*):count",
    "status==ACTIVE",
    "AVG(price)=bt=(100,500)",  // HAVING with aggregate function
    pageable
);
```

**Example with complex HAVING:**
```java
// High-value categories: (total > $100k OR avg > $500) AND count >= 5
List<Tuple> results = queryService.getAggregateResult(
    "category, SUM(price):total, AVG(price):avg, COUNT(*):count",
    "",
    "(total=gt=100000,avg=gt=500);count=ge=5",  // Complex HAVING
    pageable
);
```

#### getLOVWithSelect
```java
public List<LovDTO> getLOVWithSelect(
    String selectString,
    String filter,
    int maxResults
)
```
Returns a list of values using SELECT string syntax for field selection.

**Parameters:**
- `selectString` - SELECT expression (should select 2-3 fields: id, optional code, name)
- `filter` - RSQL filter expression
- `maxResults` - Maximum number of results to return

**Returns:** List of LovDTO objects

**Example:**
```java
// Select id and name from nested property
List<LovDTO> categories = queryService.getLOVWithSelect(
    "productType.id, productType.name",
    "productType.active==true",
    50
);

// Select id, code, and name with aliases
List<LovDTO> products = queryService.getLOVWithSelect(
    "id, code:productCode, name:productName",
    "price=lt=100",
    100
);
```

#### getSelectResult
```java
public <RESULT> List<RESULT> getSelectResult(
    Class<RESULT> resultClass,
    String selectString,
    String filter,
    Pageable pageable
)
```
Generic method for executing SELECT queries with custom result class.

**Type Parameters:**
- `RESULT` - Type of result class (e.g., custom DTO)

**Parameters:**
- `resultClass` - Class of the result type
- `selectString` - SELECT expression
- `filter` - RSQL filter expression
- `pageable` - Pagination and sorting

**Returns:** List of result objects

**Example:**
```java
// Map to custom DTO
public class ProductSummaryDTO {
    private String code;
    private String name;
    private BigDecimal price;
    // constructors, getters, setters
}

List<ProductSummaryDTO> summaries = queryService.getSelectResult(
    ProductSummaryDTO.class,
    "code, name, price",
    "status==ACTIVE",
    PageRequest.of(0, 50)
);
```

#### getSelectResultAsPage
```java
public <RESULT> Page<RESULT> getSelectResultAsPage(
    Class<RESULT> resultClass,
    String selectString,
    String filter,
    Pageable pageable
)
```
Generic method for executing paginated SELECT queries with custom result class.

**Type Parameters:**
- `RESULT` - Type of result class

**Parameters:**
- `resultClass` - Class of the result type
- `selectString` - SELECT expression
- `filter` - RSQL filter expression
- `pageable` - Pagination and sorting

**Returns:** Page of result objects

**Example:**
```java
Page<ProductSummaryDTO> page = queryService.getSelectResultAsPage(
    ProductSummaryDTO.class,
    "code, name, price, productType.name:typeName",
    "price=bt=(100,1000)",
    PageRequest.of(0, 20, Sort.by("name"))
);

long total = page.getTotalElements();
List<ProductSummaryDTO> results = page.getContent();
```

### JPQL Query Methods

#### getJpqlQueryResult
```java
public List<ENTITY_DTO> getJpqlQueryResult(
    String jpqlSelectQuery,
    String filter,
    Pageable page
)
```
Executes a custom JPQL query with RSQL filtering.

**Parameters:**
- `jpqlSelectQuery` - JPQL SELECT query
- `filter` - RSQL filter to apply
- `page` - Pagination and sorting

**Returns:** List of DTOs

**Example:**
```java
String jpql = "SELECT DISTINCT p FROM Product p LEFT JOIN p.categories c";
List<ProductDTO> products = queryService.getJpqlQueryResult(
    jpql, 
    "c.name=='Electronics'", 
    PageRequest.of(0, 20)
);
```

#### getJpqlQueryResultAsTuple
```java
public List<Tuple> getJpqlQueryResultAsTuple(
    String jpqlSelectQuery,
    String filter,
    Pageable page
)
```
Executes a JPQL query returning tuples for custom projections.

#### getJpqlQueryResultAsPage
```java
public Page<ENTITY_DTO> getJpqlQueryResultAsPage(
    String jpqlSelectQuery,
    String jpqlCountQuery,
    String filter,
    Pageable page
)
```
Executes paginated JPQL queries.

**Parameters:**
- `jpqlSelectQuery` - JPQL SELECT query
- `jpqlCountQuery` - JPQL COUNT query
- `filter` - RSQL filter
- `page` - Pagination

### Utility Methods

#### getSpecification
```java
public Specification<ENTITY> getSpecification(String filter)
```
Returns a JPA Specification from an RSQL filter string.

**Parameters:**
- `filter` - RSQL filter expression

**Returns:** JPA Specification

**Example:**
```java
Specification<Product> spec = queryService.getSpecification("price=bt=(100,500)");
// Can be combined with other specifications
Specification<Product> combined = spec.and(customSpec);
```

#### getTuple
```java
public List<Tuple> getTuple(String filter, Pageable pageable, String[] fields)
```
Returns selected fields as tuples.

**Parameters:**
- `filter` - RSQL filter expression
- `pageable` - Pagination and sorting
- `fields` - Array of field names to select

**Example:**
```java
List<Tuple> data = queryService.getTuple(
    "active==true", 
    PageRequest.of(0, 100),
    new String[]{"id", "name", "price"}
);
```

#### getResultAsMap
```java
public List<Map<String, Object>> getResultAsMap(
    String filter,
    Pageable pageable,
    String... fields
)
```
Returns query results as a list of maps.

**Parameters:**
- `filter` - RSQL filter expression
- `pageable` - Pagination and sorting
- `fields` - Variable arguments of field names

**Returns:** List of maps with field-value pairs

**Example:**
```java
List<Map<String, Object>> results = queryService.getResultAsMap(
    "category=='Books'",
    PageRequest.of(0, 50),
    "id", "title", "author", "price"
);
```

## RsqlCompiler

The `RsqlCompiler` class is responsible for compiling RSQL strings into JPA Specifications or query structures.

### Compilation Methods

#### compileToSpecification
```java
public Specification<T> compileToSpecification(
    String inputString,
    RsqlContext<T> rsqlContext
)
```
Compiles an RSQL string into a JPA Specification.

**Parameters:**
- `inputString` - RSQL filter expression
- `rsqlContext` - Context containing entity information

**Returns:** JPA Specification

**Throws:** `SyntaxErrorException` if the RSQL expression is invalid

#### compileToRsqlQuery
```java
public RsqlQuery compileToRsqlQuery(
    String inputString,
    RsqlContext<T> rsqlContext
)
```
Compiles an RSQL string into an RsqlQuery structure for JPQL generation.

**Parameters:**
- `inputString` - RSQL filter expression
- `rsqlContext` - Context containing entity information

**Returns:** RsqlQuery object containing parsed query structure

### Parameter Binding Methods

#### bindImplicitParametersForTypedQuery
```java
public static <T> void bindImplicitParametersForTypedQuery(
    RsqlQuery rsqlQuery,
    TypedQuery<T> query
)
```
Binds parameters from RsqlQuery to a TypedQuery.

**Parameters:**
- `rsqlQuery` - Query structure with parameters
- `query` - TypedQuery to bind parameters to

#### bindImplicitParametersForQuery
```java
public static void bindImplicitParametersForQuery(
    RsqlQuery rsqlQuery,
    Query query
)
```
Binds parameters from RsqlQuery to a regular Query.

### Helper Methods

#### replaceAlias
```java
public static void replaceAlias(
    RsqlQuery query,
    String fromAlias,
    String toAlias
)
```
Replaces alias names in the query structure.

**Parameters:**
- `query` - RsqlQuery to modify
- `fromAlias` - Current alias name
- `toAlias` - New alias name

#### fixIdsForNativeQuery
```java
public static void fixIdsForNativeQuery(RsqlQuery query)
```
Fixes ID field references for native SQL queries.

**Parameters:**
- `query` - RsqlQuery to modify

## Common Usage Patterns

### Basic Filtering
```java
// Simple equality
queryService.findByFilter("name=='John'");

// Multiple conditions (AND)
queryService.findByFilter("name=='John';age=gt=25");

// this is the same as
queryService.findByFilter("name=='John' and age=gt=25");

// OR conditions
queryService.findByFilter("status==#ACTIVE#,status==#PENDING#");

// this is the same as
queryService.findByFilter("status==#ACTIVE# or status==#PENDING#");

// Complex conditions
queryService.findByFilter("(status==#ACTIVE#,status==#PENDING#);createdDate=ge=#2024-01-01#");

// this is the same as
queryService.findByFilter("(status==#ACTIVE# or status==#PENDING#) and createdDate=ge=#2024-01-01#");

```

### Pagination and Sorting
```java
// Create pageable with sorting
Pageable pageable = PageRequest.of(0, 20, Sort.by("name").ascending().and(Sort.by("createdDate").descending()));

// Use with filter
Page<ProductDTO> page = queryService.findByFilter("category=='Electronics'", pageable);

// Access page information
long totalElements = page.getTotalElements();
int totalPages = page.getTotalPages();
List<ProductDTO> content = page.getContent();
```

### Working with Nested Properties
```java
// Access nested entity properties
queryService.findByFilter("customer.email=='john@example.com'");

// Multiple levels of nesting
queryService.findByFilter("order.customer.country.code=='US'");
```

### Date and Time Filtering
```java
// Date comparison
queryService.findByFilter("createdDate=ge=#2024-01-01#");

// DateTime comparison
queryService.findByFilter("lastLogin=le=#2024-01-01T23:59:59#");

// Date range
queryService.findByFilter("createdDate=bt=(#2024-01-01#,#2024-12-31#)");
```

### Pattern Matching
```java
// Contains
queryService.findByFilter("name=like='*john*'");

// Starts with
queryService.findByFilter("email=like='john*'");

// Ends with
queryService.findByFilter("email=like='*@example.com'");
```

### NULL Handling
```java
// Is null
queryService.findByFilter("deletedDate==null");

// Is not null
queryService.findByFilter("deletedDate!=null");
```

### Collections
```java
// In list
queryService.findByFilter("status=in=(#ACTIVE#,#PENDING#,#APPROVED#)");

// Not in list
queryService.findByFilter("status=nin=(#DELETED#,#ARCHIVED#)");
```