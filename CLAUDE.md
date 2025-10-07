# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Maven multi-module project implementing RSQL (RESTful Service Query Language) filtering for Spring Boot JPA applications. The project has been restructured into three modules:

1. **rsql-filter** - The core library providing RSQL filtering capabilities
2. **rsql-filter-integration-tests** - Standalone integration tests without JHipster dependencies
3. **rsql-filter-demo** - A JHipster-based demo application (previously test-appl)

## Build and Development Commands

### Building the Project

```bash
# Build entire project
mvn clean install

# Build without tests
mvn clean install -DskipTests

# Build specific module
mvn clean install -pl rsql-filter
mvn clean install -pl rsql-filter-integration-tests
mvn clean install -pl rsql-filter-demo

# Build with specific profile
mvn clean install -Pdev
mvn clean install -Pprod
```

### Testing Commands

```bash
# Run all tests
mvn test

# Run integration tests
mvn verify

# Run tests for specific module
mvn test -pl rsql-filter
mvn test -pl rsql-filter-integration-tests
mvn test -pl rsql-filter-demo

# Run a single test class
mvn test -Dtest=RsqlQueryServiceIT

# Run tests with coverage
mvn test jacoco:report
```

### Frontend Commands (rsql-filter-demo)

```bash
# Install dependencies
npm install

# Run frontend in development
npm start

# Build frontend for production
npm run webapp:prod

# Run frontend tests
npm test

# Lint TypeScript/JavaScript
npm run lint
npm run lint:fix

# Format code
npm run prettier:format

# Check formatting
npm run prettier:check
```

### Other Useful Commands

```bash
# Generate ANTLR code from grammar files
mvn antlr4:antlr4 -pl rsql-filter

# Run checkstyle
mvn checkstyle:check

# Apply Spotless formatting
mvn spotless:apply

# Run the demo application
cd rsql-filter-demo && ./mvnw

# Run with specific profile
cd rsql-filter-demo && ./mvnw -Dspring.profiles.active=dev
```

## Architecture Overview

### Core Components

1. **RsqlCompiler** - Compiles RSQL query strings into JPA Specifications or query structures
   - Uses ANTLR4 for parsing RSQL syntax
   - Converts parsed trees into executable queries

2. **RsqlQueryService** - Generic service for executing RSQL queries
   - Provides paginated and non-paginated query execution
   - Supports LOV (List of Values) queries
   - Integrates with Spring Data JPA repositories

3. **ANTLR Grammar Files** (rsql-filter/src/main/antlr/)
   - RsqlCommonLexer.g4 - Defines tokens
   - RsqlWhere.g4 - Defines WHERE clause syntax
   - RsqlSelect.g4 - Defines SELECT clause syntax

4. **Visitor Pattern Implementation**
   - WhereSpecificationVisitor - Converts parse tree to JPA Specifications
   - WhereStringVisitor - Converts to JPQL strings
   - WhereTextVisitor - Extracts text representations
   - SelectExpressionVisitor - Converts SELECT clauses to SelectExpression objects (supports arithmetic expressions)

### Integration Pattern

Services using RSQL should:
1. Extend repository with JpaSpecificationExecutor
2. Create a RsqlQueryService instance in the service layer
3. Call `findByFilter()` with the RSQL filter string

Example:
```java
@Service
public class ProductTypeService {
    private RsqlQueryService<ProductType, ProductTypeDTO, ProductTypeRepository, ProductTypeMapper> queryService;
    
    public RsqlQueryService<ProductType, ProductTypeDTO, ProductTypeRepository, ProductTypeMapper> getQueryService() {
        if (this.queryService == null) {
            this.queryService = new RsqlQueryService<>(repository, mapper, entityManager, ProductType.class);
        }
        return this.queryService;
    }
}
```

## Key Technologies

- **Java 17** - Required Java version
- **Spring Boot 3.4.4** - Main framework
- **Hibernate 6.5.3** - JPA implementation
- **ANTLR 4.13.2** - Parser generator for RSQL syntax
- **MapStruct 1.6.3** - DTO mapping
- **JHipster 8.0.0** - Test application framework
- **Angular** - Frontend framework for test application

## Module Structure

```
rsql-filter-mvn/
├── rsql-filter/              # Core library module
│   ├── src/main/antlr/           # ANTLR grammar files
│   ├── src/main/java/rsql/       # Core library code
│   └── src/test/                 # Library tests
├── rsql-filter-integration-tests/ # Integration tests
│   └── src/test/java/            # Standalone test infrastructure
└── rsql-filter-demo/             # JHipster demo application
    ├── src/main/java/            # Backend code
    ├── src/main/webapp/          # Angular frontend
    └── src/test/                 # Application tests
```

## RSQL Filter Syntax

### WHERE Clause Syntax

The library supports filtering with operators like:
- `==` (equals), `!=` (not equals)
- `=gt=`, `=ge=`, `=lt=`, `=le=` (comparisons)
- `=in=`, `=nin=` (in/not in lists)
- `=like=`, `=nlike=` (pattern matching)
- Logical operators: `;` (AND), `,` (OR)
- Alternative syntax: `and` instead of `;`, `or` instead of `,`
- Parentheses for grouping

Example: `name=='John';(age=gt=30,status=in=(ACTIVE,PENDING))`
Alternative: `name=='John' and (age=gt=30 or status=in=(ACTIVE,PENDING))`

### SELECT Clause Syntax

