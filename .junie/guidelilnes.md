# RSQL Filter Project Guidelines

This document provides guidelines and information for developers working on the RSQL Filter project.

## Build/Configuration Instructions

### Project Structure

The project consists of two main components:

1. **rsql** - The core library that provides RSQL filtering functionality for Spring Data JPA
2. **test-appl** - A test application that demonstrates the usage of the RSQL library

### Building the Project

#### Prerequisites

- Java 17 or higher
- Gradle 8.x

#### Building the Core Library

```bash
# From the project root
cd rsql
./gradlew build
```

The library is configured with the following key dependencies:
- ANTLR4 Runtime 4.13.2
- Spring Boot Data JPA
- Hibernate
- MapStruct

#### Building the Test Application

The test application is a JHipster-generated Spring Boot application with Angular frontend.

```bash
# From the project root
cd test-appl
./gradlew build
```

**Note**: The test application uses Gradle version catalogs for dependency management. If you encounter build errors related to `alias(libs.plugins.spring.boot)`, you may need to:
1. Check that the `libs` version catalog is properly defined in `settings.gradle`
2. Update the Gradle version to one that supports version catalogs (7.0+)
3. Or replace the alias with a direct plugin ID and version

### Configuration

#### Core Library Configuration

The core library configuration is in `rsql/build.gradle` and `rsql/gradle.properties`:

- Java compatibility: 17+
- Current version: 0.5.2
- Spring Boot version: 3.4.4
- Hibernate version: 6.6.11.Final
- MapStruct version: 1.6.3

#### Test Application Configuration

The test application configuration is in `test-appl/build.gradle` and related Gradle files:

- Java compatibility: 17+
- Spring profiles: dev (default), prod, tls, e2e
- Database: PostgreSQL

## Testing Information

### Test Structure

Tests are organized in the following structure:

- **rsql/src/test** - Tests for the core library
- **test-appl/src/test/java/testappl** - Tests for the test application
    - **config** - Configuration tests
    - **domain** - Domain model tests
    - **rsql** - RSQL-specific tests
    - **security** - Security tests
    - **service** - Service layer tests
    - **web** - Web layer tests

### Running Tests

#### Running Core Library Tests

```bash
# From the project root
cd rsql
./gradlew test
```

#### Running Test Application Tests

```bash
# From the project root
cd test-appl
./gradlew test                # Unit tests
./gradlew integrationTest     # Integration tests
```

### Writing RSQL Tests

The RSQL library provides a `RsqlQueryService` that can be used to execute RSQL queries. Here's an example of how to use it in tests:

```java
@Autowired
private EntityManager em;

@Autowired
private ProductTypeRepository productTypeRepository;

@Autowired
private ProductTypeMapper productTypeMapper;

private RsqlQueryService<ProductType, ProductTypeDTO, ProductTypeRepository, ProductTypeMapper> queryService;

private String jpqlSelectAll = "SELECT p FROM ProductType p";
private String jpqlSelectAllCount = "SELECT count(distinct p) FROM ProductType p";

@BeforeEach
void init() {
    queryService = new RsqlQueryService<>(
        productTypeRepository,
        productTypeMapper,
        em,
        ProductType.class,
        jpqlSelectAll,
        jpqlSelectAllCount
    );
}

@Test
void testBasicFiltering() {
    // Test filtering by name
    String filter = "name=*'Type'*";
    List<ProductTypeDTO> result = queryService.findByFilter(filter);
    
    // Verify results
    assertThat(result).isNotNull();
}
```

### RSQL Filter Syntax

The RSQL filter syntax is similar to FIQL (Feed Item Query Language):

- **Comparison Operators**:
    - `==` or `=` : Equal to
    - `!=` or `<>` : Not equal to
    - `=gt=` or `>` : Greater than
    - `=ge=` or `>=` : Greater than or equal to
    - `=lt=` or `<` : Less than
    - `=le=` or `<=` : Less than or equal to
    - `=*` : Contains
    - `=^` : Starts with
    - `=$` : Ends with

- **Logical Operators**:
    - `and` : Logical AND
    - `or` : Logical OR

- **Examples**:
    - `name=='Type'` : Name equals 'Type'
    - `name=*'Type'*` : Name contains 'Type'
    - `parent.id=gt=1 and product.id=gt=1` : Parent ID > 1 AND Product ID > 1

## Additional Development Information

### Code Style

The project follows standard Java code style conventions. Key points:

- Use 4 spaces for indentation
- Follow Java naming conventions
- Use meaningful variable and method names
- Add JavaDoc comments for public classes and methods

### Working with ANTLR

The RSQL library uses ANTLR for parsing RSQL expressions. The grammar files are located in:

- `rsql/src/main/antlr/RsqlCommonLexer.g4`
- `rsql/src/main/antlr/RsqlSelect.g4`
- `rsql/src/main/antlr/RsqlWhere.g4`

If you modify the grammar files, you'll need to regenerate the ANTLR parser classes.

### Publishing the Library

The library is configured for publishing to Maven Central via Sonatype OSSRH. To publish:

```bash
# From the project root
cd rsql
./gradlew publish
```

You'll need to set the following properties:
- `ossrhUsername`: Your Sonatype OSSRH username
- `ossrhPassword`: Your Sonatype OSSRH password

### Debugging Tips

1. Enable debug logging in application.yml:
   ```yaml
   logging:
     level:
       rsql: DEBUG
   ```

2. Use the `[DEBUG_LOG]` prefix in test output for better visibility:
   ```java
   System.out.println("[DEBUG_LOG] Your debug message");
   ```

3. For complex RSQL expressions, break them down into smaller parts for easier debugging.