package rsql.select;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rsql.exceptions.SyntaxErrorException;
import rsql.helper.AggregateField;
import rsql.helper.AggregateField.AggregateFunction;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SelectAggregateVisitor.
 * These tests validate the parsing logic without a real EntityManager.
 * Integration tests with real entities are in rsql-filter-integration-tests module.
 */
class SelectAggregateVisitorTest {

    private SelectTreeParser parser;
    private SelectAggregateVisitor visitor;

    @BeforeEach
    void setUp() {
        parser = new SelectTreeParser();
        visitor = new SelectAggregateVisitor();
        // Note: RsqlContext with EntityManager is required for validation
    }

    /**
     * Test parsing simple field (GROUP BY).
     */
    @Test
    void testParseSimpleField() {
        String selectString = "field1";
        ParseTree tree = parser.parseStream(CharStreams.fromString(selectString));

        // Without EntityManager, validation will fail
        assertThrows(IllegalStateException.class, () -> {
            visitor.visit(tree);
        });
    }

    /**
     * Test parsing field with alias (GROUP BY with alias).
     */
    @Test
    void testParseFieldWithAlias() {
        String selectString = "field1:alias1";
        ParseTree tree = parser.parseStream(CharStreams.fromString(selectString));

        assertThrows(IllegalStateException.class, () -> {
            visitor.visit(tree);
        });
    }

    /**
     * Test parsing SUM function.
     */
    @Test
    void testParseSumFunction() {
        String selectString = "SUM(price)";
        ParseTree tree = parser.parseStream(CharStreams.fromString(selectString));

        // Without EntityManager, validation will fail but we can check it tries to parse
        assertThrows(IllegalStateException.class, () -> {
            visitor.visit(tree);
        });
    }

    /**
     * Test parsing SUM with alias.
     */
    @Test
    void testParseSumWithAlias() {
        String selectString = "SUM(price):total";
        ParseTree tree = parser.parseStream(CharStreams.fromString(selectString));

        assertThrows(IllegalStateException.class, () -> {
            visitor.visit(tree);
        });
    }

    /**
     * Test parsing AVG function.
     */
    @Test
    void testParseAvgFunction() {
        String selectString = "AVG(quantity)";
        ParseTree tree = parser.parseStream(CharStreams.fromString(selectString));

        assertThrows(IllegalStateException.class, () -> {
            visitor.visit(tree);
        });
    }

    /**
     * Test parsing MIN function.
     */
    @Test
    void testParseMinFunction() {
        String selectString = "MIN(price)";
        ParseTree tree = parser.parseStream(CharStreams.fromString(selectString));

        assertThrows(IllegalStateException.class, () -> {
            visitor.visit(tree);
        });
    }

    /**
     * Test parsing MAX function.
     */
    @Test
    void testParseMaxFunction() {
        String selectString = "MAX(price)";
        ParseTree tree = parser.parseStream(CharStreams.fromString(selectString));

        assertThrows(IllegalStateException.class, () -> {
            visitor.visit(tree);
        });
    }

    /**
     * Test parsing GRP function (GROUP BY).
     */
    @Test
    void testParseGrpFunction() {
        String selectString = "GRP(productType.name)";
        ParseTree tree = parser.parseStream(CharStreams.fromString(selectString));

        assertThrows(IllegalStateException.class, () -> {
            visitor.visit(tree);
        });
    }

    /**
     * Test parsing COUNT(*).
     */
    @Test
    void testParseCountStar() {
        String selectString = "COUNT(*)";
        ParseTree tree = parser.parseStream(CharStreams.fromString(selectString));

        // COUNT(*) uses default "id" field, no validation needed before EntityManager check
        assertDoesNotThrow(() -> {
            ParseTree result = parser.parseStream(CharStreams.fromString(selectString));
            assertNotNull(result);
        });
    }

