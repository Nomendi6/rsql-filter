# Contributing to RSQL Filter

First off, thank you for considering contributing to RSQL Filter! It's people like you that make RSQL Filter such a great tool.

## Code of Conduct

There is no separate code-of-conduct document in this repository. Be respectful in issues, pull requests and reviews, and report unacceptable behavior to the project maintainers.

## How Can I Contribute?

### Reporting Bugs

Before creating bug reports, please check existing issues as you might find out that you don't need to create one. When you are creating a bug report, please include as many details as possible:

* **Use a clear and descriptive title** for the issue to identify the problem.
* **Describe the exact steps which reproduce the problem** in as many details as possible.
* **Provide specific examples to demonstrate the steps**. Include links to files or GitHub projects, or copy/pasteable snippets, which you use in those examples.
* **Describe the behavior you observed after following the steps** and point out what exactly is the problem with that behavior.
* **Explain which behavior you expected to see instead and why.**
* **Include the version of RSQL Filter you're using** - and therefore which line it comes from, 0.6.x or 0.7.x - and the versions of Spring Boot and Java.

### Suggesting Enhancements

Enhancement suggestions are tracked as GitHub issues. When creating an enhancement suggestion, please include:

* **Use a clear and descriptive title** for the issue to identify the suggestion.
* **Provide a step-by-step description of the suggested enhancement** in as many details as possible.
* **Provide specific examples to demonstrate the steps**.
* **Describe the current behavior** and **explain which behavior you expected to see instead** and why.
* **Explain why this enhancement would be useful** to most RSQL Filter users.

### Pull Requests

