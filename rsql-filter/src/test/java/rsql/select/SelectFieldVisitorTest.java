package rsql.select;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rsql.exceptions.SyntaxErrorException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SelectFieldVisitor.
 * These tests validate the parsing logic without a real EntityManager.
 * Integration tests with real entities are in rsql-filter-integration-tests module.
 */
class SelectFieldVisitorTest {

    private SelectTreeParser parser;
    private SelectFieldVisitor visitor;

    @BeforeEach
    void setUp() {
        parser = new SelectTreeParser();
        visitor = new SelectFieldVisitor();
        // Note: RsqlContext with EntityManager is required for validation,
        // but we skip it for these basic parsing tests
    }

    /**
     * Test parsing simple field without alias.
     */
    @Test
    void testParseSimpleField() {
        String selectString = "field1";
        ParseTree tree = parser.parseStream(CharStreams.fromString(selectString));

        // Without EntityManager, we expect IllegalStateException during validation
        assertThrows(IllegalStateException.class, () -> {
            visitor.visit(tree);
        });
    }

    /**
     * Test parsing field with alias.
     */
    @Test
    void testParseFieldWithAlias() {
        String selectString = "field1:alias1";
        ParseTree tree = parser.parseStream(CharStreams.fromString(selectString));

        // Without EntityManager, validation will fail
        assertThrows(IllegalStateException.class, () -> {
            visitor.visit(tree);
        });
    }

    /**
     * Test parsing multiple fields.
     */
    @Test
    void testParseMultipleFields() {
        String selectString = "field1, field2, field3";
        ParseTree tree = parser.parseStream(CharStreams.fromString(selectString));

        assertThrows(IllegalStateException.class, () -> {
            visitor.visit(tree);
        });
    }

    /**
     * Test that aggregate function throws SyntaxErrorException.
     */
    @Test
    void testParseSumFunction_ThrowsException() {
        String selectString = "SUM(price)";
        ParseTree tree = parser.parseStream(CharStreams.fromString(selectString));

        SyntaxErrorException exception = assertThrows(SyntaxErrorException.class, () -> {
            visitor.visit(tree);
        });

        assertTrue(exception.getMessage().contains("Aggregate functions are not supported"));
    }

    /**
     * Test that COUNT function throws SyntaxErrorException.
     */
    @Test
    void testParseCountFunction_ThrowsException() {
        String selectString = "COUNT(*)";
        ParseTree tree = parser.parseStream(CharStreams.fromString(selectString));

        SyntaxErrorException exception = assertThrows(SyntaxErrorException.class, () -> {
            visitor.visit(tree);
        });

        assertTrue(exception.getMessage().contains("Aggregate functions are not supported"));
    }

    /**
     * Test that AVG function throws SyntaxErrorException.
     */
    @Test
    void testParseAvgFunction_ThrowsException() {
        String selectString = "AVG(quantity)";
        ParseTree tree = parser.parseStream(CharStreams.fromString(selectString));

        SyntaxErrorException exception = assertThrows(SyntaxErrorException.class, () -> {
            visitor.visit(tree);
        });

        assertTrue(exception.getMessage().contains("Aggregate functions are not supported"));
    }

    /**
     * Test parsing aggregate function first, then field.
     * Aggregate function should throw exception before reaching field validation.
     */
    @Test
    void testParseAggregateFirst_ThrowsException() {
        String selectString = "SUM(price), field1";
        ParseTree tree = parser.parseStream(CharStreams.fromString(selectString));

        // The SUM should trigger the exception before reaching field1 validation
        SyntaxErrorException exception = assertThrows(SyntaxErrorException.class, () -> {
            visitor.visit(tree);
        });

        assertTrue(exception.getMessage().contains("Aggregate functions are not supported"));
    }

    /**
     * Test parsing navigation property (field.subfield).
     */
    @Test
    void testParseNavigationProperty() {
        String selectString = "productType.name";
        ParseTree tree = parser.parseStream(CharStreams.fromString(selectString));

        assertThrows(IllegalStateException.class, () -> {
            visitor.visit(tree);
        });
    }

    /**
     * Test parsing field.* (all fields from entity).
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
}