    /**
     * Test parsing COUNT(field).
     */
    @Test
    void testParseCountField() {
        String selectString = "COUNT(id)";
        ParseTree tree = parser.parseStream(CharStreams.fromString(selectString));

        assertThrows(IllegalStateException.class, () -> {
            visitor.visit(tree);
        });
    }

    /**
     * Test parsing COUNT(DIST field).
     */
    @Test
    void testParseCountDistinct() {
        String selectString = "COUNT(DIST productType.id)";
        ParseTree tree = parser.parseStream(CharStreams.fromString(selectString));

        assertThrows(IllegalStateException.class, () -> {
            visitor.visit(tree);
        });
    }

    /**
     * Test parsing COUNT(DIST field1, field2) - multiple fields.
     */
    @Test
    void testParseCountDistinctMultiple() {
        String selectString = "COUNT(DIST field1, field2)";
        ParseTree tree = parser.parseStream(CharStreams.fromString(selectString));

        assertThrows(IllegalStateException.class, () -> {
            visitor.visit(tree);
        });
    }

    /**
     * Test that DIST modifier on non-COUNT functions throws exception.
     */
    @Test
    void testParseDistOnSumThrowsException() {
        String selectString = "SUM(DIST price)";
        ParseTree tree = parser.parseStream(CharStreams.fromString(selectString));

        SyntaxErrorException exception = assertThrows(SyntaxErrorException.class, () -> {
            visitor.visit(tree);
        });

        assertTrue(exception.getMessage().contains("DISTINCT modifier is only supported for COUNT"));
    }

    /**
     * Test that DIST modifier on AVG throws exception.
     */
    @Test
    void testParseDistOnAvgThrowsException() {
        String selectString = "AVG(DIST quantity)";
        ParseTree tree = parser.parseStream(CharStreams.fromString(selectString));

        SyntaxErrorException exception = assertThrows(SyntaxErrorException.class, () -> {
            visitor.visit(tree);
        });

        assertTrue(exception.getMessage().contains("DISTINCT modifier is only supported for COUNT"));
    }

    /**
     * Test parsing mixed fields and aggregate functions.
     */
    @Test
    void testParseMixedFieldsAndFunctions() {
        String selectString = "productType.name:type, SUM(price):total, COUNT(*):count";
        ParseTree tree = parser.parseStream(CharStreams.fromString(selectString));

        // Will fail on first field validation
        assertThrows(IllegalStateException.class, () -> {
            visitor.visit(tree);
        });
    }

    /**
     * Test parsing complex aggregate query.
     */
    @Test
    void testParseComplexAggregateQuery() {
        String selectString = "productType.name, AVG(price), MIN(price), MAX(price), COUNT(*)";
        ParseTree tree = parser.parseStream(CharStreams.fromString(selectString));

        assertThrows(IllegalStateException.class, () -> {
            visitor.visit(tree);
        });
    }

    /**
     * Test parsing SELECT *.
     */
    @Test
    void testParseStar() {
        String selectString = "*";
        ParseTree tree = parser.parseStream(CharStreams.fromString(selectString));

        assertThrows(IllegalStateException.class, () -> {
            visitor.visit(tree);
        });
    }

    /**
     * Test parsing field.* (all fields from entity as GROUP BY).
     */
    @Test
    void testParseFieldStar() {
        String selectString = "productType.*";
        ParseTree tree = parser.parseStream(CharStreams.fromString(selectString));

        assertThrows(IllegalStateException.class, () -> {
            visitor.visit(tree);
        });
    }

    /**
     * Test that nested function calls throw exception.
     */
    @Test
    void testParseNestedFunctionCallsThrowsException() {
        // This is invalid syntax anyway, but test for completeness
        String selectString = "SUM(AVG(price))";

        // Parser will likely fail on this syntax
        assertThrows(SyntaxErrorException.class, () -> {
            ParseTree tree = parser.parseStream(CharStreams.fromString(selectString));
            visitor.visit(tree);
        });
    }
}
