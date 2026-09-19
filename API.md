# RSQL Filter API Documentation

This document provides detailed information about all the methods available in the RSQL Filter library.

## Table of Contents
- [RsqlQueryService](#rsqlqueryservice)
  - [Constructor Methods](#constructor-methods)
  - [Configuration Methods](#configuration-methods)
  - [Basic Query Methods](#basic-query-methods)
  - [Paginated Query Methods](#paginated-query-methods)
  - [LOV (List of Values) Methods](#lov-list-of-values-methods)
  - [SELECT Query Methods](#select-query-methods)
  - [JPQL Query Methods](#jpql-query-methods)
  - [Utility Methods](#utility-methods)
- [SimpleQueryExecutor](#simplequeryexecutor)
  - [Selection and Query Building](#selection-and-query-building)
  - [Query Execution Methods](#query-execution-methods)
  - [JPQL Methods](#jpql-methods)
- [AggregateQueryBuilder](#aggregatequerybuilder)
  - [Overview](#overview)
  - [Methods](#methods)
  - [Complete Example](#complete-example)
- [TupleConverter](#tupleconverter)
- [RsqlCompiler](#rsqlcompiler)
  - [Compilation Methods](#compilation-methods)
  - [Parameter Binding Methods](#parameter-binding-methods)
  - [Helper Methods](#helper-methods)
- [RsqlWhereString](#rsqlwherestring)
- [RsqlContext](#rsqlcontext)
  - [Foreign key id resolution](#foreign-key-id-resolution)
- [Parser Limits](#parser-limits)
- [RsqlFilterDescription](#rsqlfilterdescription)
  - [Describing Methods](#describing-methods)
  - [FilterDescription](#filterdescription)
  - [FilterRow](#filterrow)
  - [FilterLabelResolver](#filterlabelresolver)
  - [The Filter Tree](#the-filter-tree)
- [Common Usage Patterns](#common-usage-patterns)
  - [Basic Filtering](#basic-filtering)
  - [Named Parameters](#named-parameters)
  - [Pagination and Sorting](#pagination-and-sorting)
  - [Working with Nested Properties](#working-with-nested-properties)
  - [Date and Time Filtering](#date-and-time-filtering)
  - [Pattern Matching](#pattern-matching)
  - [NULL Handling](#null-handling)
  - [Collections](#collections)

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

**Important:** This constructor sets `useJpqlSelect` to `true`, which is a behavioural switch and not just extra
configuration: every Basic and Paginated query method - `findByFilter` and `findEntitiesByFilter` in both forms,
`findByFilterAndSort`, `findEntitiesByFilterAndSort` and `countByFilter` - then executes the supplied JPQL
instead of going through the repository and a Specification. Call `setUseJpqlSelect(false)` to switch back. The
four-argument constructor leaves it `false`.

**Important:** The custom JPQL must alias its root `a0`, the default alias applied to every query context. The
generated WHERE and ORDER BY text is built with that alias, so `FROM Product p` produces a clause referring to
`a0` and the query fails. Either write `FROM Product a0` or call `setSelectAlias()` / `setCountAlias()`.

**Note:** `RsqlQueryService` is safe to hold as a singleton Spring bean. Every public query method builds a fresh
`RsqlContext` for that one query, with its own joins map and class-metadata map, so concurrent calls do not
share parse or join state.

### Configuration Methods

#### getRsqlCompiler
```java
public RsqlCompiler<ENTITY> getRsqlCompiler()
```
Returns the compiler this service uses. This is how a caller obtains the `compiler` argument that
`AggregateQueryBuilder.createHavingPredicate()` requires; a freshly constructed `new RsqlCompiler<>()` works
just as well.

#### getEntityClass
```java
public Class<ENTITY> getEntityClass()
```
Returns the entity class the service was created for.

#### getRsqlContext
```java
public RsqlContext<ENTITY> getRsqlContext()
```
**Deprecated.** Returns a fresh `RsqlContext` for one query - not the shared field, despite the name. The query
methods build their own context internally, and a direct [SimpleQueryExecutor](#simplequeryexecutor) call can
construct one with `new RsqlContext<>(entityClass).defineEntityManager(em)`.

#### JPQL mode and aliases
```java
public void setJpqlSelectAllFromEntity(String jpqlSelectAllFromEntity)
public void setJpqlSelectCountFromEntity(String jpqlSelectCountFromEntity)
public void setUseJpqlSelect(boolean useJpqlSelect)
public boolean getUseJpqlSelect()
public void setSelectAlias(String selectAlias)
public String getSelectAlias()
public void setCountAlias(String countAlias)
public String getCountAlias()
```

| Method | Meaning |
|---|---|
| `setJpqlSelectAllFromEntity(String)` | Installs the JPQL SELECT used by the basic query methods, sets the select alias from it - which is always `a0`, see `findAliasFromJpqlSelectString` below - and sets `useJpqlSelect` to `true` |
| `setJpqlSelectCountFromEntity(String)` | Installs the JPQL COUNT used by `countByFilter` and sets the count alias the same way |
| `setUseJpqlSelect(boolean)` / `getUseJpqlSelect()` | Whether the Basic and Paginated query methods run the installed JPQL or a Specification against the repository. `false` unless the six-argument constructor or `setJpqlSelectAllFromEntity` was used |
| `setSelectAlias(String)` / `getSelectAlias()` | The alias every generated WHERE and ORDER BY refers to. Defaults to `a0`. Setting it does not touch the shared context - it is applied to each new query context, which is what keeps the service thread-safe |
| `setCountAlias(String)` / `getCountAlias()` | The same for the count query. Defaults to `a0` |

#### Paging strategy
```java
public RsqlQueryService<…> withPagingStrategy(PagingStrategy pagingStrategy)
public void setPagingStrategy(PagingStrategy pagingStrategy)
public PagingStrategy getPagingStrategy()
public RsqlQueryService<…> withRowIdExtractor(Function<ENTITY, ?> rowIdExtractor)
```

**Since 0.6.23.** How a page of a JPQL query is fetched. Applies to `findByFilter(String, Pageable)`,
`findEntitiesByFilter(String, Pageable)` and `getJpqlQueryResultAsPage` when the service runs in JPQL mode;
the Specification path and the unpaged methods are unaffected.

| `PagingStrategy` | Statements | When |
|---|---|---|
| `SINGLE_QUERY` (default) | count; the full select with filter, sort and offset/limit | Every earlier version. The database joins and sorts the whole filtered result before it skips to the page |
| `IDS_THEN_HYDRATE` | count; the page's identifiers with the sort columns, limited; the caller's select for those identifiers, unlimited and unsorted | A wide `select new` over many joins, or deep pages: the cost stops depending on the width of the select and the page number |

`withRowIdExtractor` is how `IDS_THEN_HYDRATE` reads the identifier off a hydrated row so the rows can be put
back in page order. Not needed when the select returns the entity — `select new Entity(…)` included; needed
for a DTO, which otherwise fails with an `IllegalArgumentException` that says so.

`IDS_THEN_HYDRATE` runs the single statement instead, logged at debug, for an unpaged request, an entity with a
composite identifier, and a select whose `from` clause cannot be found. See
[Two-phase paging](README.md#two-phase-paging) for the shape of the statements and what to check before
switching.

#### findAliasFromJpqlSelectString
```java
public String findAliasFromJpqlSelectString(String jpqlSelect)
```
Extracts the root alias from a JPQL SELECT string. **It returns `a0` for every input**, whatever alias the query
actually uses, which is why custom JPQL has to alias its root `a0` or set the alias explicitly.

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

**Important:** This method does **NOT** support arithmetic expressions. For arithmetic operations, use `getAggregateResultWithExpressions()` or `getAggregateResultAsPageWithExpressions()`.

**Parameters:**
- `selectString` - SELECT expression (e.g., "code:id, name, productType.name:type") - **without arithmetic**
- `filter` - RSQL filter expression
- `pageable` - Pagination and sorting

**Returns:** List of Tuples with selected fields

**Example:**
```java
List<Tuple> products = queryService.getTupleWithSelect(
    "code:productCode, name, productType.name:typeName, price",
    "status==#ACTIVE#",
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

#### getAggregateResult
```java
public List<Tuple> getAggregateResult(
    String selectString,
    String filter,
    String havingFilter,
    Pageable pageable
)
```
Executes an aggregate query with automatic GROUP BY generation and HAVING clause filtering. There is no
three-argument overload: pass `null` as `havingFilter` when there is no HAVING clause.

**Important:** This method does **NOT** support arithmetic expressions in SELECT. `SUM(price) * 1.2:total`
throws `SyntaxErrorException` - *"Arithmetic expressions with operators are not supported in this query type.
Use SelectExpressionVisitor for queries with arithmetic expressions."* Use `getAggregateResultWithExpressions()`
for arithmetic.

**Parameters:**
- `selectString` - SELECT expression with aggregate functions (e.g., "productType.name:category, SUM(price):total") - **without arithmetic**
- `filter` - RSQL WHERE filter expression (applied before aggregation)
- `havingFilter` - RSQL HAVING filter expression (applied after aggregation); may be `null`
- `pageable` - Sorting only. The page number and page size are ignored and **all** groups are returned; use `getAggregateResultAsPage()` to paginate

**Returns:** List of Tuples with aggregated results

**Important:** The ORDER BY of this method is resolved against the entity root, so it cannot sort by a SELECT
alias - `Sort.by("productType.name")` works, `Sort.by("category")` does not. `getAggregateResultAsPage()`
resolves aliases.

**HAVING Filter Syntax:**
- Can reference SELECT aliases: `"totalSales=gt=10000;productCount=ge=5"`
- Can use aggregate functions directly: `"SUM(price)=gt=50000;COUNT(*)=ge=5"`
- Supported operators: `==`, `!=`, `=gt=`, `=ge=`, `=lt=`, `=le=`, `=bt=`, `=nbt=`, `=in=`, `=nin=`, `=like=`, `=nlike=`. The case-sensitive `=clike=` / `=cnlike=` are WHERE-only
- Supports logical operators: `;` (AND), `,` (OR), parentheses for grouping

**Important:** A SELECT alias that HAVING refers to must not be a HAVING keyword. `count`, `avg`, `sum`, `min`,
`max`, `grp`, `all`, `dist`, `and`, `or`, `null`, `true` and `false` are lexer tokens there, so
`COUNT(*):count` followed by `"count=ge=10"` does not parse - it fails with *"no viable alternative at input
'count='"*. Name the alias `productCount` instead. A bare field in HAVING that is neither a SELECT alias nor one
of the GROUP BY fields derived from the SELECT string raises `IllegalArgumentException`.

For complete HAVING syntax documentation, see [HAVING.md](HAVING.md).

#### getAggregateResultAsPage
```java
public Page<Tuple> getAggregateResultAsPage(
    String selectString,
    String filter,
    String havingFilter,
    Pageable pageable
)
```
Executes an aggregate query with automatic GROUP BY generation, HAVING clause filtering, and **full pagination support**.

**Important:** Like `getAggregateResult()`, this method does **NOT** support arithmetic expressions in SELECT;
they throw `SyntaxErrorException`. Use `getAggregateResultAsPageWithExpressions()` for arithmetic.

**This method supports:**
- Real offset and limit (`setFirstResult` / `setMaxResults`), unlike `getAggregateResult()`
- Sorting by SELECT aliases (e.g., `Sort.by("total").descending()`)
- Full pagination metadata (`totalElements`, `totalPages`, etc.)
- Proper count calculation for GROUP BY queries with HAVING filters

**Parameters:**
- `selectString` - SELECT expression with aggregate functions (e.g., "productType.name:category, SUM(price):total, COUNT(*):productCount") - **without arithmetic**
- `filter` - RSQL WHERE filter expression (applied before aggregation)
- `havingFilter` - RSQL HAVING filter expression (applied after aggregation); may be `null`
- `pageable` - Pagination and sorting (use `Sort.by("aliasName")` to sort by SELECT aliases)

**Returns:** `Page<Tuple>` with aggregated results and pagination metadata

**Example:**
```java
// Paginate and sort by aggregate alias
Page<Tuple> page = queryService.getAggregateResultAsPage(
    "productType.name:category, SUM(price):total, COUNT(*):productCount",
    "status==#ACTIVE#",
    "total=gt=1000",
    PageRequest.of(0, 10, Sort.by("total").descending())
);

long totalElements = page.getTotalElements();  // Total number of groups
int totalPages = page.getTotalPages();          // Total pages
List<Tuple> results = page.getContent();        // Current page results

// Without HAVING
Page<Tuple> salesPage = queryService.getAggregateResultAsPage(
    "productType.name:category, SUM(price):total, AVG(price):avgPrice",
    "",
    null,
    PageRequest.of(0, 20, Sort.by("category"))
);
```

**Sorting (0.6.7+):**
The sort property of an aggregate query is resolved in three steps:
- **By alias**: `Sort.by("total").descending()` - Uses the alias from SELECT
- **By field path**: `Sort.by("productType.name")` - Uses the original field path from SELECT (reuses existing JOIN)
- **By entity property path**: anything else falls back to a path built on the root, which may add a JOIN the SELECT does not have

The first two approaches use the same JPA Expression from SELECT, preventing duplicate JOINs. The same three
steps apply to `getAggregateResultWithExpressions()` and `getAggregateResultAsPageWithExpressions()`, where the
first step also covers the alias of an arithmetic expression. `getAggregateResult()` is the exception: it sorts
against the entity root only.

**Count Behavior:**
- For queries **without** GROUP BY: returns total matching rows
- For queries **with** GROUP BY: returns total number of groups
- For queries **with** HAVING: returns number of groups **after** HAVING filter

**Supported aggregate functions:**
- `COUNT(*)` - Count all rows
- `COUNT(field)` - Count non-null values
- `COUNT(DIST field)` - Count distinct values
- `SUM(field)` - Sum of numeric field
- `AVG(field)` - Average of numeric field
- `MIN(field)` - Minimum value
- `MAX(field)` - Maximum value
- `GRP(field)` - No aggregation; forces the field into GROUP BY, the same as writing it bare

The set is closed: `SUM`, `AVG`, `MIN`, `MAX`, `COUNT` and `GRP`. Anything else is a syntax error.

**Example without HAVING:**
```java
// Sales statistics by product type (all categories)
List<Tuple> stats = queryService.getAggregateResult(
    "productType.name:category, COUNT(*):productCount, SUM(price):total, AVG(price):avgPrice",
    "status==#ACTIVE#",
    null,
    PageRequest.of(0, 100, Sort.by("productType.name"))
);
```

**Example with HAVING:**
```java
// Top performing categories (total sales > $50,000 AND at least 10 products)
List<Tuple> topCategories = queryService.getAggregateResult(
    "productType.name:category, COUNT(*):productCount, SUM(price):total, AVG(price):avgPrice",
    "status==#ACTIVE#",  // WHERE filter
    "total=gt=50000;productCount=ge=10",  // HAVING filter using aliases
    PageRequest.of(0, 100, Sort.by("productType.name"))
);

for (Tuple row : topCategories) {
    String category = (String) row.get("category");
    Long count = (Long) row.get("productCount");
    BigDecimal total = (BigDecimal) row.get("total");
    Double avg = (Double) row.get("avgPrice");

    System.out.printf("%s: %d items, total $%s, avg $%.2f%n",
        category, count, total, avg);
}
```

**Example with HAVING using aggregate functions:**
```java
// Categories where average price is between $100-$500
List<Tuple> midRangeCategories = queryService.getAggregateResult(
    "productType.name:category, AVG(price):avgPrice, COUNT(*):productCount",
    "status==#ACTIVE#",
    "AVG(price)=bt=(100,500)",  // HAVING with aggregate function
    pageable
);
```

**Example with complex HAVING:**
```java
// High-value categories: (total > $100k OR avg > $500) AND count >= 5
List<Tuple> results = queryService.getAggregateResult(
    "productType.name:category, SUM(price):total, AVG(price):avgPrice, COUNT(*):productCount",
    "",
    "(total=gt=100000,avgPrice=gt=500);productCount=ge=5",  // Complex HAVING
    pageable
);
```

#### getAggregateResultWithExpressions
```java
public List<Tuple> getAggregateResultWithExpressions(
    String selectString,
    String filter,
    String havingFilter,
    Pageable pageable
)
```
Executes an aggregate query whose SELECT may contain arithmetic. The SELECT string is parsed by
`SelectExpressionVisitor` instead of the aggregate-field parser, which is what makes `+ - * /` legal here.

**Important:** This method and `getAggregateResultAsPageWithExpressions()` are the **only** two methods that
accept arithmetic in a SELECT string. Everything else - `getAggregateResult()`, `getAggregateResultAsPage()`,
`getTupleWithSelect()`, `getSelectResult()` - rejects it with `SyntaxErrorException`.

**Parameters:**
- `selectString` - SELECT expression with aggregate functions and arithmetic (e.g., "account.name:accountName, SUM(debit) - SUM(credit):balance")
- `filter` - RSQL WHERE filter expression (applied before aggregation)
- `havingFilter` - RSQL HAVING filter expression (applied after aggregation); may be `null`
- `pageable` - Sorting only. The page number and page size are ignored; use `getAggregateResultAsPageWithExpressions()` to paginate. May be `null`

**Returns:** List of Tuples with aggregated results

**Supported arithmetic operators:**
- `+` - Addition
- `-` - Subtraction
- `*` - Multiplication
- `/` - Division
- `()` - Parentheses for grouping and precedence

**Note:** Arithmetic goes *around* aggregate calls, not inside them: `SUM(price) * 1.2` parses, `SUM(price * 1.2)`
does not.

**Example:**
```java
// Calculate balance (debit - credit), keeping only the positive ones
List<Tuple> balances = queryService.getAggregateResultWithExpressions(
    "account.name:accountName, SUM(debit) - SUM(credit):balance",
    "year==2024",
    "balance=gt=0",
    PageRequest.of(0, 100, Sort.by("balance").descending())
);

// Calculate price with 20% tax
List<Tuple> totals = queryService.getAggregateResultWithExpressions(
    "productType.name:category, SUM(price):subtotal, SUM(price) * 1.2:totalWithTax",
    "status==#ACTIVE#",
    null,
    null
);

// Complex calculation: adjusted average
List<Tuple> metrics = queryService.getAggregateResultWithExpressions(
    "productType.name:category, (SUM(price) - 50) * 2 / COUNT(*):adjustedAverage",
    "",
    null,
    null
);
```

#### getAggregateResultAsPageWithExpressions
```java
public Page<Tuple> getAggregateResultAsPageWithExpressions(
    String selectString,
    String filter,
    String havingFilter,
    Pageable pageable
)
```
The paginated form of `getAggregateResultWithExpressions()`: arithmetic in SELECT, real offset and limit, and
the same pagination metadata and count behaviour as `getAggregateResultAsPage()`.

**Parameters:**
- `selectString` - SELECT expression with aggregate functions and arithmetic
- `filter` - RSQL WHERE filter expression (applied before aggregation)
- `havingFilter` - RSQL HAVING filter expression (applied after aggregation); may be `null`
- `pageable` - Pagination and sorting; `Sort.by("aliasName")` resolves the alias of an arithmetic expression as well as of a plain aggregate

**Returns:** `Page<Tuple>` with aggregated results and pagination metadata

**Example:**
```java
Page<Tuple> page = queryService.getAggregateResultAsPageWithExpressions(
    "productType.name:category, SUM(price) * 1.2:totalWithTax, COUNT(*):productCount",
    "status==#ACTIVE#",
    "totalWithTax=gt=1000",  // HAVING on the calculated alias
    PageRequest.of(0, 10, Sort.by("totalWithTax").descending())
);

long totalElements = page.getTotalElements();  // Groups after the HAVING filter
List<Tuple> results = page.getContent();

for (Tuple row : results) {
    String category = (String) row.get("category");
    BigDecimal total = (BigDecimal) row.get("totalWithTax");
    Long count = (Long) row.get("productCount");
}
```

#### getLOVWithSelect
```java
public List<LovDTO> getLOVWithSelect(
    String selectString,
    String filter,
    Pageable pageable
)
```
Returns a list of values using SELECT string syntax for field selection.

**Parameters:**
- `selectString` - SELECT expression selecting two or three fields. Three fields map to `LovDTO(id, code, name)`, two map to `LovDTO(id, name)` - not to `(id, code)`
- `filter` - RSQL filter expression
- `pageable` - Pagination and sorting; the page size caps the number of rows returned

**Returns:** List of LovDTO objects

**Example:**
```java
// Select id and name from nested property
List<LovDTO> categories = queryService.getLOVWithSelect(
    "productType.id, productType.name",
    "productType.active==true",
    PageRequest.of(0, 50)
);

// Select id, code, and name with aliases
List<LovDTO> products = queryService.getLOVWithSelect(
    "id, code:productCode, name:productName",
    "price=lt=100",
    PageRequest.of(0, 100)
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

**Important:** This method does **NOT** support arithmetic expressions. Use `getAggregateResultWithExpressions()` for arithmetic.

**Type Parameters:**
- `RESULT` - Type of result class (e.g., custom DTO)

**Parameters:**
- `resultClass` - Class of the result type
- `selectString` - SELECT expression - **without arithmetic**
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
    "status==#ACTIVE#",
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
Executes a custom JPQL query with RSQL filtering. The RSQL filter is rendered to a `where` clause and appended
to the query text, then the `Sort` is appended as an `order by`.

**Parameters:**
- `jpqlSelectQuery` - JPQL SELECT query. Its root must be aliased `a0` (or whatever `setSelectAlias()` was given), because the generated `where` and `order by` text uses that alias
- `filter` - RSQL filter to apply. The paths are entity property paths resolved against the metamodel, not JPQL join aliases
- `page` - Sorting only; the offset and page size are ignored. Use `getJpqlQueryResultAsPage()` to paginate

**Returns:** List of DTOs

**Example:**
```java
String jpql = "SELECT DISTINCT a0 FROM Product a0 LEFT JOIN a0.categories";
List<ProductDTO> products = queryService.getJpqlQueryResult(
    jpql,
    "categories.name=='Electronics'",
    PageRequest.of(0, 20, Sort.by("name"))
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
Executes a JPQL query returning tuples for custom projections. Same alias and paging rules as
`getJpqlQueryResult`: the root must be `a0`, and only the `Sort` of the `Pageable` is used.

#### getJpqlQueryEntities
```java
public List<ENTITY> getJpqlQueryEntities(
    String jpqlSelectQuery,
    String filter,
    Pageable page
)
```
The entity-returning counterpart of `getJpqlQueryResult` - the same query, without the mapper step.

**Returns:** List of entities

**Example:**
```java
List<Product> products = queryService.getJpqlQueryEntities(
    "SELECT a0 FROM Product a0",
    "price=gt=100",
    PageRequest.of(0, 20, Sort.by("code").descending())
);
```

#### getJpqlQueryResultAsPage
```java
public Page<ENTITY_DTO> getJpqlQueryResultAsPage(
    String jpqlSelectQuery,
    String jpqlCountQuery,
    String filter,
    Pageable page
)
```
Executes paginated JPQL queries. Unlike `getJpqlQueryResult`, this one applies the offset and the page size.
Follows [`getPagingStrategy()`](#paging-strategy): under `IDS_THEN_HYDRATE` the page is fetched as its identifiers
first and this select second.

**Parameters:**
- `jpqlSelectQuery` - JPQL SELECT query, root aliased `a0`
- `jpqlCountQuery` - JPQL COUNT query, root aliased `a0`
- `filter` - RSQL filter
- `page` - Pagination and sorting

**Example:**
```java
Page<ProductDTO> page = queryService.getJpqlQueryResultAsPage(
    "SELECT a0 FROM Product a0",
    "SELECT COUNT(a0) FROM Product a0",
    "price=gt=100",
    PageRequest.of(0, 20, Sort.by("name"))
);
```

### Utility Methods

#### createSpecification
```java
public Specification<ENTITY> createSpecification(String filter)
```
Creates a JPA Specification from an RSQL filter string.

**Parameters:**
- `filter` - RSQL filter expression

**Returns:** JPA Specification

**Example:**
```java
Specification<Product> spec = queryService.createSpecification("price=bt=(100,500)");
// Can be combined with other specifications
Specification<Product> combined = spec.and(customSpec);
```

#### createSelections
```java
public List<Selection<?>> createSelections(
    String selectString,
    CriteriaBuilder builder,
    Root<ENTITY> root
)
```
Creates JPA Criteria Selections from a SELECT string (non-aggregate queries).
This method is analogous to `createSpecification()` but for SELECT clauses.

**Parameters:**
- `selectString` - SELECT clause string (e.g., "code, name, productType.name:typeName")
- `builder` - CriteriaBuilder for creating selections
- `root` - Query root

**Returns:** List of Selection<?> for use with CriteriaQuery.multiselect()

**SELECT string syntax:**
- Simple fields: `"code, name, price"`
- Nested properties: `"productType.name, productType.code"`
- Aliases: `"name:productName, productType.name:typeName"`

**Example:**
```java
CriteriaBuilder builder = em.getCriteriaBuilder();
CriteriaQuery<Tuple> query = builder.createQuery(Tuple.class);
Root<Product> root = query.from(Product.class);

List<Selection<?>> selections = queryService.createSelections(
    "code, name, productType.name:typeName", builder, root
);
query.multiselect(selections);
query.where(queryService.createSpecification("status==#ACTIVE#")
    .toPredicate(root, query, builder));

List<Tuple> results = em.createQuery(query).getResultList();

// Access results
for (Tuple row : results) {
    String code = (String) row.get("code");
    String name = (String) row.get("name");
    String typeName = (String) row.get("typeName");
}
```

#### createAggregateQuery
```java
public AggregateQueryBuilder<ENTITY> createAggregateQuery(
    String selectString,
    CriteriaBuilder builder,
    Root<ENTITY> root
)
```
Creates an aggregate query builder from a SELECT string containing aggregate functions.
This method is analogous to `createSpecification()` but for aggregate queries.

**Parameters:**
- `selectString` - Aggregate SELECT clause string (e.g., "category, COUNT(*):productCount, SUM(price):total")
- `builder` - CriteriaBuilder for creating selections and expressions
- `root` - Query root

**Returns:** AggregateQueryBuilder with selections, GROUP BY expressions, and HAVING state

**SELECT string syntax:**
- Simple fields for GROUP BY: `"productType.name, status"`
- Aggregate functions: `"COUNT(*):productCount, SUM(price):total, AVG(price):avgPrice"`
- Aliases for all fields: `"productType.name:category, COUNT(*):productCount"`
- COUNT DISTINCT: `"COUNT(DIST productType.id):typeCount"`
- The closed function set is `SUM`, `AVG`, `MIN`, `MAX`, `COUNT` and `GRP`; `GRP(field)` aggregates nothing and
  only forces the field into GROUP BY, as a bare field does

**AggregateQueryBuilder methods:**
- `getSelections()` - Returns SELECT clause selections
- `getGroupByExpressions()` - Returns GROUP BY clause expressions
- `createHavingPredicate(havingFilter, compiler)` - Creates HAVING clause predicate

**Example:**
```java
CriteriaBuilder builder = em.getCriteriaBuilder();
CriteriaQuery<Tuple> query = builder.createQuery(Tuple.class);
Root<Product> root = query.from(Product.class);
RsqlCompiler<Product> rsqlCompiler = queryService.getRsqlCompiler();

// Create aggregate query
AggregateQueryBuilder<Product> aggQuery = queryService.createAggregateQuery(
    "productType.name:category, COUNT(*):productCount, SUM(price):total, AVG(price):avgPrice",
    builder, root
);

// Build complete query
query.multiselect(aggQuery.getSelections());
query.groupBy(aggQuery.getGroupByExpressions());
query.where(queryService.createSpecification("status==#ACTIVE#")
    .toPredicate(root, query, builder));
query.having(aggQuery.createHavingPredicate("total=gt=50000;productCount=ge=10", rsqlCompiler));

List<Tuple> results = em.createQuery(query).getResultList();

// Access aggregated results
for (Tuple row : results) {
    String category = (String) row.get("category");
    Long count = (Long) row.get("productCount");
    BigDecimal total = (BigDecimal) row.get("total");
    Double avg = (Double) row.get("avgPrice");

    System.out.printf("%s: %d items, total $%s, avg $%.2f%n",
        category, count, total, avg);
}
```

**Note:** A Specification and an `AggregateQueryBuilder` obtained from the service each get their own query
context and therefore their own joins map. To have one shared map - so that WHERE, SELECT and GROUP BY reuse a
single JOIN - build the context yourself and compile the Specification with
[`compileToSpecification(String, RsqlContext, boolean)`](#compiletospecification-shared-joins-overload) passing
`false`.

**HAVING filter examples:**
```java
// Filter by alias from SELECT
aggQuery.createHavingPredicate("total=gt=50000;productCount=ge=10", compiler)

// Filter using aggregate functions directly
aggQuery.createHavingPredicate("SUM(price)=gt=50000;COUNT(*)=ge=10", compiler)

// Complex HAVING with OR and parentheses
aggQuery.createHavingPredicate("(total=gt=100000,avgPrice=gt=500);productCount=ge=5", compiler)

// HAVING with BETWEEN
aggQuery.createHavingPredicate("AVG(price)=bt=(100,500)", compiler)
```

#### getSpecification
```java
public Specification<ENTITY> getSpecification(String filter)
```
Alias for `createSpecification(String filter)`; prefer `createSpecification` in new code.

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

## SimpleQueryExecutor

`rsql.helper.SimpleQueryExecutor` is the static layer that `RsqlQueryService` wraps. Every method takes the
entity class, the result class, an `RsqlContext` and an `RsqlCompiler` explicitly, so it needs neither a
repository nor a mapper - which is the reason to reach for it: a query over an entity that has no
`JpaRepository`, a result class that is not the service's DTO, or a second entity queried from inside a service
built for another one. When a `RsqlQueryService` already exists for the entity, prefer its methods; they do the
same work with the context and compiler filled in.

A context for a bare call is built as:

```java
RsqlContext<Product> context = new RsqlContext<>(Product.class);
context.defineEntityManager(entityManager);
RsqlCompiler<Product> compiler = new RsqlCompiler<>();
```

### Selection and Query Building

#### createSelectionsFromString
```java
public static <ENTITY> List<Selection<?>> createSelectionsFromString(
    String selectString,
    CriteriaBuilder builder,
    Root<ENTITY> root,
    RsqlContext<ENTITY> rsqlContext
)
```
Parses a non-aggregate SELECT string into JPA Criteria selections. This is what
`RsqlQueryService.createSelections()` calls. A `null` or blank `selectString` gives an empty list.

#### createAggregateQuery
```java
public static <ENTITY> AggregateQueryBuilder<ENTITY> createAggregateQuery(
    String selectString,
    CriteriaBuilder builder,
    Root<ENTITY> root,
    RsqlContext<ENTITY> rsqlContext
)
```
Parses an aggregate SELECT string into an [AggregateQueryBuilder](#aggregatequerybuilder). This is the only way
to obtain one - the constructor is package-private.

### Query Execution Methods

All of these take `(entityClass, resultClass, …, rsqlContext, compiler)`. The `AsPage` variants return
`Page<RESULT>` and apply the offset and the page size. The `List`-returning variants differ: the two non-aggregate
ones page as well, while every aggregate one uses only the `Sort` of the `Pageable` and returns all groups.

| Method | SELECT input | Arithmetic | `Pageable` |
|---|---|---|---|
| `getQueryResult(entityClass, resultClass, String[] properties, filter, pageable, ctx, compiler)` | array of property names | no | offset, limit and sort |
| `getQueryResultWithSelect(entityClass, resultClass, selectString, filter, pageable, ctx, compiler)` | SELECT string | no | offset, limit and sort |
| `getQueryResultAsPage(…, String[] properties, …, repository)` / `getQueryResultAsPageWithSelect(…, selectString, …, repository)` | as above; these two additionally need the repository, for the count | no | full |
| `getAggregateQueryResultWithSelect(entityClass, resultClass, selectString, filter, havingFilter, pageable, ctx, compiler)` | aggregate SELECT string | **no** - throws `SyntaxErrorException` | sort only |
| `getAggregateQueryResultAsPageWithSelect(…)` | aggregate SELECT string | **no** - throws `SyntaxErrorException` | full |
| `getAggregateQueryResultWithSelectExpression(entityClass, resultClass, selectString, filter, havingFilter, pageable, ctx, compiler)` | aggregate SELECT string | **yes** | sort only |
| `getAggregateQueryResultAsPageWithSelectExpression(…)` | aggregate SELECT string | **yes** | full |
| `getAggregateQueryResult(…, List<AggregateField> selectFields, List<String> groupByFields, …)` / `getAggregateQueryResultAsPage(…)` | already-parsed fields | no | sort only / full |
| `getAggregateQueryResultWithExpressions(…, List<SelectExpression> selectExpressions, List<String> groupByFields, …)` / `getAggregateQueryResultAsPageWithExpressions(…)` | already-parsed expressions | **yes** | sort only / full |

The `*WithSelect*` and `*WithSelectExpression*` forms parse the string and delegate to the corresponding
pre-parsed form, so use the pre-parsed ones only when the SELECT clause has already been compiled - for example
when the same clause drives several queries. `RsqlQueryService.getAggregateResult*` are thin wrappers over the
four `*WithSelect*` / `*WithSelectExpression*` entries.

**Example:**
```java
List<Tuple> results = SimpleQueryExecutor.getAggregateQueryResultWithSelectExpression(
    Product.class,
    Tuple.class,
    "productType.name:category, SUM(price) * 1.2:totalWithTax",
    "status==#ACTIVE#",   // WHERE filter, may be null
    null,               // HAVING filter, may be null
    null,               // Pageable, may be null
    context,
    compiler
);
```

### JPQL Methods

#### getJpqlQueryResult
```java
public static <ENTITY, RESULT> List<RESULT> getJpqlQueryResult(
    Class<ENTITY> entityClass,
    Class<RESULT> resultClass,
    String jpqlQueryString,
    String alias,
    String filter,
    Pageable pageable,
    RsqlContext<ENTITY> rsqlContext,
    RsqlCompiler<ENTITY> compiler
)
```
Appends the compiled `where` and the `order by` to the JPQL text and runs it. Only the `Sort` of the `Pageable`
is used.

**Important:** the `alias` argument reaches the `order by` and nothing else - it is handed straight to
`getOrderByWithAlias(sort, alias)`. The `where` text is aliased by the root of the `RsqlContext` you pass, which
`initContext()` names `a0`, so the JPQL must still alias its own root `a0`. Passing `alias = "p"` for
`FROM Product p` produces `SELECT p FROM Product p where a0.price>100L order by p.name ASC`, and Hibernate
answers *"Could not interpret path expression 'a0.price'"*. To use a different alias, rename the context root -
`rsqlContext.root.alias("p")` after `defineEntityManager()` - and pass the same name as `alias`.

#### getJpqlQueryResultAsPage
```java
public static <ENTITY, RESULT> Page<RESULT> getJpqlQueryResultAsPage(
    Class<ENTITY> entityClass,
    Class<RESULT> resultClass,
    String jpqlQueryString, String selectAlias,
    String countQueryString, String countAlias,
    String filter,
    Pageable pageable,
    RsqlContext<ENTITY> rsqlContext,
    RsqlCompiler<ENTITY> compiler
)
```
The same with a separate count query; this one applies the offset and page size. `selectAlias` behaves exactly as
`alias` above - `order by` only - and `countAlias` is accepted but never used: the count query gets the same
`where` text, so both roots have to be aliased the same as the context root.

#### getJpqlQueryResultAsPageIdsThenHydrate
```java
public static <ENTITY, RESULT> Page<RESULT> getJpqlQueryResultAsPageIdsThenHydrate(
    Class<ENTITY> entityClass,
    Class<RESULT> resultClass,
    String jpqlQueryString,
    String selectAlias,
    String countQueryString,
    String countAlias,
    String filter,
    Pageable pageable,
    RsqlContext<ENTITY> rsqlContext,
    RsqlCompiler<ENTITY> compiler,
    Function<RESULT, ?> rowIdExtractor,
    int hydrationChunkSize
)
```
**Since 0.6.23.** The same page as `getJpqlQueryResultAsPage`, fetched in two statements: the identifiers of the
page first — `select distinct <alias>.<id>, <sort columns> <the select's own from clause> where … order by …,
<alias>.<id>` with the offset and the limit — then `jpqlQueryString where <alias>.<id> in (:rsqlPageIds)`,
unsorted and unlimited, with the rows put back in the order of the first statement. The count is unchanged and
runs once. This is what `RsqlQueryService` calls under `PagingStrategy.IDS_THEN_HYDRATE`.

- `rowIdExtractor` — reads the identifier off a hydrated row; `null` means `PersistenceUnitUtil.getIdentifier`,
  which is right for an entity row and throws, with a message that says what to pass, for a DTO.
- `hydrationChunkSize` — the most identifiers bound to one hydration statement; `DEFAULT_HYDRATION_CHUNK_SIZE`
  is 1000. A longer page is hydrated in several statements.

Falls back to `getJpqlQueryResultAsPage` for an unpaged `Pageable`, an entity without a single basic identifier,
and a select without a top-level `from`. The select's `from` clause is reused whole, `join fetch` demoted to
`join`, so that the filter keeps the join semantics it has in the single statement.

#### getJpqlQueryCount
```java
public static <ENTITY> Long getJpqlQueryCount(
    Class<ENTITY> entityClass,
    String countQueryString,
    String countAlias,
    String filter,
    RsqlContext<ENTITY> rsqlContext,
    RsqlCompiler<ENTITY> compiler
)
```
Runs the count query alone. This is the one JPQL method that really does rewrite the alias: when `countAlias` is
not `a0` it calls `RsqlCompiler.replaceAlias(rsqlQuery, "a0", countAlias)`, so `SELECT COUNT(p) FROM Product p`
with `countAlias = "p"` works. There is no `order by` here, so nothing else consumes the argument.

#### getOrderByWithAlias
```java
public static String getOrderByWithAlias(Sort sort, String alias)
```
Renders a Spring Data `Sort` as an ` order by alias.prop DIRECTION, …` fragment, or the empty string when the
sort is empty. Useful when assembling JPQL by hand.

#### getPropertyPathRecursive
```java
public static <ENTITY> Path<?> getPropertyPathRecursive(
    String fieldName,
    Path<?> startRoot,
    RsqlContext<ENTITY> rsqlContext,
    Map<String, Path<?>> joinsMap,
    Map<String, ManagedType<?>> classMetadataMap
)
```
Resolves a dotted property path to a JPA `Path`, creating and caching the JOINs it needs in `joinsMap`. This is
the join-reuse mechanism the rest of the library is built on; pass the same map to every clause of a query so
that a path used in both WHERE and SELECT produces one JOIN.

> **Since 0.6.22 the WHERE path has one exception.** A selector ending in the identifier of a to-one
> association — `customer.id` — is resolved against the querying table's own foreign key column and creates no
> JOIN at all, so it also puts nothing in `joinsMap`. Nothing else changes: a clause that needs the
> association for any other reason still creates and caches the JOIN, and a selector whose association is
> already in `joinsMap` still reuses it rather than taking the shortcut. The count is therefore never higher
> and never lower than what the other clauses require.
>
> WHERE, SELECT and GROUP BY share one decision about this, so they cannot disagree about whether a given
> selector needs a JOIN — a SELECT that read the foreign key while its GROUP BY read the joined column would
> name two different things for one field. HAVING resolves paths as before. A query whose SELECT names
> `customer.name` still has one JOIN, with `customer.id` reading the foreign key column beside it. See
> [Filtering on the id of a to-one association](README.md#filtering-on-the-id-of-a-to-one-association) for the
> conditions and [`RsqlContext`](#foreign-key-id-resolution) for turning it off.

## AggregateQueryBuilder

The `AggregateQueryBuilder` class encapsulates all components needed for building aggregate queries with SELECT, GROUP BY, and HAVING clauses.

### Overview

`AggregateQueryBuilder<ENTITY>` is returned by `RsqlQueryService.createAggregateQuery()` and provides a convenient way to build complex aggregate queries. It maintains shared state (like joins maps) to ensure consistency between SELECT, GROUP BY, and HAVING clauses.

This class is analogous to how `Specification` encapsulates WHERE clause logic - it encapsulates SELECT + GROUP BY + HAVING logic.

### Methods

#### getSelections
```java
public List<Selection<?>> getSelections()
```
Returns the SELECT clause selections for use with `CriteriaQuery.multiselect()`.

**Returns:** List of selections (aggregate functions and grouping fields)

**Example:**
```java
AggregateQueryBuilder<Product> aggQuery = queryService.createAggregateQuery(
    "productType.name:category, COUNT(*):productCount, SUM(price):total",
    builder, root
);

query.multiselect(aggQuery.getSelections());
```

#### getGroupByExpressions
```java
public List<Expression<?>> getGroupByExpressions()
```
Returns the GROUP BY clause expressions for use with `CriteriaQuery.groupBy()`.

**Returns:** List of expressions representing GROUP BY fields

**Example:**
```java
query.groupBy(aggQuery.getGroupByExpressions());
```

#### createHavingPredicate
```java
public Predicate createHavingPredicate(String havingFilter, RsqlCompiler<ENTITY> compiler)
```
Creates a HAVING clause predicate from a HAVING filter string. Uses internal state (SELECT fields, joins map, etc.) to ensure consistency with SELECT and GROUP BY clauses.

**Parameters:**
- `havingFilter` - RSQL HAVING filter string (can be null or empty)
- `compiler` - RSQL compiler for parsing the filter

**Returns:** HAVING Predicate, or null if havingFilter is null/empty

**Throws:** `SyntaxErrorException` if HAVING filter has syntax errors; `IllegalArgumentException` if a bare field
in the filter is neither a SELECT alias nor one of the GROUP BY fields

**HAVING filter can reference:**
- Aliases from SELECT clause: `"totalPrice=gt=10000;productCount=ge=5"`
- Aggregate functions directly: `"SUM(price)=gt=50000;COUNT(*)=ge=10"`
- Logical operators: `;` (AND), `,` (OR), parentheses for grouping
- Operators: `==`, `!=`, `=gt=`, `=ge=`, `=lt=`, `=le=`, `=bt=`, `=nbt=`, `=in=`, `=nin=`, `=like=`, `=nlike=`. The case-sensitive `=clike=` / `=cnlike=` are WHERE-only

**Important:** An alias referenced from HAVING must not be a HAVING keyword - `count`, `avg`, `sum`, `min`,
`max`, `grp`, `all`, `dist`, `and`, `or`, `null`, `true`, `false`. `"count=ge=10"` fails to parse whatever the
SELECT string called the column.

**Example:**
```java
// Filter by aliases
Predicate having = aggQuery.createHavingPredicate("total=gt=50000;productCount=ge=10", compiler);
query.having(having);

// Filter by aggregate functions
Predicate having2 = aggQuery.createHavingPredicate("SUM(price)=gt=50000;AVG(price)=bt=(100,500)", compiler);

// Complex HAVING with OR
Predicate having3 = aggQuery.createHavingPredicate("(total=gt=100000,avgPrice=gt=500);productCount=ge=5", compiler);
```

#### getSelectFields
```java
public List<AggregateField> getSelectFields()
```
Returns the parsed aggregate fields from the SELECT string. This is useful for advanced scenarios where you need access to field metadata.

**Returns:** List of AggregateField objects with field paths, functions, and aliases

**Example:**
```java
List<AggregateField> fields = aggQuery.getSelectFields();
for (AggregateField field : fields) {
    System.out.println("Field: " + field.getFieldPath() +
                      ", Function: " + field.getFunction() +
                      ", Alias: " + field.getAlias());
}
```

#### getGroupByFieldNames
```java
public List<String> getGroupByFieldNames()
```
Returns the GROUP BY field names. This is useful for debugging or for advanced HAVING filter validation.

**Returns:** List of GROUP BY field paths

**Example:**
```java
List<String> groupByFields = aggQuery.getGroupByFieldNames();
System.out.println("Grouping by: " + String.join(", ", groupByFields));
```

### Complete Example

```java
import jakarta.persistence.criteria.*;
import jakarta.persistence.Tuple;
import rsql.helper.AggregateQueryBuilder;

// Setup
CriteriaBuilder builder = em.getCriteriaBuilder();
CriteriaQuery<Tuple> query = builder.createQuery(Tuple.class);
Root<Product> root = query.from(Product.class);
RsqlCompiler<Product> rsqlCompiler = queryService.getRsqlCompiler();

// Create aggregate query builder
AggregateQueryBuilder<Product> aggQuery = queryService.createAggregateQuery(
    "productType.name:category, status, COUNT(*):productCount, SUM(price):total, AVG(price):avgPrice",
    builder, root
);

// Build complete query
query.multiselect(aggQuery.getSelections());
query.groupBy(aggQuery.getGroupByExpressions());

// Add WHERE clause (filters rows BEFORE aggregation)
Specification<Product> whereSpec = queryService.createSpecification("createdDate=ge=#2024-01-01#");
query.where(whereSpec.toPredicate(root, query, builder));

// Add HAVING clause (filters groups AFTER aggregation)
query.having(aggQuery.createHavingPredicate(
    "total=gt=50000;productCount=ge=10;avgPrice=bt=(100,500)",
    rsqlCompiler
));

// Add ORDER BY - order by a SELECT expression; the index is the position in the
// SELECT string, so 3 is SUM(price):total
query.orderBy(builder.desc((Expression<?>) aggQuery.getSelections().get(3)));

// Execute
List<Tuple> results = em.createQuery(query).getResultList();

// Process results
for (Tuple row : results) {
    String category = (String) row.get("category");
    String status = (String) row.get("status");
    Long count = (Long) row.get("productCount");
    BigDecimal total = (BigDecimal) row.get("total");
    Double avg = (Double) row.get("avgPrice");

    System.out.printf("%s (%s): %d items, total $%s, avg $%.2f%n",
        category, status, count, total, avg);
}
```

For a query that needs nothing more than this, `getAggregateResultAsPage()` builds the same thing in one call
and sorts by alias: `getAggregateResultAsPage(select, filter, having, PageRequest.of(0, 20, Sort.by("total").descending()))`.

## TupleConverter

`rsql.helper.TupleConverter` turns the `Tuple` results of the SELECT and aggregate methods into plain maps, which
Jackson serialises to JSON without any further help. Two static methods, no state:

```java
public static Map<String, Object> toMap(Tuple tuple)
public static List<Map<String, Object>> toMapList(List<Tuple> tuples)
```

Each column becomes one entry keyed on its alias, in the order the SELECT string wrote it - the map is a
`LinkedHashMap`. Null values are kept.

**Example:**
```java
List<Tuple> tuples = queryService.getTupleWithSelect(
    "code:productCode, name, productType.name:typeName, price",
    "status==#ACTIVE#",
    PageRequest.of(0, 100)
);

List<Map<String, Object>> rows = TupleConverter.toMapList(tuples);
// [{productCode=P1, name=A, typeName=Type One, price=100.00}]
```

**Important:** the key is the column's alias and nothing else. A column with no alias is keyed on `null`, and a
`LinkedHashMap` has one `null` key - so several unaliased columns collapse into a single entry holding the last
of them. Give every element an explicit `:alias` before converting. See [SELECT.md](SELECT.md) for which SELECT
forms default an alias and which do not.

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

#### compileToSpecification (shared joins overload)
```java
public Specification<T> compileToSpecification(
    String inputString,
    RsqlContext<T> rsqlContext,
    boolean clearJoinsMapOnToPredicate
)
```
The same, with control over the joins map. The two-argument form is this one with `true`.

**Parameters:**
- `clearJoinsMapOnToPredicate` - `true` wraps the Specification so that `rsqlContext.joinsMap` and `classMetadataMap` are cleared on every `toPredicate()` call. That is required for Spring Data JPA repository methods, which call `toPredicate()` twice - once for the query, once for the count - with a different `Root` each time, and Hibernate 6 SQM nodes cannot be reused across roots. Pass `false` for a hand-built aggregate `CriteriaQuery`, where the joins map has to be shared across SELECT, WHERE, GROUP BY, HAVING and ORDER BY so that one JOIN serves all of them

**Returns:** JPA Specification, or `null` for a `null`/empty filter

#### compileToFilterNode
```java
public FilterNode compileToFilterNode(String inputString)
```
Compiles an RSQL string into a neutral filter tree, for describing or rewriting a filter rather than executing
it. No `RsqlContext` is needed, since nothing is resolved against the entity.

**Parameters:**
- `inputString` - RSQL filter expression; `null` or blank gives `null`

**Returns:** The tree root, a `FilterGroup` or a `FilterCondition`, or `null` for an empty filter

**Throws:** `SyntaxErrorException` if the RSQL expression is invalid

See [RsqlFilterDescription](#rsqlfilterdescription) for what to do with it.

#### compileSelectToExpressions
```java
public List<SelectExpression> compileSelectToExpressions(
    String selectString,
    RsqlContext<T> rsqlContext
)
```
Compiles a SELECT string with arithmetic expressions into a list of SelectExpression objects.

**Important:** This is the only SELECT compilation method that supports arithmetic expressions. It is what
`getAggregateResultWithExpressions()` and `getAggregateResultAsPageWithExpressions()` use internally; the
`compileSelectTo*` helpers below and the two plain aggregate methods go through the aggregate-field parser,
which rejects operators.

**Parameters:**
- `selectString` - SELECT expression with arithmetic (e.g., `"code, SUM(price) * 1.2:totalWithTax"`)
- `rsqlContext` - Context containing entity information

**Returns:** List of SelectExpression objects representing the parsed SELECT clause

**Throws:** `SyntaxErrorException` if the SELECT expression is invalid

**Example:**
```java
RsqlCompiler<Product> compiler = new RsqlCompiler<>();
RsqlContext<Product> context = new RsqlContext<>(Product.class);
context.defineEntityManager(entityManager);

List<SelectExpression> expressions = compiler.compileSelectToExpressions(
    "category, SUM(price) - 100:adjustedTotal, COUNT(*):productCount",
    context
);

// Use expressions to build JPA query
for (SelectExpression expr : expressions) {
    Expression<?> jpaExpr = expr.toJpaExpression(builder, root, context);
    // Add to query...
}
```

**SelectExpression Types:**
- `FieldExpression` - Simple field reference (e.g., `category`)
- `FunctionExpression` - Aggregate function (e.g., `SUM(price)`, `COUNT(*)`)
- `BinaryOpExpression` - Arithmetic operation (e.g., `SUM(price) - 100`)
- `LiteralExpression` - Numeric literal (e.g., `100`, `1.2`)

**Supported Operators:**
- `+` - Addition
- `-` - Subtraction
- `*` - Multiplication
- `/` - Division
- `()` - Parentheses for precedence

#### compileSelectToFields and friends
```java
public List<SelectField> compileSelectToFields(String selectString, RsqlContext<T> rsqlContext)
public List<String> compileSelectToFieldPaths(String selectString, RsqlContext<T> rsqlContext)
public List<AggregateField> compileSelectToAggregateFields(String selectString, RsqlContext<T> rsqlContext)
public List<String> compileSelectToGroupByFields(String selectString, RsqlContext<T> rsqlContext)
```
The non-expression SELECT parsers. None of them accepts arithmetic.

| Method | Returns |
|---|---|
| `compileSelectToFields` | `List<SelectField>` - field path plus optional alias. An aggregate function is a syntax error here |
| `compileSelectToFieldPaths` | `List<String>` - the same, aliases dropped, for APIs that want plain paths |
| `compileSelectToAggregateFields` | `List<AggregateField>` - field path, aggregate function and alias; plain fields come back with function `NONE` |
| `compileSelectToGroupByFields` | `List<String>` - the field paths of the elements with function `NONE`, which is exactly the GROUP BY list the aggregate query methods derive from a SELECT string |

**Note:** A `null` or blank `selectString` returns an empty list rather than throwing.

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

**Note:** Only the parameters the compiler generated for literal values are bound here. A named parameter the
filter wrote itself - `price=gt=:min` - is the caller's to bind.

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

> **Since 0.6.20 — LIKE patterns are self-contained.** `RsqlQuery.where` now carries an explicit
> `ESCAPE '\'` clause and the pattern has its backslashes escaped, so the text is safe to execute as native
> SQL: a backslash matches literally and a pattern may end with one. The 0.6.19 restriction (reject values
> ending in `\` for pattern searches) no longer applies.
>
> ⚠ Verified on PostgreSQL. The emitted `escape '\'` is a valid string literal there, on DB2, Oracle, H2 and
> SQL Server, but **not on MySQL/MariaDB** without `NO_BACKSLASH_ESCAPES`; those are outside the supported
> native-SQL contract.

---

## RsqlWhereString

Renders an RSQL WHERE filter as JPQL-ish text and nothing more. It deliberately does not map the filter onto an
entity: there is no `EntityManager`, no entity class and no metamodel validation, so `nosuchfield==1` renders
happily. Use it to inspect or log what a filter compiles to, not to build a query.

```java
public String parseString(String inputString)
public String parseFile(String inputFile) throws IOException
```

**Returns:** The WHERE text, without the leading `where`

**Throws:** `SyntaxErrorException` if the filter does not parse; `IOException` from `parseFile` if the file
cannot be read

**Example:**
```java
new RsqlWhereString().parseString("name=*'*Type*';price=bt=(:lo,:hi)");
// lower(name) like '%type%' escape '\' and price between :lo and :hi
```

## RsqlContext

`rsql.where.RsqlContext<ENTITY>` carries the JPA pieces one query needs — the root, the `CriteriaBuilder`, the
`CriteriaQuery`, and the joins and metadata maps shared between clauses. `RsqlQueryService` builds a fresh one
per query through `createNewInstance()`, so a service is safe to hold as a singleton.

### Foreign key id resolution

```java
public boolean useForeignKeyIdShortcut = true;
public Map<String, Boolean> foreignKeyIdShortcutOverrides = new HashMap<>();

public RsqlContext<ENTITY> withForeignKeyIdShortcutFor(String... associationPaths);
public RsqlContext<ENTITY> withoutForeignKeyIdShortcutFor(String... associationPaths);
public boolean isForeignKeyIdShortcutEnabledFor(String associationPath);
public void copyForeignKeyIdShortcutSettingsFrom(RsqlContext<?> source);
```

**Since 0.6.22.** Decides whether a WHERE selector ending in the identifier of a to-one association is
resolved against the querying table's own foreign key column instead of through a `LEFT JOIN`. See
[Filtering on the id of a to-one association](README.md#filtering-on-the-id-of-a-to-one-association) for what
the shortcut covers and when it stands aside.

`useForeignKeyIdShortcut` is the default for every association; `foreignKeyIdShortcutOverrides` decides for the
ones named in it. Between the two, all four arrangements are expressible with no rule about which wins:

| Wanted | `useForeignKeyIdShortcut` | Overrides |
|---|---|---|
| Every association reads its foreign key (the default) | `true` | empty |
| Every association joins, as before 0.6.22 | `false` | empty |
| Only `ownerOrg` and `ownerCompany` read the foreign key | `false` | both `true` |
| Everything but `legacyOwner` reads the foreign key | `true` | `legacyOwner` `false` |

A key is the association path exactly as the filter writes it, **without** the identifier segment: `"ownerOrg"`
covers `ownerOrg.id`, and `"parent.parent.productType"` covers `parent.parent.productType.id`. One association
reached by two paths is two keys.

```java
RsqlContext<Document> context = new RsqlContext<>(Document.class).defineEntityManager(entityManager);
context.useForeignKeyIdShortcut = false;
context.withForeignKeyIdShortcutFor("ownerOrg", "ownerCompany");

Specification<Document> specification = compiler.compileToSpecification(filter, context);
```

Both settings survive `createNewInstance()` (the map is copied, not shared) and are carried into the separate
context a paged query builds for its count, so the count resolves the filter the same way the page does.

`RsqlQueryService` keeps its own copies rather than writing into the long-lived context it derives every query
from, and stamps them onto each fresh context. Reconfiguring a published service is therefore safe: the map is
replaced whole rather than updated in place, so a concurrent query sees one configuration or the other and
never half of one.

`RsqlQueryService` exposes the same four operations — `setUseForeignKeyIdShortcut`,
`getUseForeignKeyIdShortcut`, `withForeignKeyIdShortcutFor` and `withoutForeignKeyIdShortcutFor` — so a service
can be configured once instead of before every call:

```java
productService.getQueryService().setUseForeignKeyIdShortcut(false);
productService.getQueryService().withForeignKeyIdShortcutFor("ownerOrg", "ownerCompany");
```

## Parser Limits

Two static limits bound the recursion of the parser and of the visitors that walk its output. They live on
`rsql.where.RsqlWhereTreeParser` but govern WHERE, HAVING and SELECT alike, and they are JVM-wide: setting one
changes it for every `RsqlQueryService` in the process.

```java
public static final int DEFAULT_MAX_NESTING_DEPTH = 100;
public static final int DEFAULT_MAX_TREE_DEPTH = 500;

public static int getMaxNestingDepth();
public static void setMaxNestingDepth(int value);
public static int getMaxTreeDepth();
public static void setMaxTreeDepth(int value);
```

| Limit | Bounds | Over it |
|---|---|---|
| `maxNestingDepth`, default `100` | How deeply parentheses may nest, counted on the token stream before the parser runs | `SyntaxErrorException`: *"Filter is nested too deeply at position N - at most 100 levels of parentheses are allowed"* |
| `maxTreeDepth`, default `500` | How deep the resulting parse tree may be. A left-deep chain such as `a==1;a==1;…` adds one level per condition, while a balanced filter of 8 192 conditions is about 32 levels | `SyntaxErrorException`: *"Filter is structured too deeply - at most 500 levels of nested conditions are allowed"* |

The point of both is that an over-deep filter raises a catchable `SyntaxErrorException` instead of a
`StackOverflowError`. Raising a limit past the stack's real capacity gives the `StackOverflowError` back.

---

## RsqlFilterDescription

Turns a WHERE filter into text a reader understands. Purely textual: no `EntityManager` and no `RsqlContext`,
because the description is built from the parse tree alone. Deep or malformed input is rejected by the same tree
parser the query path uses, so any filter that can be executed can also be described.

> **Unreleased.** `rsql.describe` sits under `[Unreleased]` in the CHANGELOG and is in no tagged version yet.

```java
RsqlFilterDescription describer = new RsqlFilterDescription();
```

### Describing Methods

#### describe
```java
public FilterDescription describe(String filter)
public FilterDescription describe(String filter, FilterLabelResolver labels)
public FilterDescription describe(String filter, FilterLabelResolver labels, Map<String, Object> parameters)
```
Parses a filter and describes it.

**Parameters:**
- `filter` - RSQL filter expression; `null` or blank gives an empty description
- `labels` - How the parts are named; defaults to `FilterLabelResolver.TECHNICAL`. Must not be `null`
- `parameters` - Values for `:name` placeholders. A parameter that is absent prints as `:name`; one bound to
  `null` prints as `null`

**Returns:** A `FilterDescription`

**Throws:** `SyntaxErrorException` if the filter does not parse; `NullPointerException` if `labels` is `null`

#### describeNode
```java
public FilterDescription describeNode(FilterNode root, FilterLabelResolver labels)
public FilterDescription describeNode(FilterNode root, FilterLabelResolver labels, Map<String, Object> parameters)
```
The same, for a tree already parsed or built by hand. Named differently rather than overloaded so that a `null`
argument needs no cast.

#### parse
```java
public FilterNode parse(String filter)
```
Parses a filter into a tree without describing it, for an application that wants to inspect or rewrite it.
Returns `null` for a blank filter. `RsqlCompiler.compileToFilterNode(String)` does the same.

**Example:**
```java
RsqlFilterDescription describer = new RsqlFilterDescription();
FilterLabelResolver labels = new MapFilterLabelResolver(Map.of(
    "productType.name", "Product type",
    "price", "Price",
    "status", "Status"));

FilterDescription description = describer.describe(
    "productType.name=*'A*';(price=gt=:min,status==#ACTIVE#)",
    labels,
    Map.of("min", 100));

description.getText();
// Product type starts with (ignoring case) "A" and (Price is greater than 100 or Status is "ACTIVE")
```

### FilterDescription

| Method | Returns |
|---|---|
| `getRoot()` | The tree. The faithful form - and the only one that bypasses the resolver, so also the raw one |
| `getRows()` | `List<FilterRow>`, ready for `JRBeanCollectionDataSource` |
| `getText()` | The whole filter on one line |
| `getText(int maxLength)` | The same, truncated with an ellipsis. Throws `IllegalArgumentException` if `maxLength` is not positive |
| `isPureAndChain()` | Whether the filter contains no `OR`, in which case a plain table renders it exactly |
| `isEmpty()` | Whether the filter was empty |

### FilterRow

A JavaBean, so JasperReports can introspect it.

| Property | Meaning |
|---|---|
| `field` | The left-hand side |
| `operator` | The operator |
| `value` | The right-hand side; empty for `IS NULL` / `IS NOT NULL` |
| `connector` | The `and` / `or` in front of this row, `null` for the first |
| `depth` | Nesting level, `0` at the top |
| `openGroups` | Parentheses opening before this row |
| `closeGroups` | Parentheses closing after it |

`connector` carries the junction of the lowest common ancestor of this row and the previous one, so reading the
rows top to bottom reproduces the filter.

### FilterLabelResolver

Every method has a default, so an implementation overrides only what it changes. `FilterLabelResolver.TECHNICAL`
is the all-defaults instance: technical paths and English operators.

| Method | Purpose |
|---|---|
| `operandLabel(Operand)` | The left-hand side |
| `operatorLabel(FilterCondition)` | The operator. Takes the whole condition because the four LIKE forms read differently depending on where the wildcards are |
| `valueLabel(Operand, Object)` | A value. Strings go through `FilterLabelResolver.quote(String)` by default |
| `patternValueLabel(Operand, Object, PatternShape, String)` | A LIKE value; shows the needle rather than the raw pattern, since the operator label already says "starts with" |
| `parameterLabel(Operand, String, ParameterResolution)` | A `:name` placeholder |
| `rightSideLabel(Operand, RightSide)` | A right-hand side this version does not know |
| `junctionLabel(FilterGroup.Junction)` | `and` / `or` |
| `joinList(List<String>)`, `joinRange(String, String)` | How rendered parts are joined |

Supplied implementations:

- `MapFilterLabelResolver(Map<String, String> fieldLabels[, FilterLabelResolver delegate])` - field labels from
  a map; unknown paths keep their technical form
- `ResourceBundleFilterLabelResolver(ResourceBundle bundle[, String prefix[, FilterLabelResolver delegate]])` -
  fields, operators and junctions from a bundle. Keys: `field.<path>`, `operator.<NAME>`,
  `operator.<NAME>.<SHAPE>` for the LIKE family, `junction.AND` / `junction.OR`. A missing key falls back to the
  delegate
- `DelegatingFilterLabelResolver(FilterLabelResolver delegate)` - forwards everything, as a base for a resolver
  that overrides one or two methods without discarding a collaborator

`<NAME>` is a `FilterOperator` constant. There are sixteen, and the enumeration is closed:

| `FilterOperator` | Written in the filter as |
|---|---|
| `EQ`, `NEQ` | `==`, `!=` (or `=!`) |
| `LT`, `GT`, `LE`, `GE` | `=lt=`, `=gt=`, `=le=`, `=ge=` |
| `LIKE`, `NLIKE` | `=like=` / `=*`, `=nlike=` / `=!*` / `!=*` |
| `CLIKE`, `CNLIKE` | `=clike=` / `=^*`, `=cnlike=` / `=!^*` / `!=^*` |
| `IN`, `NIN` | `=in=`, `=nin=` |
| `BT`, `NBT` | `=bt=`, `=nbt=` |
| `IS_NULL`, `IS_NOT_NULL` | `==null`, `!=null` |

The last two are derived rather than grammatical: the filter writes `operatorBasic NULL`, and the description
follows the unary predicate the library executes.

Only the four LIKE forms take a `<SHAPE>` suffix; the other twelve are keyed by name alone. `<SHAPE>` is a
`PatternShape` constant, read off the wildcards in the value: `EXACT` (`'abc'`), `STARTS_WITH` (`'abc*'`),
`ENDS_WITH` (`'*abc'`), `CONTAINS` (`'*abc*'`) and `CUSTOM` - a `*` in the middle (`'A*B'`), or a `%` or `_`,
which this library leaves as SQL wildcards and therefore refuses to describe. A sixth constant, `NONE`, is what a
LIKE gets when its right-hand side is not a string literal at all (`name=like=otherField`), so
`operator.LIKE.NONE` is a reachable key too. A complete bundle is therefore 12 + 4 × 6 = 36 operator keys, plus
`junction.AND`, `junction.OR` and one `field.<path>` per field. Generate them with the public
`ResourceBundleFilterLabelResolver.operatorKey(FilterCondition)`, `fieldKey(String)` and
`junctionKey(FilterGroup.Junction)` rather than typing them out.

### The Filter Tree

`FilterNode` is `sealed`, permitting `FilterGroup` (a `FilterGroup.Junction` - `AND` or `OR` - and its children)
and `FilterCondition` (a left-hand side, a `FilterOperator`, and a `RightSide`). The tree is normalised: nested
groups sharing a junction are flattened, and a group of one child is replaced by that child, so `a;b;c` is one
group of three.

`RightSide` has six built-in shapes: `SingleValue`, `NoValue` (for `IS NULL`), `FieldRef`, `Parameter`,
`ValueList` (for `IN`) and `Range` (for `BETWEEN`). It is not `sealed` - an unknown shape is rendered through
`rightSideLabel`, so masking still applies.

`ValueList` and `Range` hold `ListItem`, a sealed interface over `ItemValue(Object value)`,
`ItemField(String fieldPath)` and `ItemParam(String name)` - because the grammar allows all three in those
positions: `status=in=(#ACTIVE#,#PENDING#)`, `code=in=(status,name)` and `price=bt=(:lo,:hi)` are all legal, and
a plain `List<Object>` could not tell a field reference from a string that looks like one.

`FilterCondition` validates itself: an operator and a right-hand side that contradict each other are rejected,
and for a LIKE condition the `PatternShape` and the needle must be the ones actually derived from the value.

---

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

### Named Parameters
The right-hand side of a condition may be a `:name` placeholder instead of a literal, and a field path may
appear where a value is expected.

```java
// Named parameter as the value
queryService.findByFilter("name==:p");

// Named parameters as BETWEEN bounds
queryService.findByFilter("price=bt=(:lo,:hi)");

// Field compared to another field
queryService.findByFilter("price=gt=cost");

// A field as an IN element or a BETWEEN bound
queryService.findByFilter("code=in=(status,name)");
queryService.findByFilter("price=bt=(minPrice,maxPrice)");
```

The caller binds the named parameters on the query - only the parameters the compiler generated for literal
values are bound automatically.

Two limits. A named parameter inside an `IN` or `NIN` list (`code=in=(:p1,:p2)`) parses, and
[RsqlWhereString](#rsqlwherestring) renders it, but every path that resolves against the metamodel - the
Specification and `compileToRsqlQuery` alike - throws `IllegalArgumentException("Unknown property: :p1")`. And a
parameter is rejected as the right-hand side of the whole LIKE family - `=like=`, `=nlike=`, `=clike=`,
`=cnlike=` - with a `SyntaxErrorException` such as *"Not supported like with parameter :p"*.

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

// DateTime comparison - the zone is mandatory
queryService.findByFilter("lastLogin=le=#2024-01-01T23:59:59Z#");
queryService.findByFilter("lastLogin=le=#2024-01-01T23:59:59+01:00#");

// Date range
queryService.findByFilter("createdDate=bt=(#2024-01-01#,#2024-12-31#)");
```

A datetime literal is bound in the type of the attribute it is compared with (since 0.6.24; earlier
versions always bound an `Instant`). Against a moment - `Instant`, `OffsetDateTime`, `ZonedDateTime`, `Date` -
it must carry a zone, `Z` or an offset such as `+01:00`, and equivalent offsets select the same rows. Against a
`LocalDateTime` or `LocalDate` the calendar fields are compared exactly as written: the zone is optional and
ignored, so `#2024-01-01T23:59:59#` and `#2024-01-01T23:59:59Z#` both compare 23:59:59 whatever zone the JVM
or the JDBC connection is in. A literal without a zone against a moment is a `SyntaxErrorException`. Fractional
seconds are optional; a plain date `#2024-01-01#` needs no zone, and against a `LocalDateTime` it is the start
of that day. See [Dates, datetimes and time zones](README.md#dates-datetimes-and-time-zones).

```java
queryService.findByFilter("localCreatedAt=ge=#2024-01-01T08:00:00#");        // LocalDateTime: 08:00 as written
queryService.findByFilter("createdDate=ge=#2024-01-01T08:00:00+01:00#");     // Instant: that moment
```

### Pattern Matching
```java
// Contains (case-insensitive: generates lower(name) like '%john%')
queryService.findByFilter("name=like='*john*'");

// Starts with
queryService.findByFilter("email=like='john*'");

// Ends with
queryService.findByFilter("email=like='*@example.com'");

// Case-sensitive LIKE: =clike= (alias =^*) - no lower(), pattern keeps its case
// e.g. orgPath=clike='|ABC|*' generates orgPath like '|ABC|%' (index-friendly)
queryService.findByFilter("orgPath=clike='|ABC|*'");

// Case-sensitive NOT LIKE: =cnlike= (alias =!^* / !=^*)
queryService.findByFilter("orgPath=cnlike='|ABC|*'");
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