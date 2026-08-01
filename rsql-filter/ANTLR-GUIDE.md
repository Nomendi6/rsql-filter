# ANTLR Grammar Guide for RSQL Filter

This guide explains how to work with ANTLR grammars in the RSQL Filter project.

## Overview

ANTLR (ANother Tool for Language Recognition) is used in this project to generate lexers and parsers for the RSQL query language. The project uses ANTLR 4.13.1 integrated with Maven for automatic code generation.

## Directory Structure

- **Grammar Files**: Located in `/src/main/antlr/`
  - `RsqlCommonLexer.g4`: Common lexer rules
  - `RsqlSelect.g4`: Grammar for SELECT expressions
  - `RsqlWhere.g4`: Grammar for WHERE conditions

- **Generated Code**: Output to:
  - `/src/main/java/rsql/antlr/lexer/`: Lexer code
  - `/src/main/java/rsql/antlr/select/`: Select parser code
  - `/src/main/java/rsql/antlr/where/`: Where parser code

## Using Maven for ANTLR Generation

The project is configured to generate ANTLR code automatically during the build process. You can:

1. **Generate ANTLR code only**:
   ```bash
   mvn -pl rsql-filter generate-sources
   ```

2. **Generate all sources** (including ANTLR) as part of a clean build:
   ```bash
   mvn clean generate-sources -pl rsql-filter
   ```

3. **Full build** including ANTLR generation:
   ```bash
   mvn clean install
   ```

## Modifying Grammars

When you need to modify the grammar files:

1. Edit the `.g4` files in `/src/main/antlr/`
2. Run `mvn -pl rsql-filter generate-sources` to regenerate the ANTLR code
3. The generated code will automatically be placed in the correct directories

## Maven Configuration

The ANTLR Maven plugin is configured in the `pom.xml` file with:
- ANTLR version: 4.13.1
- Visitor pattern: enabled
- Listener pattern: enabled
- Separate configurations for each grammar file to ensure proper output directory structure

### Configuration Details

```xml
<plugin>
    <groupId>org.antlr</groupId>
    <artifactId>antlr4-maven-plugin</artifactId>
    <version>${antlr4.version}</version>
    <configuration>
        <sourceDirectory>${project.basedir}/src/main/antlr</sourceDirectory>
        <listener>true</listener>
        <visitor>true</visitor>
        <treatWarningsAsErrors>false</treatWarningsAsErrors>
    </configuration>
    <executions>
        <!-- Separate executions for each grammar file -->
        <execution>
            <id>antlr-lexer</id>
            <goals>
                <goal>antlr4</goal>
            </goals>
            <configuration>
                <includes>
                    <include>RsqlCommonLexer.g4</include>
                </includes>
                <outputDirectory>${project.basedir}/src/main/java/rsql/antlr/lexer</outputDirectory>
                <libDirectory>${project.basedir}/src/main/antlr</libDirectory>
                <!-- The package of the shared lexer MUST be set here, not with @header in the grammar. -->
                <arguments>
                    <argument>-package</argument>
                    <argument>rsql.antlr.lexer</argument>
                </arguments>
            </configuration>
        </execution>
        <!-- Additional executions for other grammars -->
    </executions>
</plugin>
```

> ⚠ **Do not add `@header { package ...; }` to `RsqlCommonLexer.g4`.** ANTLR inherits a `@header` from an
> imported grammar, and `RsqlWhere.g4` and `RsqlHaving.g4` both do `import RsqlCommonLexer;`. Their generated
> sources would then contain two `package` declarations and fail to compile. Use the plugin's `-package`
> argument for the shared lexer; the importing grammars keep their own `@header` as usual.

## Working with ANTLR Grammar Files

### Grammar File Structure

ANTLR grammar files (`.g4`) contain:
- Lexer rules: Define tokens (e.g., keywords, operators, literals)
- Parser rules: Define the syntax structure

### Important Concepts

1. **Lexer vs Parser**:
   - Lexer breaks input into tokens
   - Parser assembles tokens into a parse tree

2. **Visitor and Listener Patterns**:
   - **Listeners**: React to events during tree traversal
   - **Visitors**: Explicitly control traversal and return values

3. **Grammar Dependencies**:
   - `RsqlSelect.g4` and `RsqlWhere.g4` may import tokens from `RsqlCommonLexer.g4`

## Debugging ANTLR Grammars

To debug your grammar:

1. Use ANTLR's testing tools:
   ```bash
   java -jar antlr-4.13.1-complete.jar <YourGrammar.g4>
   javac *.java
   grun <YourGrammar> <startRule> -gui
   ```

2. Alternatively, use online tools like [ANTLR Lab](https://www.antlr.org/tools.html)

## Best Practices

1. **Keep grammars modular**: Separate lexer and parser rules when possible
2. **Use meaningful rule names**: Make grammar readable and maintainable
3. **Add comments**: Document complex grammar rules
4. **Test incrementally**: Validate grammar changes with test cases

## Using Generated Code

The generated code provides:

1. **Lexer classes**: Convert input text to token streams
2. **Parser classes**: Convert token streams to parse trees
3. **Listener/Visitor interfaces**: Process parse trees

Basic usage example:

```java
// Create a lexer
RsqlWhereLexer lexer = new RsqlWhereLexer(CharStreams.fromString(inputString));

// Create a token stream
CommonTokenStream tokens = new CommonTokenStream(lexer);

// Create a parser
RsqlWhereParser parser = new RsqlWhereParser(tokens);

// Parse starting at a specific rule
ParseTree tree = parser.whereExpression();

// Use a visitor or listener to process the parse tree
MyVisitor visitor = new MyVisitor();
Object result = visitor.visit(tree);
```

## Further Reading

- [ANTLR Documentation](https://www.antlr.org/)
- [ANTLR 4 Maven Plugin](https://www.antlr.org/api/maven-plugin/latest/)
- [The Definitive ANTLR 4 Reference](https://pragprog.com/titles/tpantlr2/the-definitive-antlr-4-reference/) by Terence Parr
