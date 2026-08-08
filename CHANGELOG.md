# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.7.4] - 2026-08-08

### Fixed
- **A filter is no longer silently truncated.** The start rules (`where: condition+`, `having:
  havingCondition+`) are not anchored to `EOF`, and the tree parsers did not check that the token stream was
  consumed. Two kinds of input passed silently as a result:
  - **conditions next to each other without a logical operator** — `name=='a' code=='b'` (a space instead of
    `;` or `and`) compiled to `a0.code=:p2` with a **single** parameter: the `name` condition was dropped and
    the executed query returned **more rows than the filter asked for**;
  - **trailing uninterpreted input** — `name=='a' 123` compiled to `name='a'`.

  Both now raise `SyntaxErrorException`. The check covers the WHERE, HAVING and SELECT paths; a trailing
  newline is still accepted, so `parseFile` on a file ending with `\n` keeps working.
- **A backslash is now an ordinary character in string literals, so a value may end with one.** The lexer
  used to treat `\X` as a unit that protected `X`, while the reader never removed the backslash — so `\`
  stayed in the value and a value ending in `\` (a Windows path such as `C:\dir\`) could not be written at
  all: the only encoding that parsed, `"abc\\"`, silently yielded `abc\\`. Doubling the delimiter is now the
  single escape mechanism, which makes encoding **total** — every value can be written as
  `delimiter + value.replace(delimiter, delimiter+delimiter) + delimiter`.

### Changed
- ⚠ **Breaking:** a backslash placed immediately before the active delimiter no longer protects it.
  `name=="a\"b"` and `name=='it\'s'` used to parse (yielding a value that still contained the backslash) and
  are now a syntax error; rewrite them by doubling the delimiter — `name=="a\""b"`, `name=='it''s'`.
- ⚠ **Known silent change (three forms).** `name=="""\"`, `name=='''\'` and ``name==```\` `` used to return
  an **empty string** and now return `"\`, `'\` and `` `\ `` respectively, without an error. These inputs
  were already returning nonsense, so they are documented rather than guarded against.
- ⚠ **Native SQL note.** A LIKE pattern ending in a single backslash is now expressible in RSQL. Through
  Hibernate/JPQL it works correctly, but if `RsqlQuery.where` is executed as **native SQL** (see
  `fixIdsForNativeQuery`), PostgreSQL rejects it with `LIKE pattern must not end with escape character`,
  because the `ESCAPE` clause is not emitted yet. Guard such values on the client until that is delivered.

## [0.7.3] - 2026-08-01

### Fixed
- **The `RsqlCommonLexer` package is now set with the `-package` argument of `antlr4-maven-plugin` instead of
  an `@header` block in the grammar.** The `@header` introduced in 0.7.2 fixed the lexer itself, but ANTLR
  inherits a `@header` from an imported grammar — and `RsqlWhere.g4`, `RsqlSelect.g4` and `RsqlHaving.g4` all
  import `RsqlCommonLexer`. Regenerating them therefore produced sources with **two** `package` declarations
  (their own plus `rsql.antlr.lexer`), which do not compile.
  The sources committed in 0.7.2 each carry exactly one `package` declaration, so the **published 0.7.2
  artifact is fine** and applications depending on it are unaffected. **Building 0.7.2 from source is not** —
  on a tree without `target/` (a fresh clone, a CI job, `mvn clean install`) the plugin regenerates the
  grammars, rewrites 12 of the 19 generated files with two `package` declarations, and compilation fails with
  `class, interface, enum, or record expected`. Verified on a fresh checkout of the 0.7.2 tag.
  With 0.7.3 a fresh build succeeds and all 19 generated files carry exactly one `package` declaration;
  regeneration of all four grammars is idempotent.
- **Changing only `RsqlCommonLexer.g4` no longer leaves the WHERE/HAVING parsers with stale tokens.** Each of
  the four `antlr4-maven-plugin` executions restricts itself to a single grammar with `<includes>`, so the
  plugin never learned that `RsqlWhere.g4` and `RsqlHaving.g4` depend on the lexer they import. Editing the
  shared token set therefore regenerated the lexer only and silently left `RsqlWhereLexer`/`RsqlHavingLexer`
  on the previous token definitions — with a green build and green tests. A `maven-clean-plugin` execution
  bound to `initialize` now purges the four generated directories before each build, so every build
  regenerates all four grammars from the `.g4` files. Generation is deterministic, so the working tree stays
  clean, and `<excludeDefaultDirectories>` keeps `target/` (and incremental compilation) intact.

