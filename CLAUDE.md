# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Maven multi-module project implementing RSQL (RESTful Service Query Language) filtering for Spring Boot JPA applications. On this branch the root `pom.xml` declares **two** modules:

1. **rsql-filter** - The core library providing RSQL filtering capabilities
2. **rsql-filter-integration-tests** - Standalone integration tests without JHipster dependencies

A third directory, **rsql-filter-demo** (a JHipster-based demo application), is still on disk but is commented
out of the reactor on the 0.7.x line: it depends on JHipster 8 / Spring Boot 3 and is built only on
`release-3`. Nothing in this reactor downloads Node or npm.

## Build and Development Commands

### Building the Project

```bash
# Build entire project
mvn clean install

# Build without tests
mvn clean install -DskipTests

# Build specific module. `-pl rsql-filter-demo` fails here with "Could not find the selected project
# in the reactor" - that module exists only on release-3
mvn clean install -pl rsql-filter
mvn clean install -pl rsql-filter-integration-tests
```

### Testing Commands

```bash
# Run all tests
mvn test

# Run integration tests. In rsql-filter-integration-tests surefire is configured to include **/*IT.java,
# so `mvn test` has already run them - there is nothing left for failsafe on this branch
mvn verify

# Run tests for specific module
mvn test -pl rsql-filter
mvn test -pl rsql-filter-integration-tests

# Run a single test class - always with -pl, or the modules without a match fail the build
mvn test -pl rsql-filter-integration-tests -Dtest=RsqlQueryServiceIT

# Run tests with coverage - jacoco is wired into rsql-filter-demo only, which is not a module of this
# reactor, and for the library modules the plugin is in pluginManagement only, so `mvn jacoco:report`
# here just says "missing execution data file". There is no coverage report on this branch
```

### Frontend and Demo Commands

The `rsql-filter-demo` directory is on disk but is **not** a module of this reactor, so its Maven and npm
commands belong to the `release-3` branch. `rsql-filter-demo/README.md` and the CLAUDE.md on `release-3`
carry them; nothing on this branch needs Node, npm or a running application.

### Other Useful Commands

```bash
# Generate ANTLR code from grammar files
mvn -pl rsql-filter generate-sources

# Checkstyle, Spotless and running the demo application: rsql-filter-demo only, i.e. release-3.
# At the root here the checkstyle plugin DOES resolve and run (maven-checkstyle-plugin 3.6.0 with the
# default sun_checks) and then fails with ~16 900 violations on rsql-filter, most of them in the
# generated parsers under rsql/antlr - it is not a usable gate for this project
```

## Architecture Overview

### Core Components

1. **RsqlCompiler** - Compiles RSQL query strings into JPA Specifications or query structures
   - Uses ANTLR4 for parsing RSQL syntax
   - Converts parsed trees into executable queries

2. **RsqlQueryService** - Generic service for executing RSQL queries
   - Provides paginated and non-paginated query execution
   - Supports LOV (List of Values) queries: `getLOV(filter, pageable, idField, codeField, nameField)`,
     `getLOV(filter, pageable)`, `getLOVwithIdAndName(filter, pageable)` and
     `getLOVWithSelect(selectString, filter, pageable)` - the row count comes from the Pageable, there is no
     row-limit parameter. LovDTO maps three selected fields to (id, code, name) and two to (id, name).
   - Integrates with Spring Data JPA repositories
   - `withPagingStrategy(PagingStrategy.IDS_THEN_HYDRATE)` fetches a JPQL page as its identifiers first and
     the caller's select second, so a wide `select new` is built only for the rows that come back. JPQL mode
     and the paged methods only; default `SINGLE_QUERY`. `SimpleQueryExecutor.getJpqlQueryResultAsPageIdsThenHydrate`
     is the static form, `rsql.helper.IdsThenHydratePaging` builds the statements. Since 0.7.8 / 0.6.23.

3. **RsqlFilterDescription** - Turns a WHERE filter into report text and JasperReports rows
   - Purely textual: no EntityManager, no RsqlContext - it works on the parse tree alone
   - Shipped in 0.7.6 / 0.6.21

