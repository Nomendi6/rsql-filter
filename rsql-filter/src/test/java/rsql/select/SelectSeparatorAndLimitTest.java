package rsql.select;

import org.antlr.v4.runtime.CharStreams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rsql.exceptions.SyntaxErrorException;
import rsql.where.RsqlWhereTreeParser;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The start rule used to be {@code select: selectElements+}, which let a second group of elements begin at
 * any position. Two consequences, one cause:
 * <ul>
 *   <li>{@code code name} parsed as though the comma were there - the visitors iterate every
 *       {@code selectElements} and accumulate, so the missing separator went unnoticed;</li>
 *   <li>the parser had to decide at every {@code *} whether the current expression continued or a new
 *       group began, and that decision needs lookahead over the whole expression - {@code a+b*c} repeated
 *       200 times took about 14 s.</li>
 * </ul>
 */
class SelectSeparatorAndLimitTest {

    private SelectTreeParser parser;

    @BeforeEach
    void setUp() {
        parser = new SelectTreeParser();
    }

    // ------------------------------------------------------------------------- the missing separator

    @Test
    void elementsWithoutASeparatorAreRejected() {
        assertThrows(SyntaxErrorException.class, () -> parse("code name"));
        assertThrows(SyntaxErrorException.class, () -> parse("code,name price,qty"));
        assertThrows(SyntaxErrorException.class, () -> parse("* *"));
    }

    @Test
    void commaSeparatedElementsStillParse() {
        assertNotNull(parse("code,name"));
        assertNotNull(parse("code, name, price"));
        assertNotNull(parse("*"));
        assertNotNull(parse("productType.name:typeName, SUM(price):total, COUNT(*):count"));
        assertNotNull(parse("SUM(price)*1.2:priceWithTax"));
        assertNotNull(parse("(SUM(price)-50)*2/COUNT(*):complexMetric"));
        assertNotNull(parse("productType.*"));
        assertNotNull(parse("COUNT(DIST code,name):distinctPairs"));
    }

    // ------------------------------------------------------------------------------ recursion limits

    @Test
    void nestingAtTheLimitIsAccepted() {
        int max = RsqlWhereTreeParser.getMaxNestingDepth();
        assertNotNull(parse("(".repeat(max) + "price" + ")".repeat(max)));
    }

    @Test
    void nestingOverTheLimitIsRejected() {
        int max = RsqlWhereTreeParser.getMaxNestingDepth();
        SyntaxErrorException thrown = assertThrows(
            SyntaxErrorException.class, () -> parse("(".repeat(max + 1) + "price" + ")".repeat(max + 1)));
        assertTrue(thrown.getMessage().contains("nested too deeply"), thrown.getMessage());
    }

    @Test
    void deepNestingGivesSyntaxErrorRatherThanStackOverflow() throws InterruptedException {
        String select = "(".repeat(20_000) + "price" + ")".repeat(20_000);
        final String[] outcome = new String[1];
        Thread worker = new Thread(null, () -> {
            try {
                parse(select);
                outcome[0] = "parsed";
            } catch (StackOverflowError e) {
                outcome[0] = "StackOverflowError";
            } catch (SyntaxErrorException e) {
                outcome[0] = "SyntaxErrorException";
            } catch (Throwable t) {
                outcome[0] = t.getClass().getSimpleName();
            }
        }, "rsql-select-limit", 512L * 1024);
        worker.start();
        worker.join();
        assertEquals("SyntaxErrorException", outcome[0]);
    }

    /** Aggregate call parentheses do not nest the expression rule, so they must not count. */
    @Test
    void functionCallParenthesesDoNotCountTowardsTheLimit() {
        StringBuilder many = new StringBuilder("SUM(f0)");
        for (int i = 1; i < 300; i++) many.append(",SUM(f").append(i).append(")");
        assertNotNull(parse(many.toString()));
    }


    /**
     * {@code functionArg} may itself be a {@code functionCall}, so a nested aggregate is a genuinely
     * recursive path through the parser. An earlier version of the limit exempted aggregate parentheses and
     * so never saw it: 1 000 levels overflowed the stack during parsing, before the tree depth check ran.
     */
    @Test
    void nestedAggregateCallsAreBoundedToo() throws InterruptedException {
        String select = "SUM(".repeat(5_000) + "price" + ")".repeat(5_000);
        final String[] outcome = new String[1];
        Thread worker = new Thread(null, () -> {
            try {
                parse(select);
                outcome[0] = "parsed";
            } catch (StackOverflowError e) {
                outcome[0] = "StackOverflowError";
            } catch (SyntaxErrorException e) {
                outcome[0] = "SyntaxErrorException";
            } catch (Throwable t) {
                outcome[0] = t.getClass().getSimpleName();
            }
        }, "rsql-nested-agg", 512L * 1024);
        worker.start();
        worker.join();
        assertEquals("SyntaxErrorException", outcome[0]);
    }

    // ------------------------------------------------------------------------------------------ cost

    /**
     * {@code a+b*c} repeated took about 14 s at 200 repetitions before the start rule was fixed, and grew
     * polynomially. A generous ceiling still fails loudly if that ever comes back.
     */
    @Test
    void mixedPrecedenceExpressionParsesQuickly() {
        StringBuilder expression = new StringBuilder("a");
        for (int i = 0; i < 200; i++) expression.append("+b*c");

        long start = System.nanoTime();
        assertNotNull(parse(expression.toString()));
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;

        assertTrue(elapsedMs < 2000, "parsing took " + elapsedMs + " ms");
    }

    // --------------------------------------------------------------------------------------- helpers

    private Object parse(String select) {
        return parser.parseStream(CharStreams.fromString(select));
    }

    @SuppressWarnings("unused")
    private static String repeated(String element, int times) {
        return String.join(",", Collections.nCopies(times, element));
    }
}
