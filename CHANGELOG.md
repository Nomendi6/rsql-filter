# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.6.21] - 2026-08-10

### Fixed
- **Filters ending in a grouping `)` followed by a newline are no longer rejected.** `(name=='a')\n` failed
  with `SyntaxErrorException: Missing opening parenthesis`, even though no parenthesis was missing. This broke
  `RsqlWhereString.parseFile` systematically, since files normally end with a newline, and it reached every
  entry point - `compileToSpecification`, `compileToRsqlQuery`, `parseString` and `parseFile` all share the
  same tree parser.

  Only the *trailing* newline is affected. A leading or inner newline was rejected before and still is;
  `NEWLINE` is a real token that only the trailing-token check tolerates.

- **Parsing parentheses is no longer exponential.** Two error alternatives in the `condition` rule, added to
  produce a nicer error message, made every `)` ambiguous - it could either close a `conditionParens` or start
  the tail of `missingOpeningParenthesis` - so the adaptive prediction explored 2^n paths on n parentheses.
  The cost was paid on **valid** input:

  | filter | before | after |
  |---|---|---|
  | 26 nested levels (56 characters) | ~12 700 ms | ~2 ms |
  | 200 flat groups (1 399 characters) | ~9 200 ms | ~19 ms |
  | 5 000 nested levels | unreachable | ~12 ms |

  Both problems had the same cause and the same fix: the two alternatives were removed.

### Changed
- **The message for a stray `)` changed.** It used to be `Missing opening parenthesis`; it is now
  `Unexpected input after the filter expression at position N`, which also carries the position. The exception
  type is unchanged (`SyntaxErrorException`), and every valid filter parses exactly as before - verified over
  474 filters collected from the test suites, with identical parse trees.

  Code that matches on the *text* of the exception message needs updating.

- The `errorCondition` rule was removed. No rule ever invoked it, so the `Missing closing parenthesis` message
  it carried was never produced - which is why the assertion for it had been commented out. An unclosed
  parenthesis is reported by `CustomErrorStrategy`, as before.

- Classes under `rsql.antlr.*` are generated ANTLR output and **not a supported public API**. This release
  removes `MissingOpeningParenthesisContext`, `ErrorConditionContext`, `MissingClosingParenthesis2Context`
  and the matching visitor and listener methods, and renumbers the `RULE_*` constants.

### Added
- **The WHERE and HAVING parsers now reject filters that would exhaust the stack**, instead of throwing
  `StackOverflowError`. That was an `Error`, so `catch (SyntaxErrorException)` never saw it and a bad request
  surfaced as a 500.

  Two limits, because there are two recursions. `RsqlWhereTreeParser.getMaxNestingDepth()` (default 100)
  bounds the parser, which descends one frame per level of grouping parentheses.
  `getMaxTreeDepth()` (default 500) bounds the visitors, which walk the parse tree recursively. Both are
  settable, and both apply to HAVING as well.

  Measured overflow on this code base, one fresh JVM per data point: about 1 237 nested levels and a
  1 630-condition chain on a 512k stack, 2 801 and 3 750 on a 1M one. The defaults sit well below the
  smaller figures, since 512k is a common container default.

  Every parenthesis counts towards the nesting depth, but the depth is decremented on the closing token, so
  nothing valid is rejected: an `IN` list of 1 000 elements, 200 flat groups, 300 aggregate calls and 21 `IN`
  conditions all stay at depth 1, and parentheses inside a string literal are part of a single token and never
  reach the check at all. The same limits apply to WHERE, HAVING and SELECT.

  Nested aggregate calls are covered too. `functionArg` may itself be a `functionCall`, so
  `SUM(SUM(SUM(...)))` is a genuinely recursive path through the parser; measured thresholds for it are lower
  than for grouping parentheses (roughly 1 000-1 500 levels on a 512k stack against 1 184), which the default
  of 100 clears by an order of magnitude.

- **Long `IN` lists and long field paths no longer render in quadratic time.** `ctx.inListElement()` and
  `ctx.DOT_ID()` are `getRuleContexts` calls, and each one rebuilds its list by scanning every child of the
  node. Calling them once per loop iteration - as eight visitor loops did - made rendering O(n^2), while the
  parser itself was linear throughout.

  | input | before | after |
  |---|---|---|
  | `IN` list of 16 000 elements | ~5 000 ms | ~54 ms |
  | field path of 16 000 segments | ~2 068 ms | ~33 ms |

  Affects `WhereStringVisitor`, `RsqlWhereHelper` and the five SELECT visitors. No behaviour changes.

- **SELECT clauses parse in linear time, and a missing separator is now an error.** The start rule was
  `select: selectElements+`, which let a second group of elements begin at any position. Since a group may
  start with `*`, and `*` is also the multiplication operator, the parser had to decide at every `*` whether
  the current expression continued or a new group began - a decision needing lookahead over the whole
  expression:

  | select clause | before | after |
  |---|---|---|
  | `a+b*c` x100 (401 characters) | ~2 900 ms | ~6 ms |
  | `a+b*c` x200 (801 characters) | ~14 000 ms | ~6 ms |
  | `a+b*c` x400 (1 601 characters) | > 120 s | ~5 ms |

  The same `+` also made `code name` parse as though the comma were there, because the visitors iterate
  every group and accumulate. Such input is now rejected. Comma-separated clauses are unaffected.

## [0.6.20] - 2026-08-08

### Fixed
- **Every generated LIKE predicate now carries an explicit `ESCAPE '\'` clause, and the pattern has its
  backslashes escaped.** Until now the meaning of a backslash in a LIKE pattern was left to the database and
  to the Hibernate dialect, so the same filter could behave differently depending on where it ran. Concretely,
  a filter such as `code=like='*C:\temp*'` executed as **native SQL** on PostgreSQL matched `C:temp` instead
  of `C:\temp`, and a pattern ending in a backslash was rejected outright with
  `LIKE pattern must not end with escape character`. Both now work: the pattern is emitted as
  `%c:\\temp%` with `escape '\'`.

  Applies to all four WHERE operators (`=like=`, `=nlike=`, `=clike=`, `=cnlike=`) on the Specification, the
  JPQL-text and the string-rendering paths, and to `=like=` / `=nlike=` on the HAVING path.
- `PredicateToText` renders the escape character, so the printed predicate is again a faithful - and
  re-executable - representation of the query.

### Changed
- ⚠ The generated JPQL/SQL text changed: every LIKE predicate gained a ` escape '\'` suffix and every
  backslash in the pattern is doubled. Query **results are unchanged** through Hibernate, which already
  neutralised the database default escape; what changes is the native-SQL path, which now behaves the same
  way as the Hibernate one. Assertions that compare generated query text verbatim need updating.
- `%` and `_` are still **not** escaped - they remain SQL wildcards, so `code=like='50%'` also matches `500`.
- On the HAVING path only the backslash is escaped: `*` is **not** mapped to `%` and the expression is not
  lower-cased, exactly as before.

## [0.6.19] - 2026-08-08

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