## [0.7.2] - 2026-08-01

### Fixed
- **A doubled delimiter in a string literal is now un-escaped on the `WHERE` path.** The grammar has always
  allowed the delimiter to be escaped by doubling it (`name=="say ""hi"""`, `name=='it''s'`, ``name==`a``b` ``),
  and the `HAVING` path already handled it — but the `WHERE` path kept the doubled delimiter in the value.
  Such a filter therefore matched nothing and returned a silently empty result instead of an error.
  This affects every branch that reads a string literal: `==`, `!=`, `=in=`, `=nin=`, `=bt=`, `=nbt=` and the
  whole `LIKE` family, on both the Specification and the JPQL-text path. Enum literals (`#NAME#`) are unaffected.
  ⚠ **This changes results** for filters that use a doubled delimiter — see the README section
  *Escaping the delimiter in string literals*.
- `PredicateToText` now escapes an embedded single quote when rendering a value, so the produced text stays
  valid JPQL (`code = 'it''s'` instead of `code = 'it's'`). Without this, the fix above would have turned a
  previously accidentally-valid rendering into an invalid one.
- `WhereStringVisitor` (the `parseString` rendering path) fixed several pre-existing defects:
  - the `=nlike=` / `=!*` / `!=*` operator had no rendering at all and produced the literal text `null`
    (it affected string, field, number, parameter and date conditions alike);
  - `=nbt=` (NOT BETWEEN) had no rendering and returned `null` for the whole expression;
  - `=in=` / `=nin=` elements were emitted verbatim, so string values kept their original delimiter
    (producing invalid JPQL such as `field in ("a""b")`) and date/enum literals were left as `#…#`;
  - `=bt=` had the same problem for string and enum bounds and for a mixed pair such as `(#2020-01-01#,'a')`;
    a pair of two date or two datetime bounds was already rendered correctly and is unchanged;
  - values are now always emitted as valid JPQL string literals, with an embedded single quote doubled.
- Regenerating the parsers used to strip the `package` declaration from `RsqlCommonLexer.java`, which had to be
  restored by hand. Fixed by declaring `@header { package rsql.antlr.lexer; }` in `RsqlCommonLexer.g4`.
  ⚠ That approach turned out to be wrong — see 0.7.3, which replaces it with the plugin's `-package` argument.

## [0.7.1] - 2026-06-24

### Added
- **Case-sensitive `LIKE` operators** `=clike=` (alias `=^*`) and `=cnlike=` (aliases `=!^*` / `!=^*`).
  - Unlike the existing `=like=` / `=nlike=`, these do **not** wrap the column in `lower(...)` and do **not** lower-case the pattern, so matching is case-sensitive and the predicate stays index-friendly (sargable). The `*` → `%` wildcard mapping is unchanged.
  - Available on the Specification (`compileToSpecification`), JPQL text (`compileToRsqlQuery`) and string (`parseString`) paths. Parameter-bound patterns are not supported (mirrors `=like=`).
  - Backported from `release-3` (`0.6.17`).

### Changed
- Existing `=like=` / `=*` and `=nlike=` / `=!*` / `!=*` behavior is unchanged (still case-insensitive).

### Fixed
- `RsqlWhere.g4` now declares `@header { package rsql.antlr.where; }` (consistent with `RsqlHaving.g4`), so regenerated ANTLR sources get the `rsql.antlr.where` package automatically — the previously required manual edit is no longer needed.
- Bumped `central-publishing-maven-plugin` to `0.11.0` to fix a Maven Central publish failure (`UnrecognizedPropertyException: warnings`).

## [0.7.0] - 2026-04-25

### Changed
- Migrated to Spring Boot 4.0.3 and Hibernate 7.2.4.
- Removed `rsql-filter-demo` module from the `0.7.x` release line.

### Notes
- The library and integration tests are verified on Spring Boot 4.0.3.
- For Spring Boot 3 users, continue using `0.6.x` from the `release-3` branch.

## [0.6.12] - 2025-12-01

