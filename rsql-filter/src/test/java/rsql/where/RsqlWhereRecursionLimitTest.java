package rsql.where;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import rsql.exceptions.SyntaxErrorException;

import java.util.Collections;
import java.util.List;

import rsql.antlr.where.RsqlWhereLexer;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The parser recurses once per nesting level, and the visitors recurse once per parse tree level, so a
 * deep enough filter exhausts the stack. {@code StackOverflowError} is an {@link Error}: a caller that
 * catches {@link SyntaxErrorException} does not see it, and what should be a 400 becomes a 500.
 * <p>
 * These limits turn that into a normal syntax error. Thresholds measured on this code base, one fresh JVM
 * per data point, binary search:
 * <pre>
 *              -Xss512k   -Xss1m
 *   nesting      1 237     2 801
 *   tree depth   1 630     3 750
 * </pre>
 * The defaults sit far below the smaller of those, because 512k is a common container default.
 */
class RsqlWhereRecursionLimitTest {

    @AfterEach
    void restoreDefaults() {
        RsqlWhereTreeParser.setMaxNestingDepth(RsqlWhereTreeParser.DEFAULT_MAX_NESTING_DEPTH);
        RsqlWhereTreeParser.setMaxTreeDepth(RsqlWhereTreeParser.DEFAULT_MAX_TREE_DEPTH);
    }

    // ---------------------------------------------------------------- nesting depth (parser recursion)

    @Test
    void nestingAtTheLimitIsAccepted() {
        int max = RsqlWhereTreeParser.getMaxNestingDepth();
        String filter = "(".repeat(max) + "a==1" + ")".repeat(max);
        assertNotNull(new RsqlWhereString().parseString(filter));
    }

    @Test
    void nestingOneOverTheLimitIsRejected() {
        int max = RsqlWhereTreeParser.getMaxNestingDepth();
        String filter = "(".repeat(max + 1) + "a==1" + ")".repeat(max + 1);
        SyntaxErrorException thrown =
            assertThrows(SyntaxErrorException.class, () -> new RsqlWhereString().parseString(filter));
        assertTrue(thrown.getMessage().contains("nested too deeply"), thrown.getMessage());
    }

    /**
     * The point of the limit: without it this is a {@link StackOverflowError}, which no
     * {@code catch (SyntaxErrorException)} sees.
     */
    @Test
    void deepNestingGivesSyntaxErrorRatherThanStackOverflow() throws InterruptedException {
        String filter = "(".repeat(20_000) + "a==1" + ")".repeat(20_000);
        assertEquals("SyntaxErrorException", outcomeOnSmallStack(filter));
    }

    @Test
    void unclosedParenthesesAreRejectedWithoutStackOverflow() throws InterruptedException {
        assertEquals("SyntaxErrorException", outcomeOnSmallStack("(".repeat(20_000)));
    }

    // ------------------------------------------------------------------ tree depth (visitor recursion)

    /** Measured: an AND chain of n conditions builds a tree of depth n + 5. */
    @Test
    void conditionChainAtTheLimitIsAccepted() {
        int conditions = RsqlWhereTreeParser.getMaxTreeDepth() - 5;
        assertNotNull(new RsqlWhereString().parseString(chain(conditions)));
    }

    @Test
    void conditionChainOneOverTheLimitIsRejected() {
        int conditions = RsqlWhereTreeParser.getMaxTreeDepth() - 4;
        SyntaxErrorException thrown = assertThrows(
            SyntaxErrorException.class, () -> new RsqlWhereString().parseString(chain(conditions)));
        assertTrue(thrown.getMessage().contains("too deeply"), thrown.getMessage());
    }

    @Test
    void longConditionChainIsRejected() {
        SyntaxErrorException thrown = assertThrows(
            SyntaxErrorException.class, () -> new RsqlWhereString().parseString(chain(RsqlWhereTreeParser.getMaxTreeDepth() + 50)));
        assertTrue(thrown.getMessage().contains("too deeply"), thrown.getMessage());
    }

    @Test
    void longChainGivesSyntaxErrorRatherThanStackOverflow() throws InterruptedException {
        assertEquals("SyntaxErrorException", outcomeOnSmallStack(chain(20_000)));
    }

