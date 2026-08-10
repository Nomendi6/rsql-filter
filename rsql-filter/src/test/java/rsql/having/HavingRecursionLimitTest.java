package rsql.having;

import org.antlr.v4.runtime.CharStreams;
import org.junit.jupiter.api.Test;
import rsql.exceptions.SyntaxErrorException;
import rsql.where.RsqlWhereTreeParser;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * HAVING has the same unbounded recursion as WHERE - measured overflow at about 5 000 nested parentheses on
 * a 1M stack - and shares the limits with it.
 */
class HavingRecursionLimitTest {

    @Test
    void nestingAtTheLimitIsAccepted() {
        int max = RsqlWhereTreeParser.getMaxNestingDepth();
        String having = "(".repeat(max) + "SUM(price)=gt=1" + ")".repeat(max);
        assertNotNull(new HavingTreeParser().parseStream(CharStreams.fromString(having)));
    }

    @Test
    void nestingOverTheLimitIsRejected() {
        int max = RsqlWhereTreeParser.getMaxNestingDepth();
        String having = "(".repeat(max + 1) + "SUM(price)=gt=1" + ")".repeat(max + 1);
        SyntaxErrorException thrown = assertThrows(SyntaxErrorException.class,
            () -> new HavingTreeParser().parseStream(CharStreams.fromString(having)));
        assertTrue(thrown.getMessage().contains("nested too deeply"), thrown.getMessage());
    }

    @Test
    void deepNestingGivesSyntaxErrorRatherThanStackOverflow() throws InterruptedException {
        String having = "(".repeat(20_000) + "SUM(price)=gt=1" + ")".repeat(20_000);
        final String[] outcome = new String[1];
        Thread worker = new Thread(null, () -> {
            try {
                new HavingTreeParser().parseStream(CharStreams.fromString(having));
                outcome[0] = "parsed";
            } catch (StackOverflowError e) {
                outcome[0] = "StackOverflowError";
            } catch (SyntaxErrorException e) {
                outcome[0] = "SyntaxErrorException";
            } catch (Throwable t) {
                outcome[0] = t.getClass().getSimpleName();
            }
        }, "rsql-having-limit", 512L * 1024);
        worker.start();
        worker.join();
        assertEquals("SyntaxErrorException", outcome[0]);
    }

    /** Neither an aggregate call nor an IN / BETWEEN list nests the condition rule, so neither may count. */
    @Test
    void functionAndArgumentListParenthesesDoNotCountTowardsTheLimit() {
        HavingTreeParser parser = new HavingTreeParser();
        assertNotNull(parser.parseStream(CharStreams.fromString("SUM(price)=gt=1000")));
        assertNotNull(parser.parseStream(CharStreams.fromString("COUNT(*)=ge=5")));
        assertNotNull(parser.parseStream(CharStreams.fromString(
            "SUM(price)=in=(" + String.join(",", Collections.nCopies(500, "1")) + ")")));
        assertNotNull(parser.parseStream(CharStreams.fromString("SUM(price)=bt=(1,2)")));

        StringBuilder many = new StringBuilder("SUM(f0)=gt=1");
        for (int i = 1; i < 200; i++) many.append(";SUM(f").append(i).append(")=gt=1");
        assertNotNull(parser.parseStream(CharStreams.fromString(many.toString())));
    }
}