4. **ANTLR Grammar Files** (rsql-filter/src/main/antlr/)
   - RsqlCommonLexer.g4 - Defines tokens, imported by RsqlWhere.g4 and RsqlHaving.g4
   - RsqlWhere.g4 - Defines WHERE clause syntax
   - RsqlSelect.g4 - Defines SELECT clause syntax
   - RsqlHaving.g4 - Defines HAVING clause syntax

5. **Visitor Pattern Implementation**
   - WhereSpecificationVisitor - Converts parse tree to JPA Specifications. A selector ending in the
     identifier of a to-one association is read off the foreign key column instead of joining, unless
     Hibernate says it cannot resolve it there - see `RsqlContext.useForeignKeyIdShortcut` and
     `foreignKeyIdShortcutOverrides`. The decision lives in one place,
     `RsqlWhereHelper.foreignKeyIdShortcut`, and WHERE, SELECT and GROUP BY all route through it, so they
     cannot disagree about whether a selector needs a join. HAVING is unaffected.
   - WhereStringVisitor - Converts to JPQL strings
   - WhereTextVisitor - Extracts text representations
   - WhereDescriptionVisitor - Converts to the neutral FilterNode tree used by rsql.describe
   - HavingSpecificationVisitor - Converts HAVING parse tree to JPA Predicates
   - SelectExpressionVisitor - Converts SELECT clauses to SelectExpression objects, arithmetic included
   - SelectAggregateVisitor - The plain aggregate path; rejects arithmetic (see SELECT Clause Syntax)

### Integration Pattern

Services using RSQL should:
1. Extend repository with JpaSpecificationExecutor
2. Create a RsqlQueryService instance in the service layer
3. Call `findByFilter()` with the RSQL filter string

Example:
```java
@Service
public class ProductTypeService {
    private final RsqlQueryService<ProductType, ProductTypeDTO, ProductTypeRepository, ProductTypeMapper> queryService;

    public ProductTypeService(ProductTypeRepository repository, ProductTypeMapper mapper, EntityManager entityManager) {
        this.queryService = new RsqlQueryService<>(repository, mapper, entityManager, ProductType.class);
    }

    public RsqlQueryService<ProductType, ProductTypeDTO, ProductTypeRepository, ProductTypeMapper> getQueryService() {
        return this.queryService;
    }
}
```

One instance per entity, built once, is correct: every public method of RsqlQueryService calls
getQueryContext(), which builds a fresh RsqlContext per query, so the service is safe to hold as a singleton
bean. The six-argument constructor takes custom JPQL for the select and count queries; that JPQL must alias
its root `a0` (the default), or the alias must be declared with setSelectAlias()/setCountAlias().

## Key Technologies

- **ANTLR 4.13.2** - Parser generator for RSQL syntax
- **MapStruct 1.6.3** - DTO mapping
- **JHipster 8.10.0** - Demo application framework (`rsql-filter-demo/.yo-rc.json` and the demo POM's
  `jhipster-framework.version`). The root POM's `jhipster-dependencies.version` is 8.0.0, but that is the
  BOM artifact, not the generator version
- **Angular 19** - Frontend framework for the demo application

### Two Release Lines

The project maintains two lines in parallel, and the platform versions differ between them:

- **release-3** - the 0.6.x line, currently 0.6.25: Java 17, Spring Boot 3.4.4, Hibernate 6.6.11 (also tested on 6.5.3 and 6.6.53)
- **master** - the 0.7.x line, currently 0.7.10: Java 21, Spring Boot 4.0.3, Hibernate 7.2.4

This branch is cut from `master`, so the 0.7.x numbers are the ones that apply here. A version note in the
docs on this branch cites both lines ("Since 0.7.5 / 0.6.20"), because the same change usually ships on both;
the docs on `release-3` cite the 0.6.x number alone. `rsql-filter-demo` is a module of the 0.6.x line only -
here it is commented out of the root POM, so this reactor has two modules and builds without Node or npm.

## Module Structure

