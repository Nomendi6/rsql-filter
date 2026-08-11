# ANTLR Grammar Guide for RSQL Filter

This guide explains how to work with ANTLR grammars in the RSQL Filter project.

## Overview

ANTLR (ANother Tool for Language Recognition) is used in this project to generate lexers and parsers for the RSQL query language. The project uses ANTLR 4 integrated with Maven for automatic code generation; the exact version is the `antlr4.version` property of the parent POM, currently 4.13.2.

## Directory Structure

- **Grammar Files**: Located in `/src/main/antlr/`
  - `RsqlCommonLexer.g4`: Common lexer rules
  - `RsqlSelect.g4`: Grammar for SELECT expressions
  - `RsqlWhere.g4`: Grammar for WHERE conditions
  - `RsqlHaving.g4`: Grammar for HAVING conditions

- **Generated Code**: Output to:
  - `/src/main/java/rsql/antlr/lexer/`: Lexer code
  - `/src/main/java/rsql/antlr/select/`: Select parser code
  - `/src/main/java/rsql/antlr/where/`: Where parser code
  - `/src/main/java/rsql/antlr/having/`: Having parser code

The generated sources are committed to git - 33 tracked files under `/src/main/java/rsql/antlr/`. They are also deleted and regenerated on every build (see *Purging the Generated Sources* below), so a change to any `.g4` file produces a diff in tracked source that has to be committed along with it.

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
4. Commit the regenerated sources together with the grammar change - they are tracked by git, so every grammar edit also produces a diff under `/src/main/java/rsql/antlr/`. Leaving it out does not break the Maven build, which regenerates from the `.g4` files anyway, but it leaves the committed parsers describing a grammar that no longer exists - and that is what an IDE compiling `src/main/java` directly will use

## Maven Configuration

The ANTLR Maven plugin is configured in the `pom.xml` file with:
- ANTLR version: the `antlr4.version` property of the parent POM (currently 4.13.2)
- Visitor pattern: enabled
- Listener pattern: enabled
- Separate configurations for each grammar file to ensure proper output directory structure - one execution per grammar (`antlr-lexer`, `antlr-select`, `antlr-where`, `antlr-having`), each restricted to a single grammar with `<includes>`
- A `maven-clean-plugin` execution that purges the generated directories before every build

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

### Plugin Arguments

Two ANTLR arguments are passed from the POM, and neither can be expressed in the grammar files:

- `-package rsql.antlr.lexer` on the `antlr-lexer` execution sets the package of the shared lexer. The warning above explains why an `@header` block cannot be used instead.
- `-lib ${project.basedir}/src/main/java/rsql/antlr/lexer` on the `antlr-select`, `antlr-where` and `antlr-having` executions names the directory ANTLR searches for imported grammars and for the `.tokens` file of a `tokenVocab` option, and points it at the *generated* lexer output, where `RsqlCommonLexer.tokens` is written.

Two things about `-lib` are worth knowing before touching it. It is not what makes `import RsqlCommonLexer;` resolve - ANTLR also looks in the directory of the importing grammar, and all four `.g4` files sit in `src/main/antlr`, so generating `RsqlWhere.g4` or `RsqlHaving.g4` with this argument, with an empty `-lib` directory, or with none at all produces byte-identical output. But a `-lib` directory that does not exist is a hard error (`error(5): directory not found`), and the purge step below deletes exactly that directory. The `antlr-lexer` execution is what recreates it, so it has to stay first in the execution list.

`<libDirectory>` is the plugin's own equivalent setting and points at the grammar directory `src/main/antlr` in all four executions.

### Purging the Generated Sources

A `maven-clean-plugin` execution named `purge-antlr-generated`, bound to the `initialize` phase, deletes all four generated directories before every build. It is load-bearing, not tidiness:

- Each `antlr4-maven-plugin` execution restricts itself to a single grammar with `<includes>`, so the plugin never learns that `RsqlWhere.g4` and `RsqlHaving.g4` depend on the lexer they import. Changing only `RsqlCommonLexer.g4` would regenerate the lexer alone and silently leave `RsqlWhereLexer` and `RsqlHavingLexer` with stale token definitions - with a green build and green tests.
- Deleting the output directories first makes every build regenerate all four grammars from the `.g4` files, which are the only source of truth. Regeneration is idempotent, so on an unchanged tree the purge produces no diff.
- `<excludeDefaultDirectories>true</excludeDefaultDirectories>` is REQUIRED. Without it this execution would also wipe `target/` on every build and destroy incremental compilation.

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-clean-plugin</artifactId>
    <executions>
        <execution>
            <id>purge-antlr-generated</id>
            <phase>initialize</phase>
            <goals>
                <goal>clean</goal>
            </goals>
            <configuration>
                <excludeDefaultDirectories>true</excludeDefaultDirectories>
                <filesets>
                    <fileset><directory>${project.basedir}/src/main/java/rsql/antlr/lexer</directory></fileset>
                    <fileset><directory>${project.basedir}/src/main/java/rsql/antlr/select</directory></fileset>
                    <fileset><directory>${project.basedir}/src/main/java/rsql/antlr/where</directory></fileset>
                    <fileset><directory>${project.basedir}/src/main/java/rsql/antlr/having</directory></fileset>
                </filesets>
            </configuration>
        </execution>
    </executions>
