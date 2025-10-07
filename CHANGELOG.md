# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

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
