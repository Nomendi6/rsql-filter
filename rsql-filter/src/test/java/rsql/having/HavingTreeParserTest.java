package rsql.having;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rsql.antlr.having.RsqlHavingParser;
import rsql.exceptions.SyntaxErrorException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for HavingTreeParser.
 * Tests parsing of various HAVING clause formats.
 */
class HavingTreeParserTest {

    private HavingTreeParser parser;

    @BeforeEach
    void setUp() {
        parser = new HavingTreeParser();
    }

    /**
     * Test parsing simple comparison with alias.
     */
    @Test
    void testParseSimpleAliasComparison() {
        String havingString = "totalPrice=gt=1000";
        ParseTree tree = parser.parseStream(CharStreams.fromString(havingString));

        assertNotNull(tree);
        assertTrue(tree instanceof RsqlHavingParser.HavingContext);
    }

    /**
     * Test parsing comparison with aggregate function.
     */
    @Test
    void testParseAggregateComparison() {
        String havingString = "SUM(price)==1000";
        ParseTree tree = parser.parseStream(CharStreams.fromString(havingString));

        assertNotNull(tree);
        assertTrue(tree instanceof RsqlHavingParser.HavingContext);
    }

    /**
     * Test parsing COUNT(*) comparison.
     */
    @Test
    void testParseCountStarComparison() {
        String havingString = "COUNT(*)=ge=5";
        ParseTree tree = parser.parseStream(CharStreams.fromString(havingString));

        assertNotNull(tree);
        assertTrue(tree instanceof RsqlHavingParser.HavingContext);
    }

    /**
     * Test parsing AVG function comparison.
     */
    @Test
    void testParseAvgComparison() {
        String havingString = "AVG(price)=gt=100.5";
        ParseTree tree = parser.parseStream(CharStreams.fromString(havingString));

        assertNotNull(tree);
        assertTrue(tree instanceof RsqlHavingParser.HavingContext);
    }

    /**
     * Test parsing MIN function comparison.
     */
    @Test
    void testParseMinComparison() {
        String havingString = "MIN(price)=ge=50";
        ParseTree tree = parser.parseStream(CharStreams.fromString(havingString));

        assertNotNull(tree);
        assertTrue(tree instanceof RsqlHavingParser.HavingContext);
    }

    /**
     * Test parsing MAX function comparison.
     */
    @Test
    void testParseMaxComparison() {
        String havingString = "MAX(quantity)=le=1000";
        ParseTree tree = parser.parseStream(CharStreams.fromString(havingString));

        assertNotNull(tree);
        assertTrue(tree instanceof RsqlHavingParser.HavingContext);
    }

    /**
     * Test parsing COUNT(DIST field) comparison.
     */
    @Test
    void testParseCountDistinctComparison() {
        String havingString = "COUNT(DIST productType.id)=gt=3";
        ParseTree tree = parser.parseStream(CharStreams.fromString(havingString));

        assertNotNull(tree);
        assertTrue(tree instanceof RsqlHavingParser.HavingContext);
    }

    /**
     * Test parsing AND condition with semicolon.
     */
    @Test
    void testParseAndConditionSemicolon() {
        String havingString = "SUM(price)=gt=1000;COUNT(*)=ge=5";
        ParseTree tree = parser.parseStream(CharStreams.fromString(havingString));

        assertNotNull(tree);
        assertTrue(tree instanceof RsqlHavingParser.HavingContext);
    }

    /**
     * Test parsing AND condition with AND keyword.
     */
    @Test
    void testParseAndConditionKeyword() {
        String havingString = "SUM(price)=gt=1000 AND COUNT(*)=ge=5";
        ParseTree tree = parser.parseStream(CharStreams.fromString(havingString));

        assertNotNull(tree);
        assertTrue(tree instanceof RsqlHavingParser.HavingContext);
    }

    /**
     * Test parsing OR condition with comma.
     */
    @Test
    void testParseOrConditionComma() {
        String havingString = "SUM(price)=gt=1000,AVG(price)=lt=50";
        ParseTree tree = parser.parseStream(CharStreams.fromString(havingString));

        assertNotNull(tree);
        assertTrue(tree instanceof RsqlHavingParser.HavingContext);
    }

    /**
     * Test parsing OR condition with OR keyword.
     */
    @Test
    void testParseOrConditionKeyword() {
        String havingString = "SUM(price)=gt=1000 OR AVG(price)=lt=50";
        ParseTree tree = parser.parseStream(CharStreams.fromString(havingString));

        assertNotNull(tree);
        assertTrue(tree instanceof RsqlHavingParser.HavingContext);
    }

    /**
     * Test parsing conditions with parentheses.
     */
    @Test
    void testParseConditionWithParentheses() {
        String havingString = "(SUM(price)=gt=1000;COUNT(*)=ge=5),AVG(price)=lt=100";
        ParseTree tree = parser.parseStream(CharStreams.fromString(havingString));

        assertNotNull(tree);
        assertTrue(tree instanceof RsqlHavingParser.HavingContext);
    }

    /**
     * Test parsing BETWEEN operator.
     */
    @Test
    void testParseBetweenOperator() {
        String havingString = "AVG(price)=bt=(50,150)";
        ParseTree tree = parser.parseStream(CharStreams.fromString(havingString));

        assertNotNull(tree);
        assertTrue(tree instanceof RsqlHavingParser.HavingContext);
    }