1. Fork the repo and create your branch from the line you are fixing - `release-3` for the 0.6.x line, `master` for the 0.7.x line. Do not branch from `develop`; see [Branches and Releases](#branches-and-releases).
2. If the change applies to both lines, prepare a branch on each. The lines are never merged into one another.
3. If you've added code that should be tested, add tests.
4. If you've changed APIs, update the documentation.
5. Add your entry to CHANGELOG.md under the `## [Unreleased]` heading.
6. Ensure the test suite passes.
7. Make sure your code follows the existing code style.
8. Issue that pull request!

## Development Process

### Prerequisites

* JDK 17 on `release-3`, JDK 21 on `master` - the parent POM sets `java.version` per line
* Maven 3.6 or newer
* Network access for the demo module, on `release-3` only: `frontend-maven-plugin` downloads Node v22.14.0 and npm 11.2.0 into it, then runs a full `npm install` and Angular build. On `master` the module is out of the reactor, so no build there needs Node

### Setting up the Development Environment

1. **Clone the repository**
   ```bash
   git clone https://github.com/nomendi6/rsql-filter.git
   cd rsql-filter

   # A fresh clone lands on master, the 0.7.x line - switch if you are working on 0.6.x
   git checkout release-3
   ```

2. **Build the project**
   ```bash
   # Builds every module the line declares - on release-3 that includes the JHipster
   # demo, which downloads Node/npm and builds the Angular frontend (about 5 minutes)
   mvn clean install
   ```

   For work on the library itself, the demo module is pure cost - build only what you need. On `master` the
   demo module is commented out of the reactor, so the root build is library-only there anyway.

   ```bash
   # Core library only - about 40 seconds, no Node, no frontend
   mvn clean install -pl rsql-filter
   ```

3. **Run the tests**
   ```bash
   mvn test
   ```

### Project Structure

```
rsql-filter-mvn/
├── rsql-filter/                      # Core library
├── rsql-filter-integration-tests/    # Integration tests
├── rsql-filter-demo/                 # Demo application (release-3 only)
└── pom.xml                          # Parent POM
```

### Grammar Changes

The parsers generated from the four grammars in `rsql-filter/src/main/antlr/` are committed to git - 33 files
under `rsql-filter/src/main/java/rsql/antlr/`. Every build deletes those output directories at `initialize`
and regenerates them from the `.g4` files, so a grammar change produces a diff in tracked source that has to
be committed together with the change:

```bash
# Regenerate the parsers from the grammars
mvn -pl rsql-filter clean generate-sources

# Review what the grammar change produced
git status rsql-filter/src/main/java/rsql/antlr/
```

See [rsql-filter/ANTLR-GUIDE.md](rsql-filter/ANTLR-GUIDE.md) for how the grammars are wired together and why
the purge step exists.

### Running Tests

```bash
# Run all tests
mvn test

# Run the core library tests only (263 tests)
mvn test -pl rsql-filter

# Run integration tests only (564 tests against in-memory H2 on this line; 563 on release-3)
mvn test -pl rsql-filter-integration-tests

# Run tests with coverage. jacoco is declared only in <pluginManagement> of the parent
# POM, so nothing binds prepare-agent in the library modules - invoke it yourself or the
# report is written from no execution data. Result: rsql-filter/target/site/jacoco/index.html
mvn -pl rsql-filter clean jacoco:prepare-agent test jacoco:report
```

### Code Style

We use the following code style guidelines:

* Java code follows standard Java conventions
* Indentation: 4 spaces (no tabs)
* Maximum line length: 140 characters - the width Prettier uses in `rsql-filter-demo` (`printWidth: 140` in its `.prettierrc`). Nothing enforces it in the library: neither Spotless nor Checkstyle is bound to `rsql-filter`, so keep to it by hand
* Always use braces for if/for/while statements
* Use meaningful variable and method names

### Commit Messages

* Use the present tense ("Add feature" not "Added feature")
* Use the imperative mood ("Move cursor to..." not "Moves cursor to...")
* Limit the first line to 72 characters or less
* Reference issues and pull requests liberally after the first line

Examples:
```
Add support for UUID filtering

- Implement UUID parser in WhereSpecificationVisitor
- Add tests for UUID filtering scenarios
- Update documentation with UUID examples

Fixes #123
```

### Testing

* Write unit tests for new functionality
* Ensure all tests pass before submitting PR
* Include integration tests for complex features
* Test edge cases and error scenarios

### Documentation

* Update README.md if you change functionality; API.md, SELECT.md and HAVING.md cover the public API, the SELECT clause and the HAVING clause
* Add JavaDoc comments for public methods
* Include examples in documentation
* Add your entry to CHANGELOG.md under the `## [Unreleased]` heading. Releasing turns that heading into a version heading, so nothing is written straight under a version number

## Branches and Releases

Two release lines are maintained in parallel, and neither is an ancestor of the other. They diverged at
`1b0a02f`, the 0.6.16 release merge, and have been developed side by side since:

* `master` carries the **0.7.x** line - currently 0.7.6, Java 21, Spring Boot 4.0.3, Hibernate 7.2.4. The `rsql-filter-demo` module is commented out of the reactor there.
* `release-3` carries the **0.6.x** line - currently 0.6.21, Java 17, Spring Boot 3.4.4, Hibernate 6.5.3. This is the line that still ships the demo application.
* `develop` is legacy. Its tip is a 0.7.5-era commit that `master` already contains, it holds none of the 0.6.x work, and nothing merges into it any more. Do not branch from it.

Check for yourself which line a release belongs to:

```bash
# 0.6.x tags live only on release-3
git branch --contains v0.6.21

# 0.7.x tags live only on master
git branch --contains v0.7.6

# Neither line contains the other
git merge-base --is-ancestor release-3 master; echo $?   # 1
git merge-base --is-ancestor master release-3; echo $?   # 1
```

### Porting a Change to Both Lines

A change that applies to both lines is not merged from one into the other - it is prepared twice, once per
line, and each branch is merged into its own line. The branch names carry the line, and the history shows the
pairs:

* `feature/parser-paren-fix` into `release-3`, `feature/parser-paren-fix-master` into `master`
* `fix/nested-function-recursion` into `release-3`, `fix/nested-function-recursion-master` into `master`
* `feature/case-sensitive-like-release-3`, `feature/fix-rsql-string-release-3` and `feature/like-escape-release-3`, each merged into `release-3` after the same change had reached `master`

When the change reaches `master` first, the `release-3` side is a backport and its commit message says so -
`Backport 0.7.5 to 0.6.20`, `Backport 0.7.4 to 0.6.19`.

### Cutting a Release

1. On the branch that carries the change, bump `<version>` in the parent `pom.xml` and move the `## [Unreleased]` entries in CHANGELOG.md under a new `## [X.Y.Z] - YYYY-MM-DD` heading.
2. Merge that branch into its line with a merge commit (`git merge --no-ff`).
3. Tag the merge commit. `v0.6.21` sits on `00b6d9d`, the merge of `feature/parser-paren-fix` into `release-3`; `v0.7.6` sits on `ae43e0b`, the merge of `feature/parser-paren-fix-master` into `master`.
4. The two lines keep independent version numbers. A 0.6.x release is never merged into `master`, and a 0.7.x release is never merged into `release-3`.

Older releases used a separate `release/vX.Y.Z` branch - `git log --merges master` still shows
`Merge branch 'release/v0.7.5'` - but the version bump now travels on the feature branch itself.

## Questions?

Feel free to open an issue with your question or contact the maintainers directly.

Thank you for contributing!