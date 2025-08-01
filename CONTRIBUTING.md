# Contributing to RSQL Filter

First off, thank you for considering contributing to RSQL Filter! It's people like you that make RSQL Filter such a great tool.

## Code of Conduct

This project and everyone participating in it is governed by our Code of Conduct. By participating, you are expected to uphold this code. Please report unacceptable behavior to the project maintainers.

## How Can I Contribute?

### Reporting Bugs

Before creating bug reports, please check existing issues as you might find out that you don't need to create one. When you are creating a bug report, please include as many details as possible:

* **Use a clear and descriptive title** for the issue to identify the problem.
* **Describe the exact steps which reproduce the problem** in as many details as possible.
* **Provide specific examples to demonstrate the steps**. Include links to files or GitHub projects, or copy/pasteable snippets, which you use in those examples.
* **Describe the behavior you observed after following the steps** and point out what exactly is the problem with that behavior.
* **Explain which behavior you expected to see instead and why.**
* **Include the version of RSQL Filter you're using** and the versions of Spring Boot and Java.

### Suggesting Enhancements

Enhancement suggestions are tracked as GitHub issues. When creating an enhancement suggestion, please include:

* **Use a clear and descriptive title** for the issue to identify the suggestion.
* **Provide a step-by-step description of the suggested enhancement** in as many details as possible.
* **Provide specific examples to demonstrate the steps**.
* **Describe the current behavior** and **explain which behavior you expected to see instead** and why.
* **Explain why this enhancement would be useful** to most RSQL Filter users.

### Pull Requests

1. Fork the repo and create your branch from `develop`.
2. If you've added code that should be tested, add tests.
3. If you've changed APIs, update the documentation.
4. Ensure the test suite passes.
5. Make sure your code follows the existing code style.
6. Issue that pull request!

## Development Process

### Setting up the Development Environment

1. **Clone the repository**
   ```bash
   git clone https://github.com/nomendi6/rsql-filter.git
   cd rsql-filter
   ```

2. **Build the project**
   ```bash
   mvn clean install
   ```

3. **Run the tests**
   ```bash
   mvn test
   ```

### Project Structure

```
rsql-filter-mvn/
├── rsql-filter-core/                 # Core library
├── rsql-filter-integration-tests/    # Integration tests
├── rsql-filter-demo/                 # Demo application
└── pom.xml                          # Parent POM
```

### Running Tests

```bash
# Run all tests
mvn test

# Run integration tests only
mvn test -pl rsql-filter-integration-tests

# Run tests with coverage
mvn clean test jacoco:report
```

### Code Style

We use the following code style guidelines:

* Java code follows standard Java conventions
* Indentation: 4 spaces (no tabs)
* Maximum line length: 120 characters
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

* Update README.md if you change functionality
* Add JavaDoc comments for public methods
* Include examples in documentation
* Update CHANGELOG.md with your changes

## Release Process

1. All changes go to `develop` branch first
2. When ready for release, create a release branch from `develop`
3. Update version numbers and CHANGELOG.md
4. Merge to `master` and tag the release
5. Merge back to `develop`

## Questions?

Feel free to open an issue with your question or contact the maintainers directly.

Thank you for contributing!