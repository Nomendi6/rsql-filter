# RSQL Filter

[![Maven Central](https://img.shields.io/maven-central/v/com.nomendi6/rsql-filter.svg)](https://maven-badges.herokuapp.com/maven-central/com.nomendi6/rsql-filter)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

RSQL Filter is a Java library for Spring Boot JPA applications that provides a simple and convenient way to filter data in REST APIs using RSQL (RESTful Service Query Language) syntax.

## What is RSQL?

RSQL (RESTful Service Query Language) is a query language for parametrized filtering of entries in RESTful APIs. It's based on FIQL (Feed Item Query Language) – an URI-friendly syntax for expressing filters across the entries in an Atom Feed.

RSQL provides a simple and intuitive syntax that allows clients to filter data without exposing the underlying database structure.

## Quick Start

1. Add the dependency to your project
2. Extend your repository with `JpaSpecificationExecutor`
3. Create a `RsqlQueryService` in your service
4. Use the filter parameter in your REST endpoints

See the [complete example](#example-rest-controller) below.

For detailed API documentation, see [API.md](API.md).

## Installation

### Maven
```xml
<dependency>
    <groupId>com.nomendi6</groupId>
    <artifactId>rsql-filter</artifactId>
    <version>0.6.2</version>
</dependency>
```

### Gradle
```gradle
implementation 'com.nomendi6:rsql-filter:0.6.2'
```

### Requirements
- Java 17 or higher
- Spring Boot 3.x
- Hibernate 6.x

## Features

- **Simple Query Language**: Intuitive syntax for filtering data
- **Type Safety**: Automatic type conversion and validation
- **JPA Integration**: Seamless integration with Spring Data JPA
- **Rich Set of Operators**: Comprehensive set of comparison operators
- **Complex Queries**: Support for nested queries with AND/OR logic
- **Sorting**: Built-in support for sorting results
- **Pagination**: Full Spring Data pagination support
- **SELECT Queries**: Flexible field selection with aliases and navigation properties
- **Aggregate Functions**: COUNT, SUM, AVG, MIN, MAX with automatic GROUP BY
- **HAVING Clause**: Filter aggregated results with full RSQL syntax support
- **LOV Queries**: List of Values queries for dropdowns/autocomplete
- **ANTLR Based**: Robust parser built with ANTLR4
- **Error Handling**: Detailed error messages for invalid queries

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
| =*       | Like                     |
| =like=   | Like                     |
| !=*      | Not like                 |
| =!*      | Not like                 |
| =nlike=  | Not like                 |
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
| ==true   | Equal to true            |
| ==false  | Equal to false           |

Supported data types:

| Data Type      | Description                                                                 |
|----------------|-----------------------------------------------------------------------------|
| String         | Expression in quotes ("" or '' or ``), for example `name=='Ana'`            |
| Integer        | Integer number, for example `id==2345`                                      |
| Decimal number | Decimal number, for example `amount=gt=10.23`                               |
| Enum           | Enum name, for example `status==#ACTIVE#`                                   |
| Date           | Date in ISO format, for example `date=ge=#2019-01-01#`                      |
| Datetime       | Datetime in ISO format, for example `date=ge=#2019-01-01T00:00:00#`         |
| Boolean        | Boolean value, for example `active==true` or `active==false`                |
| UUID           | UUID value, for example `uuidField=='f47ac10b-58cc-4372-a567-0e02b2c3d479'` |



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
    private RsqlQueryService<ProductType, ProductTypeDTO, ProductTypeRepository, ProductTypeMapper> queryService;

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

## Advanced Usage

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

Instead of `;` you can use 'and', and istead of `,` you can use 'or':
```
GET /api/products?filter=(category.name=='Electronics'%20and%20category.name=='Books')&price=lt=50
```

### Nested Properties
Access nested entity properties using dot notation:
```
GET /api/orders?filter=customer.email=='john@example.com';orderDate=ge=#2024-01-01#
```

### List of Values (LOV) Queries
For autocomplete/dropdown functionality:
```java
List<LovDTO> lovs = queryService.findLovByFilter(
    "name=like='*search*'", 
    "id", "name", 10
);
```

### SELECT Queries

The library supports flexible SELECT queries with field aliases, navigation properties, and aggregate functions:

```java
// Basic SELECT with aliases
List<Tuple> products = queryService.getTupleWithSelect(
    "code:productCode, name, productType.name:typeName, price",
    "status==ACTIVE",
    pageable
);

// Access results by alias
for (Tuple row : products) {
    String code = (String) row.get("productCode");
    String type = (String) row.get("typeName");  // From related entity
}
```

**Aggregate queries with automatic GROUP BY:**
```java
// Sales statistics by category
List<Tuple> stats = queryService.getAggregateResult(
    "productType.name:category, COUNT(*):count, SUM(price):total, AVG(price):avg",
    "status==ACTIVE",
    pageable
);

for (Tuple row : stats) {
    System.out.printf("%s: %d items, total $%s%n",
        row.get("category"), row.get("count"), row.get("total")
    );
}
```

**HAVING clause for filtering aggregated results:**
```java
// Categories with total sales over $10,000 and at least 5 products
List<Tuple> topCategories = queryService.getAggregateResult(
    "productType.name:category, SUM(price):totalSales, COUNT(*):productCount",
    "status==ACTIVE",  // WHERE filter
    "totalSales=gt=10000;productCount=ge=5",  // HAVING filter
    pageable
);

// Using aggregate functions directly in HAVING
List<Tuple> stats = queryService.getAggregateResult(
    "category, SUM(price):total, AVG(price):avg",
    "",
    "SUM(price)=gt=50000;AVG(price)=bt=(100,500)",  // HAVING with aggregates
    pageable
);
```

**Supported aggregate functions:**
- `COUNT(*)` - Count all rows
- `COUNT(field)` - Count non-null values
- `COUNT(DIST field)` - Count distinct values
- `SUM(field)`, `AVG(field)`, `MIN(field)`, `MAX(field)`

**REST endpoint example:**
```http
GET /api/products?select=code:id,name,price&filter=status==ACTIVE&sort=name,asc
GET /api/products/stats?filter=status==ACTIVE&having=COUNT(*)=gt=5
GET /api/sales-by-category?having=totalSales=gt=10000;productCount=ge=5
```

For complete SELECT syntax and examples, see [SELECT.md](SELECT.md).
For complete HAVING syntax and examples, see [HAVING.md](HAVING.md).

### Custom JPQL Queries
For complex scenarios, you can provide custom JPQL:
```java
String selectQuery = "SELECT DISTINCT p FROM Product p LEFT JOIN p.categories c";
String countQuery = "SELECT COUNT(DISTINCT p) FROM Product p LEFT JOIN p.categories c";

RsqlQueryService queryService = new RsqlQueryService<>(
    repository, mapper, entityManager, Product.class,
    selectQuery, countQuery
);
```

## Error Handling

The library provides detailed error messages for invalid queries:

- **Syntax Errors**: Invalid RSQL syntax
- **Type Mismatch**: Wrong data type for field
- **Unknown Field**: Field doesn't exist in entity
- **Invalid Operator**: Operator not supported for field type

Example error response:
```json
{
  "error": "SyntaxErrorException",
  "message": "Unexpected character '&' at position 15",
  "details": "Invalid RSQL expression: name=='John' & age>30"
}
```

## Architecture

The library consists of several key components:

- **RsqlCompiler**: Compiles RSQL strings into JPA Specifications
- **RsqlQueryService**: Generic service for executing RSQL queries
- **ANTLR Grammar**: Defines the RSQL syntax (RsqlWhere.g4, RsqlSelect.g4)
- **Visitors**: Convert parse trees to JPA Specifications or JPQL

For complete method documentation and parameters, see [API.md](API.md).

## Configuration

### Basic Configuration
```java
@Service
@Transactional
public class ProductService {
    private RsqlQueryService<Product, ProductDTO, ProductRepository, ProductMapper> queryService;

    public RsqlQueryService<Product, ProductDTO, ProductRepository, ProductMapper> getQueryService() {
        if (this.queryService == null) {
            this.queryService = new RsqlQueryService<>(repository, mapper, entityManager, Product.class);
        }
        return this.queryService;
    }
}
```

### Custom Query Configuration
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

## Troubleshooting

### Common Issues

**Q: Getting "Unknown property" errors?**  
A: Ensure your entity fields are properly mapped and accessible. Check for @JsonIgnore or missing getters.

**Q: Date filtering not working?**  
A: Use ISO format with # delimiters: `date=ge=#2024-01-01#` or `datetime=le=#2024-01-01T23:59:59#`

**Q: How to filter by enum?**  
A: Use # delimiters: `status==#ACTIVE#` or `status=in=(#ACTIVE#,#PENDING#)`

**Q: Getting SQL syntax errors?**  
A: Check that field names match your entity properties exactly (case-sensitive)

**Q: How to handle special characters in string values?**  
A: Use URL encoding for special characters or different quote types: `name=="John's"` or `name=='John"s'`

## Contributing

We welcome contributions! Please see our [Contributing Guide](CONTRIBUTING.md) for details.

### Development Setup
1. Clone the repository
2. Run `mvn clean install` to build
3. Run integration tests: `mvn test -pl rsql-filter-integration-tests`

### Running the Demo Application
```bash
cd rsql-filter-demo
./mvnw spring-boot:run
```
Access the application at http://localhost:8080

### Submitting Changes
1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## Compatibility

| rsql-filter | Spring Boot | Hibernate | Java |
|-------------|-------------|-----------|------|
| 0.6.x       | 3.x         | 6.x       | 17+  |
| 0.5.x       | 2.7.x       | 5.x       | 11+  |

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

This library is inspired by and builds upon:
- [RSQL Parser](https://github.com/jirutka/rsql-parser) by Jakub Jirutka
- [RSQL JPA Specification](https://github.com/perplexhub/rsql-jpa-specification) by Perplexhub

Special thanks to the Spring Boot and ANTLR communities.