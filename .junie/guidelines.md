# RSQL Filter Project Guidelines

This document provides guidelines and information for developers working on the RSQL Filter project.

## Build/Configuration Instructions

### Project Structure

The project is a Maven multi-module reactor. On this branch (the 0.7.x line) the root `pom.xml` declares
**two** modules:

1. **rsql-filter** - The core library (`com.nomendi6:rsql-filter`) that provides RSQL filtering for Spring Data JPA
2. **rsql-filter-integration-tests** - A test-only module that exercises the library against an H2 database, with
   no JHipster dependencies

The **rsql-filter-demo** directory (a JHipster-generated Spring Boot + Angular application) is still present but
commented out of the reactor here - it depends on JHipster 8 / Spring Boot 3 and is built on `release-3` only.
So no build on this branch needs Node, npm or network access for a frontend.

There is no Gradle build here, and no `rsql` or `test-appl` directory - both were renamed when the project moved
to Maven. Use `mvn`, never `./gradlew`.

### Building the Project

#### Prerequisites

- Java 21 on this line (the `release-3` line needs Java 17)
- Maven 3.6 or newer; there is no wrapper at the root, and `rsql-filter-demo/mvnw` covers the demo module only
- No network access is needed for a frontend here: `rsql-filter-demo`, the module that downloads Node and npm
  through frontend-maven-plugin, is out of the reactor on this line

#### Building

```bash
# Build every module (~40 s with tests skipped, ~1 min with them)
mvn clean install -DskipTests

# Build and test everything
mvn clean install

# Build only the library - the fast path for library work
mvn install -pl rsql-filter

# Regenerate the ANTLR parsers after a grammar change
mvn -pl rsql-filter generate-sources
```

### Configuration

The platform versions are declared in the `<properties>` block of the root `pom.xml`:

- `java.version` 21
- `spring-boot.version` 4.0.3
- `hibernate.version` 7.2.4.Final
- `mapstruct.version` 1.6.3
- `antlr4.version` 4.13.2

`rsql-filter-demo/pom.xml` re-declares `java.version` (17), `spring-boot.version` (3.4.4) and
`mapstruct.version` (1.6.3) in its own `<properties>` block, and those shadow the inherited values. On this
line that divergence is deliberate - the demo is out of the reactor precisely because it is still a
JHipster 8 / Spring Boot 3 application - so do **not** align them here. On `release-3` they must match.

The project is maintained as two parallel lines, so check which one you are on before quoting a version:

- **release-3** - the 0.6.x line, currently 0.6.22, Java 17, Spring Boot 3.4.4, Hibernate 6.5.3.
  `rsql-filter-demo` ships only on this line.
- **master** - the 0.7.x line, currently 0.7.7, Java 21, Spring Boot 4.0.3, Hibernate 7.2.4.

The demo application is configured in `rsql-filter-demo/pom.xml` and `src/main/resources/config/`:

- Spring profiles: dev (default), prod, tls, e2e
- Database: H2 in memory under `dev`, PostgreSQL under `prod` (`src/main/docker/services.yml`)

## Testing Information

### Test Structure

- **rsql-filter/src/test/java/rsql/** - library unit tests, grouped as `app`, `describe`, `having`, `select`,
  `where` (263 tests)
- **rsql-filter-integration-tests/src/test/java/com/nomendi6/rsql/it/** - integration tests against H2
  (564 tests). Surefire is configured to include `**/*IT.java`, so these run in the `test` phase - do not reach
  for `verify`.
- **rsql-filter-demo/src/test/java/com/nomendi6/rsql/demo/** - demo application tests, with sub-packages
  config, domain, management, repository, rsql, security, service, web. These do **not** run on this line: the
  module is out of the reactor here, so `mvn test` never reaches them

### Running Tests

```bash
# Library unit tests (~12 s)
mvn test -pl rsql-filter

# Integration tests against H2 (~45 s)
mvn test -pl rsql-filter-integration-tests

# A single test class
mvn test -pl rsql-filter -Dtest=RsqlWhereStringTest
```

### Writing RSQL Tests

The library provides `RsqlQueryService` for executing RSQL queries: a 4-argument constructor for Specification
mode, and a 6-argument one that adds a JPQL SELECT and a JPQL count query. `a0` is the default root alias the
library stamps on every generated WHERE and ORDER BY, so custom JPQL must alias its root `a0` or call
`setSelectAlias` / `setCountAlias`.

```java
@Autowired
private EntityManager em;