    /**
     * Test parsing NOT BETWEEN operator.
     */
    @Test
    void testParseNotBetweenOperator() {
        String havingString = "COUNT(id)=nbt=(1,5)";
        ParseTree tree = parser.parseStream(CharStreams.fromString(havingString));

        assertNotNull(tree);
        assertTrue(tree instanceof RsqlHavingParser.HavingContext);
    }

    /**
     * Test parsing IN operator.
     */
    @Test
    void testParseInOperator() {
        String havingString = "COUNT(id)=in=(1,2,3,5)";
        ParseTree tree = parser.parseStream(CharStreams.fromString(havingString));

        assertNotNull(tree);
        assertTrue(tree instanceof RsqlHavingParser.HavingContext);
    }

    /**
     * Test parsing NOT IN operator.
     */
    @Test
    void testParseNotInOperator() {
        String havingString = "SUM(price)=nin=(100,200,300)";
        ParseTree tree = parser.parseStream(CharStreams.fromString(havingString));

        assertNotNull(tree);
        assertTrue(tree instanceof RsqlHavingParser.HavingContext);
    }

    /**
     * Test parsing NULL comparison.
     */
    @Test
    void testParseNullComparison() {
        String havingString = "COUNT(description)==NULL";
        ParseTree tree = parser.parseStream(CharStreams.fromString(havingString));

        assertNotNull(tree);
        assertTrue(tree instanceof RsqlHavingParser.HavingContext);
    }

    /**
     * Test parsing TRUE comparison.
     */
    @Test
    void testParseTrueComparison() {
        String havingString = "COUNT(active)==TRUE";
        ParseTree tree = parser.parseStream(CharStreams.fromString(havingString));

        assertNotNull(tree);
        assertTrue(tree instanceof RsqlHavingParser.HavingContext);
    }

    /**
     * Test parsing FALSE comparison.
     */
    @Test
    void testParseFalseComparison() {
        String havingString = "COUNT(deleted)==FALSE";
        ParseTree tree = parser.parseStream(CharStreams.fromString(havingString));

        assertNotNull(tree);
        assertTrue(tree instanceof RsqlHavingParser.HavingContext);
    }

    /**
     * Test parsing comparison of two expressions.
     */
    @Test
    void testParseExpressionComparison() {
        String havingString = "SUM(debit)=gt=SUM(credit)";
        ParseTree tree = parser.parseStream(CharStreams.fromString(havingString));

        assertNotNull(tree);
        assertTrue(tree instanceof RsqlHavingParser.HavingContext);
    }

    /**
     * Test parsing LIKE operator.
     */
    @Test
    void testParseLikeOperator() {
        String havingString = "MAX(description)=like='%test%'";
        ParseTree tree = parser.parseStream(CharStreams.fromString(havingString));

        assertNotNull(tree);
        assertTrue(tree instanceof RsqlHavingParser.HavingContext);
    }

    /**
     * Test parsing NOT LIKE operator.
     */
    @Test
    void testParseNotLikeOperator() {
        String havingString = "MIN(name)=nlike='%temp%'";
        ParseTree tree = parser.parseStream(CharStreams.fromString(havingString));

        assertNotNull(tree);
        assertTrue(tree instanceof RsqlHavingParser.HavingContext);
    }

    /**
     * Test parsing complex mixed conditions.
     */
    @Test
    void testParseComplexMixedConditions() {
        String havingString = "SUM(price)=gt=1000;(COUNT(*)=ge=5,AVG(price)=lt=100);totalPrice=le=10000";
        ParseTree tree = parser.parseStream(CharStreams.fromString(havingString));

        assertNotNull(tree);
        assertTrue(tree instanceof RsqlHavingParser.HavingContext);
    }

    /**
     * Test that invalid syntax throws SyntaxErrorException.
     */
    @Test
    void testParseInvalidSyntax_MissingOperator() {
        String invalidHaving = "SUM(price) 1000";

        assertThrows(SyntaxErrorException.class, () -> {
            parser.parseStream(CharStreams.fromString(invalidHaving));
        });
    }

    /**
     * Test that missing value throws SyntaxErrorException.
     */
    @Test
    void testParseInvalidSyntax_MissingValue() {
        String invalidHaving = "SUM(price)=gt=";

        assertThrows(SyntaxErrorException.class, () -> {
            parser.parseStream(CharStreams.fromString(invalidHaving));
        });
    }

    /**
     * Test that missing closing parenthesis throws SyntaxErrorException.
     */
    @Test
    void testParseInvalidSyntax_MissingClosingParenthesis() {
        String invalidHaving = "SUM(price=gt=1000";

        assertThrows(SyntaxErrorException.class, () -> {
            parser.parseStream(CharStreams.fromString(invalidHaving));
        });
    }

    /**
     * Test that empty string throws SyntaxErrorException.
     */
    @Test
    void testParseEmptyString() {
        String emptyHaving = "";

        assertThrows(SyntaxErrorException.class, () -> {
            parser.parseStream(CharStreams.fromString(emptyHaving));
        });
    }

    /**
     * Test whitespace-only string.
     */
    @Test
    void testParseWhitespaceOnly() {
        String whitespaceHaving = "   ";

        assertThrows(SyntaxErrorException.class, () -> {
            parser.parseStream(CharStreams.fromString(whitespaceHaving));
        });
    }
}
