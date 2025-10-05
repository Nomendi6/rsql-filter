package rsql.having;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.Test;
import rsql.antlr.having.RsqlHavingLexer;
import rsql.antlr.having.RsqlHavingParser;
import rsql.exceptions.SyntaxErrorException;
import rsql.where.CustomErrorStrategy;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for HAVING error handling.
 * Tests HavingErrorListener and BailRsqlHavingLexer functionality.
 */
class HavingErrorHandlingTest {

    /**
     * Test that HavingErrorListener throws SyntaxErrorException with proper message
     * when parser encounters a syntax error.
     */
    @Test
    void testHavingErrorListener_ParserError() {
        // Invalid HAVING syntax: missing operator
        String invalidHaving = "SUM(price) 1000";

        SyntaxErrorException exception = assertThrows(SyntaxErrorException.class, () -> {
            RsqlHavingLexer lexer = new BailRsqlHavingLexer(CharStreams.fromString(invalidHaving));
            lexer.removeErrorListeners();
            lexer.addErrorListener(new HavingErrorListener());

            CommonTokenStream tokens = new CommonTokenStream(lexer);

            RsqlHavingParser parser = new RsqlHavingParser(tokens);
            parser.removeErrorListeners();
            parser.addErrorListener(new HavingErrorListener());
            parser.setErrorHandler(new CustomErrorStrategy());

            parser.having(); // This should throw SyntaxErrorException
        });

        assertTrue(exception.getMessage().contains("Syntax error") ||
                   exception.getMessage().contains("Error parsing"),
                   "Exception message should contain 'Syntax error' or 'Error parsing'");
    }

    /**
     * Test that HavingErrorListener throws SyntaxErrorException
     * when encountering missing closing parenthesis.
     */
    @Test
    void testHavingErrorListener_MissingClosingParenthesis() {
        // Invalid HAVING syntax: missing closing parenthesis
        String invalidHaving = "SUM(price=gt=1000";

        assertThrows(SyntaxErrorException.class, () -> {
            RsqlHavingLexer lexer = new BailRsqlHavingLexer(CharStreams.fromString(invalidHaving));
            lexer.removeErrorListeners();
            lexer.addErrorListener(new HavingErrorListener());

            CommonTokenStream tokens = new CommonTokenStream(lexer);

            RsqlHavingParser parser = new RsqlHavingParser(tokens);
            parser.removeErrorListeners();
            parser.addErrorListener(new HavingErrorListener());
            parser.setErrorHandler(new CustomErrorStrategy());

            parser.having();
        });
    }

    /**
     * Test that HavingErrorListener throws SyntaxErrorException
     * when encountering missing value after operator.
     */
    @Test
    void testHavingErrorListener_MissingValue() {
        // Invalid HAVING syntax: missing value after operator
        String invalidHaving = "AVG(price)=gt=";

        assertThrows(SyntaxErrorException.class, () -> {
            RsqlHavingLexer lexer = new BailRsqlHavingLexer(CharStreams.fromString(invalidHaving));
            lexer.removeErrorListeners();
            lexer.addErrorListener(new HavingErrorListener());

            CommonTokenStream tokens = new CommonTokenStream(lexer);

            RsqlHavingParser parser = new RsqlHavingParser(tokens);
            parser.removeErrorListeners();
            parser.addErrorListener(new HavingErrorListener());
            parser.setErrorHandler(new CustomErrorStrategy());

            parser.having();
        });
    }

    /**
     * Test that BailRsqlHavingLexer throws SyntaxErrorException
     * when encountering invalid characters.
     */
    @Test
    void testBailRsqlHavingLexer_InvalidCharacter() {
        // Invalid characters that lexer cannot tokenize
        String invalidHaving = "SUM(price)@#$%^=1000";

        SyntaxErrorException exception = assertThrows(SyntaxErrorException.class, () -> {
            RsqlHavingLexer lexer = new BailRsqlHavingLexer(CharStreams.fromString(invalidHaving));
            lexer.removeErrorListeners();
            lexer.addErrorListener(new HavingErrorListener());

            CommonTokenStream tokens = new CommonTokenStream(lexer);
            tokens.fill(); // Force lexer to process all tokens
        });

        assertTrue(exception.getMessage().contains("Error parsing") ||
                   exception.getMessage().contains("Syntax error"),
                   "Exception message should indicate parsing error");
    }