### Fixed
- **Hibernate 6 SQM Thread-Safety Bug**: Fixed critical bug where JOINs cached in `joinsMap` were being reused across different `CriteriaQuery` instances
  - Root cause: When `repository.findAll(spec, pageable)` is called, Spring Data JPA calls `toPredicate()` twice (once for main query, once for count query), each with a different `Root` object
  - Hibernate 6 SQM nodes are query-specific and cannot be shared between queries
  - Solution: Added wrapper Specification in `RsqlCompiler.compileToSpecification()` that clears `joinsMap` at the start of every `toPredicate()` call
  - Error message before fix: `Already registered a copy: SqmSingularJoin`

### Changed
- **Logging Improvements**: Changed logging levels to allow proper control from applications
  - All DEBUG logs in `WhereSpecificationVisitor` changed to TRACE
  - All DEBUG logs in `RsqlContext` changed to TRACE
  - Replaced `System.out.println` in `SimpleQueryExecutor` with proper SLF4J logging (`log.error()`)
  - Users can now control logging via: `logging.level.rsql: OFF` (or TRACE/DEBUG/INFO/WARN)

### Removed
- Removed unused `lastRootIdentityHashCode` field from `WhereSpecificationVisitor`
- Removed unused `isSameQueryContext()` method from `WhereSpecificationVisitor`

### Tests
- Added `SpecificationRootChangeIT` with 13 integration tests for root change scenarios
- Added `MultipleRelationsThreadSafetyIT` for thread-safety validation
- All 498 integration tests pass

## [0.6.9] - 2025-10

### Fixed
- Fix duplicate predicate calls in `WhereSpecificationVisitor`
- Added integration tests for OR filters with same JOIN

## [0.6.8] - 2025-10

### Fixed
- Fix JOIN conflicts in count queries by creating fresh `RsqlContext` instances for aggregate operations

## [0.6.7] - 2025-10-07

### Added
- **Enhanced Sorting in Aggregate Queries**: Support for sorting by field paths in addition to aliases
  - Sort by alias: `Sort.by("totalDebit")` (existing)
  - Sort by field path: `Sort.by("account.code")` (new)
  - Sort by arithmetic expression alias: `Sort.by("totalWithTax")` (existing)
- Sorting precedence: Alias → Field Path → Entity Property
- All sorting approaches reuse the same JPA Expression from SELECT clause (no duplicate JOINs)

### Changed
- `SimpleQueryExecutor.getAggregateQueryResultAsPageWithExpressions` - Added `fieldPathToExpressionMap` for field path sorting
- `SimpleQueryExecutor.getAggregateQueryResultWithExpressions` - Added `fieldPathToExpressionMap` for field path sorting
- `SimpleQueryExecutor.getAggregateQueryResultAsPage` - Added `fieldPathToExpressionMap` for AggregateField-based queries

### Documentation
- Updated `SELECT.md` with new "Sorting Precedence" section
- Updated `API.md` with sorting options for version 0.6.7+
- Updated `README.md` with "What's New in 0.6.7" section
- Added comprehensive integration tests for sorting by field paths

### Tests
- Added 7 new integration tests in `RsqlQueryServiceExpressionIT`:
  - `testGetAggregateResultAsPageWithExpressions_SortByFieldPath`
  - `testGetAggregateResultAsPageWithExpressions_SortByFieldPathDescending`
  - `testGetAggregateResultAsPageWithExpressions_SortByAggregateAlias`
  - `testGetAggregateResultAsPageWithExpressions_SortByArithmeticExpressionAlias`
  - `testGetAggregateResultAsPageWithExpressions_SortByMultipleColumns`
  - `testGetAggregateResultAsPage_SortByFieldPath`

## [0.6.6] - Previous release

### Features
- Arithmetic expressions in aggregate queries (`+`, `-`, `*`, `/`)
- HAVING clause support with full RSQL syntax
- SELECT clause with aliases and navigation properties
- Aggregate functions: COUNT, SUM, AVG, MIN, MAX
- Integration with Spring Data JPA pagination

## [0.6.5] - Previous release

### Features
- Initial support for aggregate queries
- Basic SELECT clause functionality
- WHERE clause RSQL syntax support

---

For older versions, see git commit history.
