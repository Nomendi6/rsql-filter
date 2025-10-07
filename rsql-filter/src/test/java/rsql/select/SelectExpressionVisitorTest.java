package rsql.select;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rsql.exceptions.SyntaxErrorException;
import rsql.helper.*;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SelectExpressionVisitor.
 * These tests validate the parsing logic without a real EntityManager.
 * Integration tests with real entities are in rsql-filter-integration-tests module.
 */
class SelectExpressionVisitorTest {

    private SelectTreeParser parser;
    private SelectExpressionVisitor visitor;

    @BeforeEach
    void setUp() {
        parser = new SelectTreeParser();
        visitor = new SelectExpressionVisitor();
        // Note: RsqlContext with EntityManager is required for validation
    }

    /**
     * Test parsing simple field expression.
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
     * Test parsing aggregate function expression.
     */
    @Test
    void testParseSumFunction() {
        String selectString = "SUM(price)";
        ParseTree tree = parser.parseStream(CharStreams.fromString(selectString));

        // Without EntityManager, validation will fail
        assertThrows(IllegalStateException.class, () -> {
            visitor.visit(tree);
        });
    }

    /**
     * Test parsing numeric literal expression.
     */
    @Test
    void testParseNumericLiteral() {
        String selectString = "10";
        ParseTree tree = parser.parseStream(CharStreams.fromString(selectString));

        // Numeric literals don't need validation, should parse successfully
        List<SelectExpression> expressions = visitor.visit(tree);

        assertNotNull(expressions);
        assertEquals(1, expressions.size());
        assertTrue(expressions.get(0) instanceof LiteralExpression);
        LiteralExpression literal = (LiteralExpression) expressions.get(0);
        assertEquals(new BigDecimal("10"), literal.getValue());
    }

    /**
     * Test parsing decimal literal expression.
     */
    @Test
    void testParseDecimalLiteral() {
        String selectString = "1.5";
        ParseTree tree = parser.parseStream(CharStreams.fromString(selectString));

        List<SelectExpression> expressions = visitor.visit(tree);

        assertNotNull(expressions);
        assertEquals(1, expressions.size());
        assertTrue(expressions.get(0) instanceof LiteralExpression);
        LiteralExpression literal = (LiteralExpression) expressions.get(0);
        assertEquals(new BigDecimal("1.5"), literal.getValue());
    }

    /**
     * Test parsing binary expression with addition (without EntityManager for validation).
     * We check that parsing creates correct structure, even though execution would fail.
     */
    @Test
    void testParseBinaryAddition() {
        String selectString = "10 + 5";
        ParseTree tree = parser.parseStream(CharStreams.fromString(selectString));

        // Parse without validation (no EntityManager)
        List<SelectExpression> expressions = visitor.visit(tree);

        assertNotNull(expressions);
        assertEquals(1, expressions.size());
        assertTrue(expressions.get(0) instanceof BinaryOpExpression);

        BinaryOpExpression binOp = (BinaryOpExpression) expressions.get(0);
        assertEquals(BinaryOperator.ADD, binOp.getOperator());
        assertTrue(binOp.getLeft() instanceof LiteralExpression);
        assertTrue(binOp.getRight() instanceof LiteralExpression);
    }

    /**
     * Test parsing binary expression with subtraction.
     */
    @Test
    void testParseBinarySubtraction() {
        String selectString = "100 - 20";
        ParseTree tree = parser.parseStream(CharStreams.fromString(selectString));

        List<SelectExpression> expressions = visitor.visit(tree);

        assertNotNull(expressions);
        assertEquals(1, expressions.size());
        assertTrue(expressions.get(0) instanceof BinaryOpExpression);

        BinaryOpExpression binOp = (BinaryOpExpression) expressions.get(0);
        assertEquals(BinaryOperator.SUB, binOp.getOperator());
    }

    /**
     * Test parsing binary expression with multiplication.
     */
    @Test
    void testParseBinaryMultiplication() {
        String selectString = "5 * 2";
        ParseTree tree = parser.parseStream(CharStreams.fromString(selectString));

        List<SelectExpression> expressions = visitor.visit(tree);

        assertNotNull(expressions);
        assertEquals(1, expressions.size());
        assertTrue(expressions.get(0) instanceof BinaryOpExpression);

        BinaryOpExpression binOp = (BinaryOpExpression) expressions.get(0);
        assertEquals(BinaryOperator.MUL, binOp.getOperator());
    }