```
rsql-filter-mvn/
├── rsql-filter/              # Core library module
│   ├── src/main/antlr/           # ANTLR grammar files
│   ├── src/main/java/rsql/       # Core library code
│   │   └── antlr/                # Generated parsers - committed, regenerated on every build
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
- `=bt=`, `=nbt=` (between/not between, exactly two bounds: `price=bt=(10,20)`)
- `=like=`, `=nlike=` (pattern matching, case-insensitive; shorthands `=*` and `=!*` / `!=*`)
- `=clike=`, `=cnlike=` (the same patterns, case-sensitive; shorthands `=^*` and `=!^*` / `!=^*`)
- Logical operators: `;` (AND), `,` (OR)
- Alternative syntax: `and` instead of `;`, `or` instead of `,`
- Parentheses for grouping

Right-hand sides:
- Strings in `'`, `"` or `` ` ``, and the delimiter is escaped by doubling it (`'it''s'`). Backslash is an
  ordinary character.
- The LIKE wildcard `*` goes INSIDE the quotes - `name=*'*Type*'`, never `name=*'Type'*`. There is no bare
  `*` token in the WHERE grammar.
- Numbers, `#2024-01-01#`, `#2024-01-01T23:59:59Z#`, `#ACTIVE#` (enum), `:name` (named parameter).
  A datetime literal is bound in the TYPE OF THE ATTRIBUTE (`rsql.where.DatetimeLiteral.as`), never as a
  blanket `Instant`: against `Instant` / `OffsetDateTime` / `ZonedDateTime` / `Date` it must carry a zone
  (`Z` or `+01:00`) and is compared by moment; against `LocalDateTime` / `LocalDate` the calendar fields are
  compared as written and the zone is optional and ignored, so `#2024-01-01T23:59:59#` is legal there and a
  `SyntaxErrorException` against a moment. Every site that binds a literal - the single condition, BETWEEN,
  IN, HAVING, WhereTextVisitor - goes through it; a new one must too. Fractional seconds are optional.
  Since 0.7.9 / 0.6.24.
- A string against an EMBEDDABLE path (an `@EmbeddedId`, or any embeddable attribute) is converted through the
  embeddable's own `public static T valueOf(String)` - `id=='ACME~2024~17'`, `document.id=in=('…','…')` - and
  the whole value is compared (a row-value comparison). `==`, `!=`, `=in=`, `=nin=` only; the LIKE family with
  a string, the ordering operators and `=bt=`/`=nbt=` with ANY operand, a class without such a `valueOf`, and a
  literal it refuses are a `SyntaxErrorException`. "Embeddable" is decided by
  `RsqlWhereHelper.isFieldEmbeddableType` (the path's attribute), the class by `embeddableJavaType` (Hibernate's
  resolved model - `getJavaType()` is the erased bound for a generic `@MappedSuperclass` key), the conversion by
  `embeddableFromLiteral`. `=nin=` over an embeddable is an AND of `<>`, never `not(in)`: Hibernate 6 emulates a
  tuple NOT IN wrongly on SQL Server/DB2 and H2 mishandles NULL components in it. WhereSpecificationVisitor and
  WhereTextVisitor both go through these, and a new site that compares an embeddable must too. The
  `EmbeddableKey*IT` contract runs on H2 and under the SQL Server and DB2 dialects. Since 0.7.10 / 0.6.25.
- `null`, `true`, `false` - only with `==` and `!=`: `description==null`, `active==true`.
- Another field: `price=gt=cost`, and a field is also legal as an IN element or a BETWEEN bound
  (`code=in=(status,name)`, `price=bt=(minPrice,maxPrice)`). This is why an unquoted
  `status=in=(ACTIVE,PENDING)` compiles to `status in (ACTIVE,PENDING)` - two field references, not two
  values.
- A parameter-bound pattern is rejected on the Specification path for the whole LIKE family, not just
  `=clike=`: `name=*:p` raises SyntaxErrorException when the Specification is applied. The JPQL-text path
  (RsqlWhereString) renders it.

Example: `name=='John';(age=gt=30,status=in=(#ACTIVE#,#PENDING#))`
Alternative: `name=='John' and (age=gt=30 or status=in=(#ACTIVE#,#PENDING#))`

