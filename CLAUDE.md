# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Maven multi-module project implementing RSQL (RESTful Service Query Language) filtering for Spring Boot JPA applications. The project consists of two main modules:

1. **rsql** - The core library providing RSQL filtering capabilities
2. **test-appl** - A JHipster-based test application demonstrating library usage

## Build and Development Commands

### Building the Project

```bash
# Build entire project
mvn clean install

# Build without tests
mvn clean install -DskipTests

# Build specific module
mvn clean install -pl rsql
mvn clean install -pl test-appl

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
mvn test -pl rsql
mvn test -pl test-appl

# Run a single test class
mvn test -Dtest=RsqlQueryServiceIT

# Run tests with coverage
mvn test jacoco:report
```

### Frontend Commands (test-appl)

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
mvn antlr4:antlr4 -pl rsql

# Run checkstyle
mvn checkstyle:check

# Apply Spotless formatting
mvn spotless:apply

# Run the test application
cd test-appl && ./mvnw

# Run with specific profile
cd test-appl && ./mvnw -Dspring.profiles.active=dev
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

3. **ANTLR Grammar Files** (src/main/antlr/)
   - RsqlCommonLexer.g4 - Defines tokens
   - RsqlWhere.g4 - Defines WHERE clause syntax
   - RsqlSelect.g4 - Defines SELECT clause syntax

4. **Visitor Pattern Implementation**
   - WhereSpecificationVisitor - Converts parse tree to JPA Specifications
   - WhereStringVisitor - Converts to JPQL strings
   - WhereTextVisitor - Extracts text representations

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
    
    public RsqlQueryService<...> getQueryService() {
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
├── rsql/                    # Core library module
│   ├── src/main/antlr/     # ANTLR grammar files
│   ├── src/main/java/rsql/ # Core library code
│   └── src/test/           # Library tests
└── test-appl/              # JHipster test application
    ├── src/main/java/      # Backend code
    ├── src/main/webapp/    # Angular frontend
    └── src/test/           # Application tests
```

## RSQL Filter Syntax

The library supports filtering with operators like:
- `==` (equals), `!=` (not equals)
- `=gt=`, `=ge=`, `=lt=`, `=le=` (comparisons)
- `=in=`, `=nin=` (in/not in lists)
- `=like=`, `=nlike=` (pattern matching)
- Logical operators: `;` (AND), `,` (OR)
- Parentheses for grouping

Example: `name=='John';(age=gt=30,status=in=(ACTIVE,PENDING))`