@Autowired
private ProductTypeRepository productTypeRepository;

@Autowired
private ProductTypeMapper productTypeMapper;

private RsqlQueryService<ProductType, ProductTypeDTO, ProductTypeRepository, ProductTypeMapper> queryService;

private String jpqlSelectAll = "SELECT a0 FROM ProductType a0";
private String jpqlSelectAllCount = "SELECT count(distinct a0) FROM ProductType a0";

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
    // Test filtering by name - the wildcard goes inside the quotes
    String filter = "name=*'*Type*'";
    List<ProductTypeDTO> result = queryService.findByFilter(filter);

    // Verify results
    assertThat(result).isNotNull();
}
```

`RsqlQueryService` is safe to hold as a singleton bean: every public method builds a fresh `RsqlContext` for the
query, so nothing query-scoped is shared between threads.

### RSQL Filter Syntax

Only the spellings below parse. `=`, `<>`, `>`, `>=`, `<`, `<=`, `=^` and `=$` are **not** operators in this
language - all eight raise `SyntaxErrorException`, though not all at the same stage: `<>`, `>`, `>=`, `<`,
`<=` and `=^` never tokenise (`line 1:4 token recognition error at: '<'`), while `name='Type'` and
`name=$'T'` tokenise and then fail in the parser (`line 1:5 no viable alternative at input 'name='Type''`).

- **Comparison Operators**:
    - `==` : equal to
    - `!=` or `=!` : not equal to
    - `=gt=`, `=ge=`, `=lt=`, `=le=` : greater than, greater or equal, less than, less or equal
    - `=in=`, `=nin=` : in / not in a list
    - `=bt=`, `=nbt=` : between / not between
    - `=like=` or `=*`, `=nlike=` or `=!*` or `!=*` : case-insensitive pattern match
    - `=clike=` or `=^*`, `=cnlike=` or `=!^*` or `!=^*` : case-sensitive pattern match

- **Patterns**: the wildcard is `*` and it belongs **inside** the quoted value - `'*Type*'` contains,
  `'Type*'` starts with, `'*Type'` ends with. A `*` outside the quotes is a lexer error.

- **Logical Operators**:
    - `and` or `;` : logical AND
    - `or` or `,` : logical OR
    - parentheses group: `(name=='A' or name=='B') and price=gt=10`

- **Right-hand sides**: quoted strings (delimiter `'`, `"` or `` ` ``, escaped by doubling it - backslash is an
  ordinary character), numbers, `null`, `true`, `false`, `#2024-01-01#` dates, `#2024-01-01T23:59:59Z#`
  datetimes (the zone is mandatory - `#2024-01-01T23:59:59#` is a syntax error), `#ACTIVE#` enums, `:name`
  parameters that the caller binds, and another field: `price=gt=cost`, `code=in=(status,name)`.

- **Examples** (each verified against the parser):
    - `name=='Type'` : name equals 'Type'
    - `name=*'*Type*'` : name contains 'Type', ignoring case
    - `parent.id=gt=1 and product.id=gt=1` : parent ID > 1 AND product ID > 1
    - `status=in=(#ACTIVE#,#PENDING#)` : enum in a list
    - `validFrom=ge=#2024-01-01#;createdAt=lt=#2024-01-01T23:59:59Z#` : date and datetime bounds

README.md carries the maintained reference for the whole operator and literal set; link to it, do not copy it.

### SELECT and HAVING

Beyond WHERE the library has two more languages, documented in SELECT.md and HAVING.md:

- **SELECT** (`RsqlSelect.g4`) - field paths, `*` and `entity.*`, aliases written with `:`, and a closed
  function list: SUM, AVG, MIN, MAX, COUNT, GRP. Names are case-insensitive; there is no `DATE()` or any other
  scalar function. Elements are comma-separated, `*` is legal only as the first element, and a trailing comma
  is an error.
- **Arithmetic** (`+ - * /`) between selected values works only through
  `RsqlQueryService.getAggregateResultWithExpressions(select, filter, having, pageable)` and
  `getAggregateResultAsPageWithExpressions(...)`. Plain `getAggregateResult` / `getAggregateResultAsPage`
  reject it with `SyntaxErrorException("Arithmetic expressions with operators are not supported in this query
  type...")`. Arithmetic inside an aggregate call - `SUM(a*b)` - does not parse on any path.