The library supports SELECT expressions with:
- **Simple fields**: `name`, `productType.name`
- **Aggregate functions**: `SUM(price)`, `AVG(price)`, `COUNT(*)`, `MIN(price)`, `MAX(price)`, `COUNT(DIST field1, field2)`
- **Arithmetic expressions**: Support for `+`, `-`, `*`, `/` operators
- **Numeric literals**: Integer and decimal numbers
- **Parentheses**: For controlling operation precedence
- **Aliases**: Optional aliases using `:` syntax
- **SELECT ***: Select all fields from root entity
- **Entity.* syntax**: Select all fields from related entity (e.g., `productType.*`)

#### Arithmetic Expression Examples:
```
SUM(price) - 100:adjustedTotal
SUM(price) * 1.2:priceWithTax
SUM(price) / COUNT(*):avgPrice
(SUM(price) - 50) * 2 / COUNT(*):complexMetric
SUM(debit) - SUM(credit):balance
```

#### Multiple Expressions:
```
productType.name:typeName, SUM(price):total, COUNT(*):count, SUM(price) / COUNT(*):average
```

#### Operator Precedence:
- Parentheses `()` (highest)
- Multiplication `*` and Division `/`
- Addition `+` and Subtraction `-` (lowest)

Example: `10 + 5 * 2` evaluates as `10 + (5 * 2) = 20`

## Important Implementation Notes

### Running Integration Tests
Integration tests are in a separate module and test the library with a real H2 database:
```bash
mvn test -pl rsql-filter-integration-tests
```

### ANTLR Grammar Compilation
The ANTLR grammar files need to be compiled before building. This happens automatically during build, but can be done manually:
```bash
mvn antlr4:antlr4 -pl rsql-filter
```

### Package Structure Changes
The project was recently restructured:
- `test-appl` → `rsql-filter-demo`
- Package `testappl` → `com.nomendi6.rsql.demo`
- Integration tests moved to standalone module without JHipster dependencies

### Key Files for Understanding the Library

1. **Core Query Service**: `rsql-filter/src/main/java/rsql/RsqlQueryService.java`
   - Main entry point for executing RSQL queries
   - Supports both Specification-based and JPQL-based queries

2. **RSQL Compiler**: `rsql-filter/src/main/java/rsql/RsqlCompiler.java`
   - Compiles RSQL strings to JPA Specifications
   - Provides `compileSelectToExpressions()` for parsing SELECT clauses with arithmetic expressions

3. **Grammar Files**: `rsql-filter/src/main/antlr/`
   - RsqlWhere.g4 - Defines the WHERE clause syntax
   - RsqlSelect.g4 - Defines the SELECT clause syntax with arithmetic expression support
   - RsqlCommonLexer.g4 - Common lexer rules

4. **SelectExpression Hierarchy**: `rsql-filter/src/main/java/rsql/helper/`
   - SelectExpression (abstract base) - Base class for all SELECT expressions
   - FieldExpression - Simple field references
   - FunctionExpression - Aggregate functions (SUM, AVG, COUNT, MIN, MAX)
   - BinaryOpExpression - Arithmetic operations (+, -, *, /)
   - LiteralExpression - Numeric literals
   - BinaryOperator (enum) - Arithmetic operators

5. **SelectExpressionVisitor**: `rsql-filter/src/main/java/rsql/select/SelectExpressionVisitor.java`
   - Parses ANTLR parse tree into SelectExpression objects
   - Validates field paths against JPA metamodel
   - Handles operator precedence and parentheses

6. **Integration Tests**: `rsql-filter-integration-tests/src/test/java/`
   - Comprehensive tests showing all supported features
   - Good examples of how to use the library
   - SelectExpressionIT - Tests for arithmetic expressions with real JPA entities

### Common Development Tasks

#### Adding a New WHERE Operator
1. Update the grammar file (RsqlWhere.g4)
2. Regenerate ANTLR code: `mvn antlr4:antlr4 -pl rsql-filter`
3. Update WhereSpecificationVisitor to handle the new operator
4. Add integration tests

#### Adding Arithmetic Expressions to SELECT
The library now supports arithmetic expressions in SELECT clauses. To use:

```java
// Using SimpleQueryExecutor directly
List<Tuple> results = SimpleQueryExecutor.getAggregateQueryResultWithSelectExpression(
    Product.class,
    Tuple.class,
    "productType.name:typeName, SUM(price) * 1.2:totalWithTax",
    "status==ACTIVE",  // WHERE filter (optional)
    null,              // HAVING filter (optional)
    null,              // Pageable (optional)
    rsqlContext,
    compiler
);

// Using RsqlCompiler to parse expressions
List<SelectExpression> expressions = compiler.compileSelectToExpressions(
    "SUM(price) - 100:adjusted",
    rsqlContext
);

// Convert to JPA expressions
for (SelectExpression expr : expressions) {
    Expression<?> jpaExpr = expr.toJpaExpression(criteriaBuilder, root, rsqlContext);
    // Use jpaExpr in CriteriaQuery
}
```

#### Modifying SELECT Grammar
When modifying `RsqlSelect.g4`, keep in mind:
1. **Rule order matters**: In `selectElement`, `seExpression` MUST come before `seField` and `seFuncCall` to prevent ambiguity with the `*` operator
2. Expression precedence is handled by grammar structure (multiplication/division before addition/subtraction)
3. After changing grammar, regenerate: `mvn antlr4:antlr4 -pl rsql-filter`
4. Update `SelectExpressionVisitor` if adding new expression types
5. Run tests: `mvn test -pl rsql-filter` and `mvn test -pl rsql-filter-integration-tests`

#### Testing with Demo Application
```bash
cd rsql-filter-demo
./mvnw
# Frontend runs on http://localhost:9000
# Backend runs on http://localhost:8080
```

#### Publishing to Maven Central
The project is configured for Maven Central deployment. See the parent POM for GPG signing configuration.