    /**
     * Test parsing binary expression with division.
     */
    @Test
    void testParseBinaryDivision() {
        String selectString = "100 / 4";
        ParseTree tree = parser.parseStream(CharStreams.fromString(selectString));

        List<SelectExpression> expressions = visitor.visit(tree);

        assertNotNull(expressions);
        assertEquals(1, expressions.size());
        assertTrue(expressions.get(0) instanceof BinaryOpExpression);

        BinaryOpExpression binOp = (BinaryOpExpression) expressions.get(0);
        assertEquals(BinaryOperator.DIV, binOp.getOperator());
    }

    /**
     * Test operator precedence: multiplication before addition.
     * Expression: 10 + 5 * 2 should parse as 10 + (5 * 2)
     */
    @Test
    void testOperatorPrecedenceMultiplicationBeforeAddition() {
        String selectString = "10 + 5 * 2";
        ParseTree tree = parser.parseStream(CharStreams.fromString(selectString));

        List<SelectExpression> expressions = visitor.visit(tree);

        assertNotNull(expressions);
        assertEquals(1, expressions.size());
        assertTrue(expressions.get(0) instanceof BinaryOpExpression);

        BinaryOpExpression topLevel = (BinaryOpExpression) expressions.get(0);
        assertEquals(BinaryOperator.ADD, topLevel.getOperator());
        assertTrue(topLevel.getLeft() instanceof LiteralExpression);
        assertTrue(topLevel.getRight() instanceof BinaryOpExpression);

        BinaryOpExpression rightSide = (BinaryOpExpression) topLevel.getRight();
        assertEquals(BinaryOperator.MUL, rightSide.getOperator());
    }

    /**
     * Test parentheses override precedence.
     * Expression: (10 + 5) * 2 should parse as a multiplication with addition on the left
     */
    @Test
    void testParenthesesOverridePrecedence() {
        String selectString = "(10 + 5) * 2";
        ParseTree tree = parser.parseStream(CharStreams.fromString(selectString));

        List<SelectExpression> expressions = visitor.visit(tree);

        assertNotNull(expressions);
        assertEquals(1, expressions.size());
        assertTrue(expressions.get(0) instanceof BinaryOpExpression);

        BinaryOpExpression topLevel = (BinaryOpExpression) expressions.get(0);
        assertEquals(BinaryOperator.MUL, topLevel.getOperator());
        assertTrue(topLevel.getLeft() instanceof BinaryOpExpression);
        assertTrue(topLevel.getRight() instanceof LiteralExpression);

        BinaryOpExpression leftSide = (BinaryOpExpression) topLevel.getLeft();
        assertEquals(BinaryOperator.ADD, leftSide.getOperator());
    }

    /**
     * Test parsing expression with alias.
     */
    @Test
    void testParseExpressionWithAlias() {
        String selectString = "10 + 5:result";
        ParseTree tree = parser.parseStream(CharStreams.fromString(selectString));

        List<SelectExpression> expressions = visitor.visit(tree);

        assertNotNull(expressions);
        assertEquals(1, expressions.size());
        assertTrue(expressions.get(0) instanceof BinaryOpExpression);

        BinaryOpExpression binOp = (BinaryOpExpression) expressions.get(0);
        assertEquals("result", binOp.getAlias());
    }

    /**
     * Test parsing multiple expressions separated by commas.
     */
    @Test
    void testParseMultipleExpressions() {
        String selectString = "10 + 5, 20 * 2";
        ParseTree tree = parser.parseStream(CharStreams.fromString(selectString));

        List<SelectExpression> expressions = visitor.visit(tree);

        assertNotNull(expressions);
        assertEquals(2, expressions.size());
        assertTrue(expressions.get(0) instanceof BinaryOpExpression);
        assertTrue(expressions.get(1) instanceof BinaryOpExpression);
    }

    /**
     * Test toExpressionString() method for debugging.
     */
    @Test
    void testToExpressionString() {
        String selectString = "10 + 5:sum";
        ParseTree tree = parser.parseStream(CharStreams.fromString(selectString));

        List<SelectExpression> expressions = visitor.visit(tree);

        assertNotNull(expressions);
        assertEquals(1, expressions.size());

        String expressionString = expressions.get(0).toExpressionString();
        assertTrue(expressionString.contains("10"));
        assertTrue(expressionString.contains("5"));
        assertTrue(expressionString.contains("sum"));
    }
}