### SELECT Clause Syntax

The library supports SELECT expressions with:
- **Simple fields**: `name`, `productType.name`
- **Aggregate functions**: `SUM(price)`, `AVG(price)`, `COUNT(*)`, `MIN(price)`, `MAX(price)`, `GRP(field)`,
  `COUNT(DIST field1, field2)`. The list is closed - there is no `DATE()` or any other scalar function.
  Names are case-insensitive. `ALL` is accepted on any aggregate; `DIST` only on `COUNT` - `SUM(DIST x)`
  parses, then throws `SyntaxErrorException: DISTINCT modifier is only supported for COUNT function`.
- **Arithmetic expressions**: `+`, `-`, `*`, `/` **between** aggregates, fields and literals - not inside an
  aggregate call, `SUM(a*b)` does not parse
- **Numeric literals**: Integer and decimal numbers
- **Parentheses**: For controlling operation precedence
- **Aliases**: Optional aliases using `:` syntax
- **SELECT ***: Select all fields from root entity; `*` is legal only as the FIRST element
- **Entity.* syntax**: Select all fields from related entity (e.g., `productType.*`)
- **Separators**: Elements are comma-separated. A missing comma (`code name`) and a trailing comma (`code,`)
  are both errors.

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
productType.name:typeName, SUM(price):total, COUNT(*):productCount, SUM(price) / COUNT(*):average
```

Do not name an alias `count`, `sum`, `avg`, `min`, `max`, `grp`, `all` or `dist`: SELECT accepts them, but
they are HAVING keywords, so the alias can never be filtered on afterwards.

#### Operator Precedence:
- Parentheses `()` (highest)
- Multiplication `*` and Division `/`
- Addition `+` and Subtraction `-` (lowest)

Example: `10 + 5 * 2` evaluates as `10 + (5 * 2) = 20`

#### Where Arithmetic Actually Works:
Only on the expression path - `RsqlQueryService.getAggregateResultWithExpressions()` /
`getAggregateResultAsPageWithExpressions()`, `RsqlCompiler.compileSelectToExpressions()`,
`SelectExpressionVisitor`. The plain aggregate path (`getAggregateResult()` / `getAggregateResultAsPage()`,
`SelectAggregateVisitor`) rejects it with
`SyntaxErrorException: Arithmetic expressions with operators are not supported in this query type.`

### HAVING Clause Syntax

HAVING filters the grouped result with the WHERE operators, over SELECT aliases, aggregate calls and the
GROUP BY fields derived from the SELECT string - every SELECT element without an aggregate becomes a GROUP BY
field:

```
SUM(price)=gt=1000
productCount=gt=2;SUM(price)=gt=1000
```

Traps worth knowing:
- `count`, `avg`, `sum`, `min`, `max`, `grp`, `all`, `dist`, `and`, `or`, `null`, `true`, `false` are lexer
  keywords and can never be a field name or a referenced alias - `count=gt=2` is a syntax error.
- A bare field that is neither a SELECT alias nor one of the derived GROUP BY fields raises
  `java.lang.IllegalArgumentException`, not SyntaxErrorException.
- Two expressions can be compared with `==` and `!=` only. `SUM(a)=gt=SUM(b)` parses, then fails when the
  predicate is built - `=gt= =ge= =lt= =le=` cast the right side to `Comparable`.
- `COUNT(DIST a, b)` is valid in SELECT but throws in HAVING: one field only.
- HAVING has no `=clike=` / `=cnlike=`, does not map `*` to `%` and does not lower-case - the pattern is
  written with SQL wildcards directly.

## Important Implementation Notes

### Running Integration Tests
Integration tests are in a separate module and test the library with a real H2 database. Its surefire is
configured to include `**/*IT.java`, so they run in the `test` phase - `mvn verify` is not needed:
```bash
mvn test -pl rsql-filter-integration-tests
```

### ANTLR Grammar Compilation
The ANTLR grammar files need to be compiled before building. This happens automatically during build, but can be done manually:
```bash
mvn -pl rsql-filter generate-sources
```
`rsql-filter/ANTLR-GUIDE.md` covers the grammar workflow in more detail.

### Generated ANTLR Sources
The generated parsers live in `rsql-filter/src/main/java/rsql/antlr/{lexer,select,where,having}` and are
COMMITTED to git. Every build deletes those four directories at the `initialize` phase (maven-clean-plugin,
execution `purge-antlr-generated`) and regenerates them, because each of the four antlr4-plugin executions
names a single grammar and so never learns that RsqlWhere.g4 and RsqlHaving.g4 import RsqlCommonLexer.g4 -
changing only the shared lexer would otherwise leave stale token definitions behind a green build. Two
consequences: never hand-edit anything under `rsql/antlr`, and expect a full recompile of the module on every
build. Classes under `rsql.antlr.*` are generated output and NOT a supported public API - 0.7.6 / 0.6.21 removed
several context classes and renumbered the `RULE_*` constants.

### Parser Limits
`RsqlWhereTreeParser` bounds both recursions, so deep input raises `rsql.exceptions.SyntaxErrorException`
instead of `StackOverflowError`: `DEFAULT_MAX_NESTING_DEPTH = 100` (the parser, one frame per level of
grouping parentheses) and `DEFAULT_MAX_TREE_DEPTH = 500` (the visitors, walking the parse tree). Both are
`static volatile` with `get/setMaxNestingDepth()` and `get/setMaxTreeDepth()` - JVM-wide, and both govern
WHERE, HAVING and SELECT. Since 0.7.6 / 0.6.21.

### Package Structure
An earlier restructuring, long done - the old names still turn up in old branches and issues:
- `test-appl` → `rsql-filter-demo`
- Package `testappl` → `com.nomendi6.rsql.demo`
- Integration tests moved to standalone module without JHipster dependencies

### Key Files for Understanding the Library

1. **Core Query Service**: `rsql-filter/src/main/java/rsql/RsqlQueryService.java`
   - Main entry point for executing RSQL queries
   - Supports both Specification-based and JPQL-based queries
   - `findByFilter()`, the four `getAggregateResult*()` methods and the four `getLOV*()` methods

2. **RSQL Compiler**: `rsql-filter/src/main/java/rsql/RsqlCompiler.java`
   - Compiles RSQL strings to JPA Specifications
   - Provides `compileSelectToExpressions()` for parsing SELECT clauses with arithmetic expressions
   - Provides `compileToFilterNode()` for the neutral FilterNode tree used by rsql.describe

3. **Grammar Files**: `rsql-filter/src/main/antlr/`
   - RsqlWhere.g4 - Defines the WHERE clause syntax
   - RsqlSelect.g4 - Defines the SELECT clause syntax with arithmetic expression support
   - RsqlHaving.g4 - Defines the HAVING clause syntax
   - RsqlCommonLexer.g4 - Common lexer rules, imported by RsqlWhere.g4 and RsqlHaving.g4

4. **SelectExpression Hierarchy**: `rsql-filter/src/main/java/rsql/helper/`
   - SelectExpression (abstract base) - Base class for all SELECT expressions
   - FieldExpression - Simple field references
   - FunctionExpression - Aggregate functions (SUM, AVG, COUNT, MIN, MAX, and GRP, which aggregates nothing
     and only marks a GROUP BY field - `AggregateField.AggregateFunction.NONE`)
   - BinaryOpExpression - Arithmetic operations (+, -, *, /)
   - LiteralExpression - Numeric literals
   - BinaryOperator (enum) - Arithmetic operators

5. **SelectExpressionVisitor**: `rsql-filter/src/main/java/rsql/select/SelectExpressionVisitor.java`
   - Parses ANTLR parse tree into SelectExpression objects
   - Validates field paths against JPA metamodel
   - Handles operator precedence and parentheses

6. **Filter Descriptions**: `rsql-filter/src/main/java/rsql/describe/`
   - RsqlFilterDescription - entry point; `describe()` gives text and JasperReports rows, `parse()` the tree
   - FilterNode / FilterGroup / FilterCondition - the neutral tree, built by
     `rsql/where/WhereDescriptionVisitor.java`
   - FilterLabelResolver (+ Map and ResourceBundle implementations) - readable names for fields and operators
   - Shipped in 0.7.6 / 0.6.21

7. **Integration Tests**: `rsql-filter-integration-tests/src/test/java/`
   - Comprehensive tests showing all supported features
   - Good examples of how to use the library
   - SelectExpressionIT - Tests for arithmetic expressions with real JPA entities

8. **User-facing docs** at the repo root: README.md, API.md, SELECT.md, HAVING.md, CHANGELOG.md
   - A behaviour change is not finished until these are in step with it

### Common Development Tasks

#### Adding a New WHERE Operator
1. Update the grammar file (RsqlWhere.g4)
2. Regenerate ANTLR code: `mvn -pl rsql-filter generate-sources`, and commit the regenerated sources under
   `rsql/antlr/`
3. Update ALL four WHERE visitors, not just the first one: WhereSpecificationVisitor (Specifications),
   WhereStringVisitor (JPQL text), WhereTextVisitor and WhereDescriptionVisitor (rsql.describe). An operator
   handled in only one of them breaks the others: WhereTextVisitor and WhereDescriptionVisitor throw
   `SyntaxErrorException: Unknown operator`, while WhereStringVisitor renders a null operator into the JPQL
   text - silently wrong output rather than an error.
4. Decide whether HAVING gets it too - RsqlHaving.g4 and HavingSpecificationVisitor are separate
5. Add integration tests

#### Using Arithmetic Expressions in SELECT
Arithmetic works only on the expression path. From a service, that is one of the two `*WithExpressions`
methods:

```java
// All four aggregate methods take exactly (selectString, filter, havingFilter, pageable) - there is no
// three-argument overload; havingFilter and pageable may be null. A Sort is not a Pageable: wrap it as
// PageRequest.of(0, size, Sort.by(...)).
List<Tuple> rows = productService.getQueryService().getAggregateResultWithExpressions(
    "productType.name:typeName, SUM(price) * 1.2:totalWithTax",
    "status==#ACTIVE#",
    null,
    null
);
```

Which of the four to call:
- `getAggregateResult()` / `getAggregateResultWithExpressions()` return ALL groups. Page number and size are
  ignored; only the `Sort` of the Pageable is used, and it is resolved against the entity root
  (`QueryUtils.toOrders`), so it cannot sort by a SELECT alias.
- `getAggregateResultAsPage()` / `getAggregateResultAsPageWithExpressions()` apply a real offset/limit, count
  `totalElements` after HAVING, and resolve ORDER BY as SELECT alias → SELECT field path → entity path.

The layers underneath, when a query has to be assembled by hand:

```java
// Using SimpleQueryExecutor directly
List<Tuple> results = SimpleQueryExecutor.getAggregateQueryResultWithSelectExpression(
    Product.class,
    Tuple.class,
    "productType.name:typeName, SUM(price) * 1.2:totalWithTax",
    "status==#ACTIVE#",  // WHERE filter (optional)
    null,                // HAVING filter (optional)
    null,                // Pageable (optional)
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
2. **The start rule is `select: selectElements`, with no `+`**: the `+` was removed in 0.7.6 / 0.6.21 because a second
   group could then begin at any `*`, which made parsing exponential and let `code name` parse as though the
   comma were there. Do not reintroduce it. The same applies to the removed error alternatives in
   `RsqlWhere.g4` - both grammars carry a comment saying so.
3. Expression precedence is handled by grammar structure (multiplication/division before addition/subtraction)
4. After changing grammar, regenerate: `mvn -pl rsql-filter generate-sources`
5. Update `SelectExpressionVisitor` if adding new expression types
6. Run tests: `mvn test -pl rsql-filter` and `mvn test -pl rsql-filter-integration-tests`

#### Testing with the Demo Application
The demo runs on `release-3` only - it is not a module of this reactor. To exercise a library change against
it, port the change to the 0.6.x line first; `rsql-filter-demo/README.md` on that branch has the run
instructions.

#### Publishing to Maven Central
The project is configured for Maven Central deployment. See the parent POM for GPG signing configuration.