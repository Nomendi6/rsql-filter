# RSQL Filter

[![Maven Central](https://img.shields.io/maven-central/v/com.nomendi6/rsql-filter.svg)](https://maven-badges.herokuapp.com/maven-central/com.nomendi6/rsql-filter)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

RSQL Filter is a Java library for Spring Boot JPA applications that provides a simple and convenient way to filter data in REST APIs using RSQL (RESTful Service Query Language) syntax.

## What is RSQL?

RSQL (RESTful Service Query Language) is a query language for parametrized filtering of entries in RESTful APIs. It's based on FIQL (Feed Item Query Language) – an URI-friendly syntax for expressing filters across the entries in an Atom Feed.

RSQL provides a simple and intuitive syntax that allows clients to filter data without exposing the underlying database structure.

## Quick Start

1. Add the dependency to your project
2. Extend your repository with **both** `JpaRepository<Entity, Long>` and `JpaSpecificationExecutor<Entity>`
3. Create a `RsqlQueryService` in your service, with a mapper that implements `rsql.mapper.EntityMapper<Dto, Entity>`
4. Use the filter parameter in your REST endpoints

Steps 2 and 3 are compile-time requirements, not conventions: they are the bounds on the type parameters of
`RsqlQueryService` (`REPOS extends JpaRepository<ENTITY, Long> & JpaSpecificationExecutor<ENTITY>`,
`MAPPER extends EntityMapper<ENTITY_DTO, ENTITY>`). The id type is fixed to `Long`, so an entity keyed by
`UUID` or `String` cannot use this service. A JHipster-generated MapStruct mapper already matches
`EntityMapper`.

See the [complete example](#example-rest-controller) below.

For detailed API documentation, see [API.md](API.md).

## Installation

The library is maintained in two parallel lines with the same feature set. Pick the one that matches your
platform: **0.7.6** for Spring Boot 4, **0.6.21** for Spring Boot 3. The snippets below use `0.7.6`.

### Maven
```xml
<dependency>
    <groupId>com.nomendi6</groupId>
    <artifactId>rsql-filter</artifactId>
    <version>0.7.6</version>
</dependency>
```

### Gradle
```gradle
implementation 'com.nomendi6:rsql-filter:0.7.6'
```

### Requirements
- **0.6.x** — Java 17 or higher, Spring Boot 3.x, Hibernate 6.x
- **0.7.x** — Java 21 or higher, Spring Boot 4.x, Hibernate 7.x

**Note:** `rsql-filter-demo` ships only with the `0.6.x` line. It depends on JHipster 8 / Spring Boot 3 and is
excluded from `0.7.x` releases until it is migrated.

## Features

- **Simple Query Language**: Intuitive syntax for filtering data
- **Type Safety**: Automatic type conversion and validation
- **JPA Integration**: Seamless integration with Spring Data JPA
- **Rich Set of Operators**: Comprehensive set of comparison operators
- **Case-Sensitive LIKE**: `=clike=` / `=cnlike=` next to the case-insensitive `=like=` / `=nlike=`, so an index stays usable
- **Complex Queries**: Support for nested queries with AND/OR logic
- **Named Parameters**: `:name` on the right-hand side of a condition, bound by the application
- **Field-to-Field Comparison**: `price=gt=cost`, and a field as an `=in=` element or an `=bt=` bound
- **Sorting**: Built-in support for sorting results
- **Pagination**: Full Spring Data pagination support
- **SELECT Queries**: Flexible field selection with aliases and navigation properties
- **Arithmetic Expressions**: Support for `+`, `-`, `*`, `/` operators in the `*WithExpressions` aggregate methods
- **Aggregate Functions**: COUNT, SUM, AVG, MIN, MAX, GRP with automatic GROUP BY
- **HAVING Clause**: Filter aggregated results with RSQL syntax, by SELECT alias or by aggregate function
- **LOV Queries**: List of Values queries for dropdowns/autocomplete
- **JPQL Generation**: Compile a filter to JPQL text instead of a `Specification` (`RsqlWhereString`, `RsqlCompiler.compileToRsqlQuery`)
- **ANTLR Based**: Robust parser built with ANTLR4
- **Filter Descriptions** *(unreleased)*: Turn a filter into readable text or table rows for a report header
- **Filter Tree** *(unreleased)*: A neutral `FilterGroup` / `FilterCondition` tree for inspecting or rewriting a filter (`RsqlCompiler.compileToFilterNode`)
- **Error Handling**: Detailed error messages for invalid queries, with hard limits on nesting and tree depth

## Usage

To use Rsql-filter in your application, you simply need to pass a **`filter`** parameter in your REST GET request. The value of the filter parameter should be a string that specifies the filtering criteria.

For example, to retrieve all records where the **`name`** field is equal to **`John`**, you can make the following request:

```http
GET /records?filter=name=='John'

```

You can also use logical operators and parentheses to build more complex filter expressions. For example, to retrieve all records where the **`name`** field is equal to **`John`** and the **`age`** field is greater than **`30`**, you can make the following request:

```http
GET /records?filter=name=='John';age=gt=30

```

The following table shows the list of supported operators:

| Operator | Description              |
|----------|--------------------------|
| ==       | Equal to                 |
| !=       | Not equal to             |
| =!       | Not equal to             |
| =*       | Like (case-insensitive)  |
| =like=   | Like (case-insensitive)  |
| =^*      | Like (case-sensitive)    |
| =clike=  | Like (case-sensitive)    |
| !=*      | Not like (case-insensitive) |
| =!*      | Not like (case-insensitive) |
| =nlike=  | Not like (case-insensitive) |
| !=^*     | Not like (case-sensitive) |
| =!^*     | Not like (case-sensitive) |
| =cnlike= | Not like (case-sensitive) |
| =gt=     | Greater than             |
| =ge=     | Greater than or equal to |
| =lt=     | Less than                |
| =le=     | Less than or equal to    |
| =in=     | In                       |
| =nin=    | Not in                   |
| =bt=     | Between                  |
| =nbt=    | Not between              |
| ==null   | Is null                  |
| !=null   | Is not null              |
| =!null   | Is not null              |
| ==true   | Equal to true            |
| !=true   | Not equal to true        |
| ==false  | Equal to false           |
| !=false  | Not equal to false       |

`=!` is an accepted spelling of `!=` everywhere, so `=!true` and `=!false` work as well. The word forms of the
operators and the `and` / `or` / `null` / `true` / `false` keywords are case-insensitive - `name=GT=5`,
`a==1 AND b==2` and `name==NULL` all parse. Field names are **not**: they must match the entity attribute
exactly.

#### Comparing against a field or a parameter

The right-hand side of a condition does not have to be a literal.

**Another field.** Supported by `==`, `!=`, `=gt=`, `=ge=`, `=lt=`, `=le=`:

```
price=gt=cost                  -> price>cost
validTo=lt=validFrom           -> validTo<validFrom
productType.code==code         -> productType.code=code
```

The `LIKE` family is not: `price=like=cost` parses, but building the specification fails with
`SyntaxErrorException: Unknown operator: =like=`.

**A field as an `=in=` element or an `=bt=` bound.** Any element of an `IN` list and either bound of a
`BETWEEN` may be a field instead of a literal:

```
code=in=(status,name)          -> code in (status,name)
price=bt=(minPrice,maxPrice)   -> price between minPrice and maxPrice
```

**A named parameter.** `:name` on the right-hand side becomes a named parameter that your application binds:

```
name==:userName                -> name=:userName
price=gt=:minPrice             -> price>:minPrice
price=bt=(:lo,:hi)             -> price between :lo and :hi
```

The library never supplies the value - it only emits the parameter, so this is for the JPQL path
(`RsqlWhereString.parseString`, `RsqlCompiler.compileToRsqlQuery`) or for a `CriteriaQuery` you build and
execute yourself. `RsqlCompiler.bindImplicitParametersForTypedQuery` / `bindImplicitParametersForQuery` bind
only the parameters the compiler generated for literals, not yours. A parameter cannot be used as a `LIKE`
pattern: `=like=`, `=nlike=`, `=clike=` and `=cnlike=` with `:p` all raise `SyntaxErrorException` on the
specification path.

The whole round trip, with one literal and one parameter of your own:

```java
RsqlCompiler<Product> compiler = new RsqlCompiler<>();
RsqlContext<Product> context = new RsqlContext<>(Product.class).defineEntityManager(entityManager);

RsqlQuery query = compiler.compileToRsqlQuery("status==#ACTIVE#;price=gt=:minPrice", context);
// query.where  -> a0.status=:p1 and a0.price>:minPrice
// query.params -> [RsqlQueryParam{name='p1', value=ACTIVE}]   the compiler's own, for the literal

TypedQuery<Product> typedQuery = entityManager.createQuery(
    "select a0 from Product a0 where " + query.where, Product.class);

RsqlCompiler.bindImplicitParametersForTypedQuery(query, typedQuery);  // binds p1 = ACTIVE
typedQuery.setParameter("minPrice", new BigDecimal("150"));           // you bind :minPrice

List<Product> result = typedQuery.getResultList();
```

The generated text refers to the root as `a0`, so the query you write it into has to declare that alias. The
compiler names its own parameters `p1`, `p2`, … — keep clear of those names. Leave yours unbound and the
query fails at execution with `org.hibernate.QueryParameterException: No argument for named parameter
':minPrice'`.

#### Case-sensitive vs. case-insensitive LIKE

The `LIKE` family comes in two variants. In all of them, the `*` in the pattern is mapped to the SQL `%` wildcard, and every predicate is
emitted with an explicit `ESCAPE '\'` clause so that a backslash always matches literally,
independently of the database and the Hibernate dialect.

- **Case-insensitive** (`=like=` / `=*`, `=nlike=` / `=!*` / `!=*`): both the column and the pattern are lower-cased, e.g. `name=like='A*'` generates `lower(name) like 'a%'`. This is unchanged.
- **Case-sensitive** (`=clike=` / `=^*`, `=cnlike=` / `=!^*` / `!=^*`): the column is **not** wrapped in `lower(...)` and the pattern keeps its original case, e.g. `name=clike='A*'` generates `name like 'A%'`. Use this when you need exact-case matching, or to keep a plain B-tree index usable (the case-insensitive variant is non-sargable because it wraps the column in `lower(...)`).

Notes:
- The actual case-sensitivity of `=clike=` ultimately depends on the database collation (e.g. H2 default and PostgreSQL are case-sensitive; MySQL with a `_ci` collation may still match case-insensitively).
- No LIKE operator escapes literal `%` / `_` - they stay SQL wildcards, so `code=like='50%'` also matches
  `500`. A literal backslash **is** escaped: since 0.7.5 / 0.6.20 the pattern is backslash-escaped and
  carries `ESCAPE '\'`, so `code=like='*C:\temp*'` matches the literal text `C:\temp`.
  Parameter-bound patterns are not supported for any of them - `field=like=:p`, `field=nlike=:p`,
  `field=clike=:p` and `field=cnlike=:p` all raise `SyntaxErrorException` when the specification is built.

#### Escaping the delimiter in string literals

A string literal can be delimited by `"`, `'` or `` ` ``. To use the delimiter itself inside the value,
**double it**:

```
name=="say ""hi"""      -> value: say "hi"
name=='it''s'           -> value: it's
name==`a``b`            -> value: a`b
```

Only the delimiter in use has to be doubled — the other two are ordinary characters, so the simplest
option is usually to pick a delimiter the value does not contain:

```
name=="it's"            -> value: it's        (no doubling needed)
name==`say "hi"`        -> value: say "hi"    (no doubling needed)
```

This works the same way in `WHERE` and in `HAVING`, and for every operator that takes a string
(`==`, `!=`, `=in=`, `=nin=`, `=bt=`, `=nbt=`, and the whole `LIKE` family in `WHERE`). `HAVING` has only
`=like=` / `=nlike=` - there is no case-sensitive variant there - and its `LIKE` does not map `*` to `%` and
does not lower-case either side.

**A backslash has no special meaning** — it is an ordinary character that is passed through unchanged, so a
value may also *end* with one:

```
name=="C:\dir\file"     -> value: C:\dir\file
name=="C:\dir\"         -> value: C:\dir\
```

Because doubling is the only escape mechanism and backslash is inert, **every value can be encoded**:

```
encode(value, delimiter) = delimiter + value.replace(delimiter, delimiter+delimiter) + delimiter
```

> **Changed in 0.7.4 / 0.6.19.** Earlier versions treated `\` as a lexer-level escape that protected the
> next character while staying in the value, which made a value ending in `\` impossible to write. A
> backslash placed immediately before the active delimiter (`name=="a\"b"`) used to parse and is now a
> syntax error — rewrite it by doubling the delimiter (`name=="a\""b"`).

> **Changed in 0.7.2 / 0.6.18.** Earlier versions accepted the doubled delimiter but did not collapse it
> back in the `WHERE` path, so `name=="say ""hi"""` searched for the literal text `say ""hi""` and
> silently returned nothing. If you worked around this by *not* doubling the delimiter, note that such a
> filter never parsed; if you doubled it and relied on the old (broken) result, the result will now change.

Supported data types:

| Data Type      | Description                                                                 |
|----------------|-----------------------------------------------------------------------------|
| String         | Expression in quotes ("" or '' or ``), for example `name=='Ana'`. See [Escaping the delimiter](#escaping-the-delimiter-in-string-literals) |
| Integer        | Integer number, for example `id==2345`                                      |
| Decimal number | Decimal number, for example `amount=gt=10.23`                               |
| Enum           | Enum name, for example `status==#ACTIVE#`                                   |
| Date           | Date in ISO format, for example `date=ge=#2019-01-01#`                      |
| Datetime       | Datetime in ISO format **with a zone**, for example `date=ge=#2019-01-01T00:00:00Z#` |
| Boolean        | Boolean value, for example `active==true` or `active==false`                |
| UUID           | UUID value, for example `uuidField=='f47ac10b-58cc-4372-a567-0e02b2c3d479'` |

The zone suffix on a datetime literal is mandatory — either `Z` or an offset such as `+01:00` / `-05:00`.
`#2019-01-01T00:00:00#` is a syntax error (`token recognition error at: '#2019-01-01T00:00:00#'`). Fractional
seconds are optional, so `#2019-01-01T00:00:00.123Z#` is also valid. A plain date `#2019-01-01#` carries no
zone and needs none.

### Example REST controller

```java
@RestController
@RequestMapping("/api")
public class ProductTypeResource {

    private final Logger log = LoggerFactory.getLogger(ProductTypeResource.class);
    private static final String ENTITY_NAME = "productType";

    private final ProductTypeService productTypeService;

    private final ProductTypeRepository productTypeRepository;

    public ProductTypeResource(ProductTypeService productTypeService, ProductTypeRepository productTypeRepository) {
        this.productTypeService = productTypeService;
        this.productTypeRepository = productTypeRepository;
    }
    
    @GetMapping("/product-types")
    @Secured({"ROLE_ADMIN", "ROLE_USER"})
    public ResponseEntity<List<ProductTypeDTO>> getAllProductTypes(
            @Parameter(
                    name = "filter"
            ) @RequestParam(value = "filter", required = false) String filter,
            Pageable pageable
    ) throws UnsupportedEncodingException {
        if (filter != null) {
            filter = decode(filter, StandardCharsets.UTF_8);
        }

        log.debug("REST request to get ProductTypes by filter: {}", filter);
        Page<ProductTypeDTO> page = productTypeService.getQueryService().findByFilter(filter, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }
}
```

### Example service
```java
@Service
@Transactional
public class ProductTypeService {
    private final ProductTypeRepository productTypeRepository;
    private final ProductTypeMapper productTypeMapper;

    @PersistenceContext
    private EntityManager entityManager;

    private RsqlQueryService<ProductType, ProductTypeDTO, ProductTypeRepository, ProductTypeMapper> queryService;

    public ProductTypeService(ProductTypeRepository productTypeRepository, ProductTypeMapper productTypeMapper) {
        this.productTypeRepository = productTypeRepository;
        this.productTypeMapper = productTypeMapper;
    }

    /**
     * Return a rsqlQueryService used for executing queries with rsql filters.
     *
     * @return RsqlQueryService
     */
    public RsqlQueryService<ProductType, ProductTypeDTO, ProductTypeRepository, ProductTypeMapper> getQueryService() {
        if (this.queryService == null) {
            this.queryService = new RsqlQueryService<>(productTypeRepository, productTypeMapper, entityManager, ProductType.class);
        }
        return this.queryService;
    }
    
}
```

### Example repository
```java
@Repository
public interface ProductTypeRepository extends JpaRepository<ProductType, Long>, JpaSpecificationExecutor<ProductType> {
}
```

Complete example application can be found [here](./rsql-filter-demo).

> Note: `rsql-filter-demo` is excluded from `0.7.x` releases until the demo migration to the same platform stack is completed. For Spring Boot 3 compatibility, use the `0.6.x` line from `release-3.x`.

## Advanced Usage

### Describing a Filter

> **Unreleased.** `rsql.describe` is not in `0.7.6` - it sits under `[Unreleased]` in
> [CHANGELOG.md](CHANGELOG.md). Build from source to use it; this section gets a version number when the next
> release goes out.

A report that shows filtered data usually has to state which filter produced it. `RsqlFilterDescription` turns
the filter string into readable text and into table rows, without an `EntityManager` - it works on the parse
tree alone.

```java
RsqlFilterDescription describer = new RsqlFilterDescription();

describer.describe("name=*'A*';price=gt=100").getText();
// name starts with (ignoring case) "A" and price is greater than 100
```

Values are quoted and escaped so the line cannot be read back as a different filter: `name=='A and b=='` does
not turn into two conditions, a comma inside a value is not the separator of an `IN` list, and a value
containing a newline still prints on one line.

#### Readable Names

Without a resolver the description prints technical field paths. `MapFilterLabelResolver` is the smallest fix:

```java
FilterLabelResolver labels = new MapFilterLabelResolver(Map.of(
    "productType.name", "Product type",
    "price",            "Price"));

describer.describe("productType.name=='A';price=gt=100", labels).getText();
// Product type is "A" and Price is greater than 100
```

For a translated report the operators and the "and"/"or" have to be translated too, which is what
`ResourceBundleFilterLabelResolver` adds:

```properties
# messages_hr.properties
field.productType.name    = Vrsta proizvoda
field.price               = Cijena
operator.EQ               = je
operator.GT               = je veći od
operator.LIKE.STARTS_WITH = počinje s
junction.AND              = i
junction.OR               = ili
```

```java
FilterLabelResolver labels = new ResourceBundleFilterLabelResolver(
    ResourceBundle.getBundle("messages", locale));
```

A key that is missing falls back to the built-in English, so a partial translation still prints. Use
`ResourceBundleFilterLabelResolver.fieldKey(...)`, `.operatorKey(...)` and `.junctionKey(...)` to generate a
starter bundle. Implement `FilterLabelResolver` directly (or extend `DelegatingFilterLabelResolver`) to change
how values are formatted or to mask them.

#### Table Rows for JasperReports

`getRows()` returns JavaBeans ready for `JRBeanCollectionDataSource`:

```java
FilterDescription description = describer.describe(filter, labels);
parameters.put("filterRows", new JRBeanCollectionDataSource(description.getRows()));
parameters.put("filterText", description.getText(200));   // truncated with an ellipsis
```

Each row has `field`, `operator`, `value`, `connector` (the `and`/`or` in front of it, `null` for the first),
`depth`, `openGroups` and `closeGroups`. The last three only matter when the filter contains an `OR`; check
`isPureAndChain()` to know whether a plain table renders the filter exactly.

Masking applies to `getRows()` and `getText()`, which pass every part through the resolver. `getRoot()` returns
the tree with the raw values and deliberately bypasses it.

### Sorting
Add sort parameter to your requests:
```
GET /api/products?filter=price=gt=100&sort=name,asc&sort=price,desc
```

### Complex Queries
Use parentheses for grouping and `;` (AND) or `,` (OR) for combining:
```
GET /api/products?filter=(category.name=='Electronics',category.name=='Books');price=lt=50
```

Instead of `;` you can use 'and', and instead of `,` you can use 'or':
```
GET /api/products?filter=(category.name=='Electronics'%20or%20category.name=='Books')%20and%20price=lt=50
```
The whole expression stays inside the one `filter` parameter — an unencoded `&` would start a new HTTP query
parameter and silently drop the rest of the filter.

### Nested Properties
Access nested entity properties using dot notation. The `#` around a date literal is a reserved URL character,
so it is sent percent-encoded as `%23` — a raw `#` in a query string starts the fragment and the server would
receive only the part before it:
```
GET /api/orders?filter=customer.email=='john@example.com';orderDate=ge=%232024-01-01%23
```

### List of Values (LOV) Queries
For autocomplete/dropdown functionality. The row count comes from the `Pageable`, not from a separate limit
argument:
```java
// id + name (codeField left out)
List<LovDTO> lovs = queryService.getLOV(
    "name=like='*search*'",
    PageRequest.of(0, 10),
    "id", null, "name"
);

// The two shorthands: (id, code, name) and (id, name)
List<LovDTO> all = queryService.getLOV("status==#ACTIVE#", PageRequest.of(0, 10));
List<LovDTO> idName = queryService.getLOVwithIdAndName("status==#ACTIVE#", PageRequest.of(0, 10));
```

`LovDTO` has `id`, `code` and `name`. Three selected fields map to `(id, code, name)`; **two** map to
`(id, name)`. Use `getLOVWithSelect` when the code and the name come from a related entity:
```java
List<LovDTO> lovs = queryService.getLOVWithSelect(
    "id, productType.code:code, productType.name:name",
    "status==#ACTIVE#",
    PageRequest.of(0, 10)
);
```

### SELECT Queries

The library supports flexible SELECT queries with field aliases, navigation properties, arithmetic expressions, and aggregate functions:

```java
// Basic SELECT with aliases
List<Tuple> products = queryService.getTupleWithSelect(
    "code:productCode, name, productType.name:typeName, price",
    "status==#ACTIVE#",
    pageable
);

// Access results by alias
for (Tuple row : products) {
    String code = (String) row.get("productCode");
    String type = (String) row.get("typeName");  // From related entity
}
```

**Aggregate queries with automatic GROUP BY:**

Every element of the SELECT string that carries no aggregate function becomes a GROUP BY field. All four
aggregate methods take the same four arguments — `selectString`, `filter`, `havingFilter`, `pageable` — so pass
`null` for the HAVING filter when there is none:
```java
// Sales statistics by category
List<Tuple> stats = queryService.getAggregateResult(
    "productType.name:category, COUNT(*):productCount, SUM(price):total, AVG(price):avgPrice",
    "status==#ACTIVE#",
    null,             // HAVING filter
    pageable
);

for (Tuple row : stats) {
    System.out.printf("%s: %d items, total $%s%n",
        row.get("category"), row.get("productCount"), row.get("total")
    );
}
```

**Note:** `getAggregateResult` returns **all** groups. It takes only the `Sort` out of the `Pageable` and
ignores the page number and the page size, and it resolves ORDER BY against the entity root, so it cannot sort
by a SELECT alias. Use `getAggregateResultAsPage` when you need either.

**Arithmetic expressions in aggregate queries:**

Arithmetic in the SELECT string works in `getAggregateResultWithExpressions` and
`getAggregateResultAsPageWithExpressions` only. `getAggregateResult` and `getAggregateResultAsPage` take plain
fields and aggregate calls; an element containing `+`, `-`, `*` or `/` makes them throw
`SyntaxErrorException: Arithmetic expressions with operators are not supported in this query type. Use
SelectExpressionVisitor for queries with arithmetic expressions.`
```java
// Calculate price with tax (20%)
List<Tuple> products = queryService.getAggregateResultWithExpressions(
    "productType.name:category, SUM(price) * 1.2:totalWithTax, SUM(price):total",
    "status==#ACTIVE#",
    null,
    pageable
);

// Calculate balance (debit - credit), keeping only the positive ones
List<Tuple> balances = queryService.getAggregateResultWithExpressions(
    "account.name:accountName, SUM(debit) - SUM(credit):balance",
    "year==2024",
    "balance=gt=0",   // HAVING on the expression alias
    pageable
);

// Complex calculations with aggregate functions
List<Tuple> metrics = queryService.getAggregateResultWithExpressions(
    "category, (SUM(price) - 100) * 2 / COUNT(*):adjustedAverage",
    "",
    null,
    pageable
);
```

**Note:** `getAggregateResultWithExpressions` returns all groups too — only the `Sort` of the `Pageable` is
used, and it does resolve a SELECT alias. `getAggregateResultAsPageWithExpressions` is the paginating variant.
For non-aggregate queries use `getTupleWithSelect()`, which selects fields without arithmetic.

**HAVING clause for filtering aggregated results:**
```java
// Categories with total sales over $10,000 and at least 5 products
List<Tuple> topCategories = queryService.getAggregateResult(
    "productType.name:category, SUM(price):totalSales, COUNT(*):productCount",
    "status==#ACTIVE#",  // WHERE filter
    "totalSales=gt=10000;productCount=ge=5",  // HAVING filter
    pageable
);

// Using aggregate functions directly in HAVING
List<Tuple> stats = queryService.getAggregateResult(
    "category, SUM(price):total, AVG(price):avgPrice",
    "",
    "SUM(price)=gt=50000;AVG(price)=bt=(100,500)",  // HAVING with aggregates
    pageable
);
```

**Note:** `count`, `avg`, `sum`, `min`, `max`, `grp`, `all`, `dist`, `and`, `or`, `null`, `true` and `false` are
keywords of the HAVING grammar and can never be a field or a referenced alias there. `COUNT(*):count` is a
legal alias in SELECT, but `count=gt=5` as a HAVING filter does not parse — name it `:productCount`. A bare
field in HAVING must be a SELECT alias or one of the GROUP BY fields derived from the SELECT string; anything
else raises `java.lang.IllegalArgumentException`.

**Supported aggregate functions:**
- `COUNT(*)` - Count all rows
- `COUNT(field)` - Count non-null values
- `COUNT(DIST field)` - Count distinct values
- `COUNT(DIST field1, field2)` - one COUNT DISTINCT column per field, not a composite; and if you attach an
  alias, as an aggregate element normally carries, only the **first** field survives it —
  `COUNT(DIST name, code):n` selects `count(distinct name)` alone. Use one `COUNT(DIST field)` per field.
  (SELECT only - HAVING takes a single field)
- `SUM(field)`, `AVG(field)`, `MIN(field)`, `MAX(field)`
- `GRP(field)` - no aggregation, forces the field into GROUP BY

The list is closed: there is no `DATE()` or any other scalar function. Names are case-insensitive, `ALL` is
accepted on any of them and `DIST` only on `COUNT`. Arithmetic inside a call is not parsed — write
`SUM(a) * SUM(b)`, not `SUM(a*b)`.

**Arithmetic operators (in `getAggregateResultWithExpressions` / `getAggregateResultAsPageWithExpressions` only):**
- `+` - Addition
- `-` - Subtraction
- `*` - Multiplication
- `/` - Division
- `()` - Parentheses for precedence (multiplication/division before addition/subtraction)

An operand may be an aggregate call, a plain field or a number, so `SUM(price) / COUNT(*)` and
`(SUM(price) - 100) * 2` are both expressions. An expression is never a GROUP BY candidate: only the plain
fields of the SELECT are grouped, so `code, price * 2:doubled` generates
`select p1_0.code c0,(p1_0.price*2) c1 from product p1_0 group by c0` with `price` left out of the GROUP BY.
What happens next is the database's decision, and it depends on the data. H2 2.1.214, the database the
integration tests run on, rejects the query with `Column "P1_0.PRICE" must be in the GROUP BY list` only when a
group holds more than one row: with `code` unique per product the query above returns one row per record and
nothing looks wrong, while `productType.name, price * 2:doubled` — where a group really does span several rows
— fails. A database that does not enforce the rule returns an arbitrary row's value instead of an error. Keep
an aggregate in every expression of a grouped query rather than relying on the grouping column being unique.
A SELECT made only of expressions and literals (`price * 2:doubled` on its own, `1.2:lit`) has no GROUP BY at
all and returns one row per record.

**Pagination with aggregate queries:**
```java
// Paginate aggregate results with sorting by alias
Page<Tuple> page = queryService.getAggregateResultAsPage(
    "productType.name:category, SUM(price):total, COUNT(*):productCount",
    "status==#ACTIVE#",
    "total=gt=1000",  // HAVING filter
    PageRequest.of(0, 10, Sort.by("total").descending())  // Sort by aggregate alias
);

// Access pagination metadata
long totalElements = page.getTotalElements();
int totalPages = page.getTotalPages();
List<Tuple> content = page.getContent();

// With arithmetic expressions - note the different method
Page<Tuple> salesPage = queryService.getAggregateResultAsPageWithExpressions(
    "category, SUM(price) * 1.2:totalWithTax, AVG(price):avgPrice",
    "",
    null,
    PageRequest.of(0, 20, Sort.by("category").ascending())
);
```

`getAggregateResultAsPage` applies a real offset and limit, counts `totalElements` after the HAVING filter, and
resolves a sort property in three steps: SELECT alias first, then SELECT field path, then entity path. A
`Sort` is not a `Pageable` — wrap it as `PageRequest.of(0, size, Sort.by(...))`.

**REST endpoint example:**
```http
GET /api/products?select=code:id,name,price&filter=status=='ACTIVE'&sort=name,asc
GET /api/products/stats?filter=status=='ACTIVE'&having=COUNT(*)=gt=5
GET /api/sales-by-category?having=totalSales=gt=10000;productCount=ge=5
GET /api/sales-by-category?page=0&size=10&sort=totalSales,desc&having=total=gt=1000
```

For complete SELECT syntax and examples, see [SELECT.md](SELECT.md).
For complete HAVING syntax and examples, see [HAVING.md](HAVING.md).

### Custom JPQL Queries
For complex scenarios, you can provide custom JPQL. The root **must** be aliased `a0`: that is the default root
alias the library stamps on every query context, and every generated `WHERE` and `ORDER BY` refers to it. With
any other alias the query is built against a name the JPQL does not declare.
```java
String selectQuery = "SELECT DISTINCT a0 FROM Product a0 LEFT JOIN a0.categories c";
String countQuery = "SELECT COUNT(DISTINCT a0) FROM Product a0 LEFT JOIN a0.categories c";

RsqlQueryService<Product, ProductDTO, ProductRepository, ProductMapper> queryService =
    new RsqlQueryService<>(
        repository, mapper, entityManager, Product.class,
        selectQuery, countQuery
    );
```

This constructor sets `useJpqlSelect = true`, which switches every basic and paginated query method —
`findByFilter` and `findEntitiesByFilter` in both forms, `findByFilterAndSort`, `findEntitiesByFilterAndSort`
and `countByFilter` — from `Specification` execution to JPQL execution; `setUseJpqlSelect(false)` switches them
back.
Nothing reads the alias out of the strings you pass — `findAliasFromJpqlSelectString` returns `a0` for every
input, so `setJpqlSelectAllFromEntity` / `setJpqlSelectCountFromEntity` leave the aliases at `a0` as well. To
use a different one, say so explicitly:
```java
queryService.setSelectAlias("p");
queryService.setCountAlias("p");
```

## Error Handling

Parse and validation failures on `WHERE`, `HAVING` and `SELECT` raise `rsql.exceptions.SyntaxErrorException`, a
plain `RuntimeException` carrying one message. The library does not catch it and does not turn it into an HTTP
response — mapping it to a status code is the application's job, typically a `@RestControllerAdvice`.

These are the message shapes you will actually see:

| Message | Produced by |
|---------|-------------|
| `line 1:13 token recognition error at: '&'` | the lexer, for a character that is not part of the language (`name=='John' & age>30`) |
| `Unexpected input after the filter expression at position 6` | a stray `)` or anything else left over after a complete filter (`(a==1))`) |
| `Missing logical operator between conditions` | two conditions with no `;` / `,` between them |
| `Unknown property: xyz from entity com.example.ProductType` | a WHERE field that is not a JPA attribute — but only inside a **nested** path, and the message names the nested entity |
| `Unknown field: 'xyz' in path 'a.b' for entity …` | an unknown field on the aggregate, expression or `compileSelectTo*` SELECT paths |
| `Syntax error in HAVING clause at position 5: no viable alternative at input 'count='` | the HAVING parser |
| `Arithmetic expressions with operators are not supported in this query type. …` | `+ - * /` in a `getAggregateResult` / `getAggregateResultAsPage` select |

Three cases are **not** a `SyntaxErrorException`, and an exception handler that catches only that type will
let them through as HTTP 500:

| Case | Exception |
|------|-----------|
| A bare field in a HAVING filter that is neither a SELECT alias nor a derived GROUP BY field | `java.lang.IllegalArgumentException` |
| An unknown field on a simple SELECT (`getTupleWithSelect`, `getSelectResult`), and two SELECT elements whose default aliases collide | `java.lang.IllegalArgumentException` |
| An unknown **root-level** WHERE field on the specification path | Hibernate's `PathElementException`, which Spring wraps as `org.springframework.dao.InvalidDataAccessApiUsageException` — the filter parses, so the failure happens at query build |

All of these come from a filter the client typed, so they belong on HTTP 400, not on the 500 an unhandled
`RuntimeException` produces:

```java
@RestControllerAdvice
public class RsqlFilterExceptionHandler {

    @ExceptionHandler(SyntaxErrorException.class)      // rsql.exceptions.SyntaxErrorException
    public ProblemDetail onSyntaxError(SyntaxErrorException ex) {
        return badRequest(ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)  // unresolvable HAVING field, unknown or colliding SELECT field
    public ProblemDetail onIllegalArgument(IllegalArgumentException ex) {
        return badRequest(ex.getMessage());
    }

    @ExceptionHandler(InvalidDataAccessApiUsageException.class)  // unknown root-level WHERE field, raised at query build
    public ProblemDetail onUnknownField(InvalidDataAccessApiUsageException ex) {
        return badRequest(ex.getMostSpecificCause().getMessage());
    }

    private ProblemDetail badRequest(String detail) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Invalid filter");
        problem.setDetail(detail);
        return problem;
    }
}
```

Two things to decide before shipping it. `IllegalArgumentException` is thrown by plenty of code that is not
this library, so an application-wide handler for it turns unrelated bugs into 400s — if that matters, catch it
around the query call instead and rethrow something of your own. And the messages name entity attributes and
classes (`Unknown property: xyz from entity com.example.Product`), so on a public API log the message and
return a generic one.

> **Changed in 0.7.6 / 0.6.21.** A stray `)` used to report `Missing opening parenthesis`. It now reports
> `Unexpected input after the filter expression at position N`. Code that matches on the message text needs
> updating.

### Limits

Two hard limits bound the parser so that a pathological filter fails cleanly instead of overflowing the stack:

- **100 levels of grouping parentheses.** Over that: `Filter is nested too deeply at position 100 - at most 100
  levels of parentheses are allowed`.
- **500 levels of parse tree depth.** A flat `;`-chain costs one level per condition, so about 500 conditions;
  a balanced filter of the same size is far shallower. Over that: `Filter is structured too deeply - at most
  500 levels of nested conditions are allowed`.

Both are `SyntaxErrorException`, and both apply to `WHERE`, `HAVING` and `SELECT` alike. They are JVM-wide
static settings on `RsqlWhereTreeParser`:

```java
RsqlWhereTreeParser.setMaxNestingDepth(200);   // default RsqlWhereTreeParser.DEFAULT_MAX_NESTING_DEPTH = 100
RsqlWhereTreeParser.setMaxTreeDepth(1000);     // default RsqlWhereTreeParser.DEFAULT_MAX_TREE_DEPTH = 500
```

Raising them far enough trades the exception back for a `StackOverflowError`, which a caller cannot catch.

## Architecture

The library consists of several key components:

- **RsqlCompiler**: Compiles a WHERE filter into a JPA `Specification` or into JPQL text, and a SELECT string into field, aggregate, GROUP BY or `SelectExpression` lists
- **HavingCompiler**: Compiles a HAVING filter into a `Predicate` against the SELECT aliases and GROUP BY fields
- **RsqlQueryService**: Generic service for executing RSQL queries
- **ANTLR Grammar**: Defines the RSQL syntax — RsqlWhere.g4, RsqlHaving.g4, RsqlSelect.g4, and RsqlCommonLexer.g4, which the first two import
- **Visitors**: Convert parse trees to JPA Specifications or JPQL

**Note:** the classes under `rsql.antlr.*` are generated ANTLR output, not a supported public API. They are
regenerated on every build and change without notice — `0.7.6 / 0.6.21` removed context classes and renumbered the
`RULE_*` constants.

For complete method documentation and parameters, see [API.md](API.md).

## Configuration

### Basic Configuration

One `RsqlQueryService` per entity, created once and kept — see [Example service](#example-service) for the
lazy-field form.

### Custom Query Configuration

`RsqlQueryService` is safe to hold as a singleton bean. Every public method calls `getQueryContext()`, which
builds a fresh `RsqlContext` — its own root, joins map and metadata map — for that one query, so concurrent
calls never share query state:

```java
@Configuration
public class RsqlConfiguration {
    
    @Bean
    public RsqlQueryService<Product, ProductDTO, ProductRepository, ProductMapper> 
           productQueryService(ProductRepository repository, 
                              ProductMapper mapper, 
                              EntityManager entityManager) {
        return new RsqlQueryService<>(repository, mapper, entityManager, Product.class);
    }
}
```

### The root alias

The root of every generated query is aliased **`a0`**, and each fresh context re-applies it, so generated
`WHERE` and `ORDER BY` fragments read `a0.price`, `a0.productType.name` and so on. This only becomes visible
when you supply your own JPQL: alias its root `a0`, or call `setSelectAlias` / `setCountAlias` to tell the
service which alias you used. See [Custom JPQL Queries](#custom-jpql-queries).

## Troubleshooting

### Common Issues

**Q: Getting "Unknown property" errors?**  
A: The name must be a JPA persistent attribute of the entity, spelled exactly as the attribute - not the column
name and not the JSON name. Every segment before the last one must be an association or an embeddable
(`@ManyToOne`, `@OneToOne`, `@Embedded`). Resolution walks the JPA metamodel, so `@JsonIgnore` and getters are
irrelevant: an ignored field resolves fine, a transient one does not. The SELECT path words the same problem as
`Unknown field: 'x' in path 'a.b' for entity …`.

**Q: Date filtering not working?**  
A: Use ISO format with # delimiters: `date=ge=#2024-01-01#` or `datetime=le=#2024-01-01T23:59:59Z#`. A datetime
literal without a zone is a syntax error - see [Supported data types](#usage).

**Q: How to filter by enum?**  
A: Use # delimiters: `status==#ACTIVE#` or `status=in=(#ACTIVE#,#PENDING#)`

**Q: Getting SQL syntax errors?**  
A: Check that field names match your entity properties exactly (case-sensitive)

**Q: How to handle special characters in string values?**  
A: Doubling the delimiter is the rule and it covers every value:
`delimiter + value.replace(delimiter, delimiter+delimiter) + delimiter`. Picking a delimiter the value does not
contain (`name=="John's"`, `name=='John"s'`) is the convenient shortcut, but it runs out when the value holds
all three. A backslash is an ordinary character and escapes nothing. See
[Escaping the delimiter in string literals](#escaping-the-delimiter-in-string-literals). Reserved URL
characters (`&`, `+`, `#`, `%`) still need URL encoding on the way in.

## Contributing

We welcome contributions! Please see our [Contributing Guide](CONTRIBUTING.md) for details.

### Development Setup
1. Clone the repository
2. Run `mvn clean install` to build
3. Run integration tests: `mvn test -pl rsql-filter-integration-tests`

### Running the Demo Application
The demo ships with the `0.6.x` line only. On `release-3`:
```bash
cd rsql-filter-demo
./mvnw -Dspring-boot.run.profiles=dev -Dspring-boot.run.arguments=--spring.profiles.group.dev=dev
```
Access the application at http://localhost:8080. See
[rsql-filter-demo/README.md](rsql-filter-demo/README.md) for why the Spring profile has to be named that way.

### Submitting Changes
1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## Compatibility

| rsql-filter | Spring Boot | Hibernate | Java |
|-------------|-------------|-----------|------|
| 0.7.x       | 4.x         | 7.x       | 21+  |
| 0.6.x       | 3.x         | 6.x       | 17+  |
| 0.5.x       | 2.7.x       | 5.x       | 11+  |

`0.7.x` and `0.6.x` are maintained in parallel and carry the same features; `rsql-filter-demo` ships only with
`0.6.x`.

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

This library is inspired by and builds upon:
- [RSQL Parser](https://github.com/jirutka/rsql-parser) by Jakub Jirutka
- [RSQL JPA Specification](https://github.com/perplexhub/rsql-jpa-specification) by Perplexhub

Special thanks to the Spring Boot and ANTLR communities.
