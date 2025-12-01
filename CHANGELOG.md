# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

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
