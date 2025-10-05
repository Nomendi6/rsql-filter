package rsql.select;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rsql.antlr.select.RsqlSelectParser;
import rsql.exceptions.SyntaxErrorException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for SelectTreeParser.
 * Tests parsing of various SELECT clause formats.
 */
class SelectTreeParserTest {

    private SelectTreeParser parser;

    @BeforeEach
    void setUp() {
        parser = new SelectTreeParser();
    }

    /**
     * Test parsing simple field selection.
     */
    @Test
    void testParseSimpleField() {
        String selectString = "field1";
        ParseTree tree = parser.parseStream(CharStreams.fromString(selectString));

        assertNotNull(tree);
        assertTrue(tree instanceof RsqlSelectParser.SelectContext);
    }

    /**
     * Test parsing multiple fields.
     */
    @Test
    void testParseMultipleFields() {
        String selectString = "field1, field2, field3";
        ParseTree tree = parser.parseStream(CharStreams.fromString(selectString));

        assertNotNull(tree);
        assertTrue(tree instanceof RsqlSelectParser.SelectContext);
    }

    /**
     * Test parsing field with alias.
     */
    @Test
    void testParseFieldWithAlias() {
        String selectString = "field1:alias1";
        ParseTree tree = parser.parseStream(CharStreams.fromString(selectString));

        assertNotNull(tree);
        assertTrue(tree instanceof RsqlSelectParser.SelectContext);
    }

    /**
     * Test parsing navigation property (field.subfield).
     */
    @Test
    void testParseNavigationProperty() {
        String selectString = "productType.name";
        ParseTree tree = parser.parseStream(CharStreams.fromString(selectString));

        assertNotNull(tree);
        assertTrue(tree instanceof RsqlSelectParser.SelectContext);
    }

    /**
     * Test parsing SELECT *.
     */
    @Test
    void testParseStar() {
        String selectString = "*";
        ParseTree tree = parser.parseStream(CharStreams.fromString(selectString));

        assertNotNull(tree);
        assertTrue(tree instanceof RsqlSelectParser.SelectContext);
    }

    /**
     * Test parsing field.* (all fields from entity).
     */
    @Test
    void testParseFieldStar() {
        String selectString = "productType.*";
        ParseTree tree = parser.parseStream(CharStreams.fromString(selectString));

        assertNotNull(tree);
        assertTrue(tree instanceof RsqlSelectParser.SelectContext);
    }

    /**
     * Test parsing SUM aggregate function.
     */
    @Test
    void testParseSumFunction() {
        String selectString = "SUM(price)";
        ParseTree tree = parser.parseStream(CharStreams.fromString(selectString));

        assertNotNull(tree);
        assertTrue(tree instanceof RsqlSelectParser.SelectContext);
    }

    /**
     * Test parsing SUM with alias.
     */
    @Test
    void testParseSumWithAlias() {
        String selectString = "SUM(price):total";
        ParseTree tree = parser.parseStream(CharStreams.fromString(selectString));

        assertNotNull(tree);
        assertTrue(tree instanceof RsqlSelectParser.SelectContext);
    }

    /**
     * Test parsing COUNT(*).
     */
    @Test
    void testParseCountStar() {
        String selectString = "COUNT(*)";
        ParseTree tree = parser.parseStream(CharStreams.fromString(selectString));

        assertNotNull(tree);
        assertTrue(tree instanceof RsqlSelectParser.SelectContext);
    }

    /**
     * Test parsing COUNT(field).
     */
    @Test
    void testParseCountField() {
        String selectString = "COUNT(id)";
        ParseTree tree = parser.parseStream(CharStreams.fromString(selectString));

        assertNotNull(tree);
        assertTrue(tree instanceof RsqlSelectParser.SelectContext);
    }

    /**
     * Test parsing COUNT(DIST field).
     */
    @Test
    void testParseCountDistinct() {
        String selectString = "COUNT(DIST productType.id)";
        ParseTree tree = parser.parseStream(CharStreams.fromString(selectString));

        assertNotNull(tree);
        assertTrue(tree instanceof RsqlSelectParser.SelectContext);
    }