</plugin>
```

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
   - `RsqlWhere.g4` and `RsqlHaving.g4` import the tokens of `RsqlCommonLexer.g4` (`import RsqlCommonLexer;` on line 3 of each). This is what the `-package` argument and the purge step exist for.
   - `RsqlSelect.g4` is self-contained: it imports nothing and defines its own tokens.

## Changes That Must Not Be Reintroduced

`RsqlWhere.g4` and `RsqlSelect.g4` carry `NOTE` comments recording rules that were removed because adaptive prediction became exponential on *valid* input. The comments in the `.g4` files are the authority; this is their substance.

1. **No error alternatives for a stray `)` in `RsqlWhere.g4`** (`condition`, RsqlWhere.g4:12-27). Up to 0.6.20 the rule carried two of them, both emitting "Missing opening parenthesis". They made every `)` ambiguous - it could either close a `conditionParens` or start the tail of the error alternative - so prediction explored 2^n paths on n parentheses, and paid that cost on valid input: `((((...a==1...))))` with 26 levels (56 characters) took about 13 seconds. They also rejected valid filters, because a trailing newline after a grouping `)` made the inner condition swallow the `)`. A stray `)` is now reported, with its position, by `RsqlWhereTreeParser.verifyWholeInputWasUsed`.

2. **No `+` on the SELECT start rule** (`select`, RsqlSelect.g4:7-19). Up to 0.6.20 this read `selectElements+`, which let a second group of elements begin at any position. `code name` then parsed as though the comma were there, and - because `selectElements` may start with `*`, which is also the multiplication operator - the parser had to decide at every `*` whether the current expression continued or a new group began. That decision needs lookahead over the whole expression, so `a+b*c` repeated 200 times took about 14 seconds. A missing separator is now caught by `SelectTreeParser.verifyWholeInputWasUsed`.

3. **No unreachable error rules** (RsqlWhere.g4:35-41). An `errorCondition` rule emitting "Missing closing parenthesis" sat in the grammar without any rule invoking it, so that message was never produced. Removed in 0.7.6 / 0.6.21; an unclosed parenthesis is reported by `CustomErrorStrategy` instead.

The general rule these three share: report a syntax error from Java after parsing, not from an extra grammar alternative. Every alternative added to catch bad input also has to be weighed against every piece of good input.

## Debugging ANTLR Grammars

ANTLR's TestRig (`grun`) works here, but not in its textbook form. `RsqlWhere.g4`, `RsqlSelect.g4` and
`RsqlHaving.g4` each declare a package with `@header { package rsql.antlr.where; }` (and so on), so the
generated classes are package-qualified: `javac *.java` into the current directory followed by
`grun RsqlWhere where` fails with
`java.lang.NoClassDefFoundError: RsqlWhereLexer (wrong name: rsql/antlr/where/RsqlWhereLexer)`. Two things
fix it - compile with `-d <dir>` so the class files land in their package directories, and hand TestRig the
fully-qualified grammar name.

The build already generates and compiles all four grammars, so the shortest working form needs no generation
at all. From the repository root, after `mvn install -pl rsql-filter`:

```bash
M2=~/.m2/repository
RIG=$M2/org/antlr/antlr4/4.13.2/antlr4-4.13.2.jar:$M2/org/antlr/antlr4-runtime/4.13.2/antlr4-runtime-4.13.2.jar

echo "name=='John';price=gt=10" | java -cp rsql-filter/target/classes:$RIG \
    org.antlr.v4.gui.TestRig rsql.antlr.where.RsqlWhere where -tree