    // ------------------------------------------- shapes the limits must NOT reject (all shallow trees)

    @Test
    void validShallowFiltersAreNotRejected() {
        RsqlWhereString parser = new RsqlWhereString();

        // 200 flat groups: nesting depth 1, however many of them there are
        assertNotNull(parser.parseString(String.join(";", Collections.nCopies(200, "(a==1)"))));

        // an IN list of 1000 elements: the commas are separators, not OR operators
        assertNotNull(parser.parseString("a=in=(" + String.join(",", Collections.nCopies(1000, "1")) + ")"));

        // 21 IN conditions - 21 opening parentheses, all at depth 1
        StringBuilder many = new StringBuilder("f0=in=(1,2)");
        for (int i = 1; i < 21; i++) many.append(";f").append(i).append("=in=(1,2)");
        assertNotNull(parser.parseString(many.toString()));

        // parentheses inside a string literal are part of one token
        assertNotNull(parser.parseString("name=='" + "(".repeat(50) + "'"));

        // BETWEEN bounds are an argument list too
        assertNotNull(parser.parseString("a=bt=(1,2)"));
    }

    // ------------------------------------------------------------------------------------------ cost

    /**
     * The check runs on every parse, so it has to be linear. An {@code LA(i)} loop over
     * {@code CommonTokenStream} would be O(n^2) - 160 kB of input took 43 s that way.
     */
    /**
     * The check runs on every parse, so it has to be linear. An {@code LA(i)} loop over
     * {@code CommonTokenStream} would be O(n^2) - 160 kB of input took 43 s that way.
     * <p>
     * Driven directly rather than through the facade: the parser is not linear on every shape either - an
     * {@code IN} list of n elements costs roughly O(n^2) to parse - so going through {@code parseString}
     * would measure that instead of this.
     */
    @Test
    void limitCheckIsLinear() {
        List<Token> small = tokensOf(4_000);
        List<Token> large = tokensOf(32_000);                 // 8x the tokens
        for (int i = 0; i < 50; i++) {                        // warm up
            RsqlWhereTreeParser.verifyNestingIsWithinLimit(small, Integer.MAX_VALUE);
        }

        long smallNs = timeOfScan(small);
        long largeNs = timeOfScan(large);
        assertTrue(
            largeNs < smallNs * 40,
            "8x the tokens took " + largeNs / 1000 + " us against " + smallNs / 1000 + " us - looks super-linear"
        );
    }

    private static List<Token> tokensOf(int conditions) {
        CommonTokenStream tokens = new CommonTokenStream(
            new RsqlWhereLexer(CharStreams.fromString(chain(conditions))));
        tokens.fill();
        return tokens.getTokens();
    }

    private static long timeOfScan(List<Token> tokens) {
        long start = System.nanoTime();
        RsqlWhereTreeParser.verifyNestingIsWithinLimit(tokens, Integer.MAX_VALUE);
        return System.nanoTime() - start;
    }

    // --------------------------------------------------------------------------------------- helpers

    private static String chain(int conditions) {
        return String.join(";", Collections.nCopies(conditions, "a==1"));
    }

    private static String inList(int elements) {
        return "a=in=(" + String.join(",", Collections.nCopies(elements, "1")) + ")";
    }

    private static long timeOf(RsqlWhereString parser, String filter) {
        long start = System.nanoTime();
        parser.parseString(filter);
        return (System.nanoTime() - start) / 1_000_000;
    }

    /**
     * Runs on a deliberately small stack, so that a missing limit really does overflow. Rethrows nothing -
     * it reports which of the two outcomes happened, because that is the whole point of the assertion.
     */
    private static String outcomeOnSmallStack(String filter) throws InterruptedException {
        final String[] outcome = new String[1];
        Thread worker = new Thread(null, () -> {
            try {
                new RsqlWhereString().parseString(filter);
                outcome[0] = "parsed";
            } catch (StackOverflowError e) {
                outcome[0] = "StackOverflowError";
            } catch (SyntaxErrorException e) {
                outcome[0] = "SyntaxErrorException";
            } catch (Throwable t) {
                outcome[0] = t.getClass().getSimpleName();
            }
        }, "rsql-limit", 512L * 1024);
        worker.start();
        worker.join();
        return outcome[0];
    }
}