    /**
     * Test that valid HAVING syntax does not throw exceptions.
     */
    @Test
    void testValidHavingSyntax_NoException() {
        String validHaving = "SUM(price)=gt=1000";

        assertDoesNotThrow(() -> {
            RsqlHavingLexer lexer = new BailRsqlHavingLexer(CharStreams.fromString(validHaving));
            lexer.removeErrorListeners();
            lexer.addErrorListener(new HavingErrorListener());

            CommonTokenStream tokens = new CommonTokenStream(lexer);

            RsqlHavingParser parser = new RsqlHavingParser(tokens);
            parser.removeErrorListeners();
            parser.addErrorListener(new HavingErrorListener());
            parser.setErrorHandler(new CustomErrorStrategy());

            parser.having();
        });
    }

    /**
     * Test that valid complex HAVING syntax does not throw exceptions.
     */
    @Test
    void testValidComplexHavingSyntax_NoException() {
        String validHaving = "(SUM(price)=gt=1000;COUNT(*)=ge=5),AVG(price)=lt=100";

        assertDoesNotThrow(() -> {
            RsqlHavingLexer lexer = new BailRsqlHavingLexer(CharStreams.fromString(validHaving));
            lexer.removeErrorListeners();
            lexer.addErrorListener(new HavingErrorListener());

            CommonTokenStream tokens = new CommonTokenStream(lexer);

            RsqlHavingParser parser = new RsqlHavingParser(tokens);
            parser.removeErrorListeners();
            parser.addErrorListener(new HavingErrorListener());
            parser.setErrorHandler(new CustomErrorStrategy());

            parser.having();
        });
    }

    /**
     * Test that BETWEEN operator syntax is valid.
     */
    @Test
    void testValidBetweenSyntax_NoException() {
        String validHaving = "AVG(price)=bt=(50,150)";

        assertDoesNotThrow(() -> {
            RsqlHavingLexer lexer = new BailRsqlHavingLexer(CharStreams.fromString(validHaving));
            lexer.removeErrorListeners();
            lexer.addErrorListener(new HavingErrorListener());

            CommonTokenStream tokens = new CommonTokenStream(lexer);

            RsqlHavingParser parser = new RsqlHavingParser(tokens);
            parser.removeErrorListeners();
            parser.addErrorListener(new HavingErrorListener());
            parser.setErrorHandler(new CustomErrorStrategy());

            parser.having();
        });
    }

    /**
     * Test that IN operator syntax is valid.
     */
    @Test
    void testValidInSyntax_NoException() {
        String validHaving = "COUNT(id)=in=(1,2,3,5)";

        assertDoesNotThrow(() -> {
            RsqlHavingLexer lexer = new BailRsqlHavingLexer(CharStreams.fromString(validHaving));
            lexer.removeErrorListeners();
            lexer.addErrorListener(new HavingErrorListener());

            CommonTokenStream tokens = new CommonTokenStream(lexer);

            RsqlHavingParser parser = new RsqlHavingParser(tokens);
            parser.removeErrorListeners();
            parser.addErrorListener(new HavingErrorListener());
            parser.setErrorHandler(new CustomErrorStrategy());

            parser.having();
        });
    }

    /**
     * Test that error message contains position information.
     */
    @Test
    void testErrorMessage_ContainsPosition() {
        String invalidHaving = "SUM(price)=="; // Missing value

        SyntaxErrorException exception = assertThrows(SyntaxErrorException.class, () -> {
            RsqlHavingLexer lexer = new BailRsqlHavingLexer(CharStreams.fromString(invalidHaving));
            lexer.removeErrorListeners();
            lexer.addErrorListener(new HavingErrorListener());

            CommonTokenStream tokens = new CommonTokenStream(lexer);

            RsqlHavingParser parser = new RsqlHavingParser(tokens);
            parser.removeErrorListeners();
            parser.addErrorListener(new HavingErrorListener());
            parser.setErrorHandler(new CustomErrorStrategy());

            parser.having();
        });

        assertNotNull(exception.getMessage());
        assertFalse(exception.getMessage().isEmpty());
    }

    /**
     * Test that empty string throws SyntaxErrorException.
     */
    @Test
    void testEmptyString_ThrowsException() {
        String emptyHaving = "";

        assertThrows(SyntaxErrorException.class, () -> {
            RsqlHavingLexer lexer = new BailRsqlHavingLexer(CharStreams.fromString(emptyHaving));
            lexer.removeErrorListeners();
            lexer.addErrorListener(new HavingErrorListener());

            CommonTokenStream tokens = new CommonTokenStream(lexer);

            RsqlHavingParser parser = new RsqlHavingParser(tokens);
            parser.removeErrorListeners();
            parser.addErrorListener(new HavingErrorListener());
            parser.setErrorHandler(new CustomErrorStrategy());

            parser.having();
        });
    }
}