    /**
     * Test parsing AVG function.
     */
    @Test
    void testParseAvgFunction() {
        String selectString = "AVG(quantity)";
        ParseTree tree = parser.parseStream(CharStreams.fromString(selectString));

        assertNotNull(tree);
        assertTrue(tree instanceof RsqlSelectParser.SelectContext);
    }

    /**
     * Test parsing MIN function.
     */
    @Test
    void testParseMinFunction() {
        String selectString = "MIN(price)";
        ParseTree tree = parser.parseStream(CharStreams.fromString(selectString));

        assertNotNull(tree);
        assertTrue(tree instanceof RsqlSelectParser.SelectContext);
    }

    /**
     * Test parsing MAX function.
     */
    @Test
    void testParseMaxFunction() {
        String selectString = "MAX(price)";
        ParseTree tree = parser.parseStream(CharStreams.fromString(selectString));

        assertNotNull(tree);
        assertTrue(tree instanceof RsqlSelectParser.SelectContext);
    }

    /**
     * Test parsing GRP function (group by).
     */
    @Test
    void testParseGrpFunction() {
        String selectString = "GRP(productType.name)";
        ParseTree tree = parser.parseStream(CharStreams.fromString(selectString));

        assertNotNull(tree);
        assertTrue(tree instanceof RsqlSelectParser.SelectContext);
    }

    /**
     * Test parsing mixed fields and aggregate functions.
     */
    @Test
    void testParseMixedFieldsAndFunctions() {
        String selectString = "productType.name:type, SUM(price):total, COUNT(*):count";
        ParseTree tree = parser.parseStream(CharStreams.fromString(selectString));

        assertNotNull(tree);
        assertTrue(tree instanceof RsqlSelectParser.SelectContext);
    }

    /**
     * Test parsing complex aggregate query.
     */
    @Test
    void testParseComplexAggregateQuery() {
        String selectString = "productType.name, productType.code, AVG(price), MIN(price), MAX(price), COUNT(*)";
        ParseTree tree = parser.parseStream(CharStreams.fromString(selectString));

        assertNotNull(tree);
        assertTrue(tree instanceof RsqlSelectParser.SelectContext);
    }

    /**
     * Test that invalid syntax throws SyntaxErrorException.
     */
    @Test
    void testParseInvalidSyntax_MissingField() {
        String invalidSelect = "field1, ";

        assertThrows(SyntaxErrorException.class, () -> {
            parser.parseStream(CharStreams.fromString(invalidSelect));
        });
    }

    /**
     * Test that truly invalid syntax (incomplete token) throws exception.
     */
    @Test
    void testParseInvalidSyntax_IncompleteToken() {
        // Use a colon without an alias - this should be a syntax error
        String invalidSelect = "field1:";

        assertThrows(SyntaxErrorException.class, () -> {
            parser.parseStream(CharStreams.fromString(invalidSelect));
        });
    }

    /**
     * Test that missing closing parenthesis throws SyntaxErrorException.
     */
    @Test
    void testParseInvalidSyntax_MissingClosingParenthesis() {
        String invalidSelect = "SUM(price";

        assertThrows(SyntaxErrorException.class, () -> {
            parser.parseStream(CharStreams.fromString(invalidSelect));
        });
    }

    /**
     * Test that empty string throws SyntaxErrorException.
     */
    @Test
    void testParseEmptyString() {
        String emptySelect = "";

        assertThrows(SyntaxErrorException.class, () -> {
            parser.parseStream(CharStreams.fromString(emptySelect));
        });
    }

    /**
     * Test whitespace-only string.
     */
    @Test
    void testParseWhitespaceOnly() {
        String whitespaceSelect = "   ";

        assertThrows(SyntaxErrorException.class, () -> {
            parser.parseStream(CharStreams.fromString(whitespaceSelect));
        });
    }
}