```

which prints

```
(where (condition (condition (singleCondition (field name) (operator (operatorEQ ==)) 'John')) ; (condition (singleCondition (field price) (operator (operatorGT = gt =)) 10))))
```

`grun` is only an alias for that `java` command. `org.antlr.v4.gui.TestRig` lives in the ANTLR *tool* jar and
needs `antlr4-runtime` next to it - both are already in the local Maven repository, so there is nothing to
download. `-tokens` prints the token stream instead of the tree, `-gui` opens the tree viewer window and needs
a desktop session.

For a grammar the build does not generate yet, work in a scratch directory - never generate into the module,
where the purge step and the committed sources live:

```bash
mkdir -p /tmp/rsql-grun && cp rsql-filter/src/main/antlr/*.g4 /tmp/rsql-grun && cd /tmp/rsql-grun
M2=~/.m2/repository
RIG=$M2/org/antlr/antlr4/4.13.2/antlr4-4.13.2.jar:$M2/org/antlr/antlr4-runtime/4.13.2/antlr4-runtime-4.13.2.jar
GEN=$RIG:$M2/org/antlr/ST4/4.3.4/ST4-4.3.4.jar:$M2/org/antlr/antlr-runtime/3.5.3/antlr-runtime-3.5.3.jar

# copying all four .g4 files together is what makes `import RsqlCommonLexer;` resolve
java -cp $GEN org.antlr.v4.Tool RsqlWhere.g4
javac -cp $RIG -d out *.java
echo "price=bt=(1,2)" | java -cp out:$RIG org.antlr.v4.gui.TestRig rsql.antlr.where.RsqlWhere where -tree
```

The start rules and the names to pass TestRig: `rsql.antlr.where.RsqlWhere` / `where`,
`rsql.antlr.select.RsqlSelect` / `select`, `rsql.antlr.having.RsqlHaving` / `having`.

TestRig runs the STOCK lexer and parser: it recovers from errors, reports them on stderr and still returns a
tree. None of the library's hardening is in play - see *The Hardening Around the Stock Parser* below - so a
string TestRig accepts can still be rejected by `RsqlWhereTreeParser`.

Alternatively, use online tools like [ANTLR Lab](https://www.antlr.org/tools.html).

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

// Parse starting at the start rule
ParseTree tree = parser.where();

// Use a visitor or listener to process the parse tree
MyVisitor visitor = new MyVisitor();
Object result = visitor.visit(tree);
```

### The Hardening Around the Stock Parser

The snippet above is plain ANTLR and is fine for experimenting, but nothing in the library uses it. Every WHERE filter goes through `RsqlWhereTreeParser.parseStream(CharStream)`, and what that method adds is what turns ANTLR's default behaviour - recover from the error, print to stderr, carry on - into a hard failure with a `rsql.exceptions.SyntaxErrorException`:

1. **`BailRsqlWhereLexer`** overrides `recover(RecognitionException)` so a lexer error throws instead of skipping the offending character. `BailRsqlSelectLexer` and `BailRsqlHavingLexer` do the same for the other two grammars.
2. **`RsqlWhereErrorListener`** replaces the default console listener on both the lexer and the parser - `removeErrorListeners()` has to be called first, or the console listener stays attached - and throws from `syntaxError` with the line and column in the message. `SelectErrorListener` and `HavingErrorListener` are its counterparts.
3. **`CustomErrorStrategy`** replaces `DefaultErrorStrategy`, so the parser neither deletes nor inserts tokens to keep going: `recover` and `recoverInline` throw, and `sync` is a no-op. All three parsers share this one class from `rsql.where`.
4. **Two depth guards.** `verifyNestingIsWithinLimit` counts parentheses over the *filled* token stream before the parser runs; `verifyTreeDepthIsWithinLimit` measures the finished tree iteratively, so measuring cannot itself overflow. Both bound recursion that would otherwise end in a `StackOverflowError`, which a caller cannot catch. The limits live on `RsqlWhereTreeParser` - `DEFAULT_MAX_NESTING_DEPTH` is 100 and `DEFAULT_MAX_TREE_DEPTH` is 500 - and are adjustable at runtime through the static `setMaxNestingDepth` / `setMaxTreeDepth`. They are JVM-wide, and `SelectTreeParser` and `HavingTreeParser` read the same two values, so tuning one tunes all three.

`parseStream` then calls `verifyWholeInputWasUsed`, which is what makes leftover input an error instead of silently discarded text - see *Changes That Must Not Be Reintroduced* above.

A new grammar should be wired the same way. Adding a start rule and calling it directly gets you a parser that reports its errors on stderr and hands back a tree built from input it never accepted.

## Further Reading

- [ANTLR Documentation](https://www.antlr.org/)
- [ANTLR 4 Maven Plugin](https://www.antlr.org/api/maven-plugin/latest/)
- [The Definitive ANTLR 4 Reference](https://pragprog.com/titles/tpantlr2/the-definitive-antlr-4-reference/) by Terence Parr