- **HAVING** (`RsqlHaving.g4`) - filters the grouped result: `SUM(price)=gt=1000`, or a SELECT alias,
  `productCount=gt=5`. The GROUP BY list is derived from the SELECT string - every element without an aggregate
  becomes a GROUP BY expression - and a bare field in HAVING must be a SELECT alias or one of those fields,
  otherwise it raises `IllegalArgumentException`. `count`, `avg`, `sum`, `min`, `max`, `grp`, `all`, `dist`,
  `and`, `or`, `null`, `true`, `false` are lexer keywords, so never alias a column `:count` or `:avg` - use
  `:productCount` / `:avgPrice`.

All four aggregate methods take the same four arguments and `havingFilter` may be `null`. A `Sort` is not a
`Pageable`; wrap it as `PageRequest.of(0, 20, Sort.by("name"))`.

### Filter Descriptions

`rsql.describe.RsqlFilterDescription` turns a WHERE filter into readable text and into report rows. It works on
the parse tree alone - no `EntityManager`, no entity class - which also makes it the quickest way to check that
a filter parses. Shipped in 0.6.22.

```java
FilterDescription d = new RsqlFilterDescription().describe("name=*'A*';price=gt=100");
d.getText();  // name starts with (ignoring case) "A" and price is greater than 100
```

## Additional Development Information

### Code Style

The project follows standard Java code style conventions. Key points:

- Use 4 spaces for indentation
- Follow Java naming conventions
- Use meaningful variable and method names
- Add JavaDoc comments for public classes and methods

### Working with ANTLR

Four grammar files live in `rsql-filter/src/main/antlr/`:

- `RsqlCommonLexer.g4` - the tokens shared by the three parsers
- `RsqlWhere.g4` - the WHERE clause
- `RsqlSelect.g4` - the SELECT clause
- `RsqlHaving.g4` - the HAVING clause

Regenerate with `mvn -pl rsql-filter generate-sources`. The generated parsers are committed to git under
`rsql-filter/src/main/java/rsql/antlr/{lexer,select,where,having}` and are rewritten on every build, so a
grammar change produces a diff in tracked sources that must be committed with it. Treat `rsql.antlr.*` as build
output: it is not a supported public API, and its class names and `RULE_*` constants change between releases.
The grammars carry comments explaining why some rules look the way they do - removed error alternatives, the
shape of the SELECT start rule. Do not "simplify" those back; they caused exponential parser prediction. See
rsql-filter/ANTLR-GUIDE.md for the full picture.

### Error Handling

Parse and validation failures raise `rsql.exceptions.SyntaxErrorException`. Two guards protect the parsers,
shared by WHERE, HAVING and SELECT: `RsqlWhereTreeParser.DEFAULT_MAX_NESTING_DEPTH` = 100 and
`DEFAULT_MAX_TREE_DEPTH` = 500. Both are JVM-wide and adjustable through the static `setMaxNestingDepth` /
`setMaxTreeDepth`. Over the limit you get a `SyntaxErrorException`, not a `StackOverflowError`.

### Publishing the Library

The library publishes to Maven Central through `org.sonatype.central:central-publishing-maven-plugin`, wired
into the `release` profile:

```bash
# Sign and publish from the project root
mvn -Prelease deploy
```

You need a `<server>` entry with id `central` holding your Central portal token in `~/.m2/settings.xml`, and a
GPG key that maven-gpg-plugin can use.

### Debugging Tips

1. Enable debug logging in application.yml:
   ```yaml
   logging:
     level:
       rsql: DEBUG
   ```

2. To check a filter without a database, run it through `rsql.where.RsqlWhereString` (renders JPQL-ish WHERE
   text) or `rsql.describe.RsqlFilterDescription` - neither needs an `EntityManager`.

3. For complex RSQL expressions, break them down into smaller parts for easier debugging.

### Where to Look Next

- README.md - overview and the full WHERE reference
- API.md - the public API surface of the library
- SELECT.md and HAVING.md - the SELECT and HAVING languages
- rsql-filter/ANTLR-GUIDE.md - grammars, generation and parser hardening
- CONTRIBUTING.md - branches, releases and the contribution workflow
- CHANGELOG.md - what changed, including the `[Unreleased]` section
