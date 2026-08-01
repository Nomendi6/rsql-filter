# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.6.18] - 2026-08-01

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
- **Regenerating the parsers used to strip the `package` declaration from `RsqlCommonLexer.java`**, which had to
  be restored by hand. The package is now set with the `-package` argument of `antlr4-maven-plugin`.
  (An `@header` block in the grammar would not work: ANTLR inherits it into `RsqlWhere.g4` and `RsqlHaving.g4`,
  which import this lexer, giving their generated sources two `package` declarations and breaking the build
  on any tree without `target/`.) Regeneration of all four grammars is verified idempotent and every generated
  file carries exactly one `package` declaration.
- **Changing only `RsqlCommonLexer.g4` no longer leaves the WHERE/HAVING parsers with stale tokens.** Each of
  the four `antlr4-maven-plugin` executions restricts itself to a single grammar with `<includes>`, so the
  plugin never learned that `RsqlWhere.g4` and `RsqlHaving.g4` depend on the lexer they import. A
  `maven-clean-plugin` execution bound to `initialize` now purges the four generated directories before each
  build, so every build regenerates all four grammars from the `.g4` files.

### Notes
- Backported from the `0.7.x` line (`0.7.2` + the ANTLR packaging fix from `0.7.3`).

## [0.6.17] - 2026-06-24

### Added
- **Case-sensitive `LIKE` operators** `=clike=` (alias `=^*`) and `=cnlike=` (aliases `=!^*` / `!=^*`).
  - Unlike the existing `=like=` / `=nlike=`, these do **not** wrap the column in `lower(...)` and do **not** lower-case the pattern, so matching is case-sensitive and the predicate stays index-friendly (sargable). The `*` → `%` wildcard mapping is unchanged.
  - Available on the Specification (`compileToSpecification`), JPQL text (`compileToRsqlQuery`) and string (`parseString`) paths. Parameter-bound patterns are not supported (mirrors `=like=`).

### Changed
- Existing `=like=` / `=*` and `=nlike=` / `=!*` / `!=*` behavior is unchanged (still case-insensitive).

### Fixed
- `RsqlWhere.g4` now declares `@header { package rsql.antlr.where; }` (consistent with `RsqlHaving.g4`), so regenerated ANTLR sources get the `rsql.antlr.where` package automatically — the previously required manual edit is no longer needed.

